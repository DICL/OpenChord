/***************************************************************************
 *                                                                         *
 *                          ChordREmoveFuture.java                         *
 *                            -------------------                          *
 *   date                 : 15.10.2005                                     *
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

import java.io.Serializable;
import java.util.concurrent.Executor;

import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ChordFuture;
import de.uniba.wiai.lspi.chord.service.Key;

/**
 * Implementation of {@link ChordFuture} for removal of an entry from the chord
 * distributed hash table.
 * 
 * @author sven
 * @version 1.0.5
 * 
 */
class ChordRemoveFuture extends ChordFutureImpl {

	/**
	 * The instance of chord used for the invocation represented by this. 
	 */
	private Chord chord;

	
	/**
	 * The key used for the insertion. 
	 */
	private Key key;

	/**
	 * The entry to remove. 
	 */
	private Serializable entry;

	/**
	 * @param c
	 * @param k
	 * @param entry
	 */
	private ChordRemoveFuture(Chord c, Key k, Serializable entry) {
		this.chord = c;
		this.key = k;
		this.entry = entry;
	}

	/**
	 * Factory method to create an instance of this class. This method also
	 * prepares execution of the removal with help of the provided
	 * {@link Executor} <code>exec</code>.
	 * 
	 * @param exec
	 *            The executor that should asynchronously execute the removal of
	 *            <code>entry</code> with key <code>k</code>.
	 * 
	 * @param c
	 *            The instance of {@link Chord} that should be used to remove
	 *            <code>entry</code>.
	 * @param k
	 *            The {@link Key} for <code>entry</code>.
	 * @param entry
	 *            The entry to be removed.
	 * @return Instance of this class.
	 */
	final static ChordRemoveFuture create(Executor exec, Chord c, Key k,
			Serializable entry) {
		if (c == null) {
			throw new IllegalArgumentException(
					"ChordRemoveFuture: chord instance must not be null!");
		}
		if (k == null) {
			throw new IllegalArgumentException(
					"ChordRemoveFuture: key must not be null!");
		}
		if (entry == null) {
			throw new IllegalArgumentException(
					"ChordRemoveFuture: entry must not be null!");
		}
		
		ChordRemoveFuture f = new ChordRemoveFuture(c, k, entry);
		exec.execute(f.getTask());
		return f;
	}

	/**
	 * @return The runnable that executes the operation associated with this. 
	 */
	private final Runnable getTask() {
		return new RemoveTask(this.chord, this.key, this.entry);
	}

	/**
	 * Runnable to execute the removal of entry with help of chord.
	 * 
	 * @author sven
	 * @version 1.0
	 */
	private class RemoveTask implements Runnable {

		/**
		 * The instance of chord used for the invocation represented by this. 
		 */
		private Chord chord;

		
		/**
		 * The key used for the insertion. 
		 */
		private Key key;

		/**
		 * The entry to remove. 
		 */
		private Serializable entry;
		
		/**
		 * @param chord
		 * @param key
		 * @param entry
		 */
		RemoveTask(Chord chord, Key key, Serializable entry){
			this.chord = chord; 
			this.key = key; 
			this.entry = entry; 
		}
		
		public void run() {
			try {
				this.chord.remove(this.key, this.entry);
			} catch (Throwable t) {
				setThrowable(t);
			}
			setIsDone();
		}
	}
}
