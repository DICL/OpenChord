/***************************************************************************
 *                                                                         *
 *                                Proxy.java                               *
 *                            -------------------                          *
 *   date                 : 12.08.2004                                     *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *                          karsten.loesing@uni-bamberg.de                 *
 *                                                                         *
 *                                                                         *
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   A copy of the license can be found in the license.txt file supplied   *
 *   with this software or at: http://www.gnu.org/copyleft/gpl.html        *
 *                                                                         *
 ***************************************************************************/
/*
 * Created on 12.08.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.uniba.wiai.lspi.chord.com;

import static de.uniba.wiai.lspi.util.logging.Logger.LogLevel.DEBUG;
import de.uniba.wiai.lspi.chord.com.local.ThreadProxy;
import de.uniba.wiai.lspi.chord.com.rmi.RMIProxy;
import de.uniba.wiai.lspi.chord.com.socket.SocketProxy;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.util.logging.Logger;

/**
 * This class is used to represent other
 * {@link de.uniba.wiai.lspi.chord.service.Chord nodes} at a
 * {@link de.uniba.wiai.lspi.chord.service.Chord node}, so that these nodes are
 * able to connect to the node. A Proxy should establish a connection to the
 * {@link Endpoint} of the node that is represented by this proxy. So all
 * protocol specific implementation for connections between nodes must be
 * realized in an pair of {@link Endpoint} and {@link Proxy}.
 * 
 * This class has to be extended by all Proxies that are used to provide a
 * connection to a remote node via the {@link Node} interface.
 * 
 * @author sven
 * @version 1.0.5
 */
public abstract class Proxy extends Node {

	/**
	 * The logger for instances of this class.
	 */
	private final static Logger logger = Logger
			.getLogger(Proxy.class.getName());

	/**
	 * 
	 * @param url
	 */
	protected Proxy(URL url) {
		if (url == null) {
			throw new IllegalArgumentException("URL must not be null!");
		}
		this.nodeURL = url;
		logger.info("Proxy with url " + url + " initialised.");
	}

	/**
	 * Factory method to create a proxy to connect to the given {@link URL}.
	 * The protocol of url is used to determine the type of the proxy to create.
	 * The protocol of url must be a known protocol.
	 * 
	 * @param sourceUrl
	 *            {@link URL} of the local node, that wants to establish the
	 *            connection.
	 * @param destinationUrl
	 *            {@link URL} of the remote endpoint.
	 * @return Proxy to make invocations on a {@link Node} remote node.
	 * @throws CommunicationException
	 */
	public static Node createConnection(URL sourceUrl, URL destinationUrl)
			throws CommunicationException {

		if (sourceUrl == null || destinationUrl == null) {
			throw new NullPointerException("URL must not be null!");
		}

		if (sourceUrl.equals(destinationUrl)) {
			logger.fatal("URLs are equal: this url= " + sourceUrl.toString()
					+ ", the other url= " + destinationUrl.toString());
			throw new IllegalArgumentException("URLs must not be equal!");
		}

		boolean debug = logger.isEnabledFor(DEBUG);
		if (debug) {
			logger.debug("Trying to create Proxy for connection to "
					+ destinationUrl);
		}
		String protocol = destinationUrl.getProtocol();
		Node node = null;
		if (protocol.equals(URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL))) {
			node = SocketProxy.create(sourceUrl, destinationUrl);
			if (debug) {
				logger.debug("SocketProxy " + node + " created.");
			}
		} else if (protocol.equals(URL.KNOWN_PROTOCOLS.get(URL.LOCAL_PROTOCOL))) {
			node = new ThreadProxy(sourceUrl, destinationUrl);
			if (debug) {
				logger.debug("ThreadProxy " + node + " created.");
			}
		} else if (protocol.equals(URL.KNOWN_PROTOCOLS.get(URL.RMI_PROTOCOL))) {
			node = RMIProxy.create(sourceUrl, destinationUrl); 
			if (debug) {
				logger.debug("RMIProxy " + node + " created.");
			}
		} else {
			// does not happen - if it does, abort
			throw new RuntimeException(
					"This should not happen! Unknown Protocol " + protocol);
		}
		return node;
	}

}
