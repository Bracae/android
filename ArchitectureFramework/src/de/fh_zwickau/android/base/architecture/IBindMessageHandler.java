package de.fh_zwickau.android.base.architecture;

import android.os.Handler;

/**
 * jeder gebundene Service soll einen Message Handler bekommen können
 * 
 * @author georg beier
 * 
 */
public interface IBindMessageHandler {

	/**
	 * Verbinde message handler für callbacks
	 * 
	 * @param msgHandler
	 *            android message handler
	 */
	void setMessageHandler(Handler msgHandler);
}
