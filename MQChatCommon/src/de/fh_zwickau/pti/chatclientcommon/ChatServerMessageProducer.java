/**
 * 
 */
package de.fh_zwickau.pti.chatclientcommon;

/**
 * interface defines methodes for each possible message that could be sent to
 * the chat server
 * 
 * @author georg beier
 * 
 */
public interface ChatServerMessageProducer {
	/**
	 * request a list of all active chatrooms
	 */
	void getChatrooms();

	/**
	 * request a list of all idle chatters
	 */
	void getChatters();

	/**
	 * login at the server
	 * 
	 * @param uname
	 *            user nickname
	 * @param pword
	 *            user password
	 * @throws JMSException
	 */
	void login(String uname, String pword) throws Exception;

	/**
	 * logout from server
	 * 
	 * @throws Exception
	 */
	void logout() throws Exception;

	/**
	 * register at the server
	 * 
	 * @param uname
	 *            user nickname
	 * @param pword
	 *            user password
	 * @throws Exception
	 */
	void register(String uname, String pword) throws Exception;

	/**
	 * @param chatSwingClient
	 */
	void setMessageReceiver(ChatServerMessageReceiver messageReceiver);
}
