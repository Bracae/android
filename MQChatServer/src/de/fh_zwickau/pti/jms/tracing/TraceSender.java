package de.fh_zwickau.pti.jms.tracing;

/**
 * Send traces in some way
 * 
 * @author georg beier
 * 
 */
public interface TraceSender {

	/**
	 * The constructor send a trace record
	 * 
	 * @param record
	 *            a single trace record
	 */
	void sendTrace(TraceRecord record);
}