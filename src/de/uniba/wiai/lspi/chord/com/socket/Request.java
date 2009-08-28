/***************************************************************************
 *                                                                         *
 *                               Request.java                              *
 *                            -------------------                          *
 *   date                 : 02.09.2004, 13:48                              *
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

package de.uniba.wiai.lspi.chord.com.socket;

import java.io.Serializable;

import de.uniba.wiai.lspi.chord.service.Chord;

/**
 * <p>
 * This class represents a request for the invocation of a method on 
 * a {@link Chord node}. <code>Request</code>s are sent by a 
 * {@link SocketProxy} to the {@link SocketEndpoint} of the node the 
 * {@link SocketProxy} represents. 
 * </p>
 * <p>
 * Results of a method invocation are sent back to the {@link SocketProxy} 
 * by {@link SocketEndpoint} with help of a {@link Response} message. 
 * with help of 
 * </p>
 * 
 * @author sven
 * @version 1.0.5
 */
final class Request extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1295124240351172262L;

	/**
	 * The type of this request. One of the method identifiers from
	 * {@link MethodConstants}.
	 */
	private int type;

	/**
	 * The parameters for the request. Must match the parameters for the method identified by   {@link #type}   in types and order.
	 */
	private Serializable[] parameters = null;

	/**
	 * Identifier used to identify this request. This identifier must be the value of the   {@link Response#getInReplyTo()}   field of a   {@link Response}   send for this request.
	 */
	private String replyWith;

	/**
	 * Creates a new instance of Request
	 * 
	 * @param type1
	 *            The type of this request. One of the method identifiers from
	 *            {@link MethodConstants}.
	 * @param replyWith1
	 *            Identifier used to identify this request. This identifier must
	 *            be the value of the {@link Response#getInReplyTo()} field of a
	 *            {@link Response} send for this request.
	 */
	protected Request(int type1, String replyWith1) {
		super();
		this.type = type1;
		this.replyWith = replyWith1;
	}

	/**
	 * Get the type of this request.
	 * 
	 * @return The type of this request. One of the method identifiers from
	 *         {@link MethodConstants}.
	 */
	int getRequestType() {
		return this.type;
	}

	/**
	 * Set the parameters for this request.
	 * 
	 * @param parameters1
	 *            The parameters for the request. Must match the parameters for
	 *            the method identified by {@link #type} in types and order.
	 */
	void setParameters(Serializable[] parameters1) {
		this.parameters = parameters1;
	}

	/**
	 * Get the parameters that shall be passed to the method that is requested
	 * by this.
	 * 
	 * @return The parameters for the request. Must match the parameters for the
	 *         method identified by {@link #type} in types and order.
	 */
	Serializable[] getParameters() {
		return this.parameters;
	}

	/**
	 * Get the value of the identifier for this request.
	 * 
	 * @return Identifier used to identify this request. This identifier must be
	 *         the value of the {@link Response#getInReplyTo()} field of a
	 *         {@link Response} send for this request.
	 */
	String getReplyWith() {
		return this.replyWith;
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
