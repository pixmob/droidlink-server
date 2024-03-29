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

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.googlecode.objectify.ObjectifyFactory;

/**
 * Guice configuration module for this package.
 * @author Pixmob
 */
public class ServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        final ObjectifyFactory of = new ObjectifyFactory();
        of.register(Device.class);
        of.register(Event.class);
        bind(ObjectifyFactory.class).toInstance(of);
        
        bind(DeviceService.class).in(Singleton.class);
        bind(PushService.class).in(Singleton.class);
    }
}
