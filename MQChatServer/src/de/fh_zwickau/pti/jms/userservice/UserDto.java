package de.fh_zwickau.pti.jms.userservice;

import java.io.Serializable;

/**
 * data transfer objekt for User and Chatter objects
 * 
 * @author georg beier
 * 
 */
public class UserDto implements Serializable {
	private static final long serialVersionUID = 8298862600059153323L;
	private boolean chatter = false;
	private String username;

	public String getUsername() {
		return this.username;
	}

	public boolean isChatter() {
		return this.chatter;
	}

	public void setChatter(final boolean chatter) {
		this.chatter = chatter;
	}

	public void setUsername(final String username) {
		this.username = username;
	}
}