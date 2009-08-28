/***************************************************************************
 *                                                                         *
 *                                 Key.java                                *
 *                            -------------------                          *
 *   date                 : 15.08.2004                                     *
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
package de.uniba.wiai.lspi.chord.service;

/**
 * Key under which an object is stored in the chord network. This may either be
 * a unique identifier if the object to be stored is unique (e.g. for white
 * pages) or a known keyword or metadata information under which the object
 * should be retrieved together with others (e.g. for yellow pages).
 * 
 * Note that this key is different to the Chord ID, since the ID is calculated
 * by applying a hash function on this key. Thus, this key may return an
 * arbitrary long byte array with uniquely identifies the object to be stored.
 * 
 * @author Sven Kaffille, Karsten Loesing
 * @version 1.0.5
 */
public interface Key {

	/**
	 * Returns the byte for this key which is then used to calculate a unique ID
	 * for storage in the chord network.
	 * 
	 * @return Byte representation of the key.
	 */
	public abstract byte[] getBytes();

}