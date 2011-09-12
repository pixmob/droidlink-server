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

import com.pixmob.droidlink.gae.service.Event;

/**
 * Remote representation of a {@link Event} entity.
 * @author Pixmob
 */
public class EventRemote implements Externalizable {
    public String id;
    public long created;
    public int type;
    public String name;
    public String number;
    public String message;
    public String deviceId;
    
    public EventRemote() {
    }
    
    public EventRemote(final Event event) {
        id = event.id;
        deviceId = event.device.getName();
        created = event.date;
        type = event.type.ordinal();
        name = event.name;
        number = event.number;
        message = event.message;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public long getCreated() {
        return created;
    }
    
    public void setCreated(long created) {
        this.created = created;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readUTF();
        deviceId = readUTF(in);
        type = in.readInt();
        created = in.readLong();
        number = readUTF(in);
        name = readUTF(in);
        message = readUTF(in);
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(id);
        writeUTF(out, deviceId);
        out.writeInt(type);
        out.writeLong(created);
        writeUTF(out, number);
        writeUTF(out, name);
        writeUTF(out, message);
    }
    
    private static String readUTF(ObjectInput in) throws IOException {
        final String s = in.readUTF();
        return "".equals(s) ? null : s;
    }
    
    private static void writeUTF(ObjectOutput out, String str) throws IOException {
        out.writeUTF(str == null ? "" : str);
    }
}
