/***************************************************************************
 *                                                                         *
 *                                Node.java                                *
 *                            -------------------                          *
 *   date                 : 16.08.2004                                     *
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

import java.util.List;
import java.util.Set;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;

/**
 * Provides methods which remote nodes can invoke.
 * 
 * @author Sven Kaffille, Karsten Loesing
 * @version 1.0.5
 */

/*
 * 21.03.2006 changed by sven. Node needs not to be Serializable as no instances
 * of it are supposed to being serialized.
 */
public abstract class Node {

	@Override
	public final boolean equals(Object arg0) {
		if (arg0 == null || !(arg0 instanceof Node)) {
			return false;
		}
		return ((Node) arg0).nodeID.equals(this.nodeID);
	}

	@Override
	public final int hashCode() {
		return this.nodeID.hashCode();
	}

	@Override
	public String toString() {
		String id = null; 
		if (this.nodeID != null) {
			id = this.nodeID.toString(); 
		} 
		String url = "null"; 
		if (this.nodeURL != null) {
			url = this.nodeURL.toString(); 
		} 
		return "Node[type=" + this.getClass().getSimpleName() + ", id="
				+ id + ", url=" + url + "]";
	}

	/**
	 * This is the id of this node. It has to be set by every implementation of
	 * this class!
	 */
	protected ID nodeID;

	/**
	 * This is the url of this node. It has to be set by every implementation of
	 * this class!
	 */
	protected URL nodeURL;

	/**
	 * Returns the ID of a node. Is invoked by remote nodes which do not know
	 * the ID of this node, yet. After invocation, the nodeID is remembered by
	 * the remote node, s.t. future invocations of getNodeID are unnecessary.
	 * 
	 * @return ID of a node.
	 * @throws CommunicationException
	 *             If something goes wrong when contacting the node.
	 */
	public final ID getNodeID() {
		return this.nodeID;
	}

	/**
	 * 
	 * @return
	 */
	public final URL getNodeURL() {
		return this.nodeURL;
	}

	/**
	 * Returns the Chord node which is responsible for the given key.
	 * 
	 * @param key
	 *            Key for which the successor is searched for.
	 * @return Responsible node.
	 * @throws CommunicationException
	 *             Thrown if an unresolvable communication failure occurs.
	 */
	public abstract Node findSuccessor(ID key) throws CommunicationException;

	/**
	 * Requests this node's predecessor in result[0] and successor list in
	 * result[1..length-1]. This method is invoked by another node which thinks
	 * it is this node's predecessor.
	 * 
	 * @param potentialPredecessor
	 * @return A list containing the predecessor at first position of the list
	 *         and the successors in the rest of the list.
	 * @throws CommunicationException
	 *             Thrown if an unresolvable communication failure occurs.
	 */
	public abstract List<Node> notify(Node potentialPredecessor)
			throws CommunicationException;

	/**
	 * Requests this node's predecessor, successor list and entries.
	 * 
	 * @param potentialPredecessor
	 *            Remote node which invokes this method
	 * @return References to predecessor and successors and the entries this
	 *         node will be responsible for.
	 * @throws CommunicationException
	 */
	public abstract RefsAndEntries notifyAndCopyEntries(
			Node potentialPredecessor) throws CommunicationException;

	/**
	 * Requests a sign of live. This method is invoked by another node which
	 * thinks it is this node's successor.
	 * 
	 * @throws CommunicationException
	 *             Thrown if an unresolvable communication failure occurs.
	 */
	public abstract void ping() throws CommunicationException;

	/**
	 * Stores the given object under the given ID.
	 * 
	 * @param entryToInsert
	 * @throws CommunicationException
	 *             Thrown if an unresolvable communication failure occurs.
	 */
	public abstract void insertEntry(Entry entryToInsert)
			throws CommunicationException;

	/**
	 * Inserts replicates of the given entries.
	 * 
	 * @param entries
	 *            The entries that are replicated.
	 * @throws CommunicationException
	 *             Thrown if an unresolvable communication failure occurs.
	 * 
	 */
	public abstract void insertReplicas(Set<Entry> entries)
			throws CommunicationException;

	/**
	 * Removes the given object from the list stored under the given ID.
	 * 
	 * @param entryToRemove
	 *            The entry to remove from the dht.
	 * @throws CommunicationException
	 *             Thrown if an unresolvable communication failure occurs.
	 */
	public abstract void removeEntry(Entry entryToRemove)
			throws CommunicationException;

	/**
	 * Removes replicates of the given entries.
	 * 
	 * @param sendingNode
	 *            ID of sending node; if entriesToRemove is empty, all replicas
	 *            with ID smaller than the sending node's ID are removed
	 * @param replicasToRemove
	 *            Replicas to remove; if empty, all replicas with ID smaller
	 *            than the sending node's ID are removed
	 * 
	 * @throws CommunicationException
	 *             Thrown if an unresolvable communication failure occurs.
	 */
	public abstract void removeReplicas(ID sendingNode,
			Set<Entry> replicasToRemove) throws CommunicationException;

	/**
	 * Returns all entries stored under the given ID.
	 * 
	 * @param id
	 * @return A {@link Set} of entries associated with <code>id</code>.
	 * @throws CommunicationException
	 *             Thrown if an unresolvable communication failure occurs.
	 */
	public abstract Set<Entry> retrieveEntries(ID id)
			throws CommunicationException;

	/**
	 * Inform a node that its predecessor leaves the network.
	 * 
	 * @param predecessor
	 * @throws CommunicationException
	 *             Thrown if an unresolvable communication failure occurs.
	 */
	public abstract void leavesNetwork(Node predecessor)
			throws CommunicationException;

	/**
	 * Closes the connection to the node.
	 */
	public abstract void disconnect();

	/**
	 * @param nodeID
	 *            the nodeID to set
	 */
	protected final void setNodeID(ID nodeID) {
		this.nodeID = nodeID;
	}

	/**
	 * @param nodeURL
	 *            the nodeURL to set
	 */
	protected final void setNodeURL(URL nodeURL) {
		this.nodeURL = nodeURL;
	}
}