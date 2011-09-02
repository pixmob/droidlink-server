/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.c2dm.server;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

/**
 * Persistent config info for the server - authentication token
 */
@Cached
public final class C2DMConfig {
    @Id
    private Long key;
    private String authToken;
    
    public String getAuthToken() {
        return authToken == null ? "" : authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public void setKey(Long key) {
        this.key = key;
    }
}
