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
package org.pixmob.droidlink.gae;

/**
 * Application constants.
 * @author Pixmob
 */
public final class Constants {
    public static final String C2DM_SENDER_ID = "pixmobstudio@gmail.com";
    public static final String C2DM_MESSAGE_EXTRA = "message";
    public static final String C2DM_MESSAGE_SYNC = "sync";
    public static final String C2DM_SYNC_TOKEN_EXTRA = "token";
    
    public static final String JSON_MIME_TYPE = "application/json";
    
    public static final boolean ENABLE_APPSTATS = true;
    
    private Constants() {
    }
}
