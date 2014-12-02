package de.fh_zwickau.pti.jms.chat;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.fh_zwickau.pti.jms.tracing.JmsTraceSender;
import de.fh_zwickau.pti.jms.tracing.TraceGenerator;
import de.fh_zwickau.pti.jms.userservice.AuthenticationServer;
import de.fh_zwickau.pti.jms.userservice.UserDto;
import de.fh_zwickau.pti.mqgamecommon.MessageHeader;
import de.fh_zwickau.pti.mqgamecommon.MessageKind;

/**
 * JMS Service Provider und Demo-Programm für unterschiedliche ActiveMQ Features
 * 
 * @author georg beier
 * 
 */
public class ChatServer {
	/**
	 * 
	 */
	public static final String CHATQ = "chatq";
	/**
	 * 
	 */
	public static final String CHATTERQ = AuthenticationServer.USERQ;

	private Destination chatroomDestination, chatterDestination;
	/**
	 * listener für chatroom messages
	 */
	private final MessageListener chatroomListener = new MessageListener() {
		/**
		 * wird per callback für jede eingehende Methode der verbundenen Queue
		 * aufgerufen
		 * 
		 * @param message
		 *            Nachricht von einem Client (direkt oder via
		 *            AuthenticationService)
		 */
		@Override
		public void onMessage(final Message message) {
			String msgKind = "";
			String token = "";
			try {
				token = message.getStringProperty(MessageHeader.ChatroomID
						.toString());
				if ((token != null)
						&& ChatServer.this.activeChatRooms.containsKey(token)) {
					if (!(ChatServer.this.activeChatRooms.get(token)
							.processMessage(message))) {
						ChatServer.this.activeChatRooms.remove(token);
					}
				} else {
					try {
						msgKind = message
								.getStringProperty(MessageHeader.MsgKind
										.toString());
						final MessageKind kind = MessageKind.valueOf(msgKind);
						if (kind == MessageKind.chatCreate) {
							final String uid = UUID.randomUUID().toString();
							final Chatroom chatroom = new Chatroom(
									ChatServer.this.replyProducer, uid);
							chatroom.setTraceGenerator(new TraceGenerator(
									new JmsTraceSender(
											ChatServer.this.traceProducer,
											ChatServer.this.session),
									Chatroom.class.getSimpleName()));
							ChatServer.this.activeChatRooms.put(uid, chatroom);
							chatroom.processMessage(message);
						} else {
							Logger.getRootLogger().log(
									Level.ERROR,
									"No receiver for token (" + token
											+ ") or message kind " + msgKind);
						}
					} catch (final IllegalArgumentException e) {
						Logger.getRootLogger().log(Level.ERROR,
								"Unknown message kind " + msgKind);
					}
				}
			} catch (final IllegalArgumentException e) {
				Logger.getLogger("Chatroom").log(Level.ERROR,
						"Unknown message kind " + msgKind);
			} catch (final JMSException e) {
				Logger.getRootLogger().log(Level.ERROR, e);
			}
		}
	};
	/**
	 * listener für chatter messages
	 */
	private final MessageListener chatterListener = new MessageListener() {
		/**
		 * wird per callback für jede eingehende Methode der verbundenen Queue
		 * aufgerufen
		 * 
		 * @param message
		 *            Nachricht von einem Client (direkt oder via
		 *            AuthenticationService)
		 */
		@Override
		public void onMessage(final Message message) {
			String msgKind = "";
			String token = "";
			try {
				token = message.getStringProperty(MessageHeader.AuthToken
						.toString());
				msgKind = message.getStringProperty(MessageHeader.MsgKind
						.toString());
				final MessageKind kind = MessageKind.valueOf(msgKind);

				if ((token != null)
						&& ChatServer.this.activeChatters.containsKey(token)) {
					final Chatter c = ChatServer.this.activeChatters.get(token);
					if ((kind == MessageKind.loggedOut)
							|| !(c.processMessage(message))) {
						ChatServer.this.chatterNicknames
								.remove(c.getUsername());
						ChatServer.this.activeChatters.remove(token);
					}
				} else {
					if (kind == MessageKind.authenticated) {
						if (message instanceof ObjectMessage) {
							final ObjectMessage playerMessage = (ObjectMessage) message;
							final UserDto dto = (UserDto) playerMessage
									.getObject();
							if (dto.isChatter()) {
								final Chatter c = new Chatter(
										dto.getUsername(), "");
								c.setTraceGenerator(new TraceGenerator(
										new JmsTraceSender(
												ChatServer.this.traceProducer,
												ChatServer.this.session),
										Chatter.class.getSimpleName()));
								c.setProducer(ChatServer.this.replyProducer);
								c.setReplyDestination(ChatServer.this.chatterDestination);
								c.setChatroomDestination(ChatServer.this.chatroomDestination);
								ChatServer.this.activeChatters.put(token, c);
								ChatServer.this.chatterNicknames.put(
										c.getUsername(), token);
								c.processMessage(message);
							}
						}
					} else {
						Logger.getLogger("Chatter").log(
								Level.ERROR,
								"No receiver for token (" + token
										+ ") or message kind " + msgKind);
					}
				}
			} catch (final IllegalArgumentException e) {
				Logger.getRootLogger().log(Level.ERROR,
						"Unknown message kind " + msgKind);
			} catch (final JMSException e) {
				Logger.getRootLogger().log(Level.ERROR, e);
			}
		}
	};
	private final ExceptionListener exceptionListener = new ExceptionListener() {
		@Override
		public void onException(final JMSException e) {
			Logger.getRootLogger().log(Level.ERROR, "User-Server error " + e);
		}
	};
	private MessageProducer replyProducer, traceProducer;
	private Session session;
	private final StompConnection stompConnection = new StompConnection();
	private final String stompURI = "stomp://localhost:61613";

