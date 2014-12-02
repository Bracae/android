/**
 *    Copyright 2011 prashant, Michał Janiszewski, Georg Beier and others

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


 * This source code is based on work of prashant ??? at google code, published under 
 * an Apache License 2.0. The original package path was pk.aamir.stompj.* . Code was
 * decompiled from class files using Jad v1.5.8g by Michał Janiszewski and published on
 * http://www.gitorious.org/pai-market/stompj.
 *  To keep the work available, the base package path was moved to an existing url.
 * 
 * Adapted to work with Apache ActiveMQ temporary queues, added comments and use 
 * template classes
 * 
 */

package de.fh_zwickau.informatik.stompj.internal;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import de.fh_zwickau.informatik.stompj.StompMessage;

public class MessageImpl implements StompMessage {

    private static final long serialVersionUID = 9124127430655793092L;

    private byte content[];

    private String destination;

    private String messageId;

    private HashMap<String, String> properties;

    public MessageImpl() {
	this.properties = new HashMap<String, String>();
    }

    /**
     * access arbitrary content
     * 
     * @return content as byte[]
     */
    @Override
    public byte[] getContentAsBytes() {
	return this.content;
    }

    /**
     * access string message
     * 
     * @return string content
     */
    @Override
    public String getContentAsString() {
	try {
	    return new String(this.content, "UTF-8");
	} catch (final UnsupportedEncodingException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * get the stomp destination of a message.
     */
    @Override
    public String getDestination() {
	return this.destination;
    }

    @Override
    public String getMessageId() {
	return this.messageId;
    }

    @Override
    public String getProperty(final String key) {
	return this.properties.get(key);
    }

    @Override
    public String[] getPropertyNames() {
	return this.properties.keySet().toArray(new String[0]);
    }

    /**
     * set content for arbitrary message
     * 
     * @param content
     *            as byte[]
     */
    public void setContent(final byte content[]) {
	this.content = content;
    }

    /**
     * set content for string message
     * 
     * @param content
     *            as String
     */
    public void setContent(final String content) {
	try {
	    this.content = content.getBytes("UTF-8");
	} catch (final UnsupportedEncodingException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * set the stomp destination of a message.
     * 
     * @param destination
     *            a legal stomp destination
     */
    public void setDestination(final String destination) {
	this.destination = destination;
    }

    public void setMessageId(final String id) {
	this.messageId = id;
    }

    public void setProperties(final HashMap<String, String> properties) {
	this.properties = properties;
    }

    public void setProperty(final String name, final String value) {
	this.properties.put(name, value);
    }
}
