/*
 * AnyQuery.java
 *
 * Mobicents Media Gateway
 *
 * The source code contained in this file is in in the public domain.
 * It can be used in any project or product without prior permission,
 * license or royalty payments. There is  NO WARRANTY OF ANY KIND,
 * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, WITHOUT LIMITATION,
 * THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * AND DATA ACCURACY.  We do not warrant or make any representations
 * regarding the use of the software or the  results thereof, including
 * but not limited to the correctness, accuracy, reliability or
 * usefulness of the software.
 */

package org.mobicents.media.server.spi;

import java.util.ArrayList;
import java.util.Collection;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * 
 * @author Oleg Kulikov
 */
public class EndpointQuery {

	public static synchronized Endpoint findAny(String name) throws NamingException, ResourceUnavailableException {
		InitialContext ic = new InitialContext();

		Context ctx = (Context) ic.lookup(name);
		NamingEnumeration list = ctx.listBindings("");
		while (list.hasMore()) {
			Binding binding = (Binding) list.next();

			Object obj = binding.getObject();
			if (obj instanceof Endpoint) {
				Endpoint endpoint = (Endpoint) obj;
				if (!endpoint.hasConnections()) {
					return endpoint;
				}
			}
		}

		throw new ResourceUnavailableException("Endpoint unknown or in use[" + name + "]");
	}

	public static synchronized Collection findAll(String name) throws NamingException {
		InitialContext ic = new InitialContext();
		ArrayList endpoints = new ArrayList();

		Context ctx = (Context) ic.lookup(name);

		NamingEnumeration list = ctx.listBindings("");
		while (list.hasMore()) {
			Binding binding = (Binding) list.next();
			
			Endpoint endpoint = (Endpoint) binding.getObject();
			endpoints.add(endpoint);
		}

		return endpoints;
	}

	public static synchronized Endpoint find(String name) throws NamingException, ResourceUnavailableException {
		InitialContext ic = new InitialContext();
		Endpoint endpoint = (Endpoint) ic.lookup(name);
		return endpoint;
	}

	public static Endpoint lookup(String name) throws NamingException, ResourceUnavailableException {
		if (name.endsWith("/$")) {
			name = name.substring(0, name.indexOf("/$"));
			return findAny(name);
		} else
			return find(name);
	}
}

class EndpointName {

	private String contextName;
	private String name;

	public EndpointName(String fqn) {
		parse(fqn);
	}

	public String getContextName() {
		return contextName;
	}

	public String getName() {
		return name;
	}

	private void parse(String fqn) {
		int pos = 0;
		int idx = 0;
		while (idx != -1) {
			idx = fqn.indexOf("/", pos);
			if (idx != -1)
				pos = idx + 1;
		}

		contextName = fqn.substring(0, pos).trim();
		name = fqn.substring(pos).trim();
	}
}