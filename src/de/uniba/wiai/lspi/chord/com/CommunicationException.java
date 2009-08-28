/***************************************************************************
 *                                                                         *
 *                        CommunicationException.java                      *
 *                            -------------------                          *
 *   date                 : 16.08.2004                                     *
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

package de.uniba.wiai.lspi.chord.com;

/**
 * @author sven
 * @version 1.0.5
 * 
 */
public class CommunicationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7088464144284000312L;

	/**
	 * 
	 */
	public CommunicationException() {
		super();
	}

	/**
	 * @param message A message describing this exception. 
	 */
	public CommunicationException(String message) {
		super(message);
	}

	/**
	 * @param cause The Throwable that caused this Exception. 
	 */
	public CommunicationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message A message describing this exception. 
	 * @param cause The Throwable that caused this Exception. 
	 */
	public CommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

	// änderung

}
