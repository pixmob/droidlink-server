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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.api.users.User;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
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
@At("/api/1/device")
@Service
public class DeviceWebService {
    private static final String JSON_MIME_TYPE = "application/json";
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final User user;
    private final DeviceService deviceService;
    
    /**
     * Package protected constructor: use Guice to get an instance of this
     * class.
     */
    @Inject
    DeviceWebService(final DeviceService deviceService, @Nullable final User user) {
        this.deviceService = deviceService;
        this.user = user;
    }
    
    @Get
    public Reply<?> getAllDevices() {
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        final Iterable<Device> devices = deviceService.getDevices(user.getEmail());
        final List<DeviceRemote> results = new ArrayList<DeviceRemote>(4);
        for (final Device d : devices) {
            results.add(new DeviceRemote(d));
        }
        if (results.isEmpty()) {
            return Reply.saying().noContent();
        }
        
        return Reply.with(results).as(Json.class).type(JSON_MIME_TYPE);
    }
    
    @At("/:deviceId")
    @Put
    public Reply<?> registerDevice(Request request, @Named("deviceId") String deviceId) {
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        final String deviceName = "Unknown";
        logger.info("Register device " + deviceId);
        deviceService.registerDevice(user.getEmail(), deviceId, deviceName);
        
        return Reply.saying().ok();
    }
    
    @Delete
    public Reply<?> unregisterDevices() {
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        logger.info("Unregister all devices");
        try {
            deviceService.unregisterDevice(user.getEmail(), null);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        }
        
        return Reply.saying().ok();
    }
    
    @At("/:deviceId")
    @Delete
    public Reply<?> unregisterDevice(@Named("deviceId") String deviceId) {
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        logger.info("Unregister device " + deviceId);
        try {
            deviceService.unregisterDevice(user.getEmail(), deviceId);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        }
        
        return Reply.saying().ok();
    }
    
    @At("/:deviceId")
    @Post
    public Reply<?> updateDevice(Request request, @Named("deviceId") String deviceId) {
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        final DeviceRemote dr = request.read(DeviceRemote.class).as(Json.class);
        
        logger.info("Update device " + deviceId);
        try {
            deviceService.updateDevice(user.getEmail(), deviceId, dr.name);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        } catch (DeviceNotFoundException e) {
            return Reply.saying().notFound();
        }
        
        return Reply.saying().ok();
    }
    
    @At("/:deviceId/:eventId")
    @Get
    public Reply<?> getEvent(@Named("deviceId") String deviceId, @Named("eventId") String eventId) {
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
    
    @At("/:deviceId")
    @Get
    public Reply<?> getEvents(@Named("deviceId") String deviceId) {
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        final Iterable<Event> events;
        try {
            events = deviceService.getEvents(user.getEmail(), deviceId);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        } catch (DeviceNotFoundException e) {
            return Reply.saying().notFound();
        }
        
        final Set<EventRemote> results = new HashSet<EventRemote>(32);
        for (final Event event : events) {
            results.add(new EventRemote(event));
        }
        if (results.isEmpty()) {
            return Reply.saying().noContent();
        }
        
        return Reply.with(results).as(Json.class).type(JSON_MIME_TYPE);
    }
    
    @At("/:deviceId/:eventId")
    @Delete
    public Reply<?> deleteEvent(@Named("deviceId") String deviceId, @Named("eventId") String eventId) {
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        logger.info("Delete event " + eventId + " from device " + deviceId);
        try {
            deviceService.deleteEvent(user.getEmail(), deviceId, eventId);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        }
        
        return Reply.saying().ok();
    }
    
    @At("/:deviceId/:eventId")
    @Put
    public Reply<?> addEvent(Request request, @Named("deviceId") String deviceId,
            @Named("eventId") String eventId) {
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        // TODO fill event fields
        final long eventDate = System.currentTimeMillis();
        final EventType eventType = EventType.RECEIVED_SMS;
        final String eventNumber = "123";
        final String eventName = "John Doe";
        final String eventMessage = "Hello world!";
        
        logger.info("Add new event " + eventId + " for device " + deviceId);
        try {
            deviceService.addEvent(user.getEmail(), deviceId, eventId, eventDate, eventType,
                eventNumber, eventName, eventMessage);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        } catch (DeviceNotFoundException e) {
            return Reply.saying().notFound();
        }
        
        return Reply.saying().ok();
    }
}
