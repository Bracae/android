package de.fh_zwickau.pti.jms.userservice;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.fh_zwickau.pti.jms.chat.ChatterFactory;
import de.fh_zwickau.pti.mqgamecommon.MessageHeader;
import de.fh_zwickau.pti.mqgamecommon.MessageKind;

/**
 * JMS Service Provider und Demo-Programm für unterschiedliche ActiveMQ Features
 * 
 * @author georg beier
 * @author jose uhlig
 * 
 */
public class AuthenticationServer {
	/**
	 * name of the login queue
	 */
	public static final String LOGINQ = "loginq";
	/**
	 * name of the user queue
	 */
	public static final String USERQ = "userq";
	private final static String[] localConnections = {
			ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL,
			"stomp://localhost:61613" };

	private final String brokerUri;

	private final ExceptionListener exceptionListener = new ExceptionListener() {
		@Override
		public void onException(final JMSException e) {
			Logger.getRootLogger().log(Level.ERROR, "Server error " + e);
		}
	};
	private Destination logInOutDestination, userDestination;
	/**
	 * listener für login messages
	 */
	private final MessageListener logInOutListener = new MessageListener() {
		/**
		 * wird per callback für jede eingehende Methode der verbundenen Queue
		 * aufgerufen
		 * 
		 * @param message
		 *            Nachricht vn einem Client (direkt oder via PlayerService)
		 */
		@Override
		public void onMessage(final Message message) {
			String msgKind = "";
			try {
				msgKind = message.getStringProperty(MessageHeader.MsgKind
						.toString());
				final MessageKind kind = MessageKind.valueOf(msgKind);
				switch (kind) {
				case login:
				case register:
					final String uname = message
							.getStringProperty(MessageHeader.LoginUser
									.toString());
					final String pword = message
							.getStringProperty(MessageHeader.LoginPassword
									.toString());
					User p;
					final UserDto dto = new UserDto();
					dto.setUsername(uname);
					dto.setChatter(AuthenticationServer.this.userFactory instanceof ChatterFactory);
					if (kind == MessageKind.register) {
						p = AuthenticationServer.this.userFactory.registerUser(
								uname, pword);
					} else {
						p = AuthenticationServer.this.userFactory.createUser(
								uname, pword);
					}
					if (p != null) {
						final ObjectMessage replyMessage = AuthenticationServer.this.session
								.createObjectMessage(dto);

						// sende neue Message an den PlayerService
						replyMessage
								.setJMSDestination(AuthenticationServer.this.userDestination);

						// setze ReplyDestination auf die TempQueue des Clients
						replyMessage.setJMSReplyTo(message.getJMSReplyTo());

						// Informationen im Message Header setzen
						replyMessage.setStringProperty(
								MessageHeader.AuthToken.toString(),
								p.getToken());
						replyMessage.setStringProperty(
								MessageHeader.MsgKind.toString(),
								MessageKind.authenticated.toString());

						// Message absenden
						AuthenticationServer.this.replyProducer.send(
								AuthenticationServer.this.userDestination,
								replyMessage);
					} else {
						final TextMessage textMessage = AuthenticationServer.this.session
								.createTextMessage();
						if (kind == MessageKind.login) {
							textMessage.setText("Login failed");
						} else {
							textMessage.setText("Register failed");
						}

						// direkt zurück an den Client
						textMessage.setJMSDestination(message.getJMSReplyTo());

						// Informationen im Message Header setzen
						textMessage.setStringProperty(
								MessageHeader.MsgKind.toString(),
								MessageKind.failed.toString());

						// Message absenden
						AuthenticationServer.this.replyProducer.send(
								textMessage.getJMSDestination(), textMessage);
					}
					break;
				case logout:
					final String token = message
							.getStringProperty(MessageHeader.AuthToken
									.toString());
					final TextMessage replyMessage = AuthenticationServer.this.session
							.createTextMessage();
					final Destination replyDestination = message
							.getJMSReplyTo();
					replyMessage.setJMSDestination(replyDestination);
					replyMessage.setStringProperty(
							MessageHeader.MsgKind.toString(),
							MessageKind.loggedOut.toString());
					AuthenticationServer.this.replyProducer.send(
							replyDestination, replyMessage);
					final ObjectMessage invalidateMessage = AuthenticationServer.this.session
							.createObjectMessage();
					invalidateMessage.setStringProperty(
							MessageHeader.AuthToken.toString(), token);
					invalidateMessage.setStringProperty(
							MessageHeader.MsgKind.toString(),
							MessageKind.loggedOut.toString());
					invalidateMessage
							.setJMSDestination(AuthenticationServer.this.userDestination);
					AuthenticationServer.this.replyProducer.send(
							AuthenticationServer.this.userDestination,
							invalidateMessage);
					break;
				default:
					break;
				}
			} catch (final IllegalArgumentException e) {
				Logger.getRootLogger().log(Level.ERROR,
						"Unknown message kind " + msgKind);
			} catch (final JMSException e) {
				e.printStackTrace();
			}
		}
	};
	private MessageProducer replyProducer;

