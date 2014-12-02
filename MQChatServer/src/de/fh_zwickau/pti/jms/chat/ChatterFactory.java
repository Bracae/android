package de.fh_zwickau.pti.jms.chat;

import de.fh_zwickau.pti.jms.userservice.User;
import de.fh_zwickau.pti.jms.userservice.UserFactory;

/**
 * erzeugt Chatter Objekte statt User-Objekten
 * 
 * @author georg beier
 * 
 */
public class ChatterFactory extends UserFactory {

	@Override
	public User registerUser(final String uname, final String pword) {
		final User c = new Chatter(uname, pword);
		if (this.register(c)) {
			return c;
		} else {
			return null;
		}
	}
}