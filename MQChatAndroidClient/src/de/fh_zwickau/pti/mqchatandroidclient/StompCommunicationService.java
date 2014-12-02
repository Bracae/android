package de.fh_zwickau.pti.mqchatandroidclient;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import de.fh_zwickau.android.base.architecture.IBindingCallbacks;
import de.fh_zwickau.informatik.stompj.Connection;
import de.fh_zwickau.informatik.stompj.ErrorMessage;
import de.fh_zwickau.informatik.stompj.MessageHandler;
import de.fh_zwickau.informatik.stompj.StompMessage;

/**
 * @author georg beier
 */
public class StompCommunicationService extends Service {

    /**
     * @author georg beier
     */
    public class StompBinder extends Binder implements ISendStompMessages {

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.IBrokerConnection#connect(java
	 * .lang.String, int, java.lang.String, java.lang.String)
	 */
	@Override
	public void connect(final String url, final int port, final String user, final String pw) {
	    Log.v("StompCommunicationService", "connect to " + url + ":" + port);
	    final Thread t = new Thread() {
		@Override
		public void run() {
		    StompCommunicationService.this.stompConnect(url, port, user, pw);
		}
	    };
	    t.start();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.IBrokerConnection#disconnect()
	 */
	@Override
	/**
	 * dauert länger, daher als AsyncTask ausführen
	 */
	public void disconnect() {
	    AsyncTask<Void, Void, Void> x;
	    if ((StompCommunicationService.this.stompConnection != null) && StompCommunicationService.this.stompConnection.isConnected()) {
		x = new AsyncTask<Void, Void, Void>() {
		    /*
		     * (non-Javadoc)
		     * @see android.os.AsyncTask#doInBackground(Params[])
		     */
		    @Override
		    protected Void doInBackground(final Void... params) {
			StompCommunicationService.this.stompConnection.disconnect();
			return null;
		    }

		    /*
		     * (non-Javadoc)
		     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		     */
		    @Override
		    protected void onPostExecute(final Void result) {
			StompCommunicationService.this.callbackProvider.onConnection(false);
			super.onPostExecute(result);
		    }
		};
		x.execute();
	    }
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.IBrokerConnection#isConnected()
	 */
	@Override
	public boolean isConnected() {
	    return ((StompCommunicationService.this.stompConnection != null) && StompCommunicationService.this.stompConnection.isConnected());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.pti.mqchatandroidclient.ISendStompMessages#sendMessage
	 * (de.fh_zwickau.informatik.stompj.StompMessage, java.lang.String[])
	 */
	@Override
	public void sendMessage(final StompMessage message, final String... destination) {
	    if ((StompCommunicationService.this.stompConnection != null) && StompCommunicationService.this.stompConnection.isConnected()) {
		StompCommunicationService.this.stompConnection.send(message, destination);
	    }
	}

	/*
	 * (non-Javadoc)
	 * @see de.fh_zwickau.android.base.architecture.IBindMessageHandler#
	 * setMessageHandler(android.os.Handler)
	 */
	@Override
	public void setMessageHandler(final Handler msgHandler) {
	    StompCommunicationService.this.callbackProvider.setCallbackHandler(msgHandler);
	}

    }

    /**
     * @author georg beier
     */
    private class StompCallbackProvider {

	private Handler callbackHandler;

	/**
	 * @param success
	 *            if the connection was a success
	 */
	public void onConnection(final boolean success) {
	    final Bundle bundle = new Bundle();
	    bundle.putString(IBindingCallbacks.CBMETH, "onConnection");
	    bundle.putBoolean(IBindingCallbacks.CBVAL, success);
	    this.send(bundle);
	}

	/**
	 * @param error
	 *            the errormessage
	 */
	public void onError(final String error) {
	    final Bundle bundle = new Bundle();
	    bundle.putString(IBindingCallbacks.CBMETH, "onError");
	    bundle.putString(IBindingCallbacks.CBVAL, error);
	    this.send(bundle);
	}

	/**
	 * @param message
	 *            the stompmessage
	 */
	public void onStompMessage(final StompMessage message) {
	    final Bundle bundledMessage = new Bundle();
	    bundledMessage.putString(IBindingCallbacks.CBMETH, "onStompMessage");
	    bundledMessage.putSerializable(IBindingCallbacks.CBVAL, message);
	    this.send(bundledMessage);
	}

	/**
	 * @param callbackHandler
	 *            the callbackhandler to set
	 */
	public void setCallbackHandler(final Handler callbackHandler) {
	    this.callbackHandler = callbackHandler;
	}

	/**
	 * @param b
	 */
	private void send(final Bundle b) {
	    if (this.callbackHandler != null) {
		final Message msg = new Message();
		msg.setData(b);
		this.callbackHandler.sendMessage(msg);
	    }
	}
    }

    /**
	 * 
	 */
    public static final String MESSAGE_CONTENT = "msg-content";

    /**
	 * 
	 */
    public static final String STOMP_REPLY = "/temp-queue/replyq";

    private final StompBinder binder = new StompBinder();

    private final StompCallbackProvider callbackProvider = new StompCallbackProvider();

    private Connection stompConnection;

    /**
     * test message handler
     */
    private final MessageHandler stompHandler = new MessageHandler() {

	/*
	 * (non-Javadoc)
	 * @see
	 * de.fh_zwickau.informatik.stompj.MessageHandler#onMessage(de.fh_zwickau
	 * .informatik.stompj.StompMessage)
	 */
	@Override
	public void onMessage(final StompMessage message) {
	    StompCommunicationService.this.callbackProvider.onStompMessage(message);
	}
    };

    /**
	 * 
	 */
    public StompCommunicationService() {}

    /*
     * (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(final Intent intent) {
	return this.binder;
    }

    /**
     * konfiguriere Message System
     * 
     * @param brokerUrl
     *            URI of JMS Message broker
     * @param port
     *            connection port for stomp messages
     * @param uname
     *            login am Broker
     * @param pw
     *            password am Broker
     */
    private void stompConnect(final String brokerUrl, final int port, final String uname, final String pw) {
	// connection zu einem Message Broker aufbauen
	this.stompConnection = new Connection(brokerUrl, port, uname, pw);
	ErrorMessage emsg;
	try {
	    emsg = this.stompConnection.connect();
	    // wenn null zurückkommt, hat die Verbindung geklappt
	    if (emsg != null) {
		this.callbackProvider.onError(emsg.getContentAsString());
		this.callbackProvider.onConnection(false);
		Log.e("StompConnectError", emsg.getContentAsString());
	    } else {
		// subsription zu einer queue
		this.stompConnection.subscribe(StompCommunicationService.STOMP_REPLY, true);
		// message handler verbinden, können mehrere sein
		this.stompConnection.addMessageHandler(StompCommunicationService.STOMP_REPLY, this.stompHandler);
		this.callbackProvider.onConnection(true);
	    }
	} catch (final Exception e) {
	    this.callbackProvider.onError(e.toString());
	    this.callbackProvider.onConnection(false);
	    Log.e("StompConnectError", "Stomp Connection Error");
	}
    }
}