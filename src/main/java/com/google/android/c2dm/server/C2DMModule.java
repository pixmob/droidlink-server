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
package com.google.android.c2dm.server;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.googlecode.objectify.ObjectifyFactory;

/**
 * C2DM package configuration.
 * @author Pixmob
 */
public class C2DMModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(C2DMessaging.class);
        serve("/tasks/c2dm").with(C2DMRetryServlet.class);
    }
    
    @Provides
    @Singleton
    public C2DMConfigLoader getConfigLoader(ObjectifyFactory of) {
        of.register(C2DMConfig.class);
        return new C2DMConfigLoader(of);
    }
}
