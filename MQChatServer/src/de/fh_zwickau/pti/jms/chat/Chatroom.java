package de.fh_zwickau.pti.jms.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import de.fh_zwickau.pti.jms.tracing.TraceGenerator;
import de.fh_zwickau.pti.mqgamecommon.MessageHeader;
import de.fh_zwickau.pti.mqgamecommon.MessageKind;

/**
 * objects of this class manage chats between two or more participants
 * 
 * @author georg beier
 * 
 */
public class Chatroom {
	/**
	 * for debugging, trace messages can be swiched on
	 */
	private static final boolean trace = true;
	/**
	 * handle messages in state active
	 */
	private final ChatroomState active = new ChatroomState("active") {
		@Override
		protected boolean cancelRequest(final Message message)
				throws JMSException {
			final String identity = message
					.getStringProperty(MessageHeader.AuthToken.toString());
			final Destination destination = message.getJMSReplyTo();
			final String nickname = message
					.getStringProperty(MessageHeader.ChatterNickname.toString());
			final Message outMessage = new ActiveMQMessage();
			outMessage.setStringProperty(MessageHeader.MsgKind.toString(),
					MessageKind.chatterRequestCanceled.toString());
			outMessage.setStringProperty(
					MessageHeader.ChatterNickname.toString(), nickname);
			outMessage.setStringProperty(MessageHeader.RefID.toString(),
					identity);
			outMessage.setStringProperty(MessageHeader.AuthToken.toString(),
					Chatroom.this.initiator.id);
			outMessage.setJMSReplyTo(destination);
			outMessage.setJMSDestination(Chatroom.this.initiator.destination);
			Chatroom.this.messageProducer.send(
					Chatroom.this.initiator.destination, outMessage);
			return true;
		}

		@Override
		protected boolean chat(final Message message) throws JMSException {
			final String id = message.getStringProperty(MessageHeader.AuthToken
					.toString());
			final String nickname = Chatroom.this.participants.get(id).nickname;
			final String chat = ((TextMessage) message).getText();
			final TextMessage outMessage = new ActiveMQTextMessage();
			outMessage.setText(chat);
			outMessage.setStringProperty(MessageHeader.MsgKind.toString(),
					MessageKind.chatterNewChat.toString());
			outMessage.setStringProperty(
					MessageHeader.ChatterNickname.toString(), nickname);
			for (final ChatterReference participant : Chatroom.this.participants
					.values()) {
				outMessage.setJMSDestination(participant.destination);
				outMessage.setStringProperty(
						MessageHeader.AuthToken.toString(), participant.id);
				Chatroom.this.messageProducer.send(participant.destination,
						outMessage);
			}
			return true;
		}

		@Override
		protected boolean close(final Message message) throws JMSException {
			final Message outMessage = new ActiveMQMessage();
			outMessage.setStringProperty(MessageHeader.MsgKind.toString(),
					MessageKind.chatterClosed.toString());
			for (final ChatterReference participant : Chatroom.this.participants
					.values()) {
				outMessage.setJMSDestination(participant.destination);
				outMessage.setStringProperty(
						MessageHeader.AuthToken.toString(), participant.id);
				Chatroom.this.messageProducer.send(participant.destination,
						outMessage);
			}
			Chatroom.this.participants.clear();
			Chatroom.this.initiator = null;
			Chatroom.this.state = Chatroom.this.closing;
			return false;
		}

		@Override
		protected boolean leave(final Message message) throws JMSException {
			final String id = message.getStringProperty(MessageHeader.AuthToken
					.toString());
			final String nickname = Chatroom.this.participants.get(id).nickname;
			final TextMessage outMessage = new ActiveMQTextMessage();
			outMessage.setText(nickname + " left chatroom");
			outMessage.setStringProperty(MessageHeader.MsgKind.toString(),
					MessageKind.chatterParticipantLeft.toString());
			outMessage.setStringProperty(
					MessageHeader.ChatterNickname.toString(), nickname);
			for (final ChatterReference participant : Chatroom.this.participants
					.values()) {
				outMessage.setJMSDestination(participant.destination);
				outMessage.setStringProperty(
						MessageHeader.AuthToken.toString(), participant.id);
				Chatroom.this.messageProducer.send(participant.destination,
						outMessage);
			}
			Chatroom.this.participants.remove(id);
			return true;
		}

		@Override
		protected boolean newParticipant(final Message message)
				throws JMSException {
			final ChatterReference newParticipant = new ChatterReference();
			newParticipant.id = message
					.getStringProperty(MessageHeader.AuthToken.toString());
			newParticipant.destination = message.getJMSReplyTo();
			newParticipant.nickname = message
					.getStringProperty(MessageHeader.ChatterNickname.toString());
			Chatroom.this.participants.put(newParticipant.id, newParticipant);
			final TextMessage outMessage = new ActiveMQTextMessage();
			outMessage.setText(newParticipant.nickname + " entered chatroom");
			outMessage.setStringProperty(MessageHeader.MsgKind.toString(),
					MessageKind.chatterParticipantEntered.toString());
			for (final ChatterReference participant : Chatroom.this.participants
					.values()) {
				outMessage.setJMSDestination(participant.destination);
				outMessage.setStringProperty(
						MessageHeader.AuthToken.toString(), participant.id);
				outMessage.setStringProperty(
						MessageHeader.ChatterNickname.toString(),
						newParticipant.nickname);
				Chatroom.this.messageProducer.send(participant.destination,
						outMessage);
			}
			return true;
		}

		@Override
		protected boolean participationRequest(final Message message)
				throws JMSException {
			final String identity = message
					.getStringProperty(MessageHeader.AuthToken.toString());
			final Destination destination = message.getJMSReplyTo();
			final String nickname = message
					.getStringProperty(MessageHeader.ChatterNickname.toString());
			final Message outMessage = new ActiveMQMessage();
			outMessage.setStringProperty(MessageHeader.MsgKind.toString(),
					MessageKind.chatterParticipationRequest.toString());
			outMessage.setStringProperty(
					MessageHeader.ChatterNickname.toString(), nickname);
			outMessage.setStringProperty(MessageHeader.RefID.toString(),
					identity);
			outMessage.setStringProperty(MessageHeader.AuthToken.toString(),
					Chatroom.this.initiator.id);
			outMessage.setJMSReplyTo(destination);
			outMessage.setJMSDestination(Chatroom.this.initiator.destination);
			Chatroom.this.messageProducer.send(
					Chatroom.this.initiator.destination, outMessage);
			return true;
		}
	};

