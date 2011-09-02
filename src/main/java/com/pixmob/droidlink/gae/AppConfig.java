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
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;
import com.pixmob.droidlink.gae.queue.SyncQueue;
import com.pixmob.droidlink.gae.service.ServiceModule;
import com.pixmob.droidlink.gae.web.service.DeviceWebService;
import com.pixmob.droidlink.gae.web.service.SyncWebService;

/**
 * Guice application configuration.
 * @author Pixmob
 */
public class AppConfig extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new AppEngineModule(), new ServiceModule(), new WebModule(),
            new C2DMModule());
    }
    
    /**
     * Guice configuration module for AppEngine.
     * @author Pixmob
     */
    static class AppEngineModule extends AbstractModule {
        @Override
        protected void configure() {
        }
        
        @Provides
        @Singleton
        public UserService getUserService() {
            return UserServiceFactory.getUserService();
        }
        
        @Provides
        public User getUser(UserService userService) {
            return userService.getCurrentUser();
        }
        
        @Provides
        @Singleton
        public Queue getSyncQueue() {
            return QueueFactory.getQueue("sync");
        }
    }
    
    /**
     * Guice configuration module for web resources powered by Sitebricks.
     * @author Pixmob
     */
    static class WebModule extends SitebricksModule {
        @Override
        protected void configureSitebricks() {
            at("/api/1/device").serve(DeviceWebService.class);
            at("/api/1/sync").serve(SyncWebService.class);
            at("/tasks/sync").serve(SyncQueue.class);
        }
    }
}
