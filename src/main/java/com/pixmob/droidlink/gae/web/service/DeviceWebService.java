/*
 * Copyright (C) 2011 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pixmob.droidlink.gae.web.service;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.InvalidValueException;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Put;
import com.pixmob.droidlink.gae.queue.CacheQueue;
import com.pixmob.droidlink.gae.queue.SyncQueue;
import com.pixmob.droidlink.gae.service.AccessDeniedException;
import com.pixmob.droidlink.gae.service.Device;
import com.pixmob.droidlink.gae.service.DeviceNotFoundException;
import com.pixmob.droidlink.gae.service.DeviceService;
import com.pixmob.droidlink.gae.service.Event;
import com.pixmob.droidlink.gae.service.EventNotFoundException;
import com.pixmob.droidlink.gae.service.EventType;

/**
 * Remote API for managing devices.
 * @author Pixmob
 */
@At(DeviceWebService.URI)
@Service
public class DeviceWebService {
    public static final String URI = "/api/1/device";
    private static final String JSON_MIME_TYPE = "application/json";
    private static final Map<Integer, EventType> INT_TO_EVENT_TYPES = new HashMap<Integer, EventType>(
            2);
    static {
        INT_TO_EVENT_TYPES.put(0, EventType.MISSED_CALL);
        INT_TO_EVENT_TYPES.put(1, EventType.RECEIVED_SMS);
    }
    
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final UserService userService;
    private final MemcacheService memcacheService;
    private final DeviceService deviceService;
    private final Queue syncQueue;
    private final Queue cacheQueue;
    
    /**
     * Package protected constructor: use Guice to get an instance of this
     * class.
     */
    @Inject
    DeviceWebService(final DeviceService deviceService, final UserService userService,
            final MemcacheService memcacheService, @Named("sync") final Queue syncQueue,
            @Named("cache") final Queue cacheQueue) {
        this.deviceService = deviceService;
        this.userService = userService;
        this.memcacheService = memcacheService;
        this.syncQueue = syncQueue;
        this.cacheQueue = cacheQueue;
    }
    
    @SuppressWarnings("unchecked")
    @Get
    public Reply<?> getDevices() {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        Collection<DeviceRemote> results = null;
        try {
            results = (Collection<DeviceRemote>) memcacheService.get(CacheQueue
                    .getDeviceCacheKey(user.getEmail()));
        } catch (InvalidValueException e) {
            // The cache is not available.
            logger.log(Level.WARNING, "Cannot get devices from cache for user " + user.getEmail(),
                e);
            results = null;
        }
        
        if (results == null) {
            logger.info("Get devices from datastore for user " + user.getEmail());
            
            results = new HashSet<DeviceRemote>(4);
            final Iterable<Device> devices = deviceService.getDevices(user.getEmail());
            for (final Device d : devices) {
                results.add(new DeviceRemote(d));
            }
            
            triggerCache(user);
        } else {
            logger.info("Get devices from cache for user " + user.getEmail());
        }
        if (results.isEmpty()) {
            return Reply.saying().noContent();
        }
        
        return Reply.with(results).as(Json.class).type(JSON_MIME_TYPE);
    }
    
    @At("/:deviceId")
    @Put
    public Reply<?> registerDevice(Request request, @Named("deviceId") String deviceId) {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        final DeviceRemote device = request.read(DeviceRemote.class).as(Json.class);
        logger.info("Register device " + deviceId);
        try {
            deviceService.registerDevice(user.getEmail(), deviceId, device.name, device.c2dm);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        }
        
        triggerCache(user);
        
        return Reply.saying().ok();
    }
    
    @Delete
    public Reply<?> unregisterDevices() {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        logger.info("Unregister all devices");
        final Set<String> deviceIds;
        try {
            deviceIds = deviceService.unregisterDevice(user.getEmail(), null);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        }
        
        for (final String deviceId : deviceIds) {
            clearEventCache(user, deviceId);
        }
        clearDeviceCache(user);
        
        return Reply.saying().ok();
    }
    
    @At("/:deviceId")
    @Delete
    public Reply<?> unregisterDevice(@Named("deviceId") String deviceId) {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        logger.info("Unregister device " + deviceId);
        try {
            deviceService.unregisterDevice(user.getEmail(), deviceId);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        }
        
        clearEventCache(user, deviceId);
        clearDeviceCache(user);
        
        return Reply.saying().ok();
    }
    
