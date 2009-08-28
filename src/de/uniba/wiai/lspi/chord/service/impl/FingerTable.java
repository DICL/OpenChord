/***************************************************************************
 *                                                                         *
 *                             FingerTable.java                            *
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.util.logging.Logger;

/**
 * Stores references on the nodes in the finger table and provides methods for
 * querying and manipulating this table.
 * 
 * @author Karsten Loesing
 * @version 1.0.5
 */
final class FingerTable {

	/**
	 * ID of local node.
	 */
	private final ID localID;

	/**
	 * Finger table data.
	 */
	private final Node[] remoteNodes;

	/**
	 * Reference on parent object.
	 */
	private final References references;

	/**
	 * Object logger.
	 */
	private Logger logger;

	/**
	 * Creates an initially empty finger table. The table size is determined by
	 * the given ID's length. A reference on the parent object of type
	 * References is stored for being able to determine and disconnect unused
	 * references after removing them from the table.
	 * 
	 * @param localID
	 *            ID of local node.
	 * @param references
	 *            Reference on parent object.
	 * @throws NullPointerException
	 *             If either of the parameters is <code>null</code>.
	 */
	FingerTable(ID localID, References references) {

		if (localID == null || references == null) {
			throw new NullPointerException(
					"Neither parameter of the constructor may contain a null "
							+ "value!");
		}

		this.logger = Logger.getLogger(FingerTable.class + "." + localID);
		this.logger.debug("Logger initialized.");

		this.references = references;
		this.localID = localID;
		this.remoteNodes = new Node[localID.getLength()];
	}