	/**
	 * handle messages in state closing
	 */
	private final ChatroomState closing = new ChatroomState("closing") {
		@Override
		protected boolean participationRequest(final Message message)
				throws JMSException {
			final TextMessage outMessage = new ActiveMQTextMessage();
			final Destination destination = message.getJMSReplyTo();
			final String identity = message
					.getStringProperty(MessageHeader.AuthToken.toString());
			outMessage.setStringProperty(MessageHeader.MsgKind.toString(),
					MessageKind.chatterReject.toString());
			outMessage.setStringProperty(MessageHeader.AuthToken.toString(),
					identity);
			outMessage.setJMSDestination(destination);
			outMessage.setText("Chatroom is closed");
			Chatroom.this.messageProducer.send(destination, outMessage);
			return false;
		}
	};

	/**
	 * handle constructing messages
	 */
	private final ChatroomState init = new ChatroomState("init") {
		@Override
		protected boolean create(final Message message) throws JMSException {
			Chatroom.this.initiator.destination = message.getJMSReplyTo();
			Chatroom.this.initiator.id = message
					.getStringProperty(MessageHeader.AuthToken.toString());
			Chatroom.this.initiator.nickname = message
					.getStringProperty(MessageHeader.ChatterNickname.toString());
			Chatroom.this.participants.put(Chatroom.this.initiator.id,
					Chatroom.this.initiator);
			final Message outMessage = new ActiveMQMessage();
			outMessage.setStringProperty(MessageHeader.MsgKind.toString(),
					MessageKind.chatterChatCreated.toString());
			outMessage.setStringProperty(MessageHeader.AuthToken.toString(),
					Chatroom.this.initiator.id);
			outMessage.setStringProperty(MessageHeader.ChatroomID.toString(),
					Chatroom.this.roomId);
			outMessage.setJMSDestination(Chatroom.this.initiator.destination);
			Chatroom.this.messageProducer.send(
					Chatroom.this.initiator.destination, outMessage);
			Chatroom.this.state = Chatroom.this.active; // switch to next state
			return true;
		}
	};
	/**
	 * implementation of ..1 association end across jms
	 */
	private ChatterReference initiator = new ChatterReference();

	/**
	 * producer used to send jms messages
	 */
	private final MessageProducer messageProducer;

	/**
	 * implementation of ..* association end across jms
	 */
	private final HashMap<String, ChatterReference> participants = new HashMap<>();

	private final String roomId;

	/**
	 * The current state of a Chatroom object is represented by an appropriate
	 * instance of a subclass of ChatroomState. All activities that are
	 * initiated by an incoming message will be performed by this state object.
	 * This implements the Objects-for-States pattern in combination with
	 * Methods-for-Transitions
	 */
	private ChatroomState state;

	private TraceGenerator traceGenerator;

	/**
	 * set to init state on construction
	 * 
	 * @param producer
	 *            a message producer that is used to send all resulting messages
	 *            to jms
	 * @param uuid
	 *            unique key for this chat room
	 */
	public Chatroom(final MessageProducer producer, final String uuid) {
		this.messageProducer = producer;
		this.roomId = uuid;
		this.state = this.init;
	}

	/**
	 * get a list of nicknames of all participants of this chat with the
	 * initiator a first element
	 * 
	 * @return that list
	 */
	public List<String> getChatters() {
		final List<String> res = new ArrayList<>();
		res.add(this.initiator.nickname);
		for (final ChatterReference chp : this.participants.values()) {
			if (!chp.nickname.equals(this.initiator.nickname)) {
				res.add(chp.nickname);
			}
		}
		return res;
	}

	/**
	 * get nickname of chat initiator
	 */
	public String getInitiator() {
		return this.initiator.nickname;
	}

	/**
	 * handle all incoming jms messages by delegating message to appropriate
	 * method of current instance of state
	 * 
	 * @param message
	 *            a jms message
	 */
	public boolean processMessage(final Message message) {
		boolean result;
		final String fromState = this.state.getName();
		result = this.state.processMessage(message);
		if (Chatroom.trace && (this.traceGenerator != null)) {
			this.traceGenerator.trace(fromState, this.state.getName(), message);
		}
		return result;
	}

	public void setTraceGenerator(final TraceGenerator traceGen) {
		this.traceGenerator = traceGen;
		this.traceGenerator.setObjId(this.roomId);
	}
}