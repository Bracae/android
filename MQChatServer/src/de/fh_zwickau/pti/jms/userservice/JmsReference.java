package de.fh_zwickau.pti.jms.userservice;

import javax.jms.Destination;

/**
 * data structure to identify an object by its id and the jms destination where
 * it can be reached
 * 
 * @author georg beier
 * 
 */
public class JmsReference {
	public Destination destination;
	public String id;
}
