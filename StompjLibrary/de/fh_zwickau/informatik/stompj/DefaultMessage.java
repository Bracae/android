/**
 *    Copyright 2011 prashant, Michał Janiszewski, Georg Beier and others

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


 * This source code is based on work of prashant ??? at google code, published under 
 * an Apache License 2.0. The original package path was pk.aamir.stompj.* . Code was
 * decompiled from class files using Jad v1.5.8g by Michał Janiszewski and published on
 * http://www.gitorious.org/pai-market/stompj.
 *  To keep the work available, the base package path was moved to an existing url.
 * 
 * Adapted to work with Apache ActiveMQ temporary queues, added comments and use 
 * template classes
 * 
 */

package de.fh_zwickau.informatik.stompj;

import de.fh_zwickau.informatik.stompj.internal.MessageImpl;

// Referenced classes of package de.fh_zwickau.informatik.stompj:
//            StompMessage

/**
 * Default imlementation of StompMessage interface
 * 
 */
public class DefaultMessage extends MessageImpl {

	private static final long serialVersionUID = 258401280266945504L;

	/**
	 * new empty message
	 */
	public DefaultMessage() {
	}

	/**
	 * new string message
	 * 
	 * @param content
	 *            string content
	 */
	public DefaultMessage(final String content) {
		this.setContent(content);
	}

	/**
	 * new arbitrary message
	 * 
	 * @param content
	 *            arbitrary content coded as byte[]
	 */
	public DefaultMessage(final byte content[]) {
		this.setContent(content);
	}

}
