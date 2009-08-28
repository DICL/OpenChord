/***************************************************************************
 *                                                                         *
 *                               NodeImpl.java                             *
 *                            -------------------                          *
 *   date                 : 16.08.2004                                     *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *   			    		karsten.loesing@uni-bamberg.de                 *
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
package de.uniba.wiai.lspi.chord.service.impl;

import static de.uniba.wiai.lspi.util.logging.Logger.LogLevel.DEBUG;
import static de.uniba.wiai.lspi.util.logging.Logger.LogLevel.INFO;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Endpoint;
import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.RefsAndEntries;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.util.logging.Logger;

/**
 * Implements all operations which can be invoked remotely by other nodes.
 * 
 * @author Karsten Loesing
 * @version 1.0.5
 */
public final class NodeImpl extends Node {

	/**
	 * Endpoint for incoming communication.
	 */
	private Endpoint myEndpoint = null;

	/**
	 * Reference on local node.
	 */
	private ChordImpl impl;

	/**
	 * Object logger.
	 * The name of the logger is the name of this class with the nodeID appended. 
	 * The length of the nodeID depends on the number of bytes that are displayed 
	 * when the ID is shown in Hex-Representation. See documentation of {@link ID}. 
	 * E.g. de.uniba.wiai.lspi.chord.service.impl.NodeImpl.FF FF FF FF if the number 
	 * of displayed Bytes of an ID is 4. 
	 */
	private Logger logger;

	/**
	 * Routing table (including finger table, successor list, and predecessor
	 * reference)
	 */
	private References references;

	/**
	 * Repository for locally stored entries.
	 */
	private Entries entries;

	/**
	 * Executor that executes insertion and removal of entries on successors of
	 * this node.
	 */
	private Executor asyncExecutor;
	
	private Lock notifyLock; 

	/**
	 * Creates that part of the local node which answers remote requests by
	 * other nodes. Sole constructor, is invoked by ChordImpl only.
	 * 
	 * @param impl
	 *            Reference on ChordImpl instance which created this object.
	 * @param nodeID
	 *            This node's Chord ID.
	 * @param nodeURL
	 *            URL, on which this node accepts connections.
	 * @param references
	 *            Routing table of this node.
	 * @param entries
	 *            Repository for entries of this node.
	 * @throws IllegalArgumentException
	 *             If any of the parameter has value <code>null</code>.
	 */
	NodeImpl(ChordImpl impl, ID nodeID, URL nodeURL, References references,
			Entries entries) {

		if (impl == null || nodeID == null || nodeURL == null
				|| references == null || entries == null) {
			throw new IllegalArgumentException(
					"Parameters of the constructor may not have a null value!");
		}

		this.logger = Logger.getLogger(NodeImpl.class.getName() + "." + nodeID.toString());

		this.impl = impl;
		this.asyncExecutor = impl.getAsyncExecutor();
		this.nodeID = nodeID;
		this.nodeURL = nodeURL;
		this.references = references;
		this.entries = entries;
		this.notifyLock = new ReentrantLock(true); 
		
		// create endpoint for incoming connections
		this.myEndpoint = Endpoint.createEndpoint(this, nodeURL);
		this.myEndpoint.listen();
	}

