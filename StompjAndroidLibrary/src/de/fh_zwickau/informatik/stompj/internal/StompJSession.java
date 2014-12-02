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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import de.fh_zwickau.informatik.stompj.Connection;
import de.fh_zwickau.informatik.stompj.ErrorMessage;
import de.fh_zwickau.informatik.stompj.MessageHandler;
import de.fh_zwickau.informatik.stompj.StompJException;
import de.fh_zwickau.informatik.stompj.StompJRuntimeException;
import de.fh_zwickau.informatik.stompj.StompMessage;

/**
 * 
 * main implementation class that handles the connection to message brokers
 * using the stomp protocol. specially adapted and tested for use on android
 * systems as interface to apache ActiveMQ.
 * 
 * @see <a href="http://stomp.codehaus.org/Protocol"><br>
 * @see <a href="http://activemq.apache.org/">
 * 
 * @author others
 * @author georg beier
 * 
 */
public class StompJSession {

    public static final String JMS_TEMP_Q = "/remote-temp-queue/";
    public static final String RESOLVE_TEMP_NAME = "resolve-temp-name";
    public static final String TEMP_Q = "/temp-queue/";

    private final HashMap<String, Boolean> autoAckMap;
    private final Connection connection;
    private FrameReceiver frameReceiver;
    private final String host;
    private BufferedInputStream input;
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<MessageHandler>> messageHandlers;
    private BufferedOutputStream output;
    private final String password;
    private final int port;
    private Socket socket;
    private final HashMap<String, String> tempDestinationMap;
    private final String userid;

    /**
     * @param host
     * @param port
     * @param userid
     * @param password
     * @param con
     * @param handlers
     * 
     */
    public StompJSession(
	    final String host,
	    final int port,
	    final String userid,
	    final String password,
	    final Connection con,
	    final ConcurrentHashMap<String, CopyOnWriteArraySet<MessageHandler>> handlers) {
	this.host = host;
	this.port = port;
	this.userid = userid;
	this.password = password;
	this.connection = con;
	this.messageHandlers = handlers;
	this.autoAckMap = new HashMap<String, Boolean>();
	this.tempDestinationMap = new HashMap<String, String>();
    }

    /**
     * try a few seconds to connect to a message broker using host, port, login
     * and passcode. if connection fails, throw exception.
     * 
     * @return
     * 
     * @throws StompJException
     *             id host is unknown or connection does not succed within 2
     *             seconds.
     */
    public ErrorMessage connect() throws StompJException {
	try {
	    this.socket = new Socket();
	    final InetSocketAddress address = new InetSocketAddress(this.host,
		    this.port);
	    final int tout = 2000;
	    this.socket.connect(address, tout);
	    // socket = new Socket(host, port);
	    this.input = new BufferedInputStream(this.socket.getInputStream());
	    this.output = new BufferedOutputStream(
		    this.socket.getOutputStream());
	    this.output.write(this.createCONNECTFrame(this.userid,
		    this.password));
	    this.output.flush();
	    this.frameReceiver = new FrameReceiver(this, this.input,
		    this.messageHandlers);
	    final ErrorMessage errorMsg = this.frameReceiver
		    .processFirstResponse();
	    if (errorMsg == null) {
		this.frameReceiver.start();
	    } else {
		this.disconnect();
		return errorMsg;
	    }
	} catch (final UnknownHostException e) {
	    this.disconnect();
	    throw new StompJException(e.getMessage(), e);
	} catch (final IOException e) {
	    this.disconnect();
	    throw new StompJException(e.getMessage(), e);
	}
	return null;
    }

    /**
     * send a disconnect message and close socket and io streams.
     */
    public void disconnect() {
	this.sendFrame(this.createDISCONNECTFrame());
	// interrupt pending read operation on input
	this.frameReceiver.interrupt();
	try {
	    // wait for io operation to finish before closing streams
	    synchronized (this.frameReceiver) {
		try {
		    this.frameReceiver.wait(2000);
		} catch (final InterruptedException e) {
		}
	    }
	    this.input.close();
	    this.output.close();
	    this.socket.close();
	} catch (final IOException ioexception) {
	}
    }

    /**
     * find out connection state
     * 
     * @return true if connected
     */
    public boolean isConnected() {
	if (this.socket == null) {
	    return false;
	} else {
	    return !this.socket.isClosed() && this.socket.isConnected();
	}
    }

    /**
     * find the real name of a temporary destination, if already resolved
     * 
     * @param destination
     *            stomp temporary destination
     * @return the name used by the message broker, if known. else echo the
     *         input.
     */
    public String resolveTempName(final String destination) {
	if (this.tempDestinationMap.containsKey(destination)) {
	    return this.tempDestinationMap.get(destination);
	} else {
	    return destination;
	}
    }

