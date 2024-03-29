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
package org.pixmob.droidlink.gae.web.service;

import static org.pixmob.droidlink.gae.Constants.JSON_MIME_TYPE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.pixmob.droidlink.gae.service.AccessDeniedException;
import org.pixmob.droidlink.gae.service.DeviceNotFoundException;
import org.pixmob.droidlink.gae.service.DeviceService;
import org.pixmob.droidlink.gae.service.Event;
import org.pixmob.droidlink.gae.service.EventType;

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

/**
 * Remote API for managing device events.
 * @author Pixmob
 */
@At(EventWebService.URI)
@Service
public class EventWebService {
    public static final String URI = "/api/1/events";
    
    private static final Map<Integer, EventType> INT_TO_EVENT_TYPES = new HashMap<Integer, EventType>(
            2);
    static {
        INT_TO_EVENT_TYPES.put(0, EventType.MISSED_CALL);
        INT_TO_EVENT_TYPES.put(1, EventType.RECEIVED_SMS);
    }
    
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final DeviceService deviceService;
    private final UserService userService;
    
    @Inject
    EventWebService(final DeviceService deviceService, final UserService userService) {
        this.deviceService = deviceService;
        this.userService = userService;
    }
    
    @Get
    public Reply<?> getEvents() {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        final Iterable<Event> events;
        try {
            events = deviceService.getEvents(user.getEmail(), null);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        } catch (DeviceNotFoundException e) {
            return Reply.saying().noContent();
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
    
    private Reply<?> deleteEvents() {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        deviceService.deleteEvents(user.getEmail());
        
        return Reply.saying().ok();
    }
    
    @At("/:eventId")
    @Delete
    public Reply<?> deleteEvent(@Named("eventId") String eventId) {
        if ("all".equals(eventId)) {
            return deleteEvents();
        }
        
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        logger.info("Delete event " + eventId);
        try {
            deviceService.deleteEvent(user.getEmail(), eventId);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        }
        
        return Reply.saying().ok();
    }
    
    @At("/:eventId")
    @Put
    public Reply<?> addEvent(Request request, @Named("eventId") String eventId) {
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
        
        logger.info("Add new event " + eventId + " for device " + event.deviceId);
        try {
            deviceService.addEvent(user.getEmail(), event.deviceId, eventId, event.created,
                eventType, event.number, event.name, event.message);
        } catch (AccessDeniedException e) {
            return Reply.saying().forbidden();
        } catch (DeviceNotFoundException e) {
            return Reply.saying().notFound();
        }
        
        return Reply.saying().ok();
    }
    
}