	private final String tcpURI = "tcp://localhost:61616";

	private Queue tracingDestination;

	protected ConcurrentHashMap<String, Chatroom> activeChatRooms = new ConcurrentHashMap<>();

	protected ConcurrentHashMap<String, Chatter> activeChatters = new ConcurrentHashMap<>();

	protected ConcurrentHashMap<String, String> chatterNicknames = new ConcurrentHashMap<>();

	/**
	 * Server anlegen
	 */
	public ChatServer() {
		Chatter.setActiveChatters(this.activeChatters);
		Chatter.setChatterNicknames(this.chatterNicknames);
		Chatter.setActiveChatrooms(this.activeChatRooms);
	}

	/**
	 * main programm stellt JMS Message Broker Service zur Verfügung und startet
	 * den AuthenticationServer, wenn Connectoren als Kommandozeilenargumente
	 * angegeben sind
	 * 
	 * @param args
	 *            args[0] URI des AMQ Brokers. Wenn args.length == 0 wird
	 *            lokaler Broker angelegt.
	 * @throws Exception
	 *             jmsexception
	 */
	public static void main(final String[] args) throws Exception {
		final ChatServer chatServer = new ChatServer();
		chatServer.runServer();
	}

	/**
	 * 
	 */
	public void runServer() {
		try {
			// verbinde Server mit dem JMS Broker
			final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"sys", "man", this.tcpURI);
			// connection aufbauen, konfigurieren und starten
			final Connection connection = connectionFactory.createConnection();
			connection.setExceptionListener(this.exceptionListener);
			connection.start();
			// stompconnection aufbauen
			final URI connectUri = new URI("stomp://localhost:61613");
			this.stompConnection.open(this.createSocket(connectUri));
			// session anlegen
			this.session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			this.chatroomDestination = this.session
					.createQueue(ChatServer.CHATQ);
			this.chatterDestination = this.session
					.createQueue(ChatServer.CHATTERQ);
			this.tracingDestination = this.session
					.createQueue(TraceGenerator.TRACEQ);
			// producer anlegen, der nicht an eine bestimmte logInOutDestination
			// gebunden ist
			this.replyProducer = this.session.createProducer(null);
			this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			this.traceProducer = this.session
					.createProducer(this.tracingDestination);
			this.traceProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			// consumer f�r die queues anlegen und mit Listenern verkn�pfen
			this.session.createConsumer(this.chatroomDestination)
					.setMessageListener(this.chatroomListener);
			this.session.createConsumer(this.chatterDestination)
					.setMessageListener(this.chatterListener);
			System.out.println("Chat-Server läuft auf: " + this.tcpURI);
			System.out.println("Chat-Server läuft auf: " + this.stompURI);
		} catch (final Exception e) {
			Logger.getRootLogger().log(Level.ERROR,
					"Server startup error: " + e);
		}
	}

	/**
	 * @param connectUri
	 * @return
	 * @throws IOException
	 */
	private Socket createSocket(final URI connectUri) throws IOException {
		return new Socket("127.0.0.1", connectUri.getPort());
	}
}