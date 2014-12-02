package de.fh_zwickau.pti.jms.tracing;

import java.io.Serializable;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.fh_zwickau.pti.mqgamecommon.MessageHeader;

/**
 * Capture a single point for tracing in a human readable form
 * 
 * @author georg beier
 * 
 */
public class TraceRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String clazz;
	private final String event;
	private final String fromState;
	private final String objId;
	private final HashMap<String, String> properties = new HashMap<>(4);
	private final long timestamp;
	private final String toState;

	/**
	 * initialize record
	 * 
	 * @param cl
	 *            traced class (->fromState chart)
	 * @param obj
	 *            traced object
	 * @param fromSt
	 *            actual fromState before event handling
	 * @param toSt
	 *            resulting fromState after event handling
	 * @param evt
	 *            event kind
	 * @param msg
	 *            full event message (optional)
	 */
	public TraceRecord(final String cl, final String obj, final String fromSt,
			final String toSt, final String evt, final Message msg) {
		this.timestamp = System.nanoTime();
		this.clazz = cl;
		this.objId = obj;
		this.fromState = fromSt;
		this.toState = toSt;
		this.event = evt;
		try {
			this.properties.put(MessageHeader.MsgKind.toString(),
					msg.getStringProperty(MessageHeader.MsgKind.toString()));
			this.properties.put(MessageHeader.AuthToken.toString(),
					msg.getStringProperty(MessageHeader.AuthToken.toString()));
			if (msg instanceof TextMessage) {
				this.properties.put("body", ((TextMessage) msg).getText());
			}
		} catch (final JMSException e) {
			Logger.getLogger(this.getClass()).log(Level.ERROR, "tracing error",
					e);
		}
	}

	public String getClazz() {
		return this.clazz;
	}

	public String getEvent() {
		return this.event;
	}

	public String getFromState() {
		return this.fromState;
	}

	public String getObjId() {
		return this.objId;
	}

	public HashMap<String, String> getProperties() {
		return this.properties;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public String getToState() {
		return this.toState;
	}

	@Override
	public String toString() {
		return "[" + this.clazz + ", (" + this.objId + "), " + this.fromState
				+ " --<" + this.event + ">-> " + this.toState + " at "
				+ this.timestamp + "ns]";
	}
}