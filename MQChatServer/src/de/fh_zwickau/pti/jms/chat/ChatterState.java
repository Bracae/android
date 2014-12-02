package de.fh_zwickau.pti.jms.chat;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.fh_zwickau.pti.mqgamecommon.MessageHeader;
import de.fh_zwickau.pti.mqgamecommon.MessageKind;

/**
 * abstract Base type for Object-for-State Pattern
 * 
 * @author georg beier
 * 
 */
public abstract class ChatterState extends StateBase {

	public ChatterState(final String name) {
		super(name);
	}

	/**
	 * produce logging output if this method is called and was not overwritten
	 * 
	 * @param message
	 *            lms event message
	 * @throws JMSException
	 *             if message could not be decoded
	 */
	private void logUnexpectedMethod(final Message message) throws JMSException {
		Logger.getLogger(this.getClass().getName()).log(
				Level.ERROR,
				"unexpected message "
						+ message.getStringProperty(MessageHeader.MsgKind
								.toString()) + " in state " + this.getName());
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean accepted(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean authenticated(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	protected boolean chatCreated(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean closed(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean denied(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
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
		case authenticated:
			return this.authenticated(message);
		case chatterAccepted:
			return this.accepted(message);
		case chatterChatCreated:
			return this.chatCreated(message);
		case chatterClosed:
			return this.closed(message);
		case chatterDenied:
			return this.denied(message);
		case chatterInvited:
			return this.invited(message);
		case chatterNewChat:
			return this.newChat(message);
		case chatterParticipantEntered:
			return this.participantEntered(message);
		case chatterParticipantLeft:
			return this.participantLeft(message);
		case chatterParticipationRequest:
			return this.participationRequest(message);
		case chatterRequestCanceled:
			return this.requestCancelled(message);
		case chatterReject:
			return this.rejected(message);
		case chatterMsgAccept:
			return this.msgAccept(message);
		case chatterMsgAcceptInvitation:
			return this.msgAcceptInvitation(message);
		case chatterMsgCancel:
			return this.msgCancel(message);
		case chatterMsgChat:
			return this.msgChat(message);
		case chatterMsgClose:
			return this.msgClose(message);
		case chatterMsgDeny:
			return this.msgDeny(message);
		case chatterMsgInvite:
			return this.msgInvite(message);
		case chatterMsgLeave:
			return this.msgLeave(message);
		case chatterMsgReject:
			return this.msgReject(message);
		case chatterMsgRequestParticipation:
			return this.msgRequestParticipation(message);
		case chatterMsgStartChat:
			return this.msgStartChat(message);
		case chatterMsgChats:
			return this.msgQueryChats(message);
		case chatterMsgChatters:
			return this.msgQueryChatters(message);
		default:
			this.logUnexpectedMethod(message);
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
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean invited(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgAccept(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgAcceptInvitation(final Message message)
			throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgCancel(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgChat(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgClose(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgDeny(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgInvite(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgLeave(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgQueryChats(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgQueryChatters(final Message message)
			throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgReject(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgRequestParticipation(final Message message)
			throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * handle incoming jms messages from client
	 * 
	 * @throws JMSException
	 */
	protected boolean msgStartChat(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean newChat(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean participantEntered(final Message message)
			throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean participantLeft(final Message message)
			throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean participationRequest(final Message message)
			throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean rejected(final Message message) throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}

	/**
	 * method should be overwritten in all concrete subclasses if event message
	 * is really expected. else, method will be ignored and an error will be
	 * logged
	 * 
	 * @param message
	 *            incoming jms event message
	 * @return true if object will stay alive
	 * @throws JMSException
	 */
	protected boolean requestCancelled(final Message message)
			throws JMSException {
		this.logUnexpectedMethod(message);
		return true;
	}
}