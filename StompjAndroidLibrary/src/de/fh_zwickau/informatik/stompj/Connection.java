/**
 *    Copyright 2011 prashant, Michał Janiszewski, Georg Beier and others
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
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
package de.fh_zwickau.informatik.stompj;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import de.fh_zwickau.informatik.stompj.internal.MessageImpl;
import de.fh_zwickau.informatik.stompj.internal.StompJSession;

// Referenced classes of package de.fh_zwickau.informatik.stompj:
//            StompJException, MessageHandler, ErrorHandler, ErrorMessage, 
//            StompMessage

/**
 * main class to maintain a connection to the messaging systen
 * 
 * @author georg beier based on work of others
 * 
 */
public class Connection {

    private ErrorHandler errorHandler;

    private final ConcurrentHashMap<String, CopyOnWriteArraySet<MessageHandler>> messageHandlers;

    private final StompJSession session;

    /**
     * prepare default connection to localhost
     */
    public Connection() {
	this("localhost", 61613);
    }

    /**
     * prepare to connect to arbitrary host/port, ignoring authorisation
     * 
     * @param host
     * @param port
     */
    public Connection(final String host, final int port) {
	this(host, port, "", "");
    }

    /**
     * prepare to connect to arbitrary host/port, authorize with login and
     * passcode
     * 
     * @param host
     * @param port
     * @param userid
     * @param password
     */
    public Connection(final String host, final int port, final String userid,
	    final String password) {
	this.messageHandlers = new ConcurrentHashMap<String, CopyOnWriteArraySet<MessageHandler>>();
	this.session = new StompJSession(host, port, userid, password, this,
		this.messageHandlers);
    }

    /**
     * add handler object that processes incoming messages to a destination
     * 
     * @param destination
     *            where the message is delivered
     * @param handler
     *            handler object
     */
    public void addMessageHandler(String destination,
	    final MessageHandler handler) {
	destination = this.session.resolveTempName(destination);
	final CopyOnWriteArraySet<MessageHandler> set = new CopyOnWriteArraySet<MessageHandler>();
	this.messageHandlers.putIfAbsent(destination, set);
	this.messageHandlers.get(destination).add(handler);
    }

    /**
     * actually open the connection
     * 
     * @return null if connected successfully, otherwise returns the error
     *         message received from the server
     * @throws StompJException
     */
    public ErrorMessage connect() throws StompJException {
	return this.session.connect();
    }

    /**
     * close connection
     */
    public void disconnect() {
	this.session.disconnect();
    }

    /**
     * access error handler object
     * 
     * @return the error handler
     */
    public ErrorHandler getErrorHandler() {
	return this.errorHandler;
    }

    /**
     * get all handlesrs for a destination
     * 
     * @param destination
     *            where the message is delivered
     * @return array of handlers
     */
    public MessageHandler[] getMessageHandlers(final String destination) {
	return this.messageHandlers.get(destination).toArray(
		new MessageHandler[0]);
    }

    /**
     * query connection state
     * 
     * @return trfue if connected
     */
    public boolean isConnected() {
	return this.session.isConnected();
    }

    /**
     * @param destination
     */
    public void removeMessageHandlers(final String destination) {
	this.messageHandlers.remove("destination");
    }

    /**
     * send a message to stomp destination @see <a
     * href="http://stomp.codehaus.org/Protocol">
     * 
     * @param msg
     *            message to send
     * @param destination
     *            (optional) legal stomp destination
     */
    public void send(final StompMessage msg, final String... destination) {
	String dest;
	if (destination.length == 1) {
	    dest = destination[0];
	} else {
	    dest = msg.getDestination();
	}
	this.session.send(msg, dest);
    }

    /**
     * send a string message to stomp destination @see <a
     * href="http://stomp.codehaus.org/Protocol">
     * 
     * @param msg
     * @param destination
     */
    public void send(final String msg, final String destination) {
	final MessageImpl m = new MessageImpl();
	try {
	    m.setContent(msg.getBytes("UTF-8"));
	} catch (final UnsupportedEncodingException e) {
	    throw new RuntimeException(e);
	}
	this.send(((StompMessage) (m)), destination);
    }

    /**
     * set object that handles all communication errors for all destinations
     * 
     * @param handler
     *            the handler object
     */
    public void setErrorHandler(final ErrorHandler handler) {
	this.errorHandler = handler;
    }

    /**
     * subscribe to receiving messages from a stomp destination. @see <a
     * href="http://stomp.codehaus.org/Protocol">
     * 
     * @param destination
     *            when used as connector to ActiveMQ, legal values are /queue/*,
     *            /topic/*, /temp-queue/*, /temp-topic/*
     * @param autoAck
     *            if false, client acknowledges messages
     */
    public void subscribe(final String destination, final boolean autoAck) {
	this.session.subscribe(destination, autoAck);
    }

    /**
     * stop subscription
     * 
     * @param destination
     *            identifies an existing subscription
     */
    public void unsubscribe(final String destination) {
	this.session.unsubscribe(destination);
    }
}
