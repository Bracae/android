package de.fh_zwickau.pti.jms.userservice;

import java.util.HashMap;

/**
 * Klasse zur Erzeugung und Verwaltung von User Objekten im System
 * 
 * @author georg beier
 * 
 */
public class UserFactory {
	// Ablage für registrierte User Objekte
	private final HashMap<String, User> users = new HashMap<>();

	/**
	 * User Objekt aus der Ablage holen, wenn Name und Passwort stimmen
	 * 
	 * @param uname
	 *            Name
	 * @param pword
	 *            Passwort
	 * @return User oder null bei fehlerhaften Eingaben
	 */
	public User createUser(final String uname, final String pword) {
		if (this.users.containsKey(uname)
				&& this.users.get(uname).authenticate(pword)) {
			return this.users.get(uname);
		} else {
			return null;
		}
	}

	/**
	 * Neues User Objekt erzeugen, in Ablage speichern und zurückgeben.
	 * 
	 * @param uname
	 *            Name, muss eindeutig sein
	 * @param pword
	 *            Passwort
	 * @return User Objekt oder null
	 */
	public User registerUser(final String uname, final String pword) {
		if (!this.users.containsKey(uname)) {
			final User p = new User(uname, pword);
			this.users.put(uname, p);
			return p;
		} else {
			return null;
		}
	}

	/**
	 * User Objekt (oder Subklasse von User) registrieren
	 * 
	 * @param p
	 *            Objekt, das registriert werden soll
	 * @return true bei Erfolg
	 */
	protected boolean register(final User p) {
		if (!this.users.containsKey(p.getUsername())) {
			this.users.put(p.getUsername(), p);
			return true;
		}
		return false;
	}
}