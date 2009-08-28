/***************************************************************************
 *                                                                         *
 *                          CheckPredecessorTask.java                      *
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

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.util.logging.Logger;
import static de.uniba.wiai.lspi.util.logging.Logger.LogLevel.*;

/**
 * Checks if the predecessor of the local node is still alive.
 * 
 * @author Karsten Loesing
 * @version 1.0.5
 */
final class CheckPredecessorTask implements Runnable {

	/**
	 * Object logger.
	 */
	private static final Logger logger = Logger
			.getLogger(CheckPredecessorTask.class);

	/**
	 * Reference on routing table.
	 */
	private References references;

	/**
	 * Creates a new instance, but without starting a thread running it.
	 * 
	 * @param references
	 *            Reference on routing table.
	 * @throws NullPointerException
	 *             If parameter value is <code>null</code>.
	 */
	CheckPredecessorTask(References references) {

		if (references == null) {
			throw new NullPointerException(
					"Parameter references may not be null!");
		}

		this.references = references;
	}

	public void run() {

		try {

			boolean debug = CheckPredecessorTask.logger.isEnabledFor(DEBUG);
			// start of method
			if (debug) {
				CheckPredecessorTask.logger
						.debug("Check predecessor method has been invoked.");
			}

			// check if local node has a valid predecessor reference
			Node predecessor = this.references.getPredecessor();
			if (predecessor == null) {
				// I have no predecessor
				CheckPredecessorTask.logger
						.info("Nothing to check, as predecessor is null");
				return;
			} else {

				// try to reach predecessor
				try {
					predecessor.ping();
					// My predecessor responded
					if (debug) {
						CheckPredecessorTask.logger
								.debug("Predecessor reached!");
					}
				} catch (CommunicationException e) {
					if (debug) {
						CheckPredecessorTask.logger
								.debug(
										"Checking predecessor was NOT successful due "
												+ "to a communication failure! Removing "
												+ "predecessor reference.", e);
					}
					// My predecessor did not respond
					this.references.removeReference(predecessor);
					return;
				}

				CheckPredecessorTask.logger
						.info("Invocation of check predecessor on node "
								+ predecessor.getNodeID() + " was successful");
			}
		} catch (Exception e) {
			CheckPredecessorTask.logger.warn(
					"Unexpected Exception caught in CheckpredecessorTask!", e);
		}
	}
}