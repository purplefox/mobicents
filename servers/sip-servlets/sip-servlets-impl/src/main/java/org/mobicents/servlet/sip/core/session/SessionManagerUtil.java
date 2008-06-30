/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.core.session;

import java.text.ParseException;

import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Message;

/**
 * This class is used as a central place to get a session be it a sip session
 * or an sip application session. 
 * Here are the semantics of the key used for storing each kind of session :
 * <ul>
 * <li>sip session id = (FROM-ADDR,FROM-TAG,TO-ADDR,CALL-ID,APPNAME)</li>
 * <li>sip app session id = (CALL-ID,APPNAME)<li>
 * </ul>
 * It is not possible to create a sip session or an application session directly,
 * this class should be used to get a session whatever its type. If the session 
 * already exsits it will be returned otherwise it will be created. 
 * One should be expected to remove the sessions from this manager through the
 * remove methods when the sessions are no longer used.
 */
public class SessionManagerUtil {

	public final static String TAG_PARAMETER_NAME = "tag";
	
	/**
	 * Computes the sip session key from the input parameters. The sip session
	 * key will be of the form (FROM-ADDR,FROM-TAG,TO-ADDR,CALL-ID,APPNAME)
	 * @param applicationName the name of the application that will be the fifth component of the key
	 * @param message the message to get the 4 components of the key from 
	 * @param inverted TODO
	 * @return the computed key 
	 * @throws NullPointerException if application name is null
	 */
	public static SipSessionKey getSipSessionKey(final String applicationName, final Message message, boolean inverted) {		
		if(applicationName == null) {
			throw new NullPointerException("the application name cannot be null for sip session key creation");
		}
		if(inverted) {
			return new SipSessionKey(
					((ToHeader) message.getHeader(ToHeader.NAME)).getAddress().getURI().toString(),
					((ToHeader) message.getHeader(ToHeader.NAME)).getParameter(TAG_PARAMETER_NAME),
					((FromHeader) message.getHeader(FromHeader.NAME)).getAddress().getURI().toString(),
					((FromHeader) message.getHeader(FromHeader.NAME)).getParameter(TAG_PARAMETER_NAME),
					((CallIdHeader) message.getHeader(CallIdHeader.NAME)).getCallId(),
					applicationName);
		} else {
			return new SipSessionKey(
				((FromHeader) message.getHeader(FromHeader.NAME)).getAddress().getURI().toString(),
				((FromHeader) message.getHeader(FromHeader.NAME)).getParameter(TAG_PARAMETER_NAME),
				((ToHeader) message.getHeader(ToHeader.NAME)).getAddress().getURI().toString(),
				((ToHeader) message.getHeader(ToHeader.NAME)).getParameter(TAG_PARAMETER_NAME),
				((CallIdHeader) message.getHeader(CallIdHeader.NAME)).getCallId(),
				applicationName);
		}
	}
	
	/**
	 * Computes the sip application session key from the input parameters. 
	 * The sip application session key will be of the form (CALL-ID,APPNAME)
	 * @param applicationName the name of the application that will be the second component of the key
	 * @param id the Id composing the first component of the key
	 * @param isAppGeneratedKey if the id has been generated by the application through a @SipApplicationKey annotated method 
	 * @return the computed key 
	 * @throws NullPointerException if one of the two parameters is null
	 */
	public static SipApplicationSessionKey getSipApplicationSessionKey(final String applicationName, final String id, final boolean isAppGeneratedKey) {
		if(applicationName == null) {
			throw new NullPointerException("the application name cannot be null for sip application session key creation");
		}
		if(id == null) {
			throw new NullPointerException("the callId cannot be null for sip application session key creation");
		}
		return new SipApplicationSessionKey(
				id,
				applicationName,
				isAppGeneratedKey);		
	}
	
	/**
	 * Parse a sip application key that was previously generated and put as an http request param
	 * through the encodeURL method of SipApplicationSession
	 * @param sipApplicationKey the stringified version of the sip application key
	 * @return the corresponding sip application session key
	 * @throws ParseException if the stringfied key cannot be parse to a valid key
	 */
	public static SipApplicationSessionKey parseSipApplicationSessionKey(
			String sipApplicationKey) throws ParseException {
		
		int indexOfLeftParenthesis = sipApplicationKey.indexOf("(");
		int indexOfComma = sipApplicationKey.indexOf(",");
		int indexOfRightParenthesis = sipApplicationKey.indexOf(")");
		if(indexOfLeftParenthesis == -1) {
			throw new ParseException("The left parenthesis could not be found in the following key " + sipApplicationKey, 0);
		}
		if(indexOfComma == -1) {
			throw new ParseException("The comma could not be found in the following key " + sipApplicationKey, 0);
		}
		if(indexOfRightParenthesis == -1) {
			throw new ParseException("The right parenthesis could not be found in the following key " + sipApplicationKey, 0);
		}
		
		String callId = sipApplicationKey.substring(indexOfLeftParenthesis + 1, indexOfComma);
		String applicationName = sipApplicationKey.substring(indexOfComma + 1, indexOfRightParenthesis);
		
		return getSipApplicationSessionKey(applicationName, callId, false);			
	}
}
