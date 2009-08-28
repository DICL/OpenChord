/***************************************************************************
 *                                                                         *
 *                               Response.java                             *
 *                            -------------------                          *
 *   date                 : 01.09.2004, 18:07                              *
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

/**
 * @author sven
 * @version 1.0.5
 */
final class Response extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3635544762985447437L;

	/**
	 * Constant holding the value that indicates that the {@link Request} that
	 * caused this response has been executed successfully.
	 */
	public static final int REQUEST_SUCCESSFUL = 1;

	/**
	 * Constant holding the value that indicates that the {@link Request} that
	 * caused this response failed.
	 */
	public static final int REQUEST_FAILED = 0;

	/**
	 * A String describing the failure if this is a failure response.
	 * 
	 */
	private String failureReason;

	/**
	 * The result of the invocation if successful and the invocation has a
	 * result.
	 * 
	 */
	private Serializable result;

	/**
	 * The method to invoke. Must be one of the constants defined in
	 * {@link MethodConstants} .
	 * 
	 */
	private int methodIdentifier = -1;

	/**
	 * Status of the request {@link #REQUEST_FAILED} or {@link #REQUEST_SUCCESSFUL}.
	 */
	private int status = REQUEST_SUCCESSFUL;

	/**
	 * String defining the request that this is the response for.
	 */
	private String inReplyTo;

	/**
	 * If this is a failure response and the failure has been caused by any
	 * {@link Throwable} this can be set to the <code>Throwable</code>.
	 */
	private Throwable throwable = null;

	/**
	 * Creates a new instance of Response
	 * 
	 * @param status1
	 * @param methodIdentifier1
	 * @param inReplyTo1
	 */
	Response(int status1, int methodIdentifier1, String inReplyTo1) {
		super();
		this.status = status1;
		this.methodIdentifier = methodIdentifier1;
		this.inReplyTo = inReplyTo1;
	}

	/**
	 * @return The identifier of the method that was requested by the request,
	 *         for which this is the response. See {@link MethodConstants}. 
	 */
	int getMethodIdentifier() {
		return this.methodIdentifier;
	}

	/**
	 * @return Integer representing the state of this response. See
	 *         {@link Response#REQUEST_FAILED},
	 *         {@link Response#REQUEST_SUCCESSFUL}.
	 */
	int getStatus() {
		return this.status;
	}

	/**
	 * @return <code>true</code> if the request, for which this is a response,
	 *         caused a failure on the remote node.
	 */
	boolean isFailureResponse() {
		return (this.status == REQUEST_FAILED);
	}

	/**
	 * If this a failure reponse, this method returns the Throwable that caused
	 * the failure. Otherwise <code>null</code>.
	 * 
	 * @return If this a failure reponse, this method returns the Throwable that
	 *         caused the failure. Otherwise <code>null</code>.
	 */
	Throwable getThrowable() {
		return this.throwable;
	}

	/**
	 * @return The reason for failure of the request, for which this is the
	 *         response.
	 */
	String getFailureReason() {
		return this.failureReason;
	}

	/**
	 * @param t
	 *            The throwable to set.
	 */
	void setThrowable(Throwable t) {
		this.throwable = t;
	}

	/**
	 * @param reason
	 */
	void setFailureReason(String reason) {
		this.status = REQUEST_FAILED;
		this.failureReason = reason;
	}

	/**
	 * @return The result of the request for which this is the response.
	 */
	Serializable getResult() {
		return this.result;
	}

	/**
	 * @param result1
	 */
	void setResult(Serializable result1) {
		this.result = result1;
	}

	/**
	 * @return String that identifies the request for that this is the response.
	 */
	String getInReplyTo() {
		return this.inReplyTo;
	}

}
