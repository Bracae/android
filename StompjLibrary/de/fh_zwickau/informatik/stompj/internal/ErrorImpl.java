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

package de.fh_zwickau.informatik.stompj.internal;

import java.io.UnsupportedEncodingException;

import de.fh_zwickau.informatik.stompj.ErrorMessage;

public class ErrorImpl implements ErrorMessage {

	private static final long serialVersionUID = 4830480151537556036L;

	public ErrorImpl() {
	}

	@Override
	public byte[] getContentAsBytes() {
		return this.content;
	}

	@Override
	public String getContentAsString() {
		try {
			return new String(this.content, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void setContent(final byte content[]) {
		this.content = content;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	public void setMessage(final String msg) {
		this.message = msg;
	}

	private String message;
	private byte content[];
}
