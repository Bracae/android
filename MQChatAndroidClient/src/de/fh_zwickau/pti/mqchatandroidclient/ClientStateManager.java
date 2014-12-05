package de.fh_zwickau.pti.mqchatandroidclient;

import java.io.Serializable;
import android.app.Activity;
import android.content.ComponentName;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import de.fh_zwickau.pti.chatclientcommon.ChatServerMessageProducer;
import de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver;
import de.fh_zwickau.pti.jms.chatclient.android.R;

/**
 * @author georg beier
 * @author jose uhlig
 */
public class ClientStateManager implements IReceiveStompMessages,
	ChatServerMessageReceiver {

    /**
     * @author georg beier
     */
    public class ConnectedState extends ChatAndroidClientState {

	/**
	 * @param n
	 *            name of the state
	 */
	public ConnectedState(final String n) {
	    super(n);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotChatrooms(java
	 * .lang.String[])
	 */
	@Override
	public void gotChatrooms(final String[] chatrooms) {
	    ClientStateManager.this.text = new StringBuilder("gotChatrooms\n"); //$NON-NLS-1$
	    for (final String c : chatrooms) {
		ClientStateManager.this.text.append(c).append('\n');
	    }
	    ClientStateManager.this.outputText.setText(ClientStateManager.this.text);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotChatters(java
	 * .lang.String[])
	 */
	@Override
	public void gotChatters(final String[] chatters) {
	    final StringBuilder text = new StringBuilder("gotChatters\n"); //$NON-NLS-1$
	    for (final String c : chatters) {
		text.append(c).append('\n');
	    }
	    ClientStateManager.this.outputText.setText(text);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#onDisconnect
	 * ()
	 */
	@Override
	public void onDisconnect() {
	    if (ClientStateManager.this.messageProducer instanceof IBrokerConnection) {
		final IBrokerConnection msgSender = (IBrokerConnection) ClientStateManager.this.messageProducer;
		msgSender.disconnect();
		ClientStateManager.this.setState(ClientStateManager.this.notConnected);
	    } else {
		Log.e("ClientStateManager", "cannot send disconnect message"); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	}
    }

    public ChatAndroidClientState loggedIn = new ConnectedState("loggedIn") { //$NON-NLS-1$

	/*
	 * (non-Javadoc)
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
	 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotLogout()
	 */
	@Override
	public void gotLogout() {
	    ClientStateManager.this.setState(ClientStateManager.this.notLoggedIn);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.chatclientcommon.ChatClientState#onGetChatrooms()
	 */
	@Override
	public void onGetChatrooms() {
	    ClientStateManager.this.messageProducer.getChatrooms();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.chatclientcommon.ChatClientState#onGetChatters()
	 */
	@Override
	public void onGetChatters() {
	    ClientStateManager.this.messageProducer.getChatters();
	}

	/*
	 * (non-Javadoc)
	 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#onLogout()
	 */
	@Override
	public void onLogout() {
	    ClientStateManager.this.logoutButton.setEnabled(false);
	    ClientStateManager.this.setState(ClientStateManager.this.notLoggedIn);
	    try {
		ClientStateManager.this.messageProducer.logout();
	    } catch (final Exception e) {
		Log.e(this.getClass().getCanonicalName(), "logout() throws ", e); //$NON-NLS-1$
	    }
	}
    };

    private EditText portEditText;

    private EditText urlEditText;

    protected ChatServerMessageProducer messageProducer;

    ChatAndroidClientState connecting = new ChatAndroidClientState("connecting") { //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#
	 * connectFailure()
	 */
	@Override
	public void connectFailure() {
	    ClientStateManager.this.setState(ClientStateManager.this.notConnected);
	}

	/*
	 * (non-Javadoc)
	 * @see de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#
	 * connectSuccess()
	 */
	@Override
	public void connectSuccess() {
	    ClientStateManager.this.setState(ClientStateManager.this.restoreState);
	}

	/*
	 * (non-Javadoc)
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
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#exitState
	 * ()
	 */
	@Override
	public void exitState() {
	    ClientStateManager.this.connectToggleButton.setEnabled(true);
	}

    };

    ToggleButton connectToggleButton;

    ChatAndroidClientState currentState;
    Button getChatroomsButton;
    Button getChattersButton;
    ChatHistoryState historyState;
    ListView listView;

    Button loginButton;

    Button logoutButton;

    ChatAndroidClientState notBound = new ChatAndroidClientState("notBound") { //$NON-NLS-1$

	/*
	 * (non-Javadoc)
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
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#serviceBound
	 * ()
	 */
	@Override
	public void serviceBound() {
	    ClientStateManager.this.setState(ClientStateManager.this.ready);
	}

    };

    ChatAndroidClientState notConnected = new ChatAndroidClientState("notConnected") { //$NON-NLS-1$

	/*
	 * (non-Javadoc)
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
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#onConnect
	 * ()
	 */
	@Override
	public void onConnect() {
	    ClientStateManager.this.connect();
	}
    };

    ChatAndroidClientState notLoggedIn = new ConnectedState("notLoggedIn") { //$NON-NLS-1$

	/*
	 * (non-Javadoc)
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
	 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotFail()
	 */
	@Override
	public void gotFail() {
	    ClientStateManager.this.outputText.setText("got fail"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotLogout()
	 */
	@Override
	public void gotLogout() {
	    ClientStateManager.this.outputText.setText("logged out"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#gotSuccess()
	 */
	@Override
	public void gotSuccess() {
	    ClientStateManager.this.setState(ClientStateManager.this.loggedIn);
	    ClientStateManager.this.outputText.setText("got success"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#onLogin()
	 */
	@Override
	public void onLogin() {
	    final String un = ClientStateManager.this.unameEditText.getText().toString();
	    final String pw = ClientStateManager.this.pwordEditText.getText().toString();
	    ClientStateManager.this.pwordEditText.setText(""); //$NON-NLS-1$
	    try {
		ClientStateManager.this.messageProducer.login(un, pw);
	    } catch (final Exception e) {
		Log.e(this.getClass().getCanonicalName(), "login() throws ", e); //$NON-NLS-1$
	    }
	}

	/*
	 * (non-Javadoc)
	 * @see de.fh_zwickau.pti.chatclientcommon.ChatClientState#onRegister()
	 */
	@Override
	public void onRegister() {
	    final String un = ClientStateManager.this.unameEditText.getText().toString();
	    final String pw = ClientStateManager.this.pwordEditText.getText().toString();
	    ClientStateManager.this.pwordEditText.setText(""); //$NON-NLS-1$
	    try {
		ClientStateManager.this.messageProducer.register(un, pw);
	    } catch (final Exception e) {
		Log.e(this.getClass().getCanonicalName(), "register() throws ", e); //$NON-NLS-1$
	    }
	}
    };

    TextView outputText;

    EditText pwordEditText;

    /**
     * transient state that decides if
     * <ul>
     * <li>it is a new start of the activity, then proceed with notConnected</li>
     * <li>activity was suspended in notConnected state, then proceed with
     * notConnected</li>
     * <li>activity was suspended in any other state, then try to reconnect</li>
     * </ul>
     */
    ChatAndroidClientState ready = new ChatAndroidClientState("ready") { //$NON-NLS-1$
	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#enterState
	 * ()
	 */
	@Override
	public void enterState() {
	    super.enterState();
	    if ((ClientStateManager.this.historyState == ChatHistoryState.noHistory) || (ClientStateManager.this.historyState == ChatHistoryState.notConnected)) {
		ClientStateManager.this.setState(ClientStateManager.this.notConnected);
	    } else if (((IBrokerConnection) ClientStateManager.this.messageProducer).isConnected()) {
		ClientStateManager.this.setState(ClientStateManager.this.restoreState);
	    } else {
		ClientStateManager.this.connect();
	    }
	}
    };

    Button registerButton;

    ChatAndroidClientState restoreState = new ChatAndroidClientState("restoreState") { //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.ChatAndroidClientState#enterState
	 * ()
	 */
	@Override
	public void enterState() {
	    Log.v("ClientStateManager", "restoreState"); //$NON-NLS-1$ //$NON-NLS-2$
	    super.enterState();
	    switch (ClientStateManager.this.historyState) {
		case notConnected:
		case notLoggedIn:
		    ClientStateManager.this.setState(ClientStateManager.this.notLoggedIn);
		    break;
		case loggedIn:
		    ClientStateManager.this.setState(ClientStateManager.this.loggedIn);
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
		    Log.e(this.getClass().getCanonicalName(), "state not yet implemented " + ClientStateManager.this.historyState); //$NON-NLS-1$
		    break;
	    }
	}
    };

    StringBuilder text;

    EditText unameEditText;

    /**
     * initialize access to gui elements and state chart
     * 
     * @param owner
     *            the calling activity
     */
    public ClientStateManager(final Activity owner) {
	if (owner instanceof ActivityChat) {
	    this.initActivityChat(owner);
	}
	if (owner instanceof ActivityLobby) {
	    this.initActivityLobby(owner);
	}
    }

    /**
     * @param a
     *            the activity
     * @param stringBuilder
     *            an array list
     * @return a new list adapter
     */
    /*
     * public static ArrayAdapter getAdapter(final Activity a, final
     * StringBuilder stringBuilder) {
     * return new ArrayAdapter<>(a, android.R.layout.simple_list_item_1,
     * stringBuilder);
     * }
     */

    /**
     * @return the connecting
     */
    public ChatAndroidClientState getConnecting() {
	return this.connecting;
    }

    /**
     * @return the connectToggleButton
     */
    public ToggleButton getConnectToggleButton() {
	return this.connectToggleButton;
    }

    /**
     * @return the currentState
     */
    public ChatAndroidClientState getCurrentState() {
	return this.currentState;
    }

    /**
     * @return the getChatroomsButton
     */
    public Button getGetChatroomsButton() {
	return this.getChatroomsButton;
    }

    /**
     * @return the getChattersButton
     */
    public Button getGetChattersButton() {
	return this.getChattersButton;
    }

    /**
     * @return the historystate
     */
    public ChatHistoryState getHistoryState() {
	return this.historyState;
    }

    /**
     * @return the listView
     */
    public ListView getListView() {
	return this.listView;
    }

    /**
     * @return the loggedIn
     */
    public ChatAndroidClientState getLoggedIn() {
	return this.loggedIn;
    }

    /**
     * @return the loginButton
     */
    public Button getLoginButton() {
	return this.loginButton;
    }

    /**
     * @return the logoutButton
     */
    public Button getLogoutButton() {
	return this.logoutButton;
    }

    /**
     * @return the messageProducer
     */
    public ChatServerMessageProducer getMessageProducer() {
	return this.messageProducer;
    }

    /**
     * @return the notBound
     */
    public ChatAndroidClientState getNotBound() {
	return this.notBound;
    }

    /**
     * @return the notConnected
     */
    public ChatAndroidClientState getNotConnected() {
	return this.notConnected;
    }

    /**
     * @return the notLoggedIn
     */
    public ChatAndroidClientState getNotLoggedIn() {
	return this.notLoggedIn;
    }

    /**
     * @return the outputText
     */
    public TextView getOutputText() {
	return this.outputText;
    }

    /**
     * @return the portEditText
     */
    public EditText getPortEditText() {
	return this.portEditText;
    }

    /**
     * @return the pwordEditText
     */
    public EditText getPwordEditText() {
	return this.pwordEditText;
    }

    /**
     * @return the ready
     */
    public ChatAndroidClientState getReady() {
	return this.ready;
    }

    /**
     * @return the registerButton
     */
    public Button getRegisterButton() {
	return this.registerButton;
    }

    /**
     * @return the restoreState
     */
    public ChatAndroidClientState getRestoreState() {
	return this.restoreState;
    }

    /**
     * @return the text
     */
    public StringBuilder getText() {
	return this.text;
    }

    /**
     * @return the unameEditText
     */
    public EditText getUnameEditText() {
	return this.unameEditText;
    }

    /**
     * @return the urlEditText
     */
    public EditText getUrlEditText() {
	return this.urlEditText;
    }

    /*
     * (non-Javadoc)
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
     * @see
     * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver#gotFail()
     */
    @Override
    public void gotFail() {
	this.currentState.gotFail();
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver#gotLogout()
     */
    @Override
    public void gotLogout() {
	this.currentState.gotLogout();
    }

    /*
     * (non-Javadoc)
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
     * @see
     * de.fh_zwickau.android.base.architecture.IBindingCallbacks#onServiceUnbound
     * (android.content.ComponentName)
     */
    @Override
    public void onServiceUnbound(final ComponentName name) {
	Log.e("ClientStateManager.onServiceUnbound", "should never be called"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.mqchatandroidclient.IReceiveStompMessages#onStompMessage
     * (java.io.Serializable)
     */
    @Override
    public void onStompMessage(final Serializable message) {
	Log.e("ClientStateManager.onStompMessage", "should never be called"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @param connecting the connecting to set
     */
    public void setConnecting(final ChatAndroidClientState connecting) {
	this.connecting = connecting;
    }

    /**
     * @param connectToggleButton the connectToggleButton to set
     */
    public void setConnectToggleButton(final ToggleButton connectToggleButton) {
	this.connectToggleButton = connectToggleButton;
    }

    /**
     * @param currentState the currentState to set
     */
    public void setCurrentState(final ChatAndroidClientState currentState) {
	this.currentState = currentState;
    }

    /**
     * @param getChatroomsButton the getChatroomsButton to set
     */
    public void setGetChatroomsButton(final Button getChatroomsButton) {
	this.getChatroomsButton = getChatroomsButton;
    }

    /**
     * @param getChattersButton the getChattersButton to set
     */
    public void setGetChattersButton(final Button getChattersButton) {
	this.getChattersButton = getChattersButton;
    }

    /**
     * @param historyState1
     *            the historystate to set
     */
    public void setHistoryState(final ChatHistoryState historyState1) {
	if (historyState1 == null) {
	    this.historyState = ChatHistoryState.noHistory;
	} else {
	    this.historyState = historyState1;
	}
    }

    /**
     * @param listView1 the listView to set
     */
    public void setListView(final ListView listView1) {
	this.listView = listView1;
    }

    /**
     * @param loggedIn the loggedIn to set
     */
    public void setLoggedIn(final ChatAndroidClientState loggedIn) {
	this.loggedIn = loggedIn;
    }

    /**
     * @param loginButton the loginButton to set
     */
    public void setLoginButton(final Button loginButton) {
	this.loginButton = loginButton;
    }

    /**
     * @param logoutButton the logoutButton to set
     */
    public void setLogoutButton(final Button logoutButton) {
	this.logoutButton = logoutButton;
    }

    /**
     * @param messageProducer1
     *            the messageproducer to set
     */
    public void setMessageProducer(final ChatServerMessageProducer messageProducer1) {
	this.messageProducer = messageProducer1;
    }

    /**
     * @param notBound the notBound to set
     */
    public void setNotBound(final ChatAndroidClientState notBound) {
	this.notBound = notBound;
    }

    /**
     * @param notConnected the notConnected to set
     */
    public void setNotConnected(final ChatAndroidClientState notConnected) {
	this.notConnected = notConnected;
    }

    /**
     * @param notLoggedIn the notLoggedIn to set
     */
    public void setNotLoggedIn(final ChatAndroidClientState notLoggedIn) {
	this.notLoggedIn = notLoggedIn;
    }

    /**
     * @param outputText the outputText to set
     */
    public void setOutputText(final TextView outputText) {
	this.outputText = outputText;
    }

    /**
     * @param portEditText the portEditText to set
     */
    public void setPortEditText(final EditText portEditText) {
	this.portEditText = portEditText;
    }

    /**
     * @param pwordEditText the pwordEditText to set
     */
    public void setPwordEditText(final EditText pwordEditText) {
	this.pwordEditText = pwordEditText;
    }

    /**
     * @param ready the ready to set
     */
    public void setReady(final ChatAndroidClientState ready) {
	this.ready = ready;
    }

    /**
     * @param registerButton the registerButton to set
     */
    public void setRegisterButton(final Button registerButton) {
	this.registerButton = registerButton;
    }

    /**
     * @param restoreState the restoreState to set
     */
    public void setRestoreState(final ChatAndroidClientState restoreState) {
	this.restoreState = restoreState;
    }

    /**
     * setState must be called once after construction to initialize state
     * chart!
     * 
     * @param newState
     */
    public void setState(final ChatAndroidClientState newState) {
	if (this.currentState != null) {
	    this.currentState.exitState();
	}
	this.currentState = newState;
	if (this.currentState != null) {
	    this.currentState.enterState();
	}
    }

    /**
     * @param text the text to set
     */
    public void setText(final StringBuilder text) {
	this.text = text;
    }

    /**
     * @param unameEditText the unameEditText to set
     */
    public void setUnameEditText(final EditText unameEditText) {
	this.unameEditText = unameEditText;
    }

    /**
     * @param urlEditText the urlEditText to set
     */
    public void setUrlEditText(final EditText urlEditText) {
	this.urlEditText = urlEditText;
    }

    /**
     * @param owner
     */
    private void initActivityChat(final Activity owner) {
	this.urlEditText = (EditText) owner.findViewById(R.id.urlEditText);
	this.portEditText = (EditText) owner.findViewById(R.id.portEditText);
	this.unameEditText = (EditText) owner.findViewById(R.id.unameText);
	this.pwordEditText = (EditText) owner.findViewById(R.id.pwordText);
	// this.outputText = (TextView) owner.findViewById(R.id.outputText);
	this.connectToggleButton = (ToggleButton) owner.findViewById(R.id.connectToggleButton);
	this.loginButton = (Button) owner.findViewById(R.id.loginButton);
	this.logoutButton = (Button) owner.findViewById(R.id.logoutButton);
	this.registerButton = (Button) owner.findViewById(R.id.registerButton);
	// this.getChattersButton = (Button)
	// owner.findViewById(R.id.getChattersButton);
	this.getChatroomsButton = (Button) owner.findViewById(R.id.getChatroomsButton);

	this.loginButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(final View v) {
		Log.v("Message trace", "onLogin"); //$NON-NLS-1$ //$NON-NLS-2$
		ClientStateManager.this.currentState.onLogin();
	    }
	});
	this.logoutButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(final View v) {
		Log.v("Message trace", "onLogout"); //$NON-NLS-1$ //$NON-NLS-2$
		ClientStateManager.this.currentState.onLogout();
	    }
	});
	this.registerButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(final View v) {
		Log.v("Message trace", "onRegister"); //$NON-NLS-1$ //$NON-NLS-2$
		ClientStateManager.this.currentState.onRegister();
	    }
	});
	/*
	 * this.getChattersButton.setOnClickListener(new OnClickListener() {
	 * @Override
	 * public void onClick(final View v) {
	 * Log.v("Message trace", "onGetChatters"); //$NON-NLS-1$ //$NON-NLS-2$
	 * ClientStateManager.this.currentState.onGetChatters();
	 * }
	 * });
	 */

	this.getChatroomsButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(final View v) {
		Log.v("Message trace", "onGetChatrooms"); //$NON-NLS-1$ //$NON-NLS-2$
		ClientStateManager.this.currentState.onGetChatrooms();
	    }
	});

	this.connectToggleButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(final View v) {
		if (ClientStateManager.this.connectToggleButton.isChecked()) {
		    Log.v("Message trace", "onConnect"); //$NON-NLS-1$ //$NON-NLS-2$
		    ClientStateManager.this.currentState.onConnect();
		} else {
		    Log.v("Message trace", "onDisconnect"); //$NON-NLS-1$ //$NON-NLS-2$
		    ClientStateManager.this.currentState.onDisconnect();
		}
	    }
	});
    }

    /**
     * @param owner
     */
    private void initActivityLobby(final Activity owner) {
	// this.setState(this.loggedIn);
	// this.currentState.onGetChatrooms();
	// this.setListView((ListView) owner.findViewById(R.id.listView1));
	//this.getListView().setAdapter(new ArrayAdapter<>(owner, android.R.layout.simple_list_item_1, new String[] { "blub" })); //$NON-NLS-1$
    }

    /**
	 * 
	 */
    void connect() {
	if (this.messageProducer instanceof IBrokerConnection) {
	    final IBrokerConnection msgSender = (IBrokerConnection) this.messageProducer;
	    final String url = this.urlEditText.getText().toString();
	    final int port = Integer.parseInt(this.portEditText.getText().toString());
	    msgSender.connect(url, port, "sys", "man"); //$NON-NLS-1$ //$NON-NLS-2$
	    this.setState(this.connecting);
	} else {
	    Log.e("ClientStateManager", "cannot send  connect message"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }
}