/***************************************************************************
 *                                                                         *
 *                              References.java                            *
 *                            -------------------                          *
 *   date                 : 16.08.2004                                     *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *                      karsten.loesing@uni-bamberg.de                 *
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.Proxy;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.util.logging.Logger;

/**
 * Stores all remote references of nodes the local node is connected to and
 * provides methods for querying and manipulating these references. Makes use of
 * one finger table, one successor list, and one predecessor reference.
 * 
 * @author Karsten Loesing
 * @version 1.0.5
 */
final class References {

	/**
	 * Object logger.
	 */
	private Logger logger;

	/**
	 * This node's finger table.
	 */
	private FingerTable fingerTable = null;

	/**
	 * This node's successor list
	 */
	private SuccessorList successorList = null;

	/**
	 * This node's predecessor.
	 */
	private Node predecessor = null;

	/**
	 * This node's local ID.
	 */
	private ID localID = null;

	private URL localURL = null;

	private Entries entries;

	/**
	 * Creates an References object which contains no references.
	 * 
	 * @param locID
	 *            ID of local node. Must not be <code>null</code>.
	 * @param numberOfEntriesInSuccessorList
	 *            Length of successor list to be created. Must be greater or
	 *            equal 1!
	 * @param entries
	 *            Reference on this nodes' entries which is passed to creation
	 *            of the successor list. Must not be <code>null</code>.   
	 * @throws IllegalArgumentException
	 *             If any parameters is <code>null</code> or 
	 *             if number of entries in successor list is less than 1.
	 */
	References(ID locID, URL locURL, int numberOfEntriesInSuccessorList, Entries entries) {

		if (locURL == null || locID == null || entries == null) {
			throw new IllegalArgumentException(
					"No parameter of constructor may be null!");
		}

		if (numberOfEntriesInSuccessorList < 1)
			throw new IllegalArgumentException(
					"Number of entries in successor list cannot be less than 1! "
							+ numberOfEntriesInSuccessorList
							+ " is not a valid value!");

		this.logger = Logger.getLogger(References.class.getName() + "."
				+ locID);

		this.logger.debug("Logger initialized.");

		// store node id
		this.localID = locID;
		
		this.localURL = locURL; 

		this.entries = entries;

		// create empty finger table and successor list
		this.fingerTable = new FingerTable(locID, this);
		this.successorList = new SuccessorList(locID,
				numberOfEntriesInSuccessorList, this, entries);
	}

