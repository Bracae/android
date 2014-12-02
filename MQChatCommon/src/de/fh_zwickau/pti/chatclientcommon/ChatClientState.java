/**
 * 
 */
package de.fh_zwickau.pti.chatclientcommon;

/**
 * defines all methods that are implemented by ObjectForState classes
 * 
 * @author georg beier
 * 
 */
public abstract class ChatClientState {

	protected String name;

	public ChatClientState() {
		this("");
	}

	public ChatClientState(final String stateName) {
		this.name = stateName;
	}

	/**
	 * got chatrooms
	 */
	public void gotChatrooms(final String[] chatrooms) {
		this.logError("gotChatrooms");
	}

	/**
	 * got chatters
	 */
	public void gotChatters(final String[] chatters) {
		this.logError("gotChatters");
	}

	/**
	 * login failed
	 */
	public void gotFail() {
		this.logError("gotFail");
	}

	/**
	 * session ended
	 */
	public void gotLogout() {
		this.logError("gotLogout");
	}

	// events from server
	/**
	 * login succeeded
	 */
	public void gotSuccess() {
		this.logError("gotSuccess");
	}

	public void onGetChatrooms() {
		this.logError("onGetChatrooms");
	}

	public void onGetChatters() {
		this.logError("onGetChatters");
	}

	public void onLogin() {
		this.logError("onLogin");
	}

	public void onLogout() {
		this.logError("onlogout");
	}

	// events from gui
	public void onRegister() {
		this.logError("onRegister");
	}

	protected void logError(final String... evt) {
		System.err.println("unexpected event " + (evt.length > 0 ? evt[0] : "")
				+ (this.name.length() > 0 ? " in state " + this.name : ""));
	}
}
