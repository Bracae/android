package de.fh_zwickau.pti.jms.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import de.fh_zwickau.pti.jms.chat.ChatJMSAdapter;
import de.fh_zwickau.pti.mqgamecommon.ChatClientState;
import de.fh_zwickau.pti.mqgamecommon.ChatServerMessageProducer;
import de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver;

/**
 * @author georg beier, jose uhlig, rene fritzsch
 * 
 */
public class ChatSwingClient extends JFrame implements
		ChatServerMessageReceiver {
	/**
	 * special chat client states
	 * 
	 * @author deep throat
	 * 
	 */
	private abstract class Chatting extends ChatClientState {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotNewChat(java.lang
		 * .String)
		 */
		@Override
		public void gotNewChat(final String text) {
			ChatSwingClient.this.chattingFrame.print(text);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotParticipantEntered
		 * (java.lang.String)
		 */
		@Override
		public void gotParticipantEntered(final String text) {
			ChatSwingClient.this.chattingFrame.print(text);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotParticipantLeft
		 * (java.lang.String)
		 */
		@Override
		public void gotParticipantLeft(final String text) {
			ChatSwingClient.this.chattingFrame.print(text);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#onChat()
		 */
		@Override
		public void onChat() {
			try {
				ChatSwingClient.this.messageProducer
						.onChat(ChatSwingClient.this.txtAreaMessageInput
								.getText());
				ChatSwingClient.this.txtAreaMessageInput.setText("");
				ChatSwingClient.this.txtAreaMessageInput.requestFocusInWindow();
				ChatSwingClient.this.txtAreaMessageInput.setCaretPosition(0);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * a special chat client state
	 * 
	 * @author deep throat
	 * 
	 */
	private abstract class Waiting extends ChatClientState {
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotChatClosed()
		 */
		@Override
		public void gotChatClosed() {
			ChatSwingClient.this.setCard(ChatSwingClient.LOGGEDIN);
			ChatSwingClient.this.currentState = ChatSwingClient.this.loggedIn;
			ChatSwingClient.this.currentState.onChats();
		}
	}

	public final static String INVITED = "INVITEDPANEL";
	public final static String LOGGEDIN = "LOGGEDIN";
	public final static String NOTLOGGEDIN = "NOTLOGGEDIN";
	public final static String REQUESTING = "REQUESTING";
	private static final long serialVersionUID = -494594251357510136L;

	/**
	 * starts a client session
	 * 
	 * @param args
	 *            not used
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new ChatSwingClient();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private JButton btnNotLoggedInLogin, btnNotLoggedInRegister,
			btnLoggedInNewChat, btnLoggedInLogout, btnSend, btnInvite,
			btnCloseChatroom, btnInvitedAccept, btnInvitedDeny, btnRequests,
			btnAbortRequest, btnLeaveChatroom;
	private final CardLayout cardLayout = new CardLayout();
	private ChatFrame chattingFrame;
	private final String connection;
	private ChatClientState currentState;
	private final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	private Point initialClick;
	/**
	 * initialise the client state "in other chat" by using the abstract class
	 * chatting to avoid issues
	 */
	private final ChatClientState inOtherChat = new Chatting() {
		@Override
		public void gotChatClosed() {
			ChatSwingClient.this.chattingFrame.hideChattingFrame();
			ChatSwingClient.this.setVisibleChatSwingClient(true);
			ChatSwingClient.this.setCard(ChatSwingClient.LOGGEDIN);
			ChatSwingClient.this.currentState = ChatSwingClient.this.loggedIn;
			ChatSwingClient.this.currentState.onChats();
		}

		@Override
		public void onLeave() {
			try {
				ChatSwingClient.this.messageProducer.onLeave();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.chattingFrame.hideChattingFrame();
			ChatSwingClient.this.setVisibleChatSwingClient(true);
			ChatSwingClient.this.setCard(ChatSwingClient.LOGGEDIN);
			ChatSwingClient.this.currentState = ChatSwingClient.this.loggedIn;
			ChatSwingClient.this.currentState.onChats();
		}
	};
	/**
	 * initialise the client state "in own chat" by using the abstract class
	 * chatting to avoid issues
	 */
	private final ChatClientState inOwnChat = new Chatting() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotAccepted()
		 */
		@Override
		public void gotAccepted() {
			ChatSwingClient.this.chattingFrame
					.print("Eine Einladung wurde angenommen.");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotAnswerChatters(
		 * java.lang.String)
		 */
		@Override
		public void gotAnswerChatters(final String text) {
			if (text.equals(new String("no idle chatter found"))) {
				JOptionPane.showMessageDialog(null, "No chatters available!",
						"Attention", JOptionPane.PLAIN_MESSAGE);
			} else {
				ChatSwingClient.this.chattingFrame
						.clearAndShowTableChatterList();
				ChatSwingClient.this.chattingFrame.setActiveChatters(text);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotDenied()
		 */
		@Override
		public void gotDenied() {
			ChatSwingClient.this.chattingFrame
					.print("Eine Einladung wurde abgelehnt.");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotRequest(java.lang
		 * .String)
		 */
		@Override
		public void gotRequest(final String refId) {
			ChatSwingClient.this.chattingFrame.addRequester(refId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotRequestCancelled
		 * (java.lang.String)
		 */
		@Override
		public void gotRequestCancelled(final String requestor) {
			ChatSwingClient.this.chattingFrame.removeRequester(requestor);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#onAccept(java.lang
		 * .String)
		 */
		@Override
		public void onAccept(final String refId) {
			try {
				ChatSwingClient.this.messageProducer.onAccept(refId);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.chattingFrame.removeRequester(refId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#onChatters()
		 */
		@Override
		public void onChatters() {
			try {
				ChatSwingClient.this.messageProducer.onChatters();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#onClose()
		 */
		@Override
		public void onClose() {
			try {
				ChatSwingClient.this.messageProducer.onClose();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.setVisibleChatSwingClient(true);
			ChatSwingClient.this.setCard(ChatSwingClient.LOGGEDIN);
			ChatSwingClient.this.chattingFrame.hideChattingFrame();
			ChatSwingClient.this.currentState = ChatSwingClient.this.loggedIn;
			ChatSwingClient.this.currentState.onChats();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#onInvite(java.lang
		 * .String)
		 */
		@Override
		public void onInvite(final String refId) {
			try {
				ChatSwingClient.this.messageProducer.onInvite(refId);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.chattingFrame.hideTableChatterList();
			JOptionPane.showMessageDialog(null,
					"Ihre Einladung wurde entsprechend an " + refId
							+ " verschickt!", "Hinweis",
					JOptionPane.PLAIN_MESSAGE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#onReject(java.lang
		 * .String)
		 */
		@Override
		public void onReject(final String refId) {
			try {
				ChatSwingClient.this.messageProducer.onReject(refId);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.chattingFrame.removeRequester(refId);
		}
	};
	/**
	 * initialise the client state "invited" by using the abstract class waiting
	 * to avoid issues
	 */
	private final ChatClientState invited = new Waiting() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#onAcceptInvitation()
		 */
		@Override
		public void onAcceptInvitation() {
			try {
				ChatSwingClient.this.messageProducer.onAcceptionInvitation();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.chattingFrame.clearAndShowChattingFrame(false);
			ChatSwingClient.this.setVisibleChatSwingClient(false);
			ChatSwingClient.this.currentState = ChatSwingClient.this.inOtherChat;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#onDeny()
		 */
		@Override
		public void onDeny() {
			try {
				ChatSwingClient.this.messageProducer.onDeny();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.setCard(ChatSwingClient.LOGGEDIN);
			ChatSwingClient.this.currentState = ChatSwingClient.this.loggedIn;
			ChatSwingClient.this.currentState.onChats();
		}
	};
	private InvitedPanel invitedPanel;
	private JLabel labelInvitedMessage, labelRequestingMessage;
	/**
	 * initialise the client state "logged in"
	 */
	private final ChatClientState loggedIn = new ChatClientState() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotAnswerChats(java
		 * .lang.String)
		 */
		@Override
		public void gotAnswerChats(final String text) {
			if (!text.equals(new String("no active chatroom found"))) {
				ChatSwingClient.this.loggedinPanel.setActiveChatRooms(text);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotInvitation(java
		 * .lang.String)
		 */
		@Override
		public void gotInvitation(final String text) {
			ChatSwingClient.this.labelInvitedMessage
					.setText("<html><div align=\"center\">"
							+ "<b>Sie wurden von "
							+ text
							+ "</b></div>"
							+ "<div align=\"center\"><b>zu einem Chat eingeladen.</b></div>"
							+ "</html>");
			ChatSwingClient.this.setCard(ChatSwingClient.INVITED);
			ChatSwingClient.this.currentState = ChatSwingClient.this.invited;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotLogout()
		 */
		@Override
		public void gotLogout() {
			ChatSwingClient.this.setCard(ChatSwingClient.NOTLOGGEDIN);
			ChatSwingClient.this.setTitle("Login-Bereich");
			ChatSwingClient.this.currentState = ChatSwingClient.this.notLoggedIn;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#onChats()
		 */
		@Override
		public void onChats() {
			try {
				ChatSwingClient.this.messageProducer.onChats();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#onLogout()
		 */
		@Override
		public void onLogout() {
			try {
				ChatSwingClient.this.messageProducer.onLogout();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#onRequest(java.lang
		 * .String)
		 */
		@Override
		public void onRequest(final String chatroomId) {
			try {
				ChatSwingClient.this.messageProducer
						.onRequestParticipation(chatroomId);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.labelRequestingMessage
					.setText("<html><div align=\"center\"><b>Ihre Anfrage wurde entsprechend gestellt.<br>Bitte warten ...</b></div></html>");
			ChatSwingClient.this.setCard(ChatSwingClient.REQUESTING);
			ChatSwingClient.this.currentState = ChatSwingClient.this.requesting;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#onStartChat(java.lang
		 * .String)
		 */
		@Override
		public void onStartChat(final String chatroomName) {
			try {
				ChatSwingClient.this.messageProducer.onStartChat(chatroomName);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.btnLoggedInNewChat.setEnabled(false);
			ChatSwingClient.this.currentState = ChatSwingClient.this.waitForChat;
		}
	};
	private LobbyPanel loggedinPanel;
	private JPanel mainPanel, registerPanel;
	private ChatServerMessageProducer messageProducer;
	/**
	 * initialise the client state "not logged in"
	 */
	private final ChatClientState notLoggedIn = new ChatClientState() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotFail(java.lang.
		 * String)
		 */
		@Override
		public void gotFail(final String text) {
			ChatSwingClient.this.notLoggedinPanel.relockElements();
			JOptionPane.showMessageDialog(null, text, "Fehler",
					JOptionPane.PLAIN_MESSAGE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotSuccess()
		 */
		@Override
		public void gotSuccess() {
			ChatSwingClient.this.notLoggedinPanel.relockElements();
			ChatSwingClient.this.setCard(ChatSwingClient.LOGGEDIN);
			ChatSwingClient.this.currentState = ChatSwingClient.this.loggedIn;
			ChatSwingClient.this.currentState.onChats();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#onLogin()
		 */
		@Override
		public void onLogin() {
			final String un = ChatSwingClient.this.txtNotLoggedInName.getText();
			final String pw = new String(
					ChatSwingClient.this.txtNotLoggedInPassword.getPassword());
			try {
				ChatSwingClient.this.messageProducer.onLogin(un, pw);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.notLoggedinPanel.lockElements();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#onRegister()
		 */
		@Override
		public void onRegister() {
			final String un = ChatSwingClient.this.txtNotLoggedInName.getText();
			final String pw = new String(
					ChatSwingClient.this.txtNotLoggedInPassword.getPassword());
			try {
				ChatSwingClient.this.messageProducer.onRegister(un, pw);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.notLoggedinPanel.lockElements();
		}
	};

	private LoginPanel notLoggedinPanel;

	/**
	 * initialise the client state "requesting" by using the abstract class
	 * waiting to avoid issues
	 */
	private final ChatClientState requesting = new Waiting() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotParticipating()
		 */
		@Override
		public void gotParticipating() {
			ChatSwingClient.this.setVisibleChatSwingClient(false);
			ChatSwingClient.this.chattingFrame.clearAndShowChattingFrame(false);
			ChatSwingClient.this.currentState = ChatSwingClient.this.inOtherChat;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotRejected()
		 */
		@Override
		public void gotRejected() {
			ChatSwingClient.this.setCard(ChatSwingClient.LOGGEDIN);
			JOptionPane.showMessageDialog(null,
					"Dein Antrag auf Teilnahme wurde abgelehnt!");
			ChatSwingClient.this.currentState = ChatSwingClient.this.loggedIn;
			ChatSwingClient.this.currentState.onChats();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#onCancel()
		 */
		@Override
		public void onCancel() {
			try {
				ChatSwingClient.this.messageProducer.onCancel();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			ChatSwingClient.this.setCard(ChatSwingClient.LOGGEDIN);
			ChatSwingClient.this.currentState = ChatSwingClient.this.loggedIn;
			ChatSwingClient.this.currentState.onChats();
		}
	};

	private RequestPanel requestingPanel;

	private JTable tableChatterListe, chatRoomTable, tableRequestorList;

	private JTextArea txtAreaMessageInput;

	private JTextField txtNotLoggedInName;

	private JPasswordField txtNotLoggedInPassword;

	/**
	 * initialise the client state "wait for chat"
	 */
	private final ChatClientState waitForChat = new ChatClientState() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqgamecommon.ChatClientState#gotChatStarted()
		 */
		@Override
		public void gotChatStarted() {
			ChatSwingClient.this.setVisibleChatSwingClient(false);
			ChatSwingClient.this.chattingFrame.clearAndShowChattingFrame(true);
			ChatSwingClient.this.btnLoggedInNewChat.setEnabled(true);
			ChatSwingClient.this.currentState = ChatSwingClient.this.inOwnChat;
		}
	};

	private final int x = 500;

	private final int y = 300;

	/**
	 * initialise the client
	 * 
	 * @throws IOException
	 */
	public ChatSwingClient() throws IOException {
		this.connection = "tcp://localhost:61616";
		this.initializeJMS(this.connection);
		this.initializeGUI();
	}

	private void addListeners() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				ChatSwingClient.this.initialClick = e.getPoint();
				ChatSwingClient.this
						.getComponentAt(ChatSwingClient.this.initialClick);
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				final int thisX = ChatSwingClient.this.getLocation().x;
				final int thisY = ChatSwingClient.this.getLocation().y;
				final int xMoved = (thisX + e.getX())
						- (thisX + ChatSwingClient.this.initialClick.x);
				final int yMoved = (thisY + e.getY())
						- (thisY + ChatSwingClient.this.initialClick.y);
				final int X = thisX + xMoved;
				final int Y = thisY + yMoved;
				ChatSwingClient.this.setLocation(X, Y);
			}
		});
	}

	/**
	 * @return the lobby panel
	 */
	public JPanel getPanelLoggedin() {
		return this.loggedinPanel;
	}

	/**
	 * @return the login screen panel
	 */
	public JPanel getPanelNotLoggedin() {
		return this.notLoggedinPanel;
	}

	public JPanel getRegisterPanel() {
		return this.registerPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotAccepted()
	 */
	@Override
	public void gotAccepted() {
		this.currentState.gotAccepted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotAnswerChats
	 * (java.lang.String)
	 */
	@Override
	public void gotAnswerChats(final String text) {
		this.currentState.gotAnswerChats(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotAnswerChatters
	 * (java.lang.String)
	 */
	@Override
	public void gotAnswerChatters(final String text) {
		this.currentState.gotAnswerChatters(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotChatClosed()
	 */
	@Override
	public void gotChatClosed() {
		this.currentState.gotChatClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotChatStarted()
	 */
	@Override
	public void gotChatStarted() {
		this.currentState.gotChatStarted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotDenied()
	 */
	@Override
	public void gotDenied() {
		this.currentState.gotDenied();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotFail(java
	 * .lang.String)
	 */
	@Override
	public void gotFail(final String text) {
		this.currentState.gotFail(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotInvite(java
	 * .lang.String)
	 */
	@Override
	public void gotInvite(final String text) {
		this.currentState.gotInvitation(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotLogout()
	 */
	@Override
	public void gotLogout() {
		this.currentState.gotLogout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotNewChat(java
	 * .lang.String)
	 */
	@Override
	public void gotNewChat(final String text) {
		this.currentState.gotNewChat(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#
	 * gotParticipantEntered(java.lang.String)
	 */
	@Override
	public void gotParticipantEntered(final String text) {
		this.currentState.gotParticipantEntered(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotParticipantLeft
	 * (java.lang.String)
	 */
	@Override
	public void gotParticipantLeft(final String text) {
		this.currentState.gotParticipantLeft(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotParticipating
	 * ()
	 */
	@Override
	public void gotParticipating() {
		this.currentState.gotParticipating();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotRejected()
	 */
	@Override
	public void gotRejected() {
		this.currentState.gotRejected();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotRequest(java
	 * .lang.String)
	 */
	@Override
	public void gotRequest(final String refId) {
		this.currentState.gotRequest(refId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotRequestCancelled
	 * (java.lang.String)
	 */
	@Override
	public void gotRequestCancelled(final String requestor) {
		this.currentState.gotRequestCancelled(requestor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqgamecommon.ChatServerMessageReceiver#gotSuccess()
	 */
	@Override
	public void gotSuccess() {
		this.currentState.gotSuccess();
	}

	/**
	 * sets the basic settings
	 */
	private void initBasicSettings() {
		final String look = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(look);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		this.setTitle("WhistleBuster");
		this.setLayout(this.cardLayout);
		this.setLocation((this.d.width / 2) - (this.x / 2), (this.d.height / 2)
				- (this.y / 2));
		this.setSize(this.x, this.y);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * creates the elements of the gui
	 */
	public void initializeGUI() {
		this.initBasicSettings();
		this.initPanels();
		this.addListeners();
		this.setVisible(true);
		this.initReferencesToGuiElements();
	}

	private void initializeJMS(final String conn) {
		this.messageProducer = new ChatJMSAdapter();
		this.messageProducer.setMessageReceiver(this);
		final String localConnection = conn;
		((ChatJMSAdapter) this.messageProducer)
				.connectToServer(localConnection);
		this.currentState = this.notLoggedIn;
	}

	/**
	 * initialise all the panels for the gui
	 */
	private void initPanels() {
		this.mainPanel = new JPanel(this.cardLayout);
		this.notLoggedinPanel = new LoginPanel(this);
		this.notLoggedinPanel.setVisible(true);
		this.mainPanel.add(this.notLoggedinPanel, ChatSwingClient.NOTLOGGEDIN);
		this.loggedinPanel = new LobbyPanel("", this);
		this.loggedinPanel.setVisible(true);
		this.mainPanel.add(this.loggedinPanel, ChatSwingClient.LOGGEDIN);
		this.add(this.mainPanel);
		this.cardLayout.show(this.mainPanel, ChatSwingClient.NOTLOGGEDIN);
		this.invitedPanel = new InvitedPanel(this.x, this.y);
		this.invitedPanel.setVisible(true);
		this.mainPanel.add(this.invitedPanel, ChatSwingClient.INVITED);
		this.requestingPanel = new RequestPanel(this.x, this.y);
		this.requestingPanel.setVisible(true);
		this.mainPanel.add(this.requestingPanel, ChatSwingClient.REQUESTING);
	}

	/**
	 * initialise the components of the login gui
	 */
	private void initReferencesToGuiElements() {
		this.txtNotLoggedInName = this.notLoggedinPanel.getTxtName();
		this.txtNotLoggedInPassword = this.notLoggedinPanel.getTxtPassword();
		this.btnNotLoggedInLogin = this.notLoggedinPanel.getBtnLogin();
		this.btnNotLoggedInLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ChatSwingClient.this.currentState.onLogin();
			}
		});
		this.btnNotLoggedInRegister = this.notLoggedinPanel.getBtnRegister();
		this.btnNotLoggedInRegister.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ChatSwingClient.this.currentState.onRegister();
			}
		});
		this.btnLoggedInNewChat = this.loggedinPanel.getBtnNewChat();
		this.btnLoggedInNewChat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String chatroomName = JOptionPane.showInputDialog(null,
						"Chatroom name:", "Create new chatroom",
						JOptionPane.PLAIN_MESSAGE);
				if ((chatroomName != null) && !chatroomName.isEmpty()) {
					ChatSwingClient.this.currentState.onStartChat(chatroomName);
				}
			}
		});
		this.btnLoggedInLogout = this.loggedinPanel.getLogoutButton();
		this.btnLoggedInLogout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ChatSwingClient.this.currentState.onLogout();
			}
		});
		this.chattingFrame = new ChatFrame(this);
		this.btnSend = this.chattingFrame.getBtnSend();
		this.btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ChatSwingClient.this.currentState.onChat();
			}
		});
		this.txtAreaMessageInput = this.chattingFrame.getTxtAreaMessageInput();
		this.txtAreaMessageInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					ChatSwingClient.this.currentState.onChat();
				}
			}
		});
		this.btnInvite = this.chattingFrame.getBtnInvite();
		this.btnInvite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ChatSwingClient.this.currentState.onChatters();
			}
		});
		this.btnCloseChatroom = this.chattingFrame.getBtnCloseChatroom();
		this.btnCloseChatroom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ChatSwingClient.this.currentState.onClose();
			}
		});
		this.tableChatterListe = this.chattingFrame.getTableChatterList();
		this.tableChatterListe.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() >= 2) {
					ChatSwingClient.this.currentState
							.onInvite(ChatSwingClient.this.tableChatterListe
									.getValueAt(
											ChatSwingClient.this.tableChatterListe
													.getSelectedRow(), 0)
									.toString());
				}
			}
		});
		this.btnInvitedAccept = this.invitedPanel.getBtnAccept();
		this.btnInvitedAccept.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ChatSwingClient.this.currentState.onAcceptInvitation();
			}
		});
		this.btnInvitedDeny = this.invitedPanel.getBtnDeny();
		this.btnInvitedDeny.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ChatSwingClient.this.currentState.onDeny();
			}
		});
		this.labelInvitedMessage = this.invitedPanel.getLabelMessage();
		this.chatRoomTable = this.loggedinPanel.getChatRoomTable();
		this.chatRoomTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() >= 2) {
					final String chatRoom = ChatSwingClient.this.chatRoomTable
							.getValueAt(
									ChatSwingClient.this.chatRoomTable
											.getSelectedRow(), 0).toString();
					ChatSwingClient.this.currentState.onRequest(chatRoom);
				}
			}
		});
		this.btnRequests = this.chattingFrame.getBtnRequests();
		this.btnRequests.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ChatSwingClient.this.chattingFrame.showTableRequesterList();
			}
		});
		this.btnAbortRequest = this.requestingPanel.getBtnAbort();
		this.btnAbortRequest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ChatSwingClient.this.currentState.onCancel();
			}
		});
		this.tableRequestorList = this.chattingFrame.getTableRequesterList();
		this.tableRequestorList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() >= 2) {
					final String refId = ChatSwingClient.this.tableRequestorList
							.getValueAt(
									ChatSwingClient.this.tableRequestorList
											.getSelectedRow(), 0).toString();
					final Object[] options = { "Yes", "No" };
					final int result = JOptionPane.showOptionDialog(null, "Is "
							+ refId + " worthy to join the chat?",
							"requesting", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]);
					if (result == 0) {
						ChatSwingClient.this.currentState.onAccept(refId);
					} else {
						ChatSwingClient.this.currentState.onReject(refId);
					}
				}
			}
		});
		this.labelRequestingMessage = this.requestingPanel.getLabelMessage();
		this.btnLeaveChatroom = this.chattingFrame.getBtnLeaveChatroom();
		this.btnLeaveChatroom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ChatSwingClient.this.currentState.onLeave();
			}
		});
	}

	/**
	 * sets the current needed panel
	 * 
	 * @param karte
	 *            name of the panel
	 */
	public void setCard(final String karte) {
		this.cardLayout.show(this.mainPanel, karte);
		switch (karte) {
		case NOTLOGGEDIN:
			this.setTitle("subscribe");
			break;
		case LOGGEDIN:
			this.setTitle("WhistleBuster");
			break;
		case INVITED:
			this.setTitle("invitation");
			break;
		case REQUESTING:
			this.setTitle("request");
			break;
		default:
			this.setTitle("");
			break;
		}
	}

	/**
	 * sets the visibility of the frame
	 * 
	 * @param visible
	 *            true, if the frame is visible, otherwise false
	 */
	public void setVisibleChatSwingClient(final boolean visible) {
		this.setVisible(visible);
	}
}