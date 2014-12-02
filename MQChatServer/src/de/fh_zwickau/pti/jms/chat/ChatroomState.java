package de.fh_zwickau.pti.jms.chat;

import javax.jms.JMSException;
import javax.jms.Message;

import de.fh_zwickau.pti.mqgamecommon.MessageKind;

/**
 * abstract Base type for Object-for-State Pattern
 * 
 * @author georg beier
 * 
 */
public abstract class ChatroomState extends StateBase {
	/**
	 * for debugging, trace messages can be swiched on
	 */
	private static final boolean trace = true;

	public ChatroomState(final String name) {
		super(name);
	}

	/**
	 * @return the trace
	 */
	public static boolean isTrace() {
		return ChatroomState.trace;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return
	 * @throws JMSException
	 */
	protected boolean cancelRequest(final Message message) throws JMSException {
		this.logError(message);
		return false;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return
	 * @throws JMSException
	 */
	protected boolean chat(final Message message) throws JMSException {
		this.logError(message);
		return false;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return
	 * @throws JMSException
	 */
	protected boolean close(final Message message) throws JMSException {
		this.logError(message);
		return false;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return
	 * @throws JMSException
	 */
	protected boolean create(final Message message) throws JMSException {
		this.logError(message);
		return false;
	}

	/**
	 * select the appropriate event handler method for incoming method for class
	 * Chatroom
	 * 
	 * @param kind
	 *            enum defining kind of incoming method
	 * @param message
	 *            incoming jms method
	 * @return
	 * @throws JMSException
	 */
	@Override
	protected boolean executeEventMethod(final MessageKind kind,
			final Message message) throws JMSException {
		switch (kind) {
		case chatCreate:
			return this.create(message);
		case chatChat:
			return this.chat(message);
		case chatNewParticipant:
			return this.newParticipant(message);
		case chatLeave:
			return this.leave(message);
		case chatCancelRequest:
			return this.cancelRequest(message);
		case chatParticipationRequest:
			return this.participationRequest(message);
		case chatClose:
			return this.close(message);
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return
	 * @throws JMSException
	 */
	protected boolean leave(final Message message) throws JMSException {
		this.logError(message);
		return false;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return
	 * @throws JMSException
	 */
	protected boolean newParticipant(final Message message) throws JMSException {
		this.logError(message);
		return false;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return
	 * @throws JMSException
	 */
	protected boolean participationRequest(final Message message)
			throws JMSException {
		this.logError(message);
		return false;
	}
}