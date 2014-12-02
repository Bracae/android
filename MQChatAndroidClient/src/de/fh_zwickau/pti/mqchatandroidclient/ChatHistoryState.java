package de.fh_zwickau.pti.mqchatandroidclient;

/**
 * all non transient simple (i.e. not compound or pseudo) states are identified
 * by enum values to support returning to state history
 * 
 * @author georg beier
 */
public enum ChatHistoryState {
    /**
	 * 
	 */
    inOtherChat,
    /**
	 * 
	 */
    inOwnChat,
    /**
	 * 
	 */
    invited,
    /**
	 * 
	 */
    loggedIn,
    /**
	 * 
	 */
    noHistory,
    /**
	 * 
	 */
    notConnected,
    /**
	 * 
	 */
    notLoggedIn,
    /**
	 * 
	 */
    requesting,
    // waitForChat,

}
