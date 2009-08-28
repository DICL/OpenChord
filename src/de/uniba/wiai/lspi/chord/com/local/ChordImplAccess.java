/***************************************************************************
 *                                                                         *
 *                           ChordImplAccess.java                          *
 *                            -------------------                          *
 *   date                 : 13.12.2005                                     *
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
package de.uniba.wiai.lspi.chord.com.local;

import java.lang.reflect.Field;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import de.uniba.wiai.lspi.chord.service.impl.NodeImpl;

/**
 * @author sven
 * @version 1.0.5
 * 
 */
public class ChordImplAccess {

	/**
	 * Private constructor, so that no instances of this can be created by other
	 * classes.
	 * 
	 */
	private ChordImplAccess() {
		/*
		 * Nothing to do here, as there are no instance variables.
		 */
	}

	/**
	 * @param n
	 * @return Reference to an instance of {@link ChordImpl}.
	 */
	@SuppressWarnings("null")
	public static ChordImpl fetchChordImplOfNode(Node n) {
		NodeImpl node = null;
		try {
			node = (NodeImpl) n;
		} catch (ClassCastException e) {
			handleException(e); 
		}
		Field chordImplField = null;
		try {
			chordImplField = node.getClass().getDeclaredField("impl");
		} catch (SecurityException e) {
			handleException(e); 
		} catch (NoSuchFieldException e) {
			handleException(e); 
		}
		chordImplField.setAccessible(true);
		ChordImpl impl = null;
		try {
			impl = (ChordImpl) chordImplField.get(node);
		} catch (IllegalArgumentException e) {
			handleException(e); 
		} catch (IllegalAccessException e) {
			handleException(e); 
		}
		return impl;
	}

	/**
	 * @param e
	 */
	private static void handleException(Exception e) {
		System.err.println("This should not happen! This indicates that "
				+ "the implementation " + "of service layer has changed. "
				+ "This code relies heavily on internal structure of service "
				+ "layer, as it was not possible to solve some problem "
				+ "with local communication by other means!");
		System.err.println("If this happens check if you can adapt this "
				+ "code our contact one of the developers!");
		e.printStackTrace();
		throw new RuntimeException(e);
	}

}