	/**
	 * Sets one table entry to the given reference.
	 * 
	 * @param index
	 *            Index of table entry.
	 * @param proxy
	 *            Reference to store.
	 * @throws ArrayIndexOutOfBoundsException
	 *             If given index is not contained in the finger table.
	 * @throws NullPointerException
	 *             If given reference is <code>null</code>.
	 */
	private final void setEntry(int index, Node proxy) {

		if (index < 0 || index >= this.remoteNodes.length) {
			ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException(
					"setEntry was invoked with an index out of array "
							+ "bounds; index=" + index + ", length of array="
							+ this.remoteNodes.length);
			this.logger.error("Out of bounds!", e);
			throw e;
		}

		if (proxy == null) {
			NullPointerException e = new NullPointerException(
					"Reference to proxy may not be null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		this.remoteNodes[index] = proxy;

		if (this.logger.isEnabledFor(DEBUG)) {
			this.logger.debug("Entry " + index + " set to " + proxy.toString());
		}
	}

	/**
	 * Returns the reference stored at the given index.
	 * 
	 * @param index
	 *            Index of entry to be returned.
	 * @throws ArrayIndexOutOfBoundsException
	 *             If given index is not contained in the finger table.
	 * @return Reference stored at the given index.
	 */
	private final Node getEntry(int index) {

		if (index < 0 || index >= this.remoteNodes.length) {
			ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException(
					"getEntry was invoked with an index out of array "
							+ "bounds; index=" + index + ", length of array="
							+ this.remoteNodes.length);
			this.logger.error("Out of bounds!", e);
			throw e;
		}

		return this.remoteNodes[index];
	}

	/**
	 * Sets the reference at the given index to <code>null</code> and triggers
	 * to disconnect that node, if no other reference to it is kept any more.
	 * 
	 * @param index
	 *            Index of entry to be set to <code>null</code>.
	 * @throws ArrayIndexOutOfBoundsException
	 *             If given index is not contained in the finger table.
	 */
	private final void unsetEntry(int index) {
		if (index < 0 || index >= this.remoteNodes.length) {
			ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException(
					"unsetEntry was invoked with an index out of array "
							+ "bounds; index=" + index + ", length of array="
							+ this.remoteNodes.length);
			this.logger.error("Out of bounds!", e);
			throw e;
		}

		// remember overwritten reference
		Node overwrittenNode = this.getEntry(index);

		// set reference to null
		this.remoteNodes[index] = null;

		if (overwrittenNode == null) {
			this.logger.debug("unsetEntry did not change anything, because "
					+ "entry was null before.");
		} else {
			// check if overwritten reference does not exist any more
			this.references.disconnectIfUnreferenced(overwrittenNode);
			if (this.logger.isEnabledFor(DEBUG)) {
				this.logger.debug("Entry set to null: index=" + index
						+ ", overwritten node=" + overwrittenNode.toString());
			}
		}
	}

	/**
	 * Adds the given reference to all finger table entries of which the start
	 * index is in the interval (local node ID, new node ID) and of which the
	 * current entry is <code>null</code> or further away from the local node
	 * ID than the new node ID (ie. the new node ID is in the interval (local
	 * node ID, currently stored node ID) ).
	 * 
	 * @param proxy
	 *            Reference to be added to the finger table.
	 * @throws NullPointerException
	 *             If given reference is <code>null</code>.
	 */
	final void addReference(Node proxy) {

		if (proxy == null) {
			NullPointerException e = new NullPointerException(
					"Reference to add may not be null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		// for logging
		int lowestWrittenIndex = -1;
		int highestWrittenIndex = -1;

		for (int i = 0; i < this.remoteNodes.length; i++) {

			ID startOfInterval = this.localID.addPowerOfTwo(i);
			if (!startOfInterval.isInInterval(this.localID, proxy.getNodeID())) {
				break;
			}

			// for logging
			if (lowestWrittenIndex == -1) {
				lowestWrittenIndex = i;
			}
			highestWrittenIndex = i;

			if (getEntry(i) == null) {
				setEntry(i, proxy);
			} else if (proxy.getNodeID().isInInterval(this.localID,
					getEntry(i).getNodeID())) {
				Node oldEntry = getEntry(i);
				setEntry(i, proxy);
				this.references.disconnectIfUnreferenced(oldEntry);
			}
		}

		// logging
		if (this.logger.isEnabledFor(DEBUG)) {
			if (highestWrittenIndex == -1) {

				this.logger
						.debug("addReference did not add the given reference, "
								+ "because it did not fit anywhere!");

			}
		}
		if (this.logger.isEnabledFor(INFO)) {
			if (highestWrittenIndex == lowestWrittenIndex) {

				this.logger.info("Added reference to finger table entry "
						+ highestWrittenIndex);

			} else {

				this.logger.info("Added reference to finger table entries "
						+ lowestWrittenIndex + " to " + highestWrittenIndex);

			}
		}
	}

	/**
	 * Returns a copy of the finger table entries.
	 * 
	 * @return Copy of finger table entries.
	 */
	final Node[] getCopyOfReferences() {
		this.logger.debug("Returning copy of references.");

		Node[] copy = new Node[this.remoteNodes.length];
		System.arraycopy(this.remoteNodes, 0, copy, 0, this.remoteNodes.length);
		return copy;
	}

	/**
	 * Returns a formatted string representation of this finger table.
	 * 
	 * @return String representation containing one line per reference, together
	 *         with the annotation which table entries contain this reference.
	 */
	public final String toString() {

		StringBuilder result = new StringBuilder("Finger table:\n");

		int lastIndex = -1;
		ID lastNodeID = null;
		URL lastNodeURL = null;
		for (int i = 0; i < this.remoteNodes.length; i++) {
			Node next = this.remoteNodes[i];
			if (next == null) {
				// row ended or did not even start
				if ((lastIndex != -1) && (lastNodeID != null)) {
					// row ended
					result.append("  "
							+ lastNodeID
							+ ", "
							+ lastNodeURL
							+ " "
							+ ((i - 1 - lastIndex > 0) ? "(" + lastIndex + "-"
									+ (i - 1) + ")" : "(" + (i - 1) + ")")
							+ "\n");
					lastIndex = -1;
					lastNodeID = null;
					lastNodeURL = null;
				} else {
					// null at beginning
				}
			} else if (lastNodeID == null) {
				// found first reference in a row
				lastIndex = i;
				lastNodeID = next.getNodeID();
				lastNodeURL = next.getNodeURL();
			} else if (!lastNodeID.equals(next.getNodeID())) {
				// found different reference in finger table
				result.append("  "
						+ lastNodeID
						+ ", "
						+ lastNodeURL
						+ " "
						+ ((i - 1 - lastIndex > 0) ? "(" + lastIndex + "-"
								+ (i - 1) + ")" : "(" + (i - 1) + ")") + "\n");
				lastNodeID = next.getNodeID();
				lastNodeURL = next.getNodeURL();
				lastIndex = i;
			} else {
				// found next reference in a row
			}
		}

		// display last row
		if (lastNodeID != null && lastIndex != -1) {
			// row ended
			result.append("  "
					+ lastNodeID
					+ ", "
					+ lastNodeURL
					+ " "
					+ ((this.remoteNodes.length - 1 - lastIndex > 0) ? "("
							+ lastIndex + "-" + (this.remoteNodes.length - 1)
							+ ")" : "(" + (this.remoteNodes.length - 1) + ")")
					+ "\n");
			lastNodeID = null;
		}

		return result.toString();
	}

	/**
	 * Removes all occurences of the given node from finger table.
	 * 
	 * @param node1
	 *            Reference to be removed from the finger table.
	 * @throws NullPointerException
	 *             If given reference is <code>null</code>.
	 */
	final void removeReference(Node node1) {

		if (node1 == null) {
			NullPointerException e = new NullPointerException(
					"removeReference cannot be invoked with value null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		// for logging
		int lowestWrittenIndex = -1;
		int highestWrittenIndex = -1;

		// determine node reference with next larger ID than ID of node
		// reference to remove
		Node referenceForReplacement = null;
		for (int i = this.localID.getLength() - 1; i >= 0; i--) {
			Node n = this.getEntry(i);
			if (node1.equals(n)) {
				break;
			}
			if (n != null) {
				referenceForReplacement = n;
			}
		}

		// remove reference(s)
		for (int i = 0; i < this.remoteNodes.length; i++) {
			if (node1.equals(this.remoteNodes[i])) {

				// for logging
				if (lowestWrittenIndex == -1) {
					lowestWrittenIndex = i;
				}
				highestWrittenIndex = i;

				if (referenceForReplacement == null) {
					unsetEntry(i);
				} else {
					setEntry(i, referenceForReplacement);
				}
			}
		}

		// try to add references of successor list to fill 'holes' in finger
		// table
		List<Node> referencesOfSuccessorList = new ArrayList<Node>(
				this.references.getSuccessors());
		referencesOfSuccessorList.remove(node1);
		for (Node referenceToAdd : referencesOfSuccessorList) {
			this.addReference(referenceToAdd);
		}

		// logging
		if (this.logger.isEnabledFor(DEBUG)) {
			if (highestWrittenIndex == -1) {
				this.logger
						.debug("removeReference did not remove the given reference, "
								+ "because it did not exist in finger table "
								+ "anywhere!");
			} else if (highestWrittenIndex == lowestWrittenIndex) {
				this.logger.debug("Removed reference from finger table entry "
						+ highestWrittenIndex);
			} else {
				this.logger
						.debug("Removed reference from finger table entries "
								+ lowestWrittenIndex + " to "
								+ highestWrittenIndex);
			}
		}

	}

	/**
	 * Determines closest preceding node of given id.
	 * 
	 * @param key
	 *            ID of which the closest preceding node shall be determined.
	 * @throws NullPointerException
	 *             If given key is null.
	 * @return Reference to the node which most closely precedes the given ID.
	 *         <code>null</code> if no node has been found.
	 */
	final Node getClosestPrecedingNode(ID key) {
		if (key == null) {
			NullPointerException e = new NullPointerException(
					"ID to determine the closest preceding node may not be "
							+ "null!");
			this.logger.error("Null pointer", e);
			throw e;
		}
		boolean debug = this.logger.isEnabledFor(DEBUG);
		for (int i = this.remoteNodes.length - 1; i >= 0; i--) {
			if (this.remoteNodes[i] != null
					&& this.remoteNodes[i].getNodeID().isInInterval(
							this.localID, key)) {
				if (debug) {
					this.logger.debug("Closest preceding node for ID " + key
							+ " is " + this.remoteNodes[i].toString());
				}
				return this.remoteNodes[i];
			}
		}

		if (debug) {
			this.logger.debug("There is no closest preceding node for ID "
					+ key + " -- returning null!");
		}
		return null;
	}

	/**
	 * Determines if the given reference is stored somewhere in the finger
	 * table.
	 * 
	 * @param newReference
	 *            Reference of which existence shall be determined.
	 * @throws NullPointerException
	 *             If reference to look for is <code>null</code>.
	 * @return <code>true</code>, if the given reference exists in the finger
	 *         table, or <code>false</code>, else.
	 */
	final boolean containsReference(Node newReference) {
		if (newReference == null) {
			NullPointerException e = new NullPointerException(
					"Reference to check must not be null!");
			this.logger.error("Null pointer", e);
			throw e;
		}
		for (int i = 0; i < this.remoteNodes.length; i++) {
			if (newReference.equals(this.remoteNodes[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param i
	 * @return The first (i+1) entries of finger table. If there are fewer then
	 *         i+1 entries only these are returned.
	 * 
	 * 
	 * 
	 */
	final List<Node> getFirstFingerTableEntries(int i) {
		Set<Node> result = new HashSet<Node>();
		for (int j = 0; j < this.remoteNodes.length; j++) {
			if (this.getEntry(j) != null) {
				result.add(this.getEntry(j));
			}
			if (result.size() >= i) {
				break;
			}
		}
		return new ArrayList<Node>(result);
	}
}