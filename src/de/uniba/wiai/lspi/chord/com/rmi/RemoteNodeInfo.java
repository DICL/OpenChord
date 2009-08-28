/***************************************************************************
 *                                                                         *
 *                           RemoteNodeInfo.java                           *
 *                            -------------------                          *
 *   date                 : 22.02.2008, 16:52:26                           *
 *   copyright            : (C) 2008 Distributed and                       *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
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
package de.uniba.wiai.lspi.chord.com.rmi;

import java.io.Serializable;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;

/**
 * 
 * @author sven
 * @version 1.0.5
 * 
 */
final class RemoteNodeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 105L;

	/**
	 * The reference to the remote node represented by this. 
	 */
	RemoteNode remoteNode; 
	
	/**
	 * The ID of the remote node represented by this. 
	 */
	ID nodeID; 
	
	/**
	 * The URL of the remote node represented by this. 
	 */
	URL url;
	
	/**
	 * 
	 * @param rNode The reference to the remote node represented by this.
	 * @param nodeID The ID of the remote node represented by this.
	 * @param url The URL of the remote node represented by this.
	 */
	RemoteNodeInfo(RemoteNode rNode, ID nodeID, URL url) {
		if (rNode == null) {
			throw new IllegalArgumentException("Reference to remote node must not be null!"); 
		}
		if (nodeID == null) {
			throw new IllegalArgumentException("ID of remote node must not be null!"); 
		}
		if (url == null) {
			throw new IllegalArgumentException("URL of remote node must not be null!"); 
		}
		this.remoteNode = rNode; 
		this.nodeID = nodeID;
		this.url = url; 
	}

	/**
	 * @return The reference to the remote node represented by this.
	 */
	final RemoteNode getRemoteNode() {
		return remoteNode;
	}

	/**
	 * @return The URL of the remote node represented by this.
	 */
	final URL getUrl() {
		return url;
	}

	/**
	 * @return The ID of the remote node represented by this.
	 */
	final ID getNodeID() {
		return this.nodeID;
	} 
	
}
