/***************************************************************************
 *                                                                         *
 *                         RemoteRefsAndEntries.java                       *
 *                            -------------------                          *
 *   date                 : 20.04.2005                                     *
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

import java.util.List;
import java.io.Serializable;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.Entry;

/**
 * This class represents entries and {@link RemoteNodeInfo references} that 
 * have to be transferred between two nodes.  
 * 
 * @author sven
 * @version 1.0.5
 */
final class RemoteRefsAndEntries implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4409136500599950164L;

	/**
	 * Set of {@link Entry}.
	 * 
	 */
	protected Set<Entry> entries;

	/**
	 * List of {@link RemoteNodeInfo}.
	 * 
	 */
	protected List<RemoteNodeInfo> nodeInfos;

	/**
	 * @param entries1
	 * @param nodeInfos1
	 */
	protected RemoteRefsAndEntries(Set<Entry> entries1,
			List<RemoteNodeInfo> nodeInfos1) {
		this.entries = entries1;
		this.nodeInfos = nodeInfos1;
	}

	/**
	 * @return Returns the entries.
	 */
	protected Set<Entry> getEntries() {
		return this.entries;
	}

	/**
	 * @param entries1
	 *            The entries to set.
	 */
	protected void setEntries(Set<Entry> entries1) {
		this.entries = entries1;
	}

	/**
	 * @return Returns the nodeInfos.
	 */
	protected List<RemoteNodeInfo> getNodeInfos() {
		return this.nodeInfos;
	}

	/**
	 * @param nodeInfos1
	 *            The nodeInfos to set.
	 */
	protected void setNodeInfos(List<RemoteNodeInfo> nodeInfos1) {
		this.nodeInfos = nodeInfos1;
	}
}
