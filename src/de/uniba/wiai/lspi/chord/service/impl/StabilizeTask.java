/***************************************************************************
 *                                                                         *
 *                            StabilizeTask.java                           *
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

import java.util.List;

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.RefsAndEntries;
import de.uniba.wiai.lspi.util.logging.Logger;

/**
 * Invokes notify method on successor.
 * 
 * @author Karsten Loesing, Sven Kaffille
 * @version 1.0.5
 */
final class StabilizeTask implements Runnable {

	/**
	 * Parent object for performing stabilization.
	 */
	private NodeImpl parent;

	/**
	 * Reference on routing table.
	 */
	private References references;

	private Entries entries;

	/**
	 * Object logger.
	 */
	protected final static Logger logger = Logger
			.getLogger(StabilizeTask.class);

	/**
	 * Creates a new instance, but without starting a thread running it.
	 * 
	 * @param parent
	 *            Parent object for performing stabilization.
	 * @param references
	 *            Reference on routing table.
	 * @throws NullPointerException
	 *             If either of the parameters is <code>null</code>.
	 */
	StabilizeTask(NodeImpl parent, References references, Entries entries) {

		if (parent == null || references == null || entries == null) {
			throw new NullPointerException(
					"No argument to constructor may be null!");
		}

		this.parent = parent;
		this.references = references;
		this.entries = entries;
	}

	public void run() {
		try {

			final boolean debugEnabled = StabilizeTask.logger
					.isEnabledFor(DEBUG);
			final boolean infoEnabled = StabilizeTask.logger.isEnabledFor(INFO);

			// start of method
			if (debugEnabled) {
				StabilizeTask.logger
						.debug("Stabilize method has been invoked periodically");
			}

			// determine successor
			Node successor = this.references.getSuccessor();
			if (successor == null) {

				// nothing to stabilize
				if (infoEnabled) {
					StabilizeTask.logger
							.info("Nothing to stabilize, as successor is null");
					return;
				}

			} else {

				// notify successor and obtain its predecessor reference and
				// successor list
				List<Node> mySuccessorsPredecessorAndSuccessorList;
				try {
					/*
					 * NOTIFYING successor.
					 */
					mySuccessorsPredecessorAndSuccessorList = successor
							.notify(this.parent);
					if (infoEnabled) {
						StabilizeTask.logger
								.info("Received response to notify request from "
										+ "successor" + successor.getNodeID());
					}
				} catch (CommunicationException e) {
					if (debugEnabled) {
						StabilizeTask.logger
								.debug(
										"Invocation of notify on node "
												+ successor.getNodeID()
												+ " was not successful due to a "
												+ "communication failure! Successor has "
												+ "failed during stabilization! "
												+ "Removing successor!", e);
					}
					this.references.removeReference(successor);
					return;
				}

				/*
				 * 19.06.2007. sven 
				 * Test if our successor has a different
				 * predecessor than this node.
				 */
				if ((mySuccessorsPredecessorAndSuccessorList.size() > 0)
						&& (mySuccessorsPredecessorAndSuccessorList.get(0) != null)) {
					if (!this.parent.getNodeID()
							.equals(mySuccessorsPredecessorAndSuccessorList
									.get(0).getNodeID())) {
						/*
						 * If it does not know us, we have to fetch all entries
						 * relevant for us.
						 */
						RefsAndEntries refsAndEntries = successor
								.notifyAndCopyEntries(this.parent);
						mySuccessorsPredecessorAndSuccessorList = refsAndEntries
								.getRefs();
						/*
						 * and have to store them locally
						 */
						this.entries.addAll(refsAndEntries.getEntries());
					}
				}

				for (Node newReference : mySuccessorsPredecessorAndSuccessorList) {
					this.references.addReference(newReference);

					if (debugEnabled) {
						logger.debug("Added new reference: " + newReference);
					}
				}
				if (infoEnabled) {
					StabilizeTask.logger.info("Invocation of notify on node "
							+ successor.getNodeID() + " was successful");
				}
			}
		} catch (Exception e) {
			StabilizeTask.logger.warn(
					"Unexpected Exception caught in StabilizeTask!", e);
			e.printStackTrace();
		}
	}
}