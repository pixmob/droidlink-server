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

import java.util.logging.Logger;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.pixmob.droidlink.gae.queue.SyncQueue;

/**
 * Trigger user device synchronization by sending a push notification.
 * @author Pixmob
 */
@At(SyncWebService.URI)
@Service
public class SyncWebService {
    public static final String URI = "/api/1/sync";
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Queue syncQueue;
    private final UserService userService;
    
    @Inject
    SyncWebService(@Named("sync") final Queue syncQueue, final UserService userService) {
        this.syncQueue = syncQueue;
        this.userService = userService;
    }
    
    @Get
    public Reply<?> sync() {
        final User user = userService.getCurrentUser();
        if (user == null) {
            return Reply.saying().unauthorized();
        }
        
        // Delegate to the sync queue.
        logger.info("Queue device sync for user " + user.getEmail());
        syncQueue.add(withUrl(SyncQueue.URI).param(SyncQueue.USER_PARAM, user.getEmail()));
        
        return Reply.saying().ok();
    }
}
