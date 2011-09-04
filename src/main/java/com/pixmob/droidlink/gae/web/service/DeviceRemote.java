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
package com.pixmob.droidlink.gae.web.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.pixmob.droidlink.gae.service.Device;

/**
 * Remote representation of a {@link Device} entity.
 * @author Pixmob
 */
public class DeviceRemote implements Externalizable {
    public String id;
    public String name;
    public String c2dm;
    
    public DeviceRemote() {
    }
    
    public DeviceRemote(final Device device) {
        id = device.id;
        name = device.name;
        
        // Note: the C2DM key is not included from the datastore to the client.
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getC2dm() {
        return c2dm;
    }
    
    public void setC2dm(String c2dm) {
        this.c2dm = c2dm;
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readUTF();
        name = readUTF(in);
        c2dm = readUTF(in);
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(id);
        writeUTF(out, name);
        writeUTF(out, c2dm);
    }
    
    private static String readUTF(ObjectInput in) throws IOException {
        final String s = in.readUTF();
        return "".equals(s) ? null : s;
    }
    
    private static void writeUTF(ObjectOutput out, String str) throws IOException {
        out.writeUTF(str == null ? "" : str);
    }
}
