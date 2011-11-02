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
package org.pixmob.droidlink.gae.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pixmob.droidlink.gae.Constants;

import com.google.android.c2dm.server.C2DMessaging;
import com.google.inject.Inject;

/**
 * Send push notifications to user devices.
 * @author Pixmob
 */
public class PushService {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final C2DMessaging c2dm;
    private final DeviceService deviceService;
    
    @Inject
    PushService(final DeviceService deviceService, final C2DMessaging c2dm) {
        this.deviceService = deviceService;
        this.c2dm = c2dm;
    }
    
    public void syncDevices(String user, String deviceIdSource, String syncToken) {
        for (final Device device : deviceService.getDevices(user)) {
            if (device.id.equals(deviceIdSource) || device.c2dm == null) {
                // The sync is not started for devices without a C2DM
                // registration key, and for the device which triggered this
                // sync.
                continue;
            }
            
            final String collapseKey = Long.toHexString((user + "#" + device.id).hashCode());
            try {
                c2dm.sendWithRetry(device.c2dm, collapseKey, Constants.C2DM_MESSAGE_EXTRA,
                    Constants.C2DM_MESSAGE_SYNC, Constants.C2DM_SYNC_TOKEN_EXTRA, syncToken);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to trigger user sync with C2DM", e);
            }
        }
    }
}
