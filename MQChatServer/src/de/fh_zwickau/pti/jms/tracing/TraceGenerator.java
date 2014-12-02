package de.fh_zwickau.pti.jms.tracing;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.fh_zwickau.pti.mqgamecommon.MessageHeader;

/**
 * Generate event tracing records to verify asynchronous behaviour
 * 
 * @author georg beier
 * 
 */
public class TraceGenerator {
	public static final String TRACEQ = "traceq";
	private final String clazz;
	private String objId;
	private final TraceSender sender;

	/**
	 * The constructor
	 * 
	 * @param traceSender
	 *            the tracesender
	 * @param forClass
	 *            the classname
	 */
	public TraceGenerator(final TraceSender traceSender, final String forClass) {
		this(traceSender, forClass, "");
	}

	/**
	 * The constructor
	 * 
	 * @param traceSender
	 *            the tracesender
	 * @param forClass
	 *            the classname
	 * @param oid
	 *            the object id
	 */
	public TraceGenerator(final TraceSender traceSender, final String forClass,
			final String oid) {
		this.sender = traceSender;
		this.clazz = forClass;
		this.objId = oid;
	}

	public void setObjId(final String objId) {
		this.objId = objId;
	}

	public void trace(final String fromState, final String toState,
			final Message msg) {
		String event;
		try {
			event = msg.getStringProperty(MessageHeader.MsgKind.toString());
			this.sender.sendTrace(new TraceRecord(this.clazz, this.objId,
					fromState, toState, event, msg));
		} catch (final JMSException e) {
			Logger.getLogger(this.getClass()).log(Level.ERROR, "tracing error",
					e);
		}
	}
}