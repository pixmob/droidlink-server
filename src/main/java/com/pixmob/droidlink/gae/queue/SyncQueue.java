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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.pixmob.droidlink.gae.service.PushService;

/**
 * Send push notification to user devices.
 * @author Pixmob
 */
@At(SyncQueue.URI)
@Service
public class SyncQueue {
    public static final String URI = "/tasks/sync";
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final PushService pushService;
    
    @Inject
    SyncQueue(final PushService pushService) {
        this.pushService = pushService;
    }
    
    @Post
    public Reply<?> sync(Request request) {
        final String user = request.param("user");
        checkNotNull(user, "User is required");
        checkArgument(user.length() != 0, "User is required");
        
        logger.info("Sync every device for user " + user);
        pushService.syncDevices(user, null);
        
        return Reply.saying().ok();
    }
}
