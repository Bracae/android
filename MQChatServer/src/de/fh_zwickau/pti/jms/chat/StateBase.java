package de.fh_zwickau.pti.jms.chat;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.fh_zwickau.pti.mqgamecommon.MessageHeader;
import de.fh_zwickau.pti.mqgamecommon.MessageKind;

/**
 * extracted common methods for all state classes
 * 
 * @author georg beier
 * 
 */
public abstract class StateBase {

	protected String name;

	public StateBase(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * handle all incoming jms messages by delegating message to appropriate
	 * method of subclass
	 * 
	 * @param message
	 *            a jms message
	 */
	public boolean processMessage(final Message message) {
		String msgKind = "";
		try {
			msgKind = message.getStringProperty(MessageHeader.MsgKind
					.toString());
			final MessageKind kind = MessageKind.valueOf(msgKind);
			return this.executeEventMethod(kind, message);
		} catch (final IllegalArgumentException e) {
			Logger.getRootLogger().log(Level.ERROR,
					"Unknown message kind " + msgKind);
		} catch (final JMSException e) {
			Logger.getRootLogger().log(Level.ERROR, e);
		}
		return false;
	}

	/**
	 * select the appropriate event handler method for incoming method, must be
	 * implemented in subclasses
	 * 
	 * @param kind
	 *            enum defining kind of incoming method
	 * @param message
	 *            incoming jms method
	 * @return
	 * @throws JMSException
	 */
	protected abstract boolean executeEventMethod(MessageKind kind,
			Message message) throws JMSException;

	/**
	 * log errors for unexpected events
	 * 
	 * @param message
	 */
	protected void logError(final Message message) {
		try {
			Logger.getRootLogger().log(
					Level.ERROR,
					"unexpected message "
							+ message.getStringProperty(MessageHeader.MsgKind
									.toString()) + " in state " + this.name);
		} catch (final JMSException e) {
		}
	}

	/**
	 * State entry method, to be redefined by subclass if needed
	 * 
	 * @param message
	 *            message that triggered the transition
	 * @throws JMSException
	 */
	protected void onEntry(final Message message) throws JMSException {

	}

	/**
	 * State exit method, to be redefined by subclass if needed
	 * 
	 * @param message
	 *            message that triggered the transition
	 */
	protected void onExit(final Message message) {

	}
}