    /**
     * send message to given destination. the destination field in ther message
     * is not considered, so destination has to be given explicitely!
     * 
     * @param msg
     *            message to be sent
     * @param destination
     *            valid stomp destination
     */
    public void send(final StompMessage msg, final String destination) {
	this.sendFrame(this.createSENDFrame(msg, destination));
    }

    /**
     * subscribe to receiving messages from a stomp destination. @see <a
     * href="http://stomp.codehaus.org/Protocol"> special care has to be taken
     * with temporary destinations, because the message broker "invents" a new
     * name for them. So we will have to find out their real name first, before
     * we can subscribe.<br>
     * Here ist the implementation!
     * 
     * @param destination
     *            when used as connector to ActiveMQ, legal values are /queue/*,
     *            /topic/*, /temp-queue/*, /temp-topic/*
     * @param autoAck
     *            if false, client acknowledges messages
     */
    public void subscribe(final String destination, final boolean autoAck) {
	this.sendFrame(this.createSUBSCRIBEFrame(destination, autoAck));
	this.autoAckMap.put(destination, Boolean.valueOf(autoAck));
	if (this.isTempQueue(destination)) {
	    this.queryTempQueueName(destination);
	}
    }

    /**
     * stop subscription<br>
     * Here ist the implementation
     * 
     * @param destination
     *            identifies an existing subscription
     */
    public void unsubscribe(final String destination) {
	this.sendFrame(this.createUNSUBSCRIBEFrame(destination));
    }

    /**
     * creates an ACK (acknowledge) message frame according to the stomp
     * protocol specification
     * 
     * @param msgId
     *            reference to the message being acknowledged
     * @return nicely coded as byte[]
     */
    private byte[] createACKFrame(final String msgId) {
	final ByteArrayOutputStream os = new ByteArrayOutputStream();
	try {
	    os.write(this.prepareBytes("ACK"));
	    os.write(10);
	    os.write(this.prepareProperty("message-id", msgId));
	    os.write(10);
	    os.write(10);
	    os.write(0);
	} catch (final IOException e) {
	    throw new RuntimeException(e);
	}
	return os.toByteArray();
    }

    /**
     * creates a CONNECT message frame according to the stomp protocol
     * specification
     * 
     * @param userid
     * @param password
     * @return nicely coded as byte[]
     * @throws IOException
     */
    private byte[] createCONNECTFrame(final String userid, final String password)
	    throws IOException {
	final ByteArrayOutputStream os = new ByteArrayOutputStream();
	os.write(this.prepareBytes("CONNECT"));
	os.write(10);
	os.write(this.prepareBytes((new StringBuilder("login: "))
		.append(userid).toString()));
	os.write(10);
	os.write(this.prepareBytes((new StringBuilder("passcode:")).append(
		password).toString()));
	os.write(10);
	os.write(10);
	os.write(0);
	return os.toByteArray();
    }

    /**
     * creates a DISCONNECT message frame according to the stomp protocol
     * specification
     * 
     * @return nicely coded as byte[]
     */
    private byte[] createDISCONNECTFrame() {
	final ByteArrayOutputStream os = new ByteArrayOutputStream();
	try {
	    os.write(this.prepareBytes("DISCONNECT"));
	    os.write(10);
	    os.write(10);
	    os.write(0);
	} catch (final IOException e) {
	    throw new RuntimeException(e);
	}
	return os.toByteArray();
    }

    /**
     * 
     * creates a SEND message frame according to the stomp protocol
     * specification
     * 
     * @param msg
     *            what to send
     * @param destination
     *            where to send
     * @return nicely coded as byte[]
     */
    private byte[] createSENDFrame(final StompMessage msg,
	    final String destination) {
	final ByteArrayOutputStream os = new ByteArrayOutputStream();
	try {
	    os.write(this.prepareBytes("SEND"));
	    os.write(10);
	    os.write(this.prepareProperty("destination", destination));
	    os.write(10);
	    final String propNames[] = msg.getPropertyNames();
	    String as[];
	    final int j = (as = propNames).length;
	    for (int i = 0; i < j; i++) {
		final String p = as[i];
		os.write(this.prepareProperty(p, msg.getProperty(p)));
		os.write(10);
	    }

	    os.write(10);
	    os.write(msg.getContentAsBytes());
	    os.write(0);

	    // System.out.println(os.toString("UTF-8"));
	} catch (final IOException e) {
	    throw new RuntimeException(e);
	}
	return os.toByteArray();
    }

