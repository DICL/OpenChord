/***************************************************************************
 *                                                                         *
 *                             ChordFuture.java                            *
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

/**
 * <p>
 * This interface represents the result of an asynchronouse invocation on an
 * implementation of {@link AsynChord}.
 * </p>
 * <p>
 * The methods:
 * <ul>
 * <li>{@link AsynChord#insertAsync(Key, Serializable)}</li>
 * <li>{@link AsynChord#removeAsync(Key, Serializable)}</li>
 * <li>{@link AsynChord#retrieveAsync(Key)}</li>
 * </ul>
 * return immediately and return an instance of this, which can be used later on
 * to check if the execution of an insertion, removal, or retrieval has been
 * completed.
 * </p>
 * 
 * @author sven
 * @version 1.0.5
 */
public interface ChordFuture {

	/**
	 * 
	 * @return Any Throwable that occured during execution of the method
	 *         associated with this. May be <code>null</code>. If
	 *         {@link #isDone()} returns <code>true</code> and this returns
	 *         <code>null</code> the associated method has been executed
	 *         successfully.
	 */
	public abstract Throwable getThrowable();

	/**
	 * Method to test if the method associated with this has been finished.
	 * This method does not block the calling thread.  
	 * 
	 * @return <code>true</code> if the method associated with this has
	 *         finished successfully.
	 * @throws ServiceException
	 *             Thrown if the execution has not been successful. Contains the
	 *             {@link Throwable} that can be obtained by
	 *             {@link #getThrowable()} as cause.
	 */
	public abstract boolean isDone() throws ServiceException;

	/**
	 * This method blocks the calling thread until the execution of the method
	 * associated with this has been finished.
	 * 
	 * @throws ServiceException
	 *             Thrown if the execution has not been successful. Contains the
	 *             {@link Throwable} that can be obtained by
	 *             {@link #getThrowable()} as cause.
	 * 
	 * @throws InterruptedException
	 *             Occurs if the thread waiting with help of this method has
	 *             been interrupted.
	 */
	public abstract void waitForBeingDone() throws ServiceException,
			InterruptedException;

}