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
package org.pixmob.droidlink.gae.queue;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Logger;

import org.pixmob.droidlink.gae.service.PushService;

import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;

/**
 * Send push notification to user devices.
 * @author Pixmob
 */
@At(SyncQueue.URI)
@Service
public class SyncQueue {
    public static final String URI = "/tasks/sync";
    public static final String USER_PARAM = "user";
    public static final String DEVICE_ID_SOURCE_PARAM = "deviceIdSource";
    public static final String SYNC_TOKEN_PARAM = "token";
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final PushService pushService;
    
    @Inject
    SyncQueue(final PushService pushService) {
        this.pushService = pushService;
    }
    
    @Post
    public Reply<?> sync(Request request) {
        final String user = request.param(USER_PARAM);
        checkNotNull(user, "User is required");
        checkArgument(user.length() != 0, "User is required");
        
        final String deviceIdSource = request.param(DEVICE_ID_SOURCE_PARAM);
        final String syncToken = request.param(SYNC_TOKEN_PARAM);
        
        logger.info("Sync devices for user " + user + " (deviceIdSource=" + deviceIdSource + ")");
        pushService.syncDevices(user, deviceIdSource, syncToken);
        
        return Reply.saying().ok();
    }
}
