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
package com.pixmob.droidlink.gae;

import com.google.android.c2dm.server.C2DMModule;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.appstats.AppstatsFilter;
import com.google.appengine.tools.appstats.AppstatsServlet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.sitebricks.SitebricksModule;
import com.pixmob.droidlink.gae.cron.CacheCron;
import com.pixmob.droidlink.gae.queue.CacheQueue;
import com.pixmob.droidlink.gae.queue.SyncQueue;
import com.pixmob.droidlink.gae.service.ServiceModule;
import com.pixmob.droidlink.gae.web.service.ClearCacheWebService;
import com.pixmob.droidlink.gae.web.service.DeviceWebService;
import com.pixmob.droidlink.gae.web.service.SyncWebService;

import freemarker.log.Logger;

/**
 * Guice application configuration.
 * @author Pixmob
 */
public class AppConfig extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new AppstatsModule(), new AppEngineModule(),
            new ServiceModule(), new WebModule(), new C2DMModule());
    }
    
    /**
     * Guice configuration module for AppEngine.
     * @author Pixmob
     */
    static class AppEngineModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Queue.class).annotatedWith(Names.named("sync")).toInstance(
                QueueFactory.getQueue("sync"));
            bind(Queue.class).annotatedWith(Names.named("cache")).toInstance(
                QueueFactory.getQueue("cache"));
            bind(UserService.class).toInstance(UserServiceFactory.getUserService());
            bind(MemcacheService.class).toInstance(MemcacheServiceFactory.getMemcacheService());
        }
    }
    
    /**
     * Guice configuration module for web resources powered by Sitebricks.
     * @author Pixmob
     */
    static class WebModule extends SitebricksModule {
        @Override
        protected void configureSitebricks() {
            // Register web services.
            at(DeviceWebService.URI).serve(DeviceWebService.class);
            at(SyncWebService.URI).serve(SyncWebService.class);
            at(ClearCacheWebService.URI).serve(ClearCacheWebService.class);
            
            // Register task queues.
            at(SyncQueue.URI).serve(SyncQueue.class);
            at(CacheQueue.URI).serve(CacheQueue.class);
            
            // Register cron tasks.
            at(CacheCron.URI).serve(CacheCron.class);
        }
    }
    
    /**
     * Guice configuration module for AppEngine statistics.
     * @author Pixmob
     */
    static class AppstatsModule extends ServletModule {
        private final Logger logger = Logger.getLogger(getClass().getName());
        
        @Override
        protected void configureServlets() {
            if (Constants.ENABLE_APPSTATS) {
                try {
                    bind(AppstatsFilter.class).in(Singleton.class);
                    bind(AppstatsServlet.class).in(Singleton.class);
                    
                    serve("/appstats", "/appstats/*").with(AppstatsServlet.class);
                    filter("/*").through(AppstatsFilter.class);
                } catch (Exception e) {
                    logger.error("Cannot setup Appstats", e);
                }
            }
        }
    }
}
