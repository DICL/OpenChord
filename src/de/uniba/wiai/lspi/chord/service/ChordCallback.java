/***************************************************************************
 *                                                                         *
 *                            ChordCallback.java                           *
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
 * <p>
 * This is the interface that must be implemented by classes 
 * that can be used as callback for method invocations on {@link AsynChord}.
 * </p> 
 * 
 * <p>
 * An instance of this must be passed as parameter to on of the 
 * methods:
 * <ul>
 * <li>{@link AsynChord#insert(Key, Serializable, ChordCallback)}</li>
 * <li>{@link AsynChord#remove(Key, Serializable, ChordCallback)}</li>
 * <li>{@link AsynChord#retrieve(Key, ChordCallback)}</li>
 * </ul>
 * On termination of those methods the corresponding callback method 
 * on this is called. These methods are: 
 * <ul>
 * <li>{@link #inserted(Key, Serializable, Throwable)}</li>
 * <li>{@link #removed(Key, Serializable, Throwable)}</li>
 * <li>{@link #retrieved(Key, Set, Throwable)}</li>
 * </ul>
 * The {@link Throwable} parameter of these methods is <code>null</code> 
 * if the corresponding method has been executed successfully. 
 * </p>
 * 
 *  
 * @author sven
 * @version 1.0.5
 *
 */
public interface ChordCallback {

	/**
	 * This is the callback method for retrieval of values 
	 * associated with <code>key</code>. This method is called 
	 * when an invocation of 
	 * {@link AsynChord#retrieve(Key, ChordCallback)} has finished.   
	 * 
	 * @param key The {@link Key} that has been used for the retrieval.   
	 * @param entries The retrieved entries. Empty Set, if no values 
	 * are associated with <code>key</code>. 
	 * @param t Any {@link Throwable} that occured during execution 
	 * of {@link AsynChord#retrieve(Key, ChordCallback)}. 
	 * This is <code>null</code> if retrieval of <code>key</code> 
	 * was succesful.
	 */
	public void retrieved(Key key, Set<Serializable> entries, Throwable t);
	
	/**
	 * This method is called, when a call to 
	 * {@link AsynChord#insert(Key, Serializable, ChordCallback)}
	 * has been finished. 
	 * 
	 * @param key The {@link Key} that should be used for insertion.
	 * @param entry The entry that should be inserted. 
	 * @param t Any {@link Throwable} that occured during execution 
	 * of {@link AsynChord#insert(Key, Serializable, ChordCallback)}. 
	 * This is <code>null</code> if insertion of <code>key</code> and 
	 * <code>entry</code> was succesful.  
	 */
	public void inserted(Key key, Serializable entry, Throwable t); 
	
	/**
	 * This is the callback method for removal of the <code>entry</code> 
	 * with <code>key</code>. 
	 * 
	 * @param key The {@link Key} of the entry that should be removed. 
	 * @param entry The entry that should be removed. 
	 * @param t Any {@link Throwable} that occured during execution 
	 * of {@link AsynChord#remove(Key, Serializable, ChordCallback)}. 
	 * This is <code>null</code> if removal of 
	 * <code>entry</code> was succesful.
	 */
	public void removed(Key key, Serializable entry, Throwable t); 
	
}
