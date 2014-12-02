package de.fh_zwickau.pti.mqchatandroidclient;

import android.util.Log;
import de.fh_zwickau.pti.chatclientcommon.ChatClientState;

/**
 * @author georg beier
 */
public class ChatAndroidClientState extends ChatClientState {

    /**
     * @param n
     *            the chat client stat
     */
    public ChatAndroidClientState(final String n) {
	super(n);
    }

    /**
	 * 
	 */
    public void connectFailure() {
	Log.v("ChatAndroidClientState", "connection closed"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
	 * 
	 */
    public void connectSuccess() {
	this.logError("connectSuccess"); //$NON-NLS-1$
    }

    /**
	 * 
	 */
    public void enterState() {
	Log.v("State trace", "entering " + this.name); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
	 * 
	 */
    public void exitState() {}

    /**
	 * 
	 */
    public void onConnect() {
	this.logError("onConnect"); //$NON-NLS-1$
    }

    /**
	 * 
	 */
    public void onDisconnect() {
	this.logError("onDisconnect"); //$NON-NLS-1$
    }

    /**
	 * 
	 */
    public void serviceBound() {}

    /*
     * (non-Javadoc)
     * @see
     * de.fh_zwickau.pti.chatclientcommon.ChatClientState#logError(java.lang
     * .String[])
     */
    @Override
    protected void logError(final String... evt) {
	Log.e("ChatAndroidClientState", "unexpected event " + (evt.length > 0 ? evt[0] : "") + (this.name.length() > 0 ? " in state " + this.name : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
}