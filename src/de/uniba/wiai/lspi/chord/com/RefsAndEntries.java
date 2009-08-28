/***************************************************************************
 *                                                                         *
 *                            RefsAndEntries.java                          *
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
/*
 * Created on 28.02.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.uniba.wiai.lspi.chord.com;

import java.util.List;
import java.util.Set;

/**
 * @author karsten
 * @version 1.0.5
 */
public final class RefsAndEntries implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2144146590744444954L;

	/**
	 * List containing the predecessor (first in this list) and successors of a
	 * node.
	 */
	private List<Node> refs; // list of Node

	/**
	 * The entries a node is responsible for.
	 */
	private Set<Entry> entries; // set of Entry

	/**
	 * @param refs1
	 * @param entries1
	 */
	public RefsAndEntries(List<Node> refs1, Set<Entry> entries1) {
		this.refs = refs1;
		this.entries = entries1;
	}

	/**
	 * @return Returns the entries.
	 */
	public Set<Entry> getEntries() {
		return this.entries;
	}

	/**
	 * Returns references to the nodes contained within this instance.
	 * @return List containing the predecessor (first in this list) and successors of a
	 * node.
	 * 
	 */
	public List<Node> getRefs() {
		return this.refs;
	}
}