	private Session session;

	private final UserFactory userFactory;

	/**
	 * create server
	 * 
	 * @param brUri
	 *            URI from MessageBrokers
	 * @param pf
	 *            factory object to create players or subclasses of player
	 */
	public AuthenticationServer(final String brUri, final UserFactory pf) {
		this.brokerUri = brUri;
		this.userFactory = pf;
	}

	/**
	 * main programm provides JMS Message Broker Service and starts the
	 * AuthenticationServer
	 * 
	 * @param args
	 *            args[0] URI of AMQ Brokers. If args.length == 0, local broker
	 *            will be created.
	 * @throws Exception
	 *             jmsexception
	 */
	public static void main(final String[] args) throws Exception {
		String brokerUri;
		if ((args.length == 0) || (args.length == 1)) {
			brokerUri = AuthenticationServer.localConnections[0];

			// lokalen AMQ Server anlegen
			final ArrayList<String> interfaces = new ArrayList<>();
			final Enumeration<NetworkInterface> nis = NetworkInterface
					.getNetworkInterfaces();

			// Liste der IP-Adressen der Netzwerkinterfaces aufbauen
			while (nis.hasMoreElements()) {
				final NetworkInterface ni = nis.nextElement();
				final Enumeration<InetAddress> ifs = ni.getInetAddresses();
				while (ifs.hasMoreElements()) {
					final InetAddress myIp = ifs.nextElement();
					if (!myIp.isLoopbackAddress()
							&& (myIp.getAddress().length == 4)) {
						interfaces.add(myIp.getHostAddress());
						System.out.println(myIp.getHostAddress() + " -> "
								+ myIp.getCanonicalHostName());
					}
				}
			}
			final BrokerService broker = new BrokerService();
			broker.setUseJmx(false);
			for (final String connection : AuthenticationServer.localConnections) {
				if (connection.matches("\\w+://localhost:\\d{4,5}")) {
					broker.addConnector(connection);
					for (final String hostAddress : interfaces) {
						final String conn = connection.replaceFirst("://.*:",
								"://" + hostAddress + ":");
						broker.addConnector(conn);
						System.out.println("Connector on " + conn);
					}
				}
			}
			final SystemUsage systemUsage = broker.getSystemUsage();
			systemUsage.getStoreUsage().setLimit(1024 * 1024 * 1024);
			systemUsage.getTempUsage().setLimit(1024 * 1024 * 1024);
			// System.out.println(broker.getTransportConnectors());
			broker.start();
		} else {
			brokerUri = args[1];
		}
		UserFactory factory;
		if (args.length > 0) {
			factory = new UserFactory();
			System.out.println("creating players");
		} else {
			factory = new ChatterFactory();
			System.out.println("creating chatters");
		}
		final AuthenticationServer authenticationServer = new AuthenticationServer(
				brokerUri, factory);
		authenticationServer.runServer();
	}

	/**
	 * 
	 */
	public void runServer() {
		try {
			// verbinde Server mit dem JMS Broker
			final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"sys", "man", this.brokerUri);

			// connection aufbauen, konfigurieren und starten
			final Connection connection = connectionFactory.createConnection();
			connection.setExceptionListener(this.exceptionListener);
			connection.start();

			// session anlegen
			this.session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			this.logInOutDestination = this.session
					.createQueue(AuthenticationServer.LOGINQ);
			this.userDestination = this.session
					.createQueue(AuthenticationServer.USERQ);

			// producer anlegen, der nicht an eine bestimmte logInOutDestination
			// gebunden ist
			this.replyProducer = this.session.createProducer(null);
			this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// consumer für die queues anlegen und mit Listenern verknüpfen
			this.session.createConsumer(this.logInOutDestination)
					.setMessageListener(this.logInOutListener);
			System.out.println("Authentication-Server läuft, alles cool.");
		} catch (final Exception e) {
			Logger.getRootLogger()
					.log(Level.ERROR, "Server startup error " + e);
		}
	}
}