    /**
     * creates a SUBSCRIBE message frame according to the stomp protocol
     * specification
     * 
     * @param destination
     *            where to send
     * @param autoAck
     *            shall broker autoack messages or wait for ack from client
     * @return nicely coded as byte[]
     */
    private byte[] createSUBSCRIBEFrame(final String destination,
	    final boolean autoAck) {
	final ByteArrayOutputStream os = new ByteArrayOutputStream();
	try {
	    os.write(this.prepareBytes("SUBSCRIBE"));
	    os.write(10);
	    os.write(this.prepareProperty("destination", destination));
	    os.write(10);
	    os.write(this.prepareProperty("ack", autoAck ? "auto" : "client"));
	    os.write(10);
	    os.write(10);
	    os.write(0);
	} catch (final IOException e) {
	    throw new RuntimeException(e);
	}
	return os.toByteArray();
    }

    /**
     * creates a UNSUBSCRIBE message frame according to the stomp protocol
     * specification
     * 
     * @param destination
     *            where to send
     * @return nicely coded as byte[]
     */
    private byte[] createUNSUBSCRIBEFrame(final String destination) {
	final ByteArrayOutputStream os = new ByteArrayOutputStream();
	try {
	    os.write(this.prepareBytes("UNSUBSCRIBE"));
	    os.write(10);
	    os.write(this.prepareProperty("destination", destination));
	    os.write(10);
	    os.write(10);
	    os.write(0);
	} catch (final IOException e) {
	    throw new RuntimeException(e);
	}
	return os.toByteArray();
    }

    /**
     * determine if stomp destination is a temporary queue, i.e. destination
     * starts with /temp-queue/
     * 
     * @param destination
     *            stomp destination name
     * @return true if temp
     */
    private boolean isTempQueue(final String destination) {
	return destination.startsWith(StompJSession.TEMP_Q);
    }

    /**
     * code string to byte[] using UTF-8 coding and mask Exception to
     * RuntimeException
     * 
     * @param s
     * @return
     */
    private byte[] prepareBytes(final String s) {
	try {
	    return s.getBytes("UTF-8");
	} catch (final UnsupportedEncodingException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * convert a property, as from the properties map, to the stomp notation
     * 
     * @param propName
     * @param prop
     *            property value as String
     * @return "propName: prop"
     */
    private byte[] prepareProperty(final String propName, final String prop) {
	return this.prepareBytes((new StringBuilder(String.valueOf(propName)))
		.append(":").append(prop).toString());
    }

    /**
     * send a message to the temporary queue to find out which name is given by
     * the JMS broker. As this message is returned to the sender, its
     * destination attribute contains the JMS name of the temp queue.
     * 
     * @param destination
     *            temporary destination name as seen from stomp
     */
    private void queryTempQueueName(final String destination) {
	final MessageImpl selfMessage = new MessageImpl();
	final HashMap<String, String> props = new HashMap<String, String>();
	props.put(StompJSession.RESOLVE_TEMP_NAME, destination);
	selfMessage.setProperties(props);
	selfMessage.setContent(destination.getBytes());
	this.send(selfMessage, destination);
    }

    /**
     * actually write out the message frame to the socket
     * 
     * @param frame
     *            message codes as byte[]
     * @throws StompJRuntimeException
     *             wrapper for various io exceptions
     */
    private synchronized void sendFrame(final byte frame[])
	    throws StompJRuntimeException {
	if (!this.isConnected()) {
	    throw new StompJRuntimeException("Not connected to the server");
	}
	try {
	    this.output.write(frame);
	    this.output.flush();
	} catch (final IOException e) {
	    throw new StompJRuntimeException(e.getMessage(), e);
	}
    }

    /**
     * accessor method. really needed?
     * 
     * @return
     */
    Connection getConnection() {
	return this.connection;
    }

    void receivedTempQueueName(final StompMessage reply) {
	final String jmsDestination = reply.getDestination();
	final String stompDestination = reply
		.getProperty(StompJSession.RESOLVE_TEMP_NAME);
	this.tempDestinationMap.put(stompDestination, jmsDestination);
	final Boolean ack = this.autoAckMap.remove(stompDestination);
	if (ack != null) {
	    this.autoAckMap.put(jmsDestination, ack);
	} else {
	    this.autoAckMap.put(jmsDestination, true);
	}
	final CopyOnWriteArraySet<MessageHandler> handlers = this.messageHandlers
		.remove(stompDestination);
	if (handlers != null) {
	    this.messageHandlers.put(jmsDestination, handlers);
	}
    }

    /**
     * check if destination of received message requires explicit acknowledge,
     * or if autoAcknowledge was set for it. send ack frame if needed.
     * 
     * @param msg
     *            incoming message
     */
    void sendAckIfNeeded(final StompMessage msg) {
	final String dest = msg.getDestination();
	final Boolean val = this.autoAckMap.get(dest);
	if ((val != null) && !val) {
	    this.sendFrame(this.createACKFrame(msg.getMessageId()));
	}
    }
}
