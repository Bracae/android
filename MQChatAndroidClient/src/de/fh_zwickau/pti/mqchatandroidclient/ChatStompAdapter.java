package de.fh_zwickau.pti.mqchatandroidclient;

import java.io.Serializable;
import java.util.HashMap;
import android.content.ComponentName;
import android.util.Log;
import de.fh_zwickau.android.base.architecture.BindServiceHelper;
import de.fh_zwickau.informatik.stompj.StompMessage;
import de.fh_zwickau.informatik.stompj.internal.MessageImpl;
import de.fh_zwickau.pti.chatclientcommon.ChatServerMessageProducer;
import de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver;
import de.fh_zwickau.pti.mqgamecommon.MQConstantDefs;
import de.fh_zwickau.pti.mqgamecommon.MessageHeader;
import de.fh_zwickau.pti.mqgamecommon.MessageKind;

/**
 * Implements methods for all messages that shall be sent to the chat server.
 * For that, appropriate stomp messages are created and sent using the Binder
 * interface of theStompCommunicationService.<br>
 * Also, incoming Stomp messages are received and decoded and the appropriate
 * methods of a ChatServerMessageReceiver are called.
 * 
 * @author georg beier
 */
public class ChatStompAdapter implements ChatServerMessageProducer,
	IBrokerConnection, IReceiveStompMessages {

    private static final String LOGINQ = "/queue/" + MQConstantDefs.LOGINQ; //$NON-NLS-1$

    private String authToken = ""; //$NON-NLS-1$

    private String chatServiceQ;

    private ChatServerMessageReceiver messageReceiver;

    private BindServiceHelper<ISendStompMessages, IReceiveStompMessages, ActivityChat> serviceHelper;

    private ISendStompMessages stompServiceBinder;

    /**
	 * 
	 */
    public ChatStompAdapter() {}

    /**
     * set user identity into message properties
     * 
     * @param message
     *            stomp message
     * @param uname
     *            user name
     * @param pword
     *            password
     */
    private static void setUid(final MessageImpl message, final String uname, final String pword) {
	message.setProperty(MessageHeader.LoginUser.toString(), uname);
	message.setProperty(MessageHeader.LoginPassword.toString(), pword);
	message.setProperty(MessageHeader.ChatterNickname.toString(), uname);
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.mqchatandroidclient.IBrokerConnection#connect(java.
     * lang.String, int, java.lang.String, java.lang.String)
     */
    @Override
    public void connect(final String url, final int port, final String user, final String pw) {
	if (this.stompServiceBinder != null) {
	    this.stompServiceBinder.connect(url, port, user, pw);
	} else {
	    Log.e("ChatStompAdapter", "no service binder"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    /*
     * (non-Javadoc)
     * @see de.fh_zwickau.pti.mqchatandroidclient.IBrokerConnection#disconnect()
     */
    @Override
    public void disconnect() {
	if (this.stompServiceBinder != null) {
	    this.stompServiceBinder.disconnect();
	} else {
	    Log.e("ChatStompAdapter", "no service binder"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    /**
     * @return the token
     */
    public String getAuthToken() {
	return this.authToken;
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageProducer#getChatrooms
     * ()
     */
    @Override
    public void getChatrooms() {
	final MessageImpl msg = this.makeMessage(MessageKind.chatterMsgChats);
	if (this.stompServiceBinder != null) {
	    this.stompServiceBinder.sendMessage(msg, this.chatServiceQ);
	} else {
	    Log.e("ChatStompAdapter", "no service binder"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    /**
     * @return the servicequeue
     */
    public String getChatServiceQ() {
	return this.chatServiceQ;
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageProducer#getChatters
     * ()
     */
    @Override
    public void getChatters() {
	final MessageImpl msg = this.makeMessage(MessageKind.chatterMsgChatters);
	if (this.stompServiceBinder != null) {
	    this.stompServiceBinder.sendMessage(msg, this.chatServiceQ);
	} else {
	    Log.e("ChatStompAdapter", "no service binder"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.mqchatandroidclient.IBrokerConnection#isConnected()
     */
    @Override
    public boolean isConnected() {
	if (this.stompServiceBinder != null) { return this.stompServiceBinder.isConnected(); }
	Log.e("ChatStompAdapter", "no service binder"); //$NON-NLS-1$ //$NON-NLS-2$
	return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageProducer#login(java
     * .lang.String, java.lang.String)
     */
    @Override
    public void login(final String uname, final String pword) throws Exception {
	final MessageImpl loginMessage = this.makeMessage(MessageKind.login);
	ChatStompAdapter.setUid(loginMessage, uname, pword);
	if (this.stompServiceBinder != null) {
	    this.stompServiceBinder.sendMessage(loginMessage, ChatStompAdapter.LOGINQ);
	} else {
	    Log.e("ChatStompAdapter", "no service binder"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageProducer#logout()
     */
    @Override
    public void logout() throws Exception {
	final MessageImpl logoutMessage = this.makeMessage(MessageKind.logout);
	if (this.stompServiceBinder != null) {
	    this.stompServiceBinder.sendMessage(logoutMessage, ChatStompAdapter.LOGINQ);
	} else {
	    Log.e("ChatStompAdapter", "no service binder"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.mqchatandroidclient.IReceiveStompMessages#onConnection
     * (boolean)
     */
    @Override
    public void onConnection(final boolean success) {
	if (this.messageReceiver instanceof IReceiveStompMessages) {
	    ((IReceiveStompMessages) this.messageReceiver).onConnection(success);
	} else {
	    Log.e("ChatStompAdapter.onConnection", "no receiver for connect message"); //$NON-NLS-1$ //$NON-NLS-2$
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
	if (this.messageReceiver instanceof IReceiveStompMessages) {
	    ((IReceiveStompMessages) this.messageReceiver).onError(error);
	} else {
	    Log.e("ChatStompAdapter.onError", "no receiver for error message: " + error); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.android.base.architecture.IBindingCallbacks#onServiceBound
     * (android.content.ComponentName)
     */
    @Override
    public void onServiceBound(final ComponentName name) {
	if (this.messageReceiver instanceof IReceiveStompMessages) {
	    if (this.serviceHelper != null) {
		this.serviceHelper.bindMessageHandler();
		this.stompServiceBinder = this.serviceHelper.service();
		this.serviceHelper.startService();
	    } else {
		Log.e("ChatStompAdapter.onServiceBound", "serviceHelper is null"); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	    ((IReceiveStompMessages) this.messageReceiver).onServiceBound(name);
	} else {
	    Log.e("ChatStompAdapter.onServiceBound", "no receiver for onBound message"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.android.base.architecture.IBindingCallbacks#onServiceUnbound
     * (android.content.ComponentName)
     */
    @Override
    public void onServiceUnbound(final ComponentName name) {
	this.stompServiceBinder = null;
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.mqchatandroidclient.IReceiveStompMessages#onStompMessage
     * (java.io.Serializable)
     */
    @Override
    public void onStompMessage(final Serializable message) {
	if (message instanceof StompMessage) {
	    final StompMessage stompMessage = (StompMessage) message;
	    final String msgKind = stompMessage.getProperty(MessageHeader.MsgKind.toString());
	    final MessageKind messageKind = MessageKind.valueOf(msgKind);
	    Log.v("Message trace", msgKind); //$NON-NLS-1$
	    switch (messageKind) {
		case authenticated:
		    this.authToken = stompMessage.getProperty(MessageHeader.AuthToken.toString());
		    this.chatServiceQ = stompMessage.getProperty("reply-to"); //$NON-NLS-1$
		    if (this.messageReceiver != null) {
			this.messageReceiver.gotSuccess();
		    }
		    break;
		case failed:
		    if (this.messageReceiver != null) {
			this.messageReceiver.gotFail();
		    }
		    break;
		case loggedOut:
		    if (this.messageReceiver != null) {
			this.messageReceiver.gotLogout();
		    }
		    break;
		case clientAnswerChat:
		    if (this.messageReceiver != null) {
			final String[] s = stompMessage.getContentAsString().split("[\\n\\r]+"); //$NON-NLS-1$
			this.messageReceiver.gotChatrooms(s);
		    }
		    break;
		case clientAnswerChatters:
		    if (this.messageReceiver != null) {
			final String[] s = stompMessage.getContentAsString().split("[\\n\\r]+"); //$NON-NLS-1$
			this.messageReceiver.gotChatters(s);
		    }
		    break;
		default:
		    break;
	    }
	}
    }

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.chatclientcommon.ChatServerMessageProducer#register
     * (java.lang.String, java.lang.String)
     */
    @Override
    public void register(final String uname, final String pword) throws Exception {
	final MessageImpl registerMessage = this.makeMessage(MessageKind.register);
	ChatStompAdapter.setUid(registerMessage, uname, pword);
	if (this.stompServiceBinder != null) {
	    this.stompServiceBinder.sendMessage(registerMessage, ChatStompAdapter.LOGINQ);
	} else {
	    Log.e("ChatStompAdapter", "no service binder"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    /**
     * @param authToken1
     *            the token to set
     */
    public void setAuthToken(final String authToken1) {
	if (authToken1 == null) {
	    this.authToken = ""; //$NON-NLS-1$
	} else {
	    this.authToken = authToken1;
	}
    }

    /**
     * @param chatServiceQ1
     *            the service to set
     */
    public void setChatServiceQ(final String chatServiceQ1) {
	this.chatServiceQ = chatServiceQ1;
    }

    /*
     * (non-Javadoc)
     * @see de.fh_zwickau.pti.chatclientcommon.ChatServerMessageProducer#
     * setMessageReceiver
     * (de.fh_zwickau.pti.chatclientcommon.ChatServerMessageReceiver)
     */
    @Override
    public void setMessageReceiver(final ChatServerMessageReceiver msgReceiver) {
	this.messageReceiver = msgReceiver;
    }

    /**
     * @param serviceHelper1
     *            the servicehelper to set
     */
    public void setServiceHelper(final BindServiceHelper<ISendStompMessages, IReceiveStompMessages, ActivityChat> serviceHelper1) {
	this.serviceHelper = serviceHelper1;
    }

    /**
     * create Stomp message and initialize some properties
     * 
     * @param kind
     *            meaning of this message
     * @return partly initialized message
     */
    private MessageImpl makeMessage(final MessageKind kind) {
	final MessageImpl msg = new MessageImpl();
	// message properties werden als HashMap übergeben
	final HashMap<String, String> props = new HashMap<>();
	props.put("reply-to", StompCommunicationService.STOMP_REPLY); //$NON-NLS-1$
	props.put(MessageHeader.MsgKind.toString(), kind.toString());
	props.put(MessageHeader.AuthToken.toString(), this.authToken);
	msg.setProperties(props);
	msg.setContent(""); //$NON-NLS-1$
	return msg;
    }
}