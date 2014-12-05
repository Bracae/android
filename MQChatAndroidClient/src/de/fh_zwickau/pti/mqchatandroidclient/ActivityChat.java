package de.fh_zwickau.pti.mqchatandroidclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import de.fh_zwickau.android.base.architecture.BindServiceHelper;
import de.fh_zwickau.pti.jms.chatclient.android.R;

/**
 * @author georg beier
 * @author jose uhlig
 */
public class ActivityChat extends Activity {

    private static final String AUTHTOKEN = "AuthToken"; //$NON-NLS-1$
    private static final String HISTORY = "HistoryState"; //$NON-NLS-1$
    private static final String SERVICEQ = "ChatServiceQ"; //$NON-NLS-1$
    private ClientStateManager stateManager;
    private ChatStompAdapter stompAdapter;
    private BindServiceHelper<ISendStompMessages, IReceiveStompMessages, ActivityChat> stompServiceHelper;

    /**
     * @param view
     *            the view
     */
    public void changeView(final View view) {
	final Intent i = new Intent(this, ActivityLobby.class);
	this.startActivity(i);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	this.getMenuInflater().inflate(R.menu.chat, menu);
	return true;
    }

    /**
     * @param item
     */
    public void onExit(final MenuItem item) {
	try {
	    this.stompAdapter.logout();
	} catch (final Exception e) {}
	this.releaseService();
	this.finish();
    }

    /**
     * 
     */
    private void releaseService() {
	if (this.stompServiceHelper.isBound()) {
	    this.stompAdapter.disconnect();
	    this.stompServiceHelper.stopService();
	}
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	this.setContentView(R.layout.activity_login);

	this.stateManager = new ClientStateManager(this);
	this.stompAdapter = new ChatStompAdapter();
	this.stompServiceHelper = new BindServiceHelper<ISendStompMessages, IReceiveStompMessages, ActivityChat>(this.stompAdapter, this, new Intent(this, StompCommunicationService.class));
	this.stompAdapter.setServiceHelper(this.stompServiceHelper);
	this.stompAdapter.setMessageReceiver(this.stateManager);
	this.stateManager.setMessageProducer(this.stompAdapter);
	this.stateManager.initState();
	this.stateManager.setState(this.stateManager.getLoggedIn());
	if (savedInstanceState != null) {
	    final ChatHistoryState hist = (ChatHistoryState) savedInstanceState.get(ActivityChat.HISTORY);
	    this.stateManager.setHistoryState(hist);
	    Log.i("ActivityChat", "history " + hist.name()); //$NON-NLS-1$ //$NON-NLS-2$
	    final String tok = savedInstanceState.getString(ActivityChat.AUTHTOKEN);
	    this.stompAdapter.setAuthToken(tok);
	    Log.i("ActivityChat", "token " + tok); //$NON-NLS-1$ //$NON-NLS-2$
	    final String serQ = savedInstanceState.getString(ActivityChat.SERVICEQ);
	    this.stompAdapter.setChatServiceQ(serQ);
	    Log.i("ActivityChat", "sevice queue " + serQ); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putSerializable(ActivityChat.HISTORY, this.stateManager.getHistoryState());
	outState.putString(ActivityChat.AUTHTOKEN, this.stompAdapter.getAuthToken());
	outState.putString(ActivityChat.SERVICEQ, this.stompAdapter.getChatServiceQ());
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
	super.onStart();
	this.stompServiceHelper.bindService();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
	super.onStop();
	this.stompServiceHelper.unbindService();
    }
}
