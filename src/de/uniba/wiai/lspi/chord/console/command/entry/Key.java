/***************************************************************************
 *                                                                         *
 *                                 Key.java                                *
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

/**
 * Represents a key used within 
 * {@link de.uniba.wiai.lspi.chord.console.Main console} to 
 * store a {@link Value} within a chord network. 
 * 
 * @author  sven
 * @version 1.0.5
 */
public final class Key implements de.uniba.wiai.lspi.chord.service.Key {

	/**
	 * 
	 */
	private String key;

	/** 
	 * Creates a new instance of Key 
	 * @param key1 
	 */
	public Key(String key1) {
		this.key = key1;
	}

	/**
	 * Returns the byte for this key which is then used to calculate a unique ID
	 * for storage in the chord network.
	 * 
	 * @return Byte representation of the key.
	 */
	public byte[] getBytes() {
		return this.key.getBytes();
	}

	public String toString() {
		return "[Key: " + this.key + "]";
	}

	public boolean equals(Object o) {
		if (o instanceof Key) {
			return (this.key.equals(((Key) o).key));
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.key.hashCode();
	}
}
