/***************************************************************************
 *                                                                         *
 *                               Message.java                              *
 *                            -------------------                          *
 *   date                 : 01.09.2004, 17:54                              *
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
 * Message.java
 *
 * Created on 1. September 2004, 17:54
 */

package de.uniba.wiai.lspi.chord.com.socket;

import java.io.Serializable;

/**
 * This class represents a message sent over socket protocol supported 
 * by {@link SocketEndpoint} and {@link SocketProxy}. 
 * 
 * @author sven
 * @version 1.0.5 
 */
abstract class Message implements Serializable {

	/**
	 * Time stamp of this message.
	 */
	private final long timeStamp;

	// private final ID sender;

	/**
	 * Constructs a message with time stamp of current system time. 
	 */
	protected Message() {
		this.timeStamp = System.currentTimeMillis();
	}

	/**
	 * @return Returns the timeStamp.
	 */
	public final long getTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * Overwritten from {@link java.lang.Object}. 
	 * @return String representation of this. 
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("[Message@");
		buffer.append(this.hashCode());
		buffer.append(" from time ");
		buffer.append(this.timeStamp);
		buffer.append("]");
		return buffer.toString();
	}
}