    @At("/:deviceId/:eventId")
    @Get
    public Reply<?> getEvent(@Named("deviceId") String deviceId, @Named("eventId") String eventId) {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        final Event event;
        try {
            event = deviceService.getEvent(user.getEmail(), deviceId, eventId);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        } catch (DeviceNotFoundException e) {
            return Reply.saying().notFound();
        } catch (EventNotFoundException e) {
            return Reply.saying().notFound();
        }
        
        return Reply.with(new EventRemote(event)).as(Json.class).type(JSON_MIME_TYPE);
    }
    
    @SuppressWarnings("unchecked")
    @At("/:deviceId")
    @Get
    public Reply<?> getEvents(@Named("deviceId") String deviceId) {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        final String eventCacheKey = CacheQueue.getEventCacheKey(user.getEmail(), deviceId);
        Collection<EventRemote> results = null;
        try {
            results = (Collection<EventRemote>) memcacheService.get(eventCacheKey);
        } catch (InvalidValueException e) {
            // The cache is not available.
            logger.log(Level.WARNING, "Cannot get events of device " + deviceId
                    + " from cache for user " + user.getEmail(), e);
            results = null;
        }
        
        if (results == null) {
            logger.info("Get events of device " + deviceId + " from datastore for user "
                    + user.getEmail());
            
            results = new HashSet<EventRemote>(32);
            final Iterable<Event> events;
            try {
                events = deviceService.getEvents(user.getEmail(), deviceId);
            } catch (AccessDeniedException e) {
                return Reply.saying().forbidden();
            } catch (DeviceNotFoundException e) {
                return Reply.saying().notFound();
            }
            
            for (final Event event : events) {
                results.add(new EventRemote(event));
            }
        } else {
            logger.info("Get events of device " + deviceId + " from cache for user "
                    + user.getEmail());
        }
        
        if (results.isEmpty()) {
            return Reply.saying().noContent();
        }
        
        return Reply.with(results).as(Json.class).type(JSON_MIME_TYPE);
    }
    
    @At("/:deviceId/:eventId")
    @Delete
    public Reply<?> deleteEvent(@Named("deviceId") String deviceId, @Named("eventId") String eventId) {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        logger.info("Delete event " + eventId + " from device " + deviceId);
        try {
            deviceService.deleteEvent(user.getEmail(), deviceId, eventId);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        }
        
        triggerUserSync(user, deviceId);
        triggerCache(user);
        
        return Reply.saying().ok();
    }
    
    @At("/:deviceId/:eventId")
    @Put
    public Reply<?> addEvent(Request request, @Named("deviceId") String deviceId,
            @Named("eventId") String eventId) {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        final EventRemote event = request.read(EventRemote.class).as(Json.class);
        final EventType eventType = INT_TO_EVENT_TYPES.get(event.type);
        if (eventType == null) {
            logger.warning("Invalid event type: " + event.type);
            return Reply.saying().error();
        }
        
        logger.info("Add new event " + eventId + " for device " + deviceId);
        try {
            deviceService.addEvent(user.getEmail(), deviceId, eventId, event.created, eventType,
                event.number, event.name, event.message);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        } catch (DeviceNotFoundException e) {
            return Reply.saying().notFound();
        }
        
        triggerUserSync(user, deviceId);
        triggerCache(user);
        
        return Reply.saying().ok();
    }
    
    private void triggerUserSync(User user, String deviceIdSource) {
        // Use a queue to close the Http request as soon as possible.
        logger.info("Queue device sync for user " + user.getEmail());
        syncQueue.add(withUrl(SyncQueue.URI).param(SyncQueue.USER_PARAM, user.getEmail()).param(
            SyncQueue.DEVICE_ID_SOURCE_PARAM, deviceIdSource));
    }
    
    private void triggerCache(User user) {
        logger.info("Queue data cache for user " + user.getEmail());
        cacheQueue.add(withUrl(CacheQueue.URI).param(CacheQueue.USER_PARAM, user.getEmail()));
    }
    
    private void clearDeviceCache(User user) {
        logger.info("Clear device cache for user " + user.getEmail());
        memcacheService.delete(CacheQueue.getDeviceCacheKey(user.getEmail()));
    }
    
    private void clearEventCache(User user, String deviceId) {
        logger.info("Clear event cache for user " + user.getEmail());
        memcacheService.delete(CacheQueue.getEventCacheKey(user.getEmail(), deviceId));
    }
}
