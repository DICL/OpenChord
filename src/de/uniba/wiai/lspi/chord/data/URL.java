/***************************************************************************
 *                                                                         *
 *                                URL.java                                 *
 *                            -------------------                          *
 *   date                 : 16.08.2004                                     *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *   			    		karsten.loesing@uni-bamberg.de                 *
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
package de.uniba.wiai.lspi.chord.data;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Address of nodes.
 * 
 * Once created, a URL instance is unmodifiable.
 * 
 * @author Sven Kaffille, Karsten Loesing
 * @version 1.0.5
 */
public class URL implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8223277048826783692L;

	/**
	 * String representation of URL
	 */
	private transient String urlString;

	/**
	 * The protocol of this URL.
	 */
	private final String protocol;

	/**
	 * The host of this URL.
	 */
	private final String host;

	/**
	 * The port of this URL.
	 */
	private final int port;

	/**
	 * The path for this URL.
	 */
	private final String path;

	/**
	 * The names of the protocols known to this chord implementation. The name
	 * for each protocol can be referenced with help of the constants for the
	 * protocoal e.g. <code>SOCKET_PROTOCOL</code>.
	 */
	public final static List<String> KNOWN_PROTOCOLS = java.util.Collections
			.unmodifiableList(java.util.Arrays.asList(new String[] {
					"ocsocket", "oclocal", "ocrmi" }));

	/**
	 * Array containing default ports for all known protocols. The port for each
	 * protocol can be referenced with help of the constants for the protocoal
	 * e.g. <code>SOCKET_PROTOCOL</code>.
	 */
	private final static int[] DEFAULT_PORTS = new int[] { 4242, -1, 4242 };

	/**
	 * Index of socket protocol in <code>{@link #KNOWN_PROTOCOLS}</code>.
	 */
	public final static int SOCKET_PROTOCOL = 0;

	/**
	 * Index of thread protocol (for local chord network ) in
	 * <code>{@link #KNOWN_PROTOCOLS}</code>.
	 */
	public final static int LOCAL_PROTOCOL = 1;
	
	/**
	 * Index of socket protocol in <code>{@link #KNOWN_PROTOCOLS}</code>.
	 */
	public final static int RMI_PROTOCOL = 2;

	/**
	 * Constant for URL parsing.
	 */
	private final static String DCOLON = ":";

	/**
	 * Constant for URL parsing.
	 */
	private final static String SLASH = "/";

	/**
	 * Constant for URL parsing.
	 */
	private final static String DCOLON_SLASHES = DCOLON + SLASH + SLASH;

	/**
	 * Create an instance of URL from <code>urlString</code>.
	 * 
	 * @param urlString
	 *            The string to create an URL from.
	 * @throws MalformedURLException
	 *             This can occur if <code>urlString</code> does not match the
	 *             pattern <code>protocol://host[:port]/path</code>, an
	 *             unknown protocol is specified, or port is negative.
	 * 
	 */
	public URL(String urlString) throws MalformedURLException {

		// store textual representation of URL
		this.urlString = urlString;

		// parse protocol
		int indexOfColonAndTwoSlashes = urlString.indexOf(DCOLON_SLASHES);
		if (indexOfColonAndTwoSlashes < 0) {
			throw new MalformedURLException("Not a valid URL");
		}
		this.protocol = urlString.substring(0, indexOfColonAndTwoSlashes);
		urlString = urlString.substring(indexOfColonAndTwoSlashes + 3);

		// parse host and port
		int endOfHost = urlString.indexOf(DCOLON);
		if (endOfHost >= 0) {
			this.host = urlString.substring(0, endOfHost);
			urlString = urlString.substring(endOfHost + 1);
			int endOfPort = urlString.indexOf(SLASH);
			if (endOfPort < 0) {
				throw new MalformedURLException("Not a valid URL!");
			}
			/* initialise port */
			int tmp_port = Integer.parseInt(urlString.substring(0, endOfPort));
			/* port must not be negative */
			if ((tmp_port <= 0) || (tmp_port >= 65536)) {
				throw new MalformedURLException("Not a valid URL! "
						+ "Port must be between 0 and 65536!");
			}
			this.port = tmp_port;
			urlString = urlString.substring(endOfPort + 1);
		} else {
			endOfHost = urlString.indexOf(SLASH);
			if (endOfHost < 0) {
				throw new MalformedURLException("Not a valid URL");
			}
			this.host = urlString.substring(0, endOfHost);
			urlString = urlString.substring(endOfHost + 1);
			if (this.protocol
					.equalsIgnoreCase(KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL))) {
				this.port = URL.DEFAULT_PORTS[URL.SOCKET_PROTOCOL];
			} else if (this.protocol
					.equalsIgnoreCase(KNOWN_PROTOCOLS.get(URL.RMI_PROTOCOL))) {
				this.port = URL.DEFAULT_PORTS[URL.RMI_PROTOCOL];
			} else {
				this.port = URL.DEFAULT_PORTS[URL.LOCAL_PROTOCOL];
			}
		}

		// parse path
		this.path = urlString;

		// check if protocol is known
		boolean protocolIsKnown = false;
		for (int i = 0; i < KNOWN_PROTOCOLS.size() && !protocolIsKnown; i++) {
			if (this.protocol.equals(KNOWN_PROTOCOLS.get(i))) {
				protocolIsKnown = true;
			}
		}
		if (!protocolIsKnown) {
			throw new MalformedURLException("Protocol is not known! "
					+ this.protocol);
		}

	}

	/**
	 * Get the protocol of this URL.
	 * 
	 * @return The protocol of this URL.
	 */
	public final String getProtocol() {
		return this.protocol;
	}

	/**
	 * Get the host name contained in this URL.
	 * 
	 * @return Host name contained in this URL.
	 */
	public final String getHost() {
		return this.host;
	}

	/**
	 * Get the path contained in this URL.
	 * 
	 * @return The path contained in this URL.
	 */
	public final String getPath() {
		return this.path;
	}

	/**
	 * Get the port contained in this URL.
	 * 
	 * @return The port of this URL. Has value <code>NO_PORT</code> if no port
	 *         has been specified for this URL.
	 */
	public final int getPort() {
		return this.port;
	}

	/** ******************************************************* */
	/* START: Overwritten methods from java.lang.Object */
	/** ******************************************************* */

	/**
	 * Overwritten from {@link java.lang.Object}.
	 * 
	 * @return Hash code of this URL.
	 */
	public final int hashCode() {
		int hash = 17;
		hash += 37 * this.protocol.hashCode();
		hash += 37 * this.host.hashCode();
		hash += 37 * this.path.hashCode();
		hash += 37 * this.port;
		return hash;
	}

	/**
	 * Overwritten from {@link java.lang.Object}.
	 * 
	 * @param obj
	 * @return <code>true</code> if provided <code>obj</code> is an instance
	 *         of <code>URL</code> and has the same attributes as this
	 *         <code>URL</code>.
	 */
	public final boolean equals(Object obj) {
		if (obj instanceof URL) {
			URL url = (URL) obj;

			if (!url.getProtocol().equalsIgnoreCase(this.protocol)) {
				return false;
			}
			if (!url.getHost().equalsIgnoreCase(this.host)) {
				return false;
			}
			if (!(url.getPort() == this.port)) {
				return false;
			}
			if (!url.getPath().equals(this.path)) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Overwritten from {@link java.lang.Object}.
	 * 
	 * @return String representation of this URL.
	 */
	public final String toString() {
		if (this.urlString == null) {
			StringBuilder builder = new StringBuilder();
			builder.append(this.protocol);
			builder.append(DCOLON_SLASHES);
			builder.append(this.host);
			builder.append(DCOLON);
			builder.append(this.port);
			builder.append(SLASH);
			builder.append(this.path);
			this.urlString = builder.toString().toLowerCase();
		}
		return this.urlString;
	}

}