/***************************************************************************
 *                                                                         *
 *                      ChordRetrievalFutureImpl.java                      *
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
import java.util.Set;
import java.util.concurrent.Executor;

import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ChordRetrievalFuture;
import de.uniba.wiai.lspi.chord.service.Key;
import de.uniba.wiai.lspi.chord.service.ServiceException;

/**
 * Implementation of {@link ChordRetrievalFuture}.
 * 
 * @author sven
 * @version 1.0.5
 * 
 */
class ChordRetrievalFutureImpl extends ChordFutureImpl implements
		ChordRetrievalFuture {

	/**
	 * The result of the retrieval request associated with this.
	 */
	private Set<Serializable> result;

	/**
	 * The chord instance used for the operation that is associated with this. 
	 */
	private Chord chord = null;

	/**
	 * The key to retrieve the associated entries for. 
	 */
	private Key key = null;

	/**
	 * 
	 * @param c
	 * @param k
	 */
	private ChordRetrievalFutureImpl(Chord c, Key k) {
		super();
		this.chord = c;
		this.key = k;
	}

	/**
	 * 
	 * @param r
	 */
	final void setResult(Set<Serializable> r) {
		this.result = r;
	}

	/**
	 * @see ChordRetrievalFuture
	 */
	public final Set<Serializable> getResult() throws ServiceException,
			InterruptedException {
		synchronized (this) {
			while (!this.isDone()) {
				this.wait();
			}
		}
		Throwable t = this.getThrowable();
		if (t != null) {
			throw new ServiceException(t.getMessage(), t);
		}
		return this.result;
	}

	/**
	 * 
	 * @return Runnable that performs the retrieve operation. 
	 */
	private Runnable getTask() {
		return new RetrievalTask(this.chord, this.key);
	}

	/**
	 * Factory method to create an instance of this class. This method also
	 * prepares execution of the retrieval with help of the provided
	 * {@link Executor} <code>exec</code>.
	 * 
	 * @param exec
	 *            The executor that should asynchronously execute the retrieval
	 *            of entries with key <code>k</code>.
	 * @param c
	 *            The {@link Chord} instance to be used for retrieval.
	 * @param k
	 *            The {@link Key} for which the entries should be retrieved.
	 * @return An instance of this.
	 */
	final static ChordRetrievalFutureImpl create(Executor exec, Chord c, Key k) {
		if (c == null) {
			throw new IllegalArgumentException(
					"ChordRetrievalFuture: chord instance must not be null!");
		}
		if (k == null) {
			throw new IllegalArgumentException(
					"ChordRetrievalFuture: key must not be null!");
		}

		ChordRetrievalFutureImpl future = new ChordRetrievalFutureImpl(c, k);
		exec.execute(future.getTask());
		return future;
	}

	/**
	 * Runnable to execute the retrieval of entries associated with key from
	 * chord.
	 * 
	 * @author sven
	 * @version 1.0
	 */
	private class RetrievalTask implements Runnable {

		/**
		 * The chord instance used for the operation that is associated with this. 
		 */
		private Chord chord = null;

		/**
		 * The key to retrieve the associated entries for. 
		 */
		private Key key = null;
		
		/**
		 * @param chord
		 * @param key
		 */
		private RetrievalTask(Chord chord, Key key) {
			this.chord = chord; 
			this.key = key; 
		}

		public void run() {
			try {
				setResult(this.chord.retrieve(this.key));
			} catch (Throwable t) {
				setThrowable(t);
			}
			setIsDone();
		}
	}

}
