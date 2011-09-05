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
package com.pixmob.droidlink.gae.cron;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.logging.Logger;

import com.google.appengine.api.taskqueue.Queue;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.pixmob.droidlink.gae.queue.CacheQueue;

/**
 * Populate data cache for every users.
 * @author Pixmob
 */
@At(CacheCron.URI)
@Service
public class CacheCron {
    public static final String URI = "/cron/cache";
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Queue cacheQueue;
    
    @Inject
    CacheCron(@Named("cache") final Queue cacheQueue) {
        this.cacheQueue = cacheQueue;
    }
    
    @Get
    public Reply<?> cache() {
        logger.info("Queue cache task for every users");
        cacheQueue.add(withUrl(CacheQueue.URI));
        return Reply.saying().ok();
    }
}
