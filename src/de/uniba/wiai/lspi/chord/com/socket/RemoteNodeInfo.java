/***************************************************************************
 *                                                                         *
 *                           RemoteNodeInfo.java                           *
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
/*
 * Created on 20.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.uniba.wiai.lspi.chord.com.socket;

import java.io.Serializable;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;

/** 
 * This class represents information about a remote node 
 * that can be used to construct a {@link SocketProxy}. This class 
 * is sent over the network by {@link SocketEndpoint endpoints} 
 * instead of sending complete {@link SocketProxy proxies}. 
 * The receiver has to construct {@link SocketProxy proxies} from the 
 * information contained within this class. 
 * 
 * @author sven
 * @version 1.0.5
 */
final class RemoteNodeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2812912784369964792L;

	/**
	 * The {@link URL} of the node that this represents. 
	 */
	protected URL nodeURL;

	/**
	 * The {@link ID} of the node that this represents. 
	 */
	protected ID nodeID;

	/**
	 * Constructs an object containing information about a node. 
	 * @param nodeURL1
	 * @param nodeID1
	 */
	protected RemoteNodeInfo(URL nodeURL1, ID nodeID1) {
		this.nodeURL = nodeURL1;
		this.nodeID = nodeID1;
	}

	/**
	 * @return Returns the nodeID.
	 */
	protected ID getNodeID() {
		return this.nodeID;
	}

	/**
	 * @param nodeID1
	 *            The nodeID to set.
	 */
	protected void setNodeID(ID nodeID1) {
		this.nodeID = nodeID1;
	}

	/**
	 * @return Returns the nodeURL.
	 */
	protected URL getNodeURL() {
		return this.nodeURL;
	}

	/**
	 * @param nodeURL1
	 *            The nodeURL to set.
	 */
	protected void setNodeURL(URL nodeURL1) {
		this.nodeURL = nodeURL1;
	}
}
