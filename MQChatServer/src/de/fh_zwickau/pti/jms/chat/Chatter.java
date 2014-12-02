package de.fh_zwickau.pti.jms.chat;

import java.io.Serializable;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import de.fh_zwickau.pti.jms.tracing.TraceGenerator;
import de.fh_zwickau.pti.jms.userservice.JmsReference;
import de.fh_zwickau.pti.jms.userservice.User;
import de.fh_zwickau.pti.mqgamecommon.MessageHeader;
import de.fh_zwickau.pti.mqgamecommon.MessageKind;

/**
 * Repräsentiert einen Chat Teilnehmer auf Serverseite. Über die reine
 * Identifikation des externen Users, die durch die Basisklasse User gegeben
 * ist, verfügt Chatter über folgende Funktionalitäten:
 * <ul>
 * <li>State Chart Implementierung bildet die Kommunikation zwischen Chatter und
 * Chatroom ab</li>
 * <li>Message-Kommunikation mit externem Client</li>
 * <li>Message Kommunikation mit Chatroom</li>
 * </ul>
 * 
 * @author georg beier
 * 
 */
public class Chatter extends User implements Serializable {
	/**
	 * Implementierung des Compound State Chatting aus der State Chart
	 * 
	 * @author georg beier
	 * 
	 */
	private class Chatting extends ChatterState {

		public Chatting(final String name) {
			super(name);
		}

		@Override
		protected boolean msgChat(final Message message) throws JMSException {
			final TextMessage outMsg = new ActiveMQTextMessage();
			if (message instanceof TextMessage) {
				outMsg.setText(((TextMessage) message).getText());
			}
			Chatter.this.sendChatroom(message, outMsg, Chatter.this.chatID.id,
					MessageKind.chatChat);
			return true;
		}

		@Override
		protected boolean newChat(final Message message) throws JMSException {
			final TextMessage outMsg = new ActiveMQTextMessage();
			if (message instanceof TextMessage) {
				outMsg.setText(((TextMessage) message).getText());
			}
			Chatter.this.sendClient(message, outMsg, MessageKind.clientNewChat);
			return true;
		}

		@Override
		protected boolean participantEntered(final Message message)
				throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendClient(message, outMsg,
					MessageKind.clientParticipantEntered);
			return true;
		}

