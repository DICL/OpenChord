/***************************************************************************
 *                                                                         *
 *                         ChordRetrievalFuture.java 					   *
 *                            -------------------                          *
 *   date                 : 15.10.2005                                     *
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
package de.uniba.wiai.lspi.chord.service;

import java.io.Serializable;
import java.util.Set;

/**
 * This {@link ChordFuture} represents the invocation result of
 * {@link AsynChord#retrieveAsync(Key)}.
 * 
 * The result can be obtained with help of {@link #getResult()}.
 * 
 * @author sven
 * @version 1.0.5
 */
public interface ChordRetrievalFuture extends ChordFuture {

	/**
	 * Method to obtain the result of the retrieve operation associated with
	 * this. This method blocks the calling thread until the invocation of the
	 * retrieve operation has finished (either by obtaining a result or a
	 * {@link Throwable}/{@link Exception} that occured).
	 * 
	 * @return The entries that have been retrieved. Empty {@link Set} if no
	 *         entries have been found.
	 * @throws ServiceException
	 *             Thrown if the execution has not been successful. Contains the
	 *             {@link Throwable} that can be obtained by
	 *             {@link ChordFuture#getThrowable()} as cause.
	 * @throws InterruptedException
	 *             If the thread, which invokes this method, has been
	 *             interrupted while waiting for the result.
	 */
	public Set<Serializable> getResult() throws ServiceException,
			InterruptedException;
}