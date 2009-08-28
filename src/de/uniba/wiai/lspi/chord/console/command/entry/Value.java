/***************************************************************************
 *                                                                         *
 *                                Value.java                               *
 *                            -------------------                          *
 *   date                 : 12.10.2004, 11:06                              *
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

package de.uniba.wiai.lspi.chord.console.command.entry;

import java.io.Serializable;

/**
 * A Value to be stored within a chord network by a command issued from 
 * {@link de.uniba.wiai.lspi.chord.console.Main console}. 
 * 
 * @author sven 
 * @version 1.0.5
 */
public final class Value implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2144157610883545352L;
	/**
	 * 
	 */
	private String value;

	/** 
	 * Creates a new instance of Value 
	 * @param value1 
	 */
	public Value(String value1) {
		this.value = value1;
	}

	public String toString() {
		return this.value;
	}

	public boolean equals(Object o) {
		if (o instanceof Value) {
			return (this.value.equals(((Value) o).value));
		}
		return false;
	}

	public int hashCode() {
		return this.value.hashCode();
	}

}