		@Override
		protected boolean participantLeft(final Message message)
				throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendClient(message, outMsg,
					MessageKind.clientParticipantLeft);
			return true;
		}
	}

	private static Map<String, Chatroom> activeChatrooms;
	private static Map<String, Chatter> activeChatters;
	private static Map<String, String> chatterNicknames;
	private static final long serialVersionUID = 1788756022188693509L;
	/**
	 * for debugging, trace messages can be swiched in
	 */
	private static final boolean trace = true;

	/**
	 * implementation of ..1 association end across jms by a uid
	 */
	private final JmsReference chatID = new JmsReference();

	private Destination chatroomDestination;

	private final ChatterState create = new ChatterState("create@"
			+ this.getUsername()) {

		@Override
		protected boolean authenticated(final Message message)
				throws javax.jms.JMSException {
			Chatter.this.setClientDestination(message.getJMSReplyTo());
			final String token = message
					.getStringProperty(MessageHeader.AuthToken.toString());
			final TextMessage tm = new ActiveMQTextMessage();
			if (message instanceof TextMessage) {
				tm.setText(((TextMessage) message).getText());
			}
			tm.setStringProperty(MessageHeader.AuthToken.toString(), token);
			Chatter.this.sendClient(message, tm, MessageKind.authenticated);
			Chatter.this.changeState(Chatter.this.idle, message);
			return true;
		};

	};

	/**
	 * Implementierung des Objects for States Patterns für die State Chart
	 */
	private final ChatterState idle = new ChatterState("idle@"
			+ this.getUsername()) {
		@Override
		protected boolean chatCreated(final Message message)
				throws javax.jms.JMSException {
			Chatter.this.chatID.id = message
					.getStringProperty(MessageHeader.ChatroomID.toString());
			Chatter.this.chatID.destination = message.getJMSReplyTo();
			Chatter.this.sendClient(message, new ActiveMQMessage(),
					MessageKind.clientChatStarted);
			Chatter.this.changeState(Chatter.this.owningChat, message);
			return true;
		}

		@Override
		protected boolean closed(final Message message)
				throws javax.jms.JMSException {
			Chatter.this.sendClient(message, new ActiveMQMessage(),
					MessageKind.clientChatClosed);
			return true;
		}

		@Override
		protected boolean invited(final Message message)
				throws javax.jms.JMSException {
			Chatter.this.chatID.id = message
					.getStringProperty(MessageHeader.ChatroomID.toString());
			final Message reply = new ActiveMQMessage();
			Chatter.this.sendClient(message, reply,
					MessageKind.clientInvitation);
			Chatter.this
					.changeState(Chatter.this.invitedToParticipate, message);
			return true;
		}

		@Override
		protected boolean msgDeny(final Message message) throws JMSException {
			return true;
		}

		@Override
		protected boolean msgQueryChats(final Message message)
				throws JMSException {
			final TextMessage outMsg = new ActiveMQTextMessage();
			final StringBuilder body = new StringBuilder();
			for (final String key : Chatter.activeChatrooms.keySet()) {
				final Chatroom chatroom = Chatter.activeChatrooms.get(key);
				if (chatroom != null) {
					body.append(key).append(": ")
							.append(chatroom.getInitiator()).append('\n');
				}
			}
			if (body.length() > 0) {
				outMsg.setText(body.toString());
			} else {
				outMsg.setText("no active chatroom found");
			}
			Chatter.this.sendClient(message, outMsg,
					MessageKind.clientAnswerChat);
			return true;
		}

		@Override
		protected boolean msgQueryChatters(final Message message)
				throws JMSException {
			final TextMessage outMsg = new ActiveMQTextMessage();

			final StringBuilder body = new StringBuilder();
			for (final String cKey : Chatter.activeChatters.keySet()) {
				Chatter ch;
				if (((ch = Chatter.activeChatters.get(cKey)) != null)
						&& ch.isIdle()) {
					body.append(ch.getUsername()).append('\n');
				}
			}
			if (body.length() > 0) {
				outMsg.setText(body.toString());
			} else {
				outMsg.setText("no idle chatter found");
			}
			Chatter.this.sendClient(message, outMsg,
					MessageKind.clientAnswerChatters);
			return true;
		}

		@Override
		protected boolean msgRequestParticipation(final Message message)
				throws javax.jms.JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.chatID.id = message
					.getStringProperty(MessageHeader.RefID.toString());
			Chatter.this.sendChatroom(message, outMsg, Chatter.this.chatID.id,
					MessageKind.chatParticipationRequest);
			Chatter.this.changeState(Chatter.this.requestingParticipation,
					message);
			return true;
		};

		@Override
		protected boolean msgStartChat(final Message message)
				throws javax.jms.JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendChatroom(message, outMsg, "",
					MessageKind.chatCreate);
			return true;
		};

		@Override
		protected boolean participantLeft(final Message message)
				throws JMSException {
			return true;
		};
	};

	private final ChatterState invitedToParticipate = new ChatterState(
			"invitedToParticipate@" + this.getUsername()) {

		@Override
		protected boolean closed(final Message message) throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendClient(message, outMsg,
					MessageKind.clientChatClosed);
			Chatter.this.changeState(Chatter.this.idle, message);
			return true;
		}

		@Override
		protected boolean msgAcceptInvitation(final Message message)
				throws javax.jms.JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendPeer(message, outMsg,
					MessageKind.chatterMsgAcceptInvitation);
			Chatter.this.changeState(Chatter.this.participatingInChat, message);
			return true;
		}

		@Override
		protected boolean msgDeny(final Message message)
				throws javax.jms.JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendPeer(message, outMsg, MessageKind.chatterMsgDeny);
			Chatter.this.changeState(Chatter.this.idle, message);
			return true;
		}

	};

	private MessageProducer messageProducer;

	private final ChatterState owningChat = new Chatting("owningChat@"
			+ this.getUsername()) {

		@Override
		protected boolean denied(final Message message) throws JMSException {
			return true;
		}

		@Override
		protected boolean msgAccept(final Message message) throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendPeer(message, outMsg, MessageKind.chatterAccepted);
			return true;
		}

		@Override
		protected boolean msgAcceptInvitation(final Message message)
				throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this
					.sendClient(message, outMsg, MessageKind.clientAccepted);
			return true;
		}

		@Override
		protected boolean msgClose(final Message message) throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendChatroom(message, outMsg, Chatter.this.chatID.id,
					MessageKind.chatClose);
			Chatter.this.changeState(Chatter.this.idle, message);
			return true;
		}

		@Override
		protected boolean msgDeny(final Message message) throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendClient(message, outMsg, MessageKind.clientDenied);
			return true;
		};

		@Override
		protected boolean msgInvite(final Message message) throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendPeer(message, outMsg, MessageKind.chatterInvited);
			return true;
		};

		@Override
		protected boolean msgQueryChats(final Message message)
				throws JMSException {
			return true;
		}

		@Override
		protected boolean msgQueryChatters(final Message message)
				throws JMSException {
			final TextMessage outMsg = new ActiveMQTextMessage();
			final StringBuilder body = new StringBuilder();
			for (final String cKey : Chatter.activeChatters.keySet()) {
				Chatter ch;
				if (((ch = Chatter.activeChatters.get(cKey)) != null)
						&& ch.isIdle()) {
					body.append(ch.getUsername()).append('\n');
				}
			}
			if (body.length() > 0) {
				outMsg.setText(body.toString());
			} else {
				outMsg.setText("no idle chatter found");
			}
			Chatter.this.sendClient(message, outMsg,
					MessageKind.clientAnswerChatters);
			return true;
		}

		@Override
		protected boolean msgReject(final Message message) throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendPeer(message, outMsg, MessageKind.chatterReject);
			return true;
		};

		@Override
		protected boolean participationRequest(final Message message)
				throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendClient(message, outMsg, MessageKind.clientRequest);
			return true;
		};

		@Override
		protected boolean requestCancelled(final Message message)
				throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendClient(message, outMsg,
					MessageKind.clientRequestCancelled);
			return true;
		}
	};

	private final ChatterState participatingInChat = new Chatting(
			"participatingInChat@" + this.getUsername()) {

		@Override
		protected boolean closed(final Message message) throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendClient(message, outMsg,
					MessageKind.clientChatClosed);
			Chatter.this.changeState(Chatter.this.idle, message);
			return true;
		}

		@Override
		protected boolean msgLeave(final Message message) throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendChatroom(message, outMsg, Chatter.this.chatID.id,
					MessageKind.chatLeave);
			Chatter.this.changeState(Chatter.this.idle, message);
			return true;
		}

		@Override
		protected void onEntry(final Message message) throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendChatroom(message, outMsg, Chatter.this.chatID.id,
					MessageKind.chatNewParticipant);
		}

	};

	private final ChatterState requestingParticipation = new ChatterState(
			"requestingParticipation@" + this.getUsername()) {

		@Override
		protected boolean accepted(final Message message)
				throws javax.jms.JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendClient(message, outMsg,
					MessageKind.clientParticipating);
			Chatter.this.changeState(Chatter.this.participatingInChat, message);
			return true;
		}

		@Override
		protected boolean closed(final Message message) throws JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendClient(message, outMsg,
					MessageKind.clientChatClosed);
			Chatter.this.changeState(Chatter.this.idle, message);
			return true;
		}

		@Override
		protected boolean invited(final Message message) throws JMSException {
			return true;
		}

		@Override
		protected boolean msgCancel(final Message message)
				throws javax.jms.JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this.sendChatroom(message, outMsg, Chatter.this.chatID.id,
					MessageKind.chatCancelRequest);
			Chatter.this.changeState(Chatter.this.idle, message);
			return true;
		}

		@Override
		protected boolean rejected(final Message message)
				throws javax.jms.JMSException {
			final Message outMsg = new ActiveMQMessage();
			Chatter.this
					.sendClient(message, outMsg, MessageKind.clientRejected);
			Chatter.this.changeState(Chatter.this.idle, message);
			return true;
		}
	};

	/**
	 * The current state of a Chatter object is represented by an appropriate
	 * instance of a subclass of ChatterState. All activities that are initiated
	 * by an incoming method will be performed by this state object. This
	 * implements the Objects-for-States pattern in combination with
	 * Methods-for-Transitions
	 */
	private ChatterState state;

	private TraceGenerator traceGenerator;

	/**
	 * initialize new instance
	 * 
	 * @param uname
	 *            user name
	 * @param pword
	 *            password
	 */
	public Chatter(final String uname, final String pword) {
		super(uname, pword);
		this.state = this.create;
	}

	/**
	 * @param rooms
	 */
	public static void setActiveChatrooms(final Map<String, Chatroom> rooms) {
		Chatter.activeChatrooms = rooms;
	}

	/**
	 * @param activeChatters
	 */
	public static void setActiveChatters(
			final Map<String, Chatter> activeChatters) {
		Chatter.activeChatters = activeChatters;
	}

	/**
	 * @param chatterNicknames
	 */
	public static void setChatterNicknames(
			final Map<String, String> chatterNicknames) {
		Chatter.chatterNicknames = chatterNicknames;
	}

	/**
	 * get reference of chat
	 * 
	 * @return id string of chat
	 */
	public String getChatId() {
		if (this.chatID != null) {
			return this.chatID.id;
		} else {
			return "";
		}
	}

	/**
	 * is this chatter idle?
	 * 
	 * @return
	 */
	public boolean isIdle() {
		return this.state == this.idle;
	}

	/**
	 * is this chatter owning a chat?
	 * 
	 * @return
	 */
	public boolean isOwningChat() {
		return this.state == this.owningChat;
	}

	/**
	 * handle all incoming jms messages by delegating message to appropriate
	 * method of current instance of state
	 * 
	 * @param message
	 *            a jms message
	 */
	@Override
	public boolean processMessage(final Message message) {
		boolean result;
		final String fromState = this.state.getName();
		result = this.state.processMessage(message);
		if (Chatter.trace && (this.traceGenerator != null)) {
			try {
				this.traceGenerator.setObjId(this.getUsername()
						+ "@"
						+ message.getStringProperty(MessageHeader.AuthToken
								.toString()));
			} catch (final JMSException e) {
			}
			this.traceGenerator.trace(fromState, this.state.getName(), message);
		}
		return result;
	}

	/**
	 * @param chatrooms
	 */
	public void setChatroomDestination(final Destination chatrooms) {
		this.chatroomDestination = chatrooms;
	}

	/**
	 * @param replyProducer
	 */
	public void setProducer(final MessageProducer replyProducer) {
		this.messageProducer = replyProducer;
	}

	/**
	 * @param generator
	 */
	public void setTraceGenerator(final TraceGenerator generator) {
		this.traceGenerator = generator;
		this.traceGenerator.setObjId(this.getUsername());
	}

	/**
	 * should be used by external transitions to change the state
	 * 
	 * @param s
	 *            new state
	 * @param messagemessage
	 *            that triggered transition
	 */
	private void changeState(final ChatterState s, final Message message) {
		this.state = s;
		try {
			this.state.onEntry(message);
		} catch (final JMSException e) {
		}
	}

	/**
	 * send message to chatroom
	 * 
	 * @param inMsg
	 *            message from Chatter
	 * @param outMsg
	 *            new empty message to be completed and sent
	 * @param chatId
	 *            identifies the addressed chatroom, empty string if not yet
	 *            created
	 * @param kind
	 *            event kind
	 * @throws JMSException
	 */
	private void sendChatroom(final Message inMsg, final Message outMsg,
			final String chatId, final MessageKind kind) throws JMSException {
		outMsg.setJMSDestination(this.chatroomDestination);
		outMsg.setJMSReplyTo(this.getReplyDestination());
		outMsg.setStringProperty(MessageHeader.ChatterNickname.toString(),
				this.getUsername());
		outMsg.setStringProperty(MessageHeader.AuthToken.toString(),
				inMsg.getStringProperty(MessageHeader.AuthToken.toString()));
		outMsg.setStringProperty(MessageHeader.MsgKind.toString(),
				kind.toString());
		outMsg.setStringProperty(MessageHeader.ChatroomID.toString(), chatId);
		this.messageProducer.send(this.chatroomDestination, outMsg);
	}

	/**
	 * send message to extern client
	 * 
	 * @param inMsg
	 *            last incoming message from chatroom holds important reference
	 *            information
	 * @param outMsg
	 *            new outgoing message
	 * @param kind
	 *            event kind
	 * @throws JMSException
	 */
	private void sendClient(final Message inMsg, final Message outMsg,
			final MessageKind kind) throws JMSException {
		String id = inMsg.getStringProperty(MessageHeader.RefID.toString());
		if ((id != null) && Chatter.activeChatters.keySet().contains(id)) {
			outMsg.setStringProperty(MessageHeader.RefID.toString(),
					Chatter.activeChatters.get(id).getUsername());
		} else {
			outMsg.setStringProperty(MessageHeader.RefID.toString(), id);
		}
		id = inMsg.getStringProperty(MessageHeader.ChatroomID.toString());
		if (id != null) {
			outMsg.setStringProperty(MessageHeader.ChatroomID.toString(), id);
		}
		outMsg.setJMSReplyTo(this.getReplyDestination());
		outMsg.setJMSDestination(this.getClientDestination());
		// outMsg.setStringProperty(MessageHeader.AuthToken.toString(), inMsg
		// .getStringProperty(MessageHeader.AuthToken.toString()));
		outMsg.setStringProperty(MessageHeader.MsgKind.toString(),
				kind.toString());
		this.messageProducer.send(this.getClientDestination(), outMsg);
	}

	/**
	 * send message to peer chatter
	 * 
	 * @param inMsg
	 *            message from Client
	 * @param outMsg
	 *            new empty message to be completed and sent
	 * @throws JMSException
	 */
	private void sendPeer(final Message inMsg, final Message outMsg,
			final MessageKind kind) throws JMSException {
		// String requestorId = inMsg.getStringProperty(MessageHeader.RefID
		// .toString());
		final String requestorId = inMsg.getStringProperty(MessageHeader.RefID
				.toString());
		String token;
		if (requestorId != null) {
			token = Chatter.chatterNicknames.get(requestorId);
			if (token != null) {
				final String chatId = inMsg
						.getStringProperty(MessageHeader.ChatroomID.toString());
				if (chatId != null) {
					outMsg.setStringProperty(
							MessageHeader.ChatroomID.toString(), chatId);
				}
				outMsg.setJMSDestination(inMsg.getJMSReplyTo());
				outMsg.setJMSReplyTo(this.getReplyDestination());
				outMsg.setStringProperty(MessageHeader.AuthToken.toString(),
						token);
				outMsg.setStringProperty(MessageHeader.MsgKind.toString(),
						kind.toString());
				outMsg.setStringProperty(
						MessageHeader.ChatterNickname.toString(),
						this.getUsername());
				// outMsg.setStringProperty(MessageHeader.RefID.toString(),
				// inMsg.getStringProperty(MessageHeader.AuthToken.toString()));
				outMsg.setStringProperty(MessageHeader.RefID.toString(),
						this.getUsername());
				this.messageProducer.send(inMsg.getJMSDestination(), outMsg);
			}
		}
	}
}