	/**
	 * Makes this endpoint accept entries by other nodes. Is invoked by
	 * ChordImpl only.
	 */
	final void acceptEntries() {
		this.myEndpoint.acceptEntries();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void disconnect() {
		this.myEndpoint.disconnect();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Node findSuccessor(ID key) {
		return this.impl.findSuccessor(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<Node> notify(Node potentialPredecessor) {
		/*
		 * Mutual exclusion between notify and notifyAndCopyEntries. 17.03.2008. sven.
		 */
		this.notifyLock.lock(); 
		try {
			// the result will contain the list of successors as well as the
			// predecessor of this node
			List<Node> result = new LinkedList<Node>();

			// add reference on predecessor as well as on successors to result
			if (this.references.getPredecessor() != null) {
				result.add(this.references.getPredecessor());
			} else {
				result.add(potentialPredecessor); 
			}
			result.addAll(this.references.getSuccessors());

//			 add potential predecessor to successor list and finger table and
			// set
			// it as predecessor if no better predecessor is available
			this.references.addReferenceAsPredecessor(potentialPredecessor);			
			return result;
		} finally {
			this.notifyLock.unlock(); 
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final RefsAndEntries notifyAndCopyEntries(Node potentialPredecessor)
			throws CommunicationException {
		/*
		 * Mutual exclusion between notify and notifyAndCopyEntries. 17.03.2008. sven.
		 */
		this.notifyLock.lock(); 
		try {
			// copy all entries which lie between the local node ID and the ID
			// of
			// the potential predecessor, including those equal to potential
			// predecessor
			Set<Entry> copiedEntries = this.entries.getEntriesInInterval(
					this.nodeID, potentialPredecessor.getNodeID());

			return new RefsAndEntries(this.notify(potentialPredecessor),
					copiedEntries);
		} finally {
			this.notifyLock.unlock(); 
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void ping() {
		// do nothing---returning of method is proof of live
		return;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void insertEntry(Entry toInsert) throws CommunicationException {
		if (this.logger.isEnabledFor(DEBUG)) {
			this.logger.debug("Inserting entry with id " + toInsert.getId()
					+ " at node " + this.nodeID);
		}

		// Possible, but rare situation: a new node has joined which now is
		// responsible for the id!
		if ((this.references.getPredecessor() == null)
				|| !toInsert.getId().isInInterval(
						this.references.getPredecessor().getNodeID(),
						this.nodeID)) {
			this.references.getPredecessor().insertEntry(toInsert); 
			return;
		}

		// add entry to local repository
		this.entries.add(toInsert);

		// create set containing this entry for insertion of replicates at all
		// nodes in successor list
		Set<Entry> newEntries = new HashSet<Entry>();
		newEntries.add(toInsert);

		// invoke insertReplicates method on all nodes in successor list
		final Set<Entry> mustBeFinal = new HashSet<Entry>(newEntries);
		for (final Node successor : this.references.getSuccessors()) {
			this.asyncExecutor.execute(new Runnable() {
				public void run() {
					try {
						successor.insertReplicas(mustBeFinal);
					} catch (CommunicationException e) {
						// do nothing
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void insertReplicas(Set<Entry> replicatesToInsert) {
		this.entries.addAll(replicatesToInsert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeEntry(Entry entryToRemove)
			throws CommunicationException {

		if (this.logger.isEnabledFor(DEBUG)) {
			this.logger.debug("Removing entry with id " + entryToRemove.getId()
					+ " at node " + this.nodeID);
		}

		// Possible, but rare situation: a new node has joined which now is
		// responsible for the id!
		if (this.references.getPredecessor() != null
				&& !entryToRemove.getId().isInInterval(
						this.references.getPredecessor().getNodeID(),
						this.nodeID)) {
			this.references.getPredecessor().removeEntry(entryToRemove);
			return;
		}

		// remove entry from repository
		this.entries.remove(entryToRemove);

		// create set containing this entry for removal of replicates at all
		// nodes in successor list
		final Set<Entry> entriesToRemove = new HashSet<Entry>();
		entriesToRemove.add(entryToRemove);

		// invoke removeReplicates method on all nodes in successor list
		List<Node> successors = this.references.getSuccessors();
		final ID id = this.nodeID;
		for (final Node successor : successors) {
			this.asyncExecutor.execute(new Runnable() {
				public void run() {
					try {
						// remove only replica of removed entry
						successor.removeReplicas(id, entriesToRemove);
					} catch (CommunicationException e) {
						// do nothing for the moment
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeReplicas(ID sendingNodeID,
			Set<Entry> replicasToRemove) {
		if (replicasToRemove.size() == 0) {
			// remove all replicas in interval
			boolean debug = this.logger.isEnabledFor(DEBUG);
			if (debug) {
				this.logger.debug("Removing replicas. Current no. of entries: "
						+ this.entries.getNumberOfStoredEntries());
			}
			/*
			 * Determine entries to remove. These entries are located between
			 * the id of the local peer and the argument sendingNodeID
			 */
			Set<Entry> allReplicasToRemove = this.entries.getEntriesInInterval(
					this.nodeID, sendingNodeID);
			if (debug) {
				this.logger.debug("Replicas to remove " + allReplicasToRemove);
				this.logger.debug("Size of replicas to remove "
						+ allReplicasToRemove.size());
			}

			/*
			 * Remove entries
			 */
			this.entries.removeAll(allReplicasToRemove);

			if (debug) {
				this.logger
						.debug("Removed replicas??? Current no. of entries: "
								+ this.entries.getNumberOfStoredEntries());
			}
		} else {
			// remove only replicas of given entry
			this.entries.removeAll(replicasToRemove);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Set<Entry> retrieveEntries(ID id)
			throws CommunicationException {

		// Possible, but rare situation: a new node has joined which now is
		// responsible for the id!
		if (this.references.getPredecessor() != null
				&& !id.isInInterval(this.references.getPredecessor()
						.getNodeID(), this.nodeID)) {
			this.logger.fatal("The rare situation has occured at time "
					+ System.currentTimeMillis() + ", id to look up=" + id
					+ ", id of local node=" + this.nodeID
					+ ", id of predecessor="
					+ this.references.getPredecessor().getNodeID());
			return this.references.getPredecessor().retrieveEntries(id);
		}

		// return entries from local repository
		// for this purpose create a copy of the Set in order to allow the
		// thread retrieving the entries to modify the Set without modifying the
		// internal Set of entries. sven
		return this.entries.getEntries(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final public void leavesNetwork(Node predecessor) {
		if (this.logger.isEnabledFor(INFO)) {
			this.logger.info("Leaves network invoked; " + this.nodeID
					+ ". Updating references.");
			this.logger.info("New predecessor " + predecessor.getNodeID());
		}
		if (this.logger.isEnabledFor(DEBUG)) {
			this.logger.debug("References before update: "
					+ this.references.toString());
		}
		this.references.removeReference(this.references.getPredecessor());
		if (this.logger.isEnabledFor(DEBUG)) {
			this.logger.debug("References after update: "
					+ this.references.toString());
		}
	}

	/**
	 * 
	 * @return
	 */
	final Executor getAsyncExecutor() {
		return this.asyncExecutor;
	}

}