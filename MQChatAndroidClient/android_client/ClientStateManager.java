package de.fh_zwickau.pti.mqchatandroidclient;

import java.io.Serializable;
import java.util.ArrayList;
import android.app.Activity;
import android.content.ComponentName;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import de.fh_zwickau.pti.chatclientcommon.ChatServerMessageProducer;
import de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver;
import de.fh_zwickau.pti.jms.chatclient.android.R;

/**
 * @author georg beier
 * 
 */
public class ClientStateManager implements IReceiveStompMessages,
		ChatServerMessageReceiver {

	/**
	 * @author georg beier
	 * 
	 */
	private class ConnectedState extends ChatAndroidClientState {

		/**
		 * @param n
		 *            name of the state
		 */
		public ConnectedState(final String n) {
			super(n);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotChatrooms(java
		 * .lang.String[])
		 */
		@Override
		public void gotChatrooms(final String[] chatrooms) {
			ClientStateManager.this.liste = new ArrayList<String>();
			for (final String c : chatrooms) {
				ClientStateManager.this.liste.add(c);
			}

			// ClientStateManager.this.outputText.setText(text);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotChatters(java
		 * .lang.String[])
		 */
		@Override
		public void gotChatters(final String[] chatters) {
			final StringBuilder text = new StringBuilder("gotChatters\n");
			for (final String c : chatters) {
				text.append(c).append('\n');
			}
			ClientStateManager.this.outputText.setText(text);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#onDisconnect
		 * ()
		 */
		@Override
		public void onDisconnect() {
			if (ClientStateManager.this.messageProducer instanceof IBrokerConnection) {
				final IBrokerConnection msgSender = (IBrokerConnection) ClientStateManager.this.messageProducer;
				msgSender.disconnect();
				ClientStateManager.this
						.setState(ClientStateManager.this.notConnected);
			} else {
				Log.e("ClientStateManager", "cannot send disconnect message");
			}
		}
	}

	private ToggleButton connectToggleButton;

	private ChatAndroidClientState currentState;

	private Button getChatroomsButton, getChattersButton, loginButton,
			logoutButton, registerButton;
	private ChatHistoryState historyState;
	private ArrayList<String> liste;
	private ListView listView;
	private TextView outputText;

	private EditText portEditText, pwordEditText, unameEditText, urlEditText;

	protected ChatServerMessageProducer messageProducer;

	ChatAndroidClientState connecting = new ChatAndroidClientState("connecting") {

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#
		 * connectFailure()
		 */
		@Override
		public void connectFailure() {
			ClientStateManager.this
					.setState(ClientStateManager.this.notConnected);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#
		 * connectSuccess()
		 */
		@Override
		public void connectSuccess() {
			ClientStateManager.this
					.setState(ClientStateManager.this.restoreState);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#enterState
		 * ()
		 */
		@Override
		public void enterState() {
			super.enterState();
			ClientStateManager.this.connectToggleButton.setEnabled(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#exitState
		 * ()
		 */
		@Override
		public void exitState() {
			ClientStateManager.this.connectToggleButton.setEnabled(true);
		}

	};

	ChatAndroidClientState loggedIn = new ConnectedState("loggedIn") {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#enterState
		 * ()
		 */
		@Override
		public void enterState() {
			super.enterState();
			ClientStateManager.this.historyState = ChatHistoryState.loggedIn;
			ClientStateManager.this.loginButton.setEnabled(false);
			ClientStateManager.this.registerButton.setEnabled(false);
			ClientStateManager.this.logoutButton.setEnabled(true);
			ClientStateManager.this.getChattersButton.setEnabled(true);
			ClientStateManager.this.getChatroomsButton.setEnabled(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#exitState
		 * ()
		 */
		@Override
		public void exitState() {
			ClientStateManager.this.getChattersButton.setEnabled(false);
			ClientStateManager.this.getChatroomsButton.setEnabled(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotLogout()
		 */
		@Override
		public void gotLogout() {
			ClientStateManager.this
					.setState(ClientStateManager.this.notLoggedIn);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.chatclientcommon.ChatClientState#onGetChatrooms()
		 */
		@Override
		public void onGetChatrooms() {
			ClientStateManager.this.messageProducer.getChatrooms();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.chatclientcommon.ChatClientState#onGetChatters()
		 */
		@Override
		public void onGetChatters() {
			ClientStateManager.this.messageProducer.getChatters();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#onLogout()
		 */
		@Override
		public void onLogout() {
			ClientStateManager.this.logoutButton.setEnabled(false);
			ClientStateManager.this
					.setState(ClientStateManager.this.notLoggedIn);
			try {
				ClientStateManager.this.messageProducer.logout();
			} catch (final Exception e) {
				Log.e(this.getClass().getCanonicalName(), "logout() throws ", e);
			}
		}
	};

	ChatAndroidClientState notBound = new ChatAndroidClientState("notBound") {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#enterState
		 * ()
		 */
		@Override
		public void enterState() {
			super.enterState();
			ClientStateManager.this.connectToggleButton.setChecked(false);
			ClientStateManager.this.connectToggleButton.setEnabled(false);
			ClientStateManager.this.loginButton.setEnabled(false);
			ClientStateManager.this.registerButton.setEnabled(false);
			ClientStateManager.this.logoutButton.setEnabled(false);
			ClientStateManager.this.getChatroomsButton.setEnabled(false);
			ClientStateManager.this.getChattersButton.setEnabled(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#serviceBound
		 * ()
		 */
		@Override
		public void serviceBound() {
			ClientStateManager.this.setState(ClientStateManager.this.ready);
		}

	};

	ChatAndroidClientState notConnected = new ChatAndroidClientState(
			"notConnected") {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#enterState
		 * ()
		 */
		@Override
		public void enterState() {
			super.enterState();
			ClientStateManager.this.historyState = ChatHistoryState.notConnected;
			ClientStateManager.this.connectToggleButton.setChecked(false);
			ClientStateManager.this.connectToggleButton.setEnabled(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#onConnect
		 * ()
		 */
		@Override
		public void onConnect() {
			ClientStateManager.this.connect();
		}
	};

	ChatAndroidClientState notLoggedIn = new ConnectedState("notLoggedIn") {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#enterState
		 * ()
		 */
		@Override
		public void enterState() {
			super.enterState();
			ClientStateManager.this.historyState = ChatHistoryState.notLoggedIn;
			ClientStateManager.this.loginButton.setEnabled(true);
			ClientStateManager.this.registerButton.setEnabled(true);
			ClientStateManager.this.logoutButton.setEnabled(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotFail()
		 */
		@Override
		public void gotFail() {
			ClientStateManager.this.outputText.setText("got fail");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotLogout()
		 */
		@Override
		public void gotLogout() {
			ClientStateManager.this.outputText.setText("logged out");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotSuccess()
		 */
		@Override
		public void gotSuccess() {
			ClientStateManager.this.setState(ClientStateManager.this.loggedIn);
			ClientStateManager.this.outputText.setText("got success");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#onLogin()
		 */
		@Override
		public void onLogin() {
			final String un = ClientStateManager.this.unameEditText.getText()
					.toString();
			final String pw = ClientStateManager.this.pwordEditText.getText()
					.toString();
			ClientStateManager.this.pwordEditText.setText("");
			try {
				ClientStateManager.this.messageProducer.login(un, pw);
			} catch (final Exception e) {
				Log.e(this.getClass().getCanonicalName(), "login() throws ", e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#onRegister()
		 */
		@Override
		public void onRegister() {
			final String un = ClientStateManager.this.unameEditText.getText()
					.toString();
			final String pw = ClientStateManager.this.pwordEditText.getText()
					.toString();
			ClientStateManager.this.pwordEditText.setText("");
			try {
				ClientStateManager.this.messageProducer.register(un, pw);
			} catch (final Exception e) {
				Log.e(this.getClass().getCanonicalName(), "register() throws ",
						e);
			}
		}
	};

	/**
	 * transient state that decides if
	 * <ul>
	 * <li>it is a new start of the activity, then proceed with notConnected</li>
	 * <li>activity was suspended in notConnected state, then proceed with
	 * notConnected</li>
	 * <li>activity was suspended in any other state, then try to reconnect</li>
	 * </ul>
	 */
	ChatAndroidClientState ready = new ChatAndroidClientState("ready") {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#enterState
		 * ()
		 */
		@Override
		public void enterState() {
			super.enterState();
			if ((ClientStateManager.this.historyState == ChatHistoryState.noHistory)
					|| (ClientStateManager.this.historyState == ChatHistoryState.notConnected)) {
				ClientStateManager.this
						.setState(ClientStateManager.this.notConnected);
			} else if (((IBrokerConnection) ClientStateManager.this.messageProducer)
					.isConnected()) {
				ClientStateManager.this
						.setState(ClientStateManager.this.restoreState);
			} else {
				ClientStateManager.this.connect();
			}
		}
	};

	ChatAndroidClientState restoreState = new ChatAndroidClientState(
			"restoreState") {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#enterState
		 * ()
		 */
		@Override
		public void enterState() {
			Log.v("ClientStateManager", "restoreState");
			super.enterState();
			switch (ClientStateManager.this.historyState) {
			case notConnected:
			case notLoggedIn:
				ClientStateManager.this
						.setState(ClientStateManager.this.notLoggedIn);
				break;
			case loggedIn:
				ClientStateManager.this
						.setState(ClientStateManager.this.loggedIn);
				break;
			// case requesting:
			// setState(requesting);
			// break;
			// case invited:
			// setState(invited);
			// break;
			// case inOtherChat:
			// setState(inOtherChat);
			// break;
			// case inOwnChat:
			// setState(inOwnChat);
			// break;
			default:
				Log.e(this.getClass().getCanonicalName(),
						"state not yet implemented "
								+ ClientStateManager.this.historyState);
				break;
			}
			;
		}
	};

	/**
	 * initialize access to gui elements and state chart
	 * 
	 * @param owner
	 *            the calling activity
	 */
	public ClientStateManager(final Activity owner) {
		this.initChatActivity(owner);
		this.initChatroomActivity(owner);
	}

	/**
	 * @param a
	 *            the activity
	 * @param al
	 *            an array list
	 * @return a new list adapter
	 * 
	 */
	public ListAdapter getAdapter(final Activity a, final ArrayList<String> al) {
		return new ArrayAdapter<>(a, android.R.layout.simple_list_item_1, al);
	}

	/**
	 * @return the historystate
	 */
	public ChatHistoryState getHistoryState() {
		return this.historyState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver#gotChatrooms
	 * ()
	 */
	@Override
	public void gotChatrooms(final String[] c) {
		this.currentState.gotChatrooms(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver#gotChatters
	 * ()
	 */
	@Override
	public void gotChatters(final String[] c) {
		this.currentState.gotChatters(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver#gotFail()
	 */
	@Override
	public void gotFail() {
		this.currentState.gotFail();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver#gotLogout()
	 */
	@Override
	public void gotLogout() {
		this.currentState.gotLogout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver#gotSuccess()
	 */
	@Override
	public void gotSuccess() {
		this.currentState.gotSuccess();
	}

	/**
	 * 
	 */
	public void initState() {
		this.setHistoryState(ChatHistoryState.noHistory);
		this.setState(this.notBound);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.IReceiveStompMessages#onConnection
	 * (boolean)
	 */
	@Override
	public void onConnection(final boolean success) {
		if (success) {
			this.currentState.connectSuccess();
		} else {
			this.currentState.connectFailure();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.IReceiveStompMessages#onError(java
	 * .lang.String)
	 */
	@Override
	public void onError(final String error) {
		this.outputText.setText(error);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.android.base.architecture.IBindingCallbacks#onServiceBound
	 * (android.content.ComponentName)
	 */
	@Override
	public void onServiceBound(final ComponentName name) {
		this.currentState.serviceBound();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.android.base.architecture.IBindingCallbacks#onServiceUnbound
	 * (android.content.ComponentName)
	 */
	@Override
	public void onServiceUnbound(final ComponentName name) {
		Log.e("ClientStateManager.onServiceUnbound", "should never be called");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.IReceiveStompMessages#onStompMessage
	 * (java.io.Serializable)
	 */
	@Override
	public void onStompMessage(final Serializable message) {
		Log.e("ClientStateManager.onStompMessage", "should never be called");
	}

	/**
	 * @param historyState
	 *            the historystate to set
	 */
	public void setHistoryState(final ChatHistoryState historyState) {
		if (historyState == null) {
			this.historyState = ChatHistoryState.noHistory;
		} else {
			this.historyState = historyState;
		}
	}

	/**
	 * @param messageProducer
	 *            the messageproducer to set
	 */
	public void setMessageProducer(
			final ChatServerMessageProducer messageProducer) {
		this.messageProducer = messageProducer;
	}

	/**
	 * 
	 */
	private void connect() {
		if (this.messageProducer instanceof IBrokerConnection) {
			final IBrokerConnection msgSender = (IBrokerConnection) this.messageProducer;
			final String url = this.urlEditText.getText().toString();
			final int port = Integer.parseInt(this.portEditText.getText()
					.toString());
			msgSender.connect(url, port, "sys", "man");
			this.setState(this.connecting);
		} else {
			Log.e("ClientStateManager", "cannot send  connect message");
		}
	}

	/**
	 * @param owner
	 */
	private void initChatActivity(final Activity owner) {
		this.urlEditText = (EditText) owner.findViewById(R.id.urlEditText);
		this.portEditText = (EditText) owner.findViewById(R.id.portEditText);
		this.unameEditText = (EditText) owner.findViewById(R.id.unameText);
		this.pwordEditText = (EditText) owner.findViewById(R.id.pwordText);
		this.outputText = (TextView) owner.findViewById(R.id.outputText);
		this.connectToggleButton = (ToggleButton) owner
				.findViewById(R.id.connectToggleButton);
		this.loginButton = (Button) owner.findViewById(R.id.loginButton);
		this.logoutButton = (Button) owner.findViewById(R.id.logoutButton);
		this.registerButton = (Button) owner.findViewById(R.id.registerButton);
		this.getChattersButton = (Button) owner
				.findViewById(R.id.getChattersButton);
		this.getChatroomsButton = (Button) owner
				.findViewById(R.id.getChatroomsButton);
		this.loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.v("Message trace", "onLogin");
				ClientStateManager.this.currentState.onLogin();
			}
		});
		this.logoutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.v("Message trace", "onLogout");
				ClientStateManager.this.currentState.onLogout();
			}
		});
		this.registerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.v("Message trace", "onRegister");
				ClientStateManager.this.currentState.onRegister();
			}
		});
		this.getChattersButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.v("Message trace", "onGetChatters");
				ClientStateManager.this.currentState.onGetChatters();
			}
		});
		this.getChatroomsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.v("Message trace", "onGetChatrooms");
				ClientStateManager.this.currentState.onGetChatrooms();
			}
		});
		this.connectToggleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (ClientStateManager.this.connectToggleButton.isChecked()) {
					Log.v("Message trace", "onConnect");
					ClientStateManager.this.currentState.onConnect();
				} else {
					Log.v("Message trace", "onDisconnect");
					ClientStateManager.this.currentState.onDisconnect();
				}
			}
		});
	}

	/**
	 * @param owner
	 */
	private void initChatroomActivity(final Activity owner) {
		this.listView = (ListView) owner.findViewById(R.id.listView1);
		ClientStateManager.this.currentState.onGetChatrooms();
		this.listView.setAdapter(this.getAdapter(owner, this.liste));
	}

	/**
	 * setState must be called once after construction to initialize state
	 * chart!
	 * 
	 * @param newState
	 */
	private void setState(final ChatAndroidClientState newState) {
		if (this.currentState != null) {
			this.currentState.exitState();
		}
		this.currentState = newState;
		if (this.currentState != null) {
			this.currentState.enterState();
		}
	}
}