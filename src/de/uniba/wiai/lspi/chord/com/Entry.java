/***************************************************************************
 *                                                                         *
 *                                Entry.java                               *
 *                            -------------------                          *
 *   date                 : 28.02.2005                                     *
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

import java.io.Serializable;

import de.uniba.wiai.lspi.chord.data.ID;

/**
 * @author karsten
 * @version 1.0.5
 */
public final class Entry implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3473253817147038992L;

	/**
	 * The id of this entry. 
	 */
	private ID id;

	/**
	 * The stored value. 
	 * 
	 */
	private Serializable value;

	/**
	 * @param id1
	 * @param value1
	 */
	public Entry(ID id1, Serializable value1) {
		this.id = id1;
		this.value = value1;
	}

	/**
	 * @return Returns the id.
	 */
	public ID getId() {
		return this.id;
	}

	/**
	 * @return Returns the value.
	 */
	public Serializable getValue() {
		return this.value;
	}

	public String toString() {
		return "( key = " + this.id.toString() + ", value = " + this.value
				+ ")";
	}

	public int hashCode() {
		int result = 17;
		result += 37 * this.id.hashCode();
		result += 37 * this.value.hashCode();
		return result;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Entry)) {
			return false;
		}
		Entry entry = (Entry) o;
		return (entry.id.equals(this.id) && entry.value.equals(this.value));
	}
}
