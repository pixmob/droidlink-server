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
package com.pixmob.droidlink.gae.service;

/**
 * Exception when a {@link Device} is not found.
 * @author Pixmob
 */
public class DeviceNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String deviceId;
    
    public DeviceNotFoundException(final String deviceId) {
        super("Device not found: " + deviceId);
        this.deviceId = deviceId;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
}
