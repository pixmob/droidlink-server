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

import java.util.logging.Logger;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;

/**
 * Clear data stored by Memcache.
 * @author Pixmob
 */
@At(ClearCacheWebService.URI)
@Service
public class ClearCacheWebService {
    public static final String URI = "/api/1/clearcache";
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final MemcacheService memcacheService;
    
    @Inject
    ClearCacheWebService(final MemcacheService memcacheService) {
        this.memcacheService = memcacheService;
    }
    
    @Get
    public Reply<?> clearCache() {
        logger.info("Clear cache");
        memcacheService.clearAll();
        return Reply.saying().ok();
    }
}