	/**
	 * Determines the closest preceding node for the given ID based on finger
	 * table, successor list, and predecessor, but without testing the node's
	 * liveliness.
	 * 
	 * @param key
	 *            ID to find closest preceding node for.
	 * @throws NullPointerException
	 *             If ID is <code>null</code>.
	 * @return Reference on closest preceding node.
	 */
	final synchronized Node getClosestPrecedingNode(ID key) {

		if (key == null) {
			NullPointerException e = new NullPointerException(
					"ID may not be null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		Map<ID, Node> foundNodes = new HashMap<ID, Node>();
		// determine closest preceding reference of finger table
		Node closestNodeFT = this.fingerTable.getClosestPrecedingNode(key);
		if (closestNodeFT != null) {
			foundNodes.put(closestNodeFT.getNodeID(), closestNodeFT);
		}

		// determine closest preceding reference of successor list
		Node closestNodeSL = this.successorList.getClosestPrecedingNode(key);
		if (closestNodeSL != null) {
			foundNodes.put(closestNodeSL.getNodeID(), closestNodeSL);
		}

		// predecessor is appropriate only if it precedes the given id
		Node predecessorIfAppropriate = null;
		if (this.predecessor != null
				&& key.isInInterval(this.predecessor.getNodeID(), this.localID)) {
			predecessorIfAppropriate = this.predecessor;
			foundNodes.put(this.predecessor.getNodeID(), predecessor);
		}

		// with three references which may be null, there are eight (8) cases we
		// have to enumerate...
		Node closestNode = null;
		List<ID> orderedIDList = new ArrayList<ID>(foundNodes.keySet());
		orderedIDList.add(key);
		int sizeOfList = orderedIDList.size();
		// size of list must be greater than one to not only contain the key.
		// if (sizeOfList > 1) {

		/*
		 * Sort list in ascending order
		 */
		Collections.sort(orderedIDList);
		/*
		 * The list item with one index lower than that of the key must be the
		 * id of the closest predecessor or the key.
		 */
		int keyIndex = orderedIDList.indexOf(key);
		/*
		 * As all ids are located on a ring if the key is the first item in the
		 * list we have to select the last item as predecessor with help of this
		 * calculation.
		 */
		int index = (sizeOfList + (keyIndex - 1)) % sizeOfList;
		/*
		 * Get the references to the node from the map of collected nodes.
		 */
		ID idOfclosestNode = orderedIDList.get(index);
		closestNode = foundNodes.get(idOfclosestNode);
		if (closestNode == null) {
			throw new NullPointerException("closestNode must not be null!");
		}

		/*
		 * Following code is too complicated.
		 */
		// if (closestNodeFT == null) {
		// if (closestNodeSL == null) {
		// if (predecessorIfAppropriate == null) {
		// // no reference is appropriate
		// closestNode = null;
		// } else {
		// // only predecessor is appropriate (case should not occur,
		// // but anyway...
		// closestNode = predecessorIfAppropriate;
		// }
		// } else {
		// if (predecessorIfAppropriate == null) {
		// // only reference of successor list is appropriate
		// closestNode = closestNodeSL;
		// } else {
		// // either predecessor or reference of successor list is
		// // appropriate; determine one of both
		// if (predecessorIfAppropriate.nodeID.isInInterval(
		// closestNodeSL.nodeID, key)) {
		// closestNode = predecessorIfAppropriate;
		// } else {
		// closestNode = closestNodeSL;
		// }
		// }
		// }
		// } else {
		// if (closestNodeSL == null) {
		// if (predecessorIfAppropriate == null) {
		// // only reference of finger table is appropriate
		// closestNode = closestNodeFT;
		// } else {
		// // either predecessor or reference of finger table is
		// // appropriate; determine one of both
		// if (predecessorIfAppropriate.nodeID.isInInterval(
		// closestNodeFT.nodeID, key)) {
		// closestNode = predecessorIfAppropriate;
		// } else {
		// closestNode = closestNodeFT;
		// }
		// }
		// } else {
		// if (predecessorIfAppropriate == null) {
		// // either reference of successor list or reference of finger
		// // table is appropriate; determine one of both
		// if (closestNodeSL.nodeID.isInInterval(closestNodeFT.nodeID,
		// key)) {
		// closestNode = closestNodeSL;
		// } else {
		// closestNode = closestNodeFT;
		// }
		// } else {
		// // either of the three reference is appropriate; determine
		// // first one of the references of successor list and finger
		// // table is more appropriate and afterwards compare with
		// // predecessor
		// if (closestNodeSL.nodeID.isInInterval(closestNodeFT.nodeID,
		// key)) {
		// if (predecessorIfAppropriate.nodeID.isInInterval(
		// closestNodeSL.nodeID, key)) {
		// closestNode = predecessorIfAppropriate;
		// } else {
		// closestNode = closestNodeSL;
		// }
		// } else {
		// if (predecessorIfAppropriate.nodeID.isInInterval(
		// closestNodeFT.nodeID, key)) {
		// closestNode = predecessorIfAppropriate;
		// } else {
		// closestNode = closestNodeFT;
		// }
		// }
		// }
		// }
		// }
		if (this.logger.isEnabledFor(DEBUG)) {
			this.logger.debug("Closest preceding node of ID "
					+ key
					+ " at node "
					+ this.localID.toString()
					+ " is "
					+ closestNode.getNodeID()
					+ " with closestNodeFT="
					+ (closestNodeFT == null ? "null" : ""
							+ closestNodeFT.getNodeID())
					+ " and closestNodeSL="
					+ (closestNodeSL == null ? "null" : ""
							+ closestNodeSL.getNodeID())
					+ " and predecessor (only if it precedes given ID)="
					+ (predecessorIfAppropriate == null ? "null" : ""
							+ predecessorIfAppropriate.getNodeID()));
		}
		return closestNode;
	}

	/**
	 * Adds the given node reference to the finger table and successor list, if
	 * appropriate. The reference is NOT set as predecessor, even if is closer
	 * to this node. Therefore use {@link #addReferenceAsPredecessor(Node)}.
	 * 
	 * @param newReference
	 *            Reference to be added to the local data structures.
	 * @throws NullPointerException
	 *             If the given reference is null.
	 */
	final synchronized void addReference(Node newReference) {

		if (newReference == null) {
			NullPointerException e = new NullPointerException(
					"Node reference to be added must not be null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		boolean debug = this.logger.isEnabledFor(DEBUG);
		// June 21, 2006. Moved here by sven to avoid failing of checkIfProxy()
		if (newReference.getNodeID().equals(this.localID)) {
			if (debug) {
				this.logger.debug("Reference on myself was not added");
			}
			return;
		}

		// check parameters
		this.checkIfProxy(newReference);

		this.fingerTable.addReference(newReference);
		this.successorList.addSuccessor(newReference);

		if (debug) {
			this.logger.debug("Attempted to add reference "
					+ newReference.getNodeID().toString()
					+ " to finger table and successor list. Whether it fit "
					+ "or not depends on those data structures.");
		}
	}

	/**
	 * Removes the given node reference from the finger table and the successor
	 * list. If the given reference is the current predecessor, the predecessor
	 * reference will be <code>null</code> afterwards.
	 * 
	 * @param oldReference
	 *            Reference to remove from ALL data structures.
	 * @throws NullPointerException
	 *             If reference to remove is <code>null</code>.
	 */
	final synchronized void removeReference(Node oldReference) {

		if (oldReference == null) {
			NullPointerException e = new NullPointerException(
					"Reference to remove must not be null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		this.fingerTable.removeReference(oldReference);
		this.successorList.removeReference(oldReference);

		if (oldReference.equals(this.getPredecessor())) {
			this.predecessor = null;
		}

		disconnectIfUnreferenced(oldReference);

		if (this.logger.isEnabledFor(DEBUG)) {
			this.logger
					.debug("Attempted to remove reference "
							+ oldReference
							+ " from all data structures including predecessor reference.");
		}
	}

	/**
	 * Closes the connection to the given reference, if it is not kept in any
	 * data structure (ie. finger table, successor list, predecessor) any more.
	 * 
	 * @param removedReference
	 *            Node to which the connection shall be closed, if there exists
	 *            no reference any more.
	 * @throws NullPointerException
	 *             If given reference is null.
	 */
	void disconnectIfUnreferenced(Node removedReference) {
		if (removedReference == null) {
			NullPointerException e = new NullPointerException(
					"Reference may not be null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		if (!this.containsReference(removedReference)) {
			if (!(removedReference instanceof Proxy)) {
				this.logger
						.error("Attempt to disconnect unused reference failed");
				throw new RuntimeException("Reference should be of type Proxy");
			}
			this.logger.debug("Disconnecting unused reference on node "
					+ removedReference);
			removedReference.disconnect();
		}
	}

	/**
	 * Determines this node's direct successor and returns it. If no successor
	 * is known, <code>null</code> is returned.
	 * 
	 * @return The local node's direct successor, or <code>null</code> if no
	 *         successor is known.
	 */
	final synchronized Node getSuccessor() {
		// direct successor is the first entry in my successor list
		return this.successorList.getDirectSuccessor();
	}

	/**
	 * Returns a formatted string of the IDs of all references stored on this
	 * node. This includes references in the finger table and successor list as
	 * well as the predecessor.
	 * 
	 * @return Formatted string of references.
	 */
	public synchronized String toString() {
		StringBuilder result = new StringBuilder("Node: "
				+ this.localID.toString() + ", " + this.localURL + "\n");
		result.append(this.fingerTable.toString());

		result.append(this.successorList.toString());
		result.append("Predecessor: "
				+ (this.predecessor == null ? "null" : ""
						+ this.predecessor.getNodeID() + ", "
						+ this.predecessor.getNodeURL()));
		return result.toString();
	}

	/**
	 * Returns the reference on this node's predecessor, if available. If no
	 * predecessor exists for this node, <code>null</code> is returned.
	 * 
	 * @return Reference on this node's predecessor, if available. If no
	 *         predecessor exists for this node, <code>null</code> is
	 *         returned.
	 */
	final synchronized Node getPredecessor() {
		return this.predecessor;
	}

	/**
	 * Sets the given reference as this node's predecessor. If the former value
	 * of this predecessor's node was <code>null</code> and if this reference
	 * is not used any more (eg. in finger table or successor list), the
	 * connection to it is closed.
	 * 
	 * @param potentialPredecessor
	 *            Reference on the node to be set as new predecessor; may not be
	 *            null
	 * @throws NullPointerException
	 *             If potential predecessor is null.
	 */
	final synchronized void setPredecessor(Node potentialPredecessor) {

		if (potentialPredecessor == null) {
			NullPointerException e = new NullPointerException(
					"Potential predecessor of method setPredecessor may not be "
							+ "null!");
			this.logger.error("Null pointer", e);
			throw e;
		}
		this.checkIfProxy(potentialPredecessor);

		boolean info = this.logger.isEnabledFor(INFO);
		if (!(potentialPredecessor.equals(this.predecessor))) {
			Node formerPredecessor = this.predecessor;
			this.predecessor = potentialPredecessor;
			if (formerPredecessor != null) {
				this.disconnectIfUnreferenced(formerPredecessor);
				/*
				 * The replicas, which are in the range between the old and the
				 * new predecessor, on the last successor of this node have to
				 * be removed if the successor list is full. => capacity of sl ==
				 * length of sl.
				 */
				int sLSize = this.successorList.getSize();
				if (this.successorList.getCapacity() == sLSize) {
					Node lastSuccessor = this.successorList.getReferences()
							.get(sLSize - 1);
					try {
						lastSuccessor.removeReplicas(this.predecessor
								.getNodeID(), new HashSet<Entry>());
					} catch (CommunicationException e) {
						logger
								.warn(
										"Could not remove replicas on last predecessor",
										e);
					}
				}
				if (this.logger.isEnabledFor(DEBUG)) {
					this.logger.debug("Old predecessor " + formerPredecessor
							+ " was replaced by " + potentialPredecessor);
				}
			} else {
				if (info) {
					this.logger.info("Predecessor reference set to "
							+ potentialPredecessor + "; was null before.");
				}
				Set<Entry> entriesToRep = this.entries.getEntriesInInterval(
						this.predecessor.getNodeID(), this.localID);
				List<Node> successors = this.successorList.getReferences();
				for (Node successor : successors) {
					try {
						successor.insertReplicas(entriesToRep);
					} catch (CommunicationException e) {
						this.logger.warn(
								"Damn. Could not replicate to successor "
										+ successor.getNodeID(), e);
					}
				}
			}
		}
	}

	/**
	 * Returns an unmodifiable list of this node's successors.
	 * 
	 * @return Unmodifiable successor list.
	 */
	final synchronized List<Node> getSuccessors() {
		return this.successorList.getReferences();
	}

	/**
	 * Determines if the given reference is contained in any one data structure,
	 * ie. finger table, successor list, or predecessor reference.
	 * 
	 * @param newReference
	 *            Reference to look up.
	 * @throws NullPointerException
	 *             If given reference is <code>null</code>.
	 * @return <code>true</code> if the reference is contained and
	 *         <code>false</code> if not.
	 */
	final synchronized boolean containsReference(Node newReference) {
		if (newReference == null) {
			NullPointerException e = new NullPointerException(
					"Reference to look up must not be null!");
			this.logger.error("Null pointer", e);
			throw e;
		}
		return (this.fingerTable.containsReference(newReference)
				|| this.successorList.containsReference(newReference) || newReference
				.equals(this.predecessor));
	}

	/**
	 * Returns a formatted string of this node's finger table.
	 * 
	 * @return String representation of finger table.
	 */
	final String printFingerTable() {
		return this.fingerTable.toString();
	}

	/**
	 * Returns a formatted string of this node's successor list.
	 * 
	 * @return String representation of successor list.
	 */
	final String printSuccessorList() {
		return this.successorList.toString();
	}

	/**
	 * Adds the given node reference to the finger table and successor list AND
	 * sets it as new predecessor reference, if appropriate. Appropriateness is
	 * given if either the former predecessor reference was <code>null</code>
	 * or the new reference's ID is located between the old predecessor ID and
	 * this node's ID. Even if not appropriate as predecessor, the reference is
	 * added to finger table and successor list.
	 * 
	 * @param potentialPredecessor
	 *            Reference which should be this node's predecessor.
	 * @throws NullPointerException
	 *             If the given reference is <code>null</code>.
	 */
	void addReferenceAsPredecessor(Node potentialPredecessor) {

		this.checkIfProxy(potentialPredecessor);
		if (potentialPredecessor == null) {
			NullPointerException e = new NullPointerException(
					"Reference to potential predecessor may not be null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		// if the local node did not have a predecessor reference before
		// or if the potential predecessor is closer to this local node,
		// replace the predecessor reference
		if (this.predecessor == null
				|| potentialPredecessor.getNodeID().isInInterval(
						this.predecessor.getNodeID(), this.localID)) {

			if (this.logger.isEnabledFor(INFO)) {
				this.logger
						.info("Setting a new predecessor reference: New reference is "
								+ potentialPredecessor
								+ ", old reference was "
								+ (this.predecessor == null ? "null"
										: this.predecessor));
			}

			// replace predecessor reference
			this.setPredecessor(potentialPredecessor);

			/*
			 * If a new predecessor, better than the old one has arrived, we
			 * have to copy the entries, that are relevant for this new
			 * predecessor. This has to be done by the predecessor itself, NOT
			 * here. 18.06.2007. sven
			 */
			// final Set<Entry> entries = this.entries.getEntriesInInterval(
			// this.predecessor.nodeID, this.localID);
			// this.localChord.getAsyncExecutor().execute(new Runnable() {
			// public void run() {
			// try {
			// for (Entry entryToInsert : entries) {
			// getPredecessor().insertEntry(entryToInsert);
			// }
			// } catch (CommunicationException e) {
			// }
			// }
			// });
		}

		// add reference
		this.addReference(potentialPredecessor);

	}

	/**
	 * Determines the first i entries in the finger table.
	 * 
	 * @param i
	 * @return The first (i+1) entries of finger table. If there are fewer then
	 *         i+1 entries only these are returned.
	 */
	public List<Node> getFirstFingerTableEntries(int i) {
		return this.fingerTable.getFirstFingerTableEntries(i);
	}

	/**
	 * Added by sven on 21.03.2006. This data strucutre is supposed to work with
	 * remote references therefore it must be instances of Proxy. This method is
	 * used in every method that adds a new reference to this to check that it
	 * is an instance of Proxy. -> TODO: Consider to change type of all Nodes
	 * within this data structure to type Proxy!!!!
	 * 
	 * @param node
	 * @throws RuntimeException
	 */
	private void checkIfProxy(Node node) {
		if (!(node instanceof Proxy)) {
			String msg = "Trying to use local node " + node
					+ " with references. This is not possible."
					+ "If you see this Exception contact the developers!";
			RuntimeException e = new RuntimeException(msg);
			this.logger.fatal(msg, e);
			throw e;
		}
	}

}