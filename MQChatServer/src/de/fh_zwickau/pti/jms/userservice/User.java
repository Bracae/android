package de.fh_zwickau.pti.jms.userservice;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.jms.Destination;
import javax.jms.Message;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Implementierung der Basisfunktionalität für einen User, der über username
 * identifiziert und über pwhash authentifiziert wird. Objekte sind
 * serialisierbar und können daher als message body einer JMS ObjectMessage
 * übertragen werden.
 * 
 * @author georg beier
 * 
 */
public class User implements Serializable {
	private static MessageDigest md;
	private static final long serialVersionUID = 5640483852632353563L;

	// implementation of ..1 association end across jms by a unique destination
	private Destination clientDestination;

	private String pwhash;

	private Destination replyDestination;

	private final String username;

	/**
	 * Neues User-Objekt anlegen. Der Passworthash wird aus uname und pword
	 * erzeugt.
	 * 
	 * @param uname
	 *            login Name
	 * @param pword
	 *            Klartext-Passwort
	 */
	public User(final String uname, final String pword) {
		this.username = uname;
		if ((pword != null) && (pword.length() != 0)) {
			this.pwhash = User.hash(uname + pword, 100);
		} else {
			this.pwhash = "";
		}
	}

	/**
	 * berechnet einen möglichst sicheren Passworthash durch wiederholte
	 * Anwendung der digest() Methode
	 * 
	 * @param pwin
	 *            Klartext, der gehasht werden soll
	 * @param loop
	 *            Anzahl der Wiederholungen der Hash-Funktion
	 * @return hash des Eingabestrings
	 */
	public static String hash(final String pwin, final int loop) {
		byte[] input = pwin.getBytes();
		for (int i = 0; i < loop; i++) {
			input = User.getDigest().digest(input);
		}
		return User.makeToken(input);
	}

	/**
	 * erzeugt ein MessageDigest Objekt
	 */
	private static synchronized MessageDigest getDigest() {
		if (User.md == null) {
			try {
				User.md = MessageDigest.getInstance("SHA-1");
			} catch (final NoSuchAlgorithmException e) {
				Logger.getRootLogger().log(Level.ERROR,
						"could not create MessageDigest");
			}
		}
		return User.md;
	}

	/**
	 * byte[] -> String
	 * 
	 * @param input
	 *            arbitrary byte array
	 * @return string of hex numbers
	 */
	private static String makeToken(final byte[] input) {
		final StringBuilder buf = new StringBuilder();
		for (final byte b : input) {
			buf.append(String.format("%02x", b));
		}
		return buf.toString();
	}

	/**
	 * Authentifiziere User
	 * 
	 * @param uname
	 *            login Name
	 * @param pword
	 *            Klartext-Passwort
	 * @return true bei Erfolg
	 */
	public boolean authenticate(final String pword) {
		return (User.hash(this.username + pword, 100).equals(this.pwhash));
	}

	public Destination getReplyDestination() {
		return this.replyDestination;
	}

	/**
	 * erzeuge einen temporären möglichst nicht vorhersagbaren
	 * Authentifizierungstoken
	 * 
	 * @return der neue Token
	 */
	public String getToken() {
		return User.hash(this.username + System.currentTimeMillis(), 1);
	}

	public String getUsername() {
		return this.username;
	}

	/**
	 * whatever has to be done to process a message
	 * 
	 * @param message
	 *            incoming jms message
	 */
	public boolean processMessage(final Message message) {
		return false;
	}

	public void setReplyDestination(final Destination replyDestination) {
		this.replyDestination = replyDestination;
	}

	protected Destination getClientDestination() {
		return this.clientDestination;
	}

	protected void setClientDestination(final Destination clientDestination) {
		this.clientDestination = clientDestination;
	}
}