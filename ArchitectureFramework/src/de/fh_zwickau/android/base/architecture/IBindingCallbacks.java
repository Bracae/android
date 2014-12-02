package de.fh_zwickau.android.base.architecture;

import android.content.ComponentName;

public interface IBindingCallbacks {
	public static String CBMETH = "CBMETH";
	public static String CBVAL = "CBVAL";

	/**
	 * callback, wenn Service gebunden ist
	 * 
	 * @param name
	 *            name des service
	 */
	void onServiceBound(ComponentName name);

	/**
	 * callback, wenn Service getrennt ist
	 * 
	 * @param name
	 *            name des service
	 */
	void onServiceUnbound(ComponentName name);
}
