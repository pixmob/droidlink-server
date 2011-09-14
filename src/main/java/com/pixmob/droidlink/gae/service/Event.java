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
package com.pixmob.droidlink.gae.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * Event datastore entity.
 * @author Pixmob
 */
@Cached
@Unindexed
public class Event implements Externalizable {
    @Id
    @Indexed
    public String id;
    @Parent
    @Indexed
    public Key<Device> device;
    public EventType type;
    public long date;
    public String number;
    public String name;
    public String message;
    @Indexed
    public long update;
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (String) in.readObject();
        device = new Key<Device>(Device.class, (String) in.readObject());
        type = EventType.valueOf((String) in.readObject());
        date = in.readLong();
        number = (String) in.readObject();
        name = (String) in.readObject();
        message = (String) in.readObject();
        update = in.readLong();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
        out.writeObject(device.getName());
        out.writeObject(type.name());
        out.writeLong(date);
        out.writeObject(number);
        out.writeObject(name);
        out.writeObject(message);
        out.writeLong(update);
    }
}
