package de.fh_zwickau.pti.jms.tracing;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Send trace via jms destination
 * 
 * @author georg beier
 * 
 */
public class JmsTraceSender implements TraceSender {
	private final MessageProducer producer;
	private final Session session;

	/**
	 * The constructor
	 * 
	 * @param p
	 *            the messageproducer
	 * @param s
	 *            the session
	 */
	public JmsTraceSender(final MessageProducer p, final Session s) {
		this.producer = p;
		this.session = s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.jms.tracing.TraceSender#sendTrace(de.fh_zwickau.pti
	 * .jms.tracing.TraceRecord)
	 */
	@Override
	public void sendTrace(final TraceRecord record) {
		try {
			final ObjectMessage objMsg = this.session
					.createObjectMessage(record);
			this.producer.send(objMsg);
		} catch (final JMSException e) {
			Logger.getLogger(this.getClass()).log(Level.ERROR, "tracing error",
					e);
		}
	}
}