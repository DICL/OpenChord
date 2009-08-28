/***************************************************************************
 *                                                                         *
 *                               Report.java                               *
 *                            -------------------                          *
 *   date                 : 15.08.2005                                     *
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
 * Provides the user application with methods for retrieving internal
 * information about the state of a Chord node, e.g. entries or references.
 * 
 * @author Karsten Loesing
 * @version 1.0.5
 */
public interface Report {

	/**
	 * Returns a formatted String containing all entries stored on this node.
	 * 
	 * @return Formatted String containing all entries stored on this node.
	 */
	public abstract String printEntries();

	/**
	 * Returns a formatted String containing all references stored in the finger
	 * table of this node.
	 * 
	 * @return Formatted String containing all references stored in the finger
	 *         table of this node.
	 */
	public abstract String printFingerTable();

	/**
	 * Returns a formatted String containing all references stored in the
	 * successor list of this node.
	 * 
	 * @return Formatted String containing all references stored in the
	 *         successor list of this node.
	 */
	public abstract String printSuccessorList();

	/**
	 * Returns a formatted String containing all references stored on this node.
	 * 
	 * @return Formatted String containing all references stored on this node.
	 */
	public abstract String printReferences();

	/**
	 * Returns a formatted String containing the predecessor reference of this
	 * node.
	 * 
	 * @return Formatted String containing the predecessor reference of this
	 *         node.
	 */
	public abstract String printPredecessor();
}
