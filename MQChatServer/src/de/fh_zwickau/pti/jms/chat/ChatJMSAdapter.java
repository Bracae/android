package de.fh_zwickau.pti.jms.chat;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.swing.SwingUtilities;

import org.apache.activemq.ActiveMQConnectionFactory;

import de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer;
import de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver;
import de.fh_zwickau.pti.mqgamecommon.MQConstantDefs;
import de.fh_zwickau.pti.mqgamecommon.MessageHeader;
import de.fh_zwickau.pti.mqgamecommon.MessageKind;

/**
 * send and receive chat messages
 * 
 * @author deep throat
 * 
 */
public class ChatJMSAdapter implements ChatServerMessageProducer {
	private String currentChatroomId, username, authToken;
	/**
	 * ein ExceptionListener
	 * 
	 */
	private final ExceptionListener excListener = new ExceptionListener() {
		@Override
		public void onException(final JMSException arg0) {
			arg0.printStackTrace();
		}
	};
	private Destination loginQ, reply, chatServiceQ;
	private ChatServerMessageReceiver messageReceiver;
	/**
	 * ein MessageListener
	 * 
	 */
	private final MessageListener msgListener = new MessageListener() {
		@Override
		public void onMessage(final Message replyMessage) {
			try {
				if (replyMessage instanceof TextMessage) {
					final TextMessage textMessage = (TextMessage) replyMessage;
					final String msgKind = textMessage
							.getStringProperty(MessageHeader.MsgKind.toString());
					final MessageKind messageKind = MessageKind
							.valueOf(msgKind);
					final String text = textMessage.getText();
					switch (messageKind) {
					case authenticated:
						ChatJMSAdapter.this.authToken = textMessage
								.getStringProperty(MessageHeader.AuthToken
										.toString());
						ChatJMSAdapter.this.chatServiceQ = textMessage
								.getJMSReplyTo();
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotSuccess();
								}
							});
						}
						break;
					case failed:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotFail(text);
								}
							});
						}
						break;
					case loggedOut:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotLogout();
								}
							});
						}

						break;
					case clientNewChat:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotNewChat(text);
								}
							});
						}
						break;
					case clientAnswerChat:
						ChatJMSAdapter.this.messageReceiver
								.gotAnswerChats(text);
						break;
					case clientAnswerChatters:
						ChatJMSAdapter.this.messageReceiver
								.gotAnswerChatters(text);
						break;
					default:
						break;
					}
				}
			} catch (final JMSException e) {
				e.printStackTrace();
			}
			try {
				if ((replyMessage instanceof Message)
						&& !(replyMessage instanceof TextMessage)) {
					final String msgKind = replyMessage
							.getStringProperty(MessageHeader.MsgKind.toString());
					final MessageKind messageKind = MessageKind
							.valueOf(msgKind);

					final String incomingRefId = replyMessage
							.getStringProperty(MessageHeader.RefID.toString());

					switch (messageKind) {
					case clientChatStarted:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							ChatJMSAdapter.this.currentChatroomId = replyMessage
									.getStringProperty(MessageHeader.ChatroomID
											.toString());

							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotChatStarted();
								}
							});
						}
						break;
					case clientInvitation:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotInvite(incomingRefId);
								}
							});
						}
						break;
					case clientParticipantEntered:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotParticipantEntered("Ein neuer Teilnehmer ist dem Chat beigetreten.");
								}
							});
						}
						break;
					case clientParticipantLeft:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotParticipantLeft("Ein Teilnehmer hat den Chat verlassen");
								}
							});
						}
						break;
					case clientAccepted:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotAccepted();
								}
							});
						}
						break;
					case clientDenied:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotDenied();
								}
							});
						}
						break;
					case clientRequest:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotRequest(incomingRefId);
								}
							});
						}
						break;
					case clientParticipating:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotParticipating();
								}
							});
						}
						break;
					case clientChatClosed:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotChatClosed();
								}
							});
						}
						break;
					case clientRejected:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotRejected();
								}
							});
						}
						break;
					case clientRequestCancelled:
						if (ChatJMSAdapter.this.messageReceiver != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ChatJMSAdapter.this.messageReceiver
											.gotRequestCancelled(incomingRefId);
								}
							});
						}
						break;
					default:
						break;
					}
				}
			} catch (final JMSException e) {
				e.printStackTrace();
			}
		}
	};

	private MessageProducer requestProducer;

	private Session session;

	/**
	 * Connect to server
	 * 
	 * @param brokerUri
	 *            die Broker URI
	 */
	public void connectToServer(final String brokerUri) {
		try {
			this.authToken = "";

			// Factory für Verbindungen zu einem JMS Server
			final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"sys", "man", brokerUri);
			final Connection connection = connectionFactory.createConnection();
			connection.setExceptionListener(this.excListener);
			connection.start();
			this.session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			this.loginQ = this.session.createQueue(MQConstantDefs.LOGINQ);
			this.chatServiceQ = this.session.createQueue(MQConstantDefs.USERQ);
			this.reply = this.session.createTemporaryQueue();
			this.requestProducer = this.session.createProducer(null);
			this.requestProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			final MessageConsumer consumer = this.session
					.createConsumer(this.reply);
			consumer.setMessageListener(this.msgListener);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onAccept(java
	 * .lang.String)
	 */
	@Override
	public void onAccept(final String refId) throws Exception {
		final String msgKind = MessageKind.chatterMsgAccept.toString();
		final TextMessage txtMessage = this.createMessage(this.chatServiceQ);
		txtMessage.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		txtMessage.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		txtMessage.setStringProperty(MessageHeader.RefID.toString(), refId);
		this.requestProducer.send(this.chatServiceQ, txtMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#
	 * onAcceptionInvitation()
	 */
	@Override
	public void onAcceptionInvitation() throws Exception {
		final String msgKind = MessageKind.chatterMsgAcceptInvitation
				.toString();
		final Message message = this.createMessage(this.chatServiceQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		message.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		this.requestProducer.send(this.chatServiceQ, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onCancel()
	 */
	@Override
	public void onCancel() throws Exception {
		final String msgKind = MessageKind.chatterMsgCancel.toString();
		final Message message = this.createMessage(this.chatServiceQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		message.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		this.requestProducer.send(this.chatServiceQ, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onChat(java.
	 * lang.String)
	 */
	@Override
	public void onChat(final String text) throws Exception {
		// Message bauen und versenden
		final String msgKind = MessageKind.chatterMsgChat.toString();
		final TextMessage txtMessage = this.createMessage(this.chatServiceQ);
		txtMessage.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		txtMessage.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		txtMessage.setText(this.username + ": " + text);
		this.requestProducer.send(this.chatServiceQ, txtMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onChats()
	 */
	@Override
	public void onChats() throws Exception {
		final String msgKind = MessageKind.chatterMsgChats.toString();
		final Message message = this.createMessage(this.chatServiceQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		message.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		this.requestProducer.send(this.chatServiceQ, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onChatters()
	 */
	@Override
	public void onChatters() throws Exception {
		final String msgKind = MessageKind.chatterMsgChatters.toString();
		final Message message = this.createMessage(this.chatServiceQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		message.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		this.requestProducer.send(this.chatServiceQ, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onClose()
	 */
	@Override
	public void onClose() throws Exception {
		final String msgKind = MessageKind.chatterMsgClose.toString();
		final Message message = this.createMessage(this.chatServiceQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		message.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		this.requestProducer.send(this.chatServiceQ, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onDeny()
	 */
	@Override
	public void onDeny() throws Exception {
		final String msgKind = MessageKind.chatterMsgDeny.toString();
		final Message message = this.createMessage(this.chatServiceQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		message.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		this.requestProducer.send(this.chatServiceQ, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onInvite(java
	 * .lang.String)
	 */
	@Override
	public void onInvite(final String refId) throws Exception {
		final String msgKind = MessageKind.chatterMsgInvite.toString();
		final TextMessage txtMessage = this.createMessage(this.chatServiceQ);
		txtMessage.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		txtMessage.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		txtMessage.setStringProperty(MessageHeader.RefID.toString(), refId);
		txtMessage.setStringProperty(MessageHeader.ChatroomID.toString(),
				this.currentChatroomId);
		this.requestProducer.send(this.chatServiceQ, txtMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onLeave()
	 */
	@Override
	public void onLeave() throws Exception {
		final String msgKind = MessageKind.chatterMsgLeave.toString();
		final Message message = this.createMessage(this.chatServiceQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		message.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		this.requestProducer.send(this.chatServiceQ, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onLogin(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public void onLogin(final String uname, final String pword)
			throws Exception {
		this.loginOrRegister(false, uname, pword);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onLogout()
	 */
	@Override
	public void onLogout() throws Exception {
		final TextMessage message = this.createMessage(this.chatServiceQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(),
				MessageKind.logout.toString());
		message.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		this.requestProducer.send(this.loginQ, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onRegister(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public void onRegister(final String uname, final String pword)
			throws Exception {
		this.loginOrRegister(true, uname, pword);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onReject(java
	 * .lang.String)
	 */
	@Override
	public void onReject(final String refId) throws Exception {
		final String msgKind = MessageKind.chatterMsgReject.toString();
		final TextMessage txtMessage = this.createMessage(this.chatServiceQ);
		txtMessage.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		txtMessage.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		txtMessage.setStringProperty(MessageHeader.RefID.toString(), refId);
		this.requestProducer.send(this.chatServiceQ, txtMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#
	 * onRequestParticipation(java.lang.String)
	 */
	@Override
	public void onRequestParticipation(final String chatroomId)
			throws Exception {
		final String msgKind = MessageKind.chatterMsgRequestParticipation
				.toString();
		final Message message = this.createMessage(this.chatServiceQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		message.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		message.setStringProperty(MessageHeader.RefID.toString(),
				chatroomId.substring(0, chatroomId.indexOf(":")));
		this.requestProducer.send(this.chatServiceQ, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer#onStartChat(
	 * java.lang.String)
	 */
	@Override
	public void onStartChat(final String chatroomName) throws Exception {
		final String msgKind = MessageKind.chatterMsgStartChat.toString();
		final Message message = this.createMessage(this.chatServiceQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		message.setStringProperty(MessageHeader.AuthToken.toString(),
				this.authToken);
		this.requestProducer.send(this.chatServiceQ, message);
	}

	@Override
	public void setMessageReceiver(
			final ChatServerMessageReceiver messageReceiver) {
		this.messageReceiver = messageReceiver;
	}

	private TextMessage createMessage(final Destination destination)
			throws JMSException {
		final TextMessage textMessage = this.session.createTextMessage();
		textMessage.setJMSReplyTo(this.reply);
		textMessage.setJMSDestination(destination);
		return textMessage;
	}

	private void loginOrRegister(final boolean register, final String uname,
			final String pword) throws JMSException {
		this.username = uname;
		String msgKind;
		if (register) {
			msgKind = MessageKind.register.toString();
		} else {
			msgKind = MessageKind.login.toString();
		}
		final Message message = this.createMessage(this.loginQ);
		message.setStringProperty(MessageHeader.MsgKind.toString(), msgKind);
		message.setStringProperty(MessageHeader.LoginUser.toString(), uname);
		message.setStringProperty(MessageHeader.ChatterNickname.toString(),
				uname);
		message.setStringProperty(MessageHeader.LoginPassword.toString(), pword);
		this.requestProducer.send(this.loginQ, message);
	}
}