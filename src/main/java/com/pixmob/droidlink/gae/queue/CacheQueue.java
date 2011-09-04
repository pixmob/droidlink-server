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
package com.pixmob.droidlink.gae.queue;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.pixmob.droidlink.gae.service.Device;
import com.pixmob.droidlink.gae.service.DeviceService;
import com.pixmob.droidlink.gae.service.Event;
import com.pixmob.droidlink.gae.web.service.DeviceRemote;
import com.pixmob.droidlink.gae.web.service.EventRemote;

/**
 * Cache user devices and events.
 * @author Pixmob
 */
@At(CacheQueue.URI)
@Service
public class CacheQueue {
    public static final String URI = "/tasks/cache";
    public static final String USER_PARAM = "user";
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final DeviceService deviceService;
    private final MemcacheService memcacheService;
    
    @Inject
    CacheQueue(final DeviceService deviceService, final MemcacheService memcacheService) {
        this.deviceService = deviceService;
        this.memcacheService = memcacheService;
    }
    
    @Post
    public Reply<?> cacheUserData(Request request) {
        String userParam = request.param(USER_PARAM);
        if (userParam == null) {
            // Cache data for every users.
            for (final String user : deviceService.getRegisteredUsers()) {
                try {
                    cacheUserData(user);
                } catch (Exception e) {
                    return Reply.saying().error();
                }
            }
        } else {
            // Cache data for this user only.
            try {
                cacheUserData(userParam);
            } catch (Exception e) {
                return Reply.saying().error();
            }
        }
        
        return Reply.saying().ok();
    }
    
    private void cacheUserData(String user) throws Exception {
        logger.info("Cache data for user " + user);
        
        final String deviceCacheKey = getDeviceCacheKey(user);
        final Set<DeviceRemote> cachedDevices = new HashSet<DeviceRemote>(4);
        for (final Device device : deviceService.getDevices(user)) {
            cachedDevices.add(new DeviceRemote(device));
        }
        memcacheService.put(deviceCacheKey, cachedDevices);
        
        final Set<EventRemote> cachedEvents = new HashSet<EventRemote>(32);
        for (final DeviceRemote device : cachedDevices) {
            final String eventCacheKey = getEventCacheKey(user, device.getId());
            try {
                for (final Event event : deviceService.getEvents(user, device.getId())) {
                    cachedEvents.add(new EventRemote(event));
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Cannot cache events of device " + device.getId()
                        + " for user " + user, e);
                memcacheService.delete(eventCacheKey);
                throw e;
            }
            
            memcacheService.put(eventCacheKey, cachedEvents);
            cachedEvents.clear();
        }
    }
    
    /**
     * Get the key to use for looking up a collection of {@link DeviceRemote}
     * with a {@link MemcacheService} instance.
     */
    public static String getDeviceCacheKey(String user) {
        return "Devices-" + user;
    }
    
    /**
     * Get the key to use for looking up a collection of {@link EventRemote}
     * with a {@link MemcacheService} instance.
     */
    public static String getEventCacheKey(String user, String deviceId) {
        return "Events-" + deviceId + user;
    }
}
