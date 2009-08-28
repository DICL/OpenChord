/***************************************************************************
 *                                                                         *
 *                        EndpointStateListener.java                       *
 *                            -------------------                          *
 *   date                 : 01.09.2004, 17:18                              *
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

package de.uniba.wiai.lspi.chord.com;

/**
 * This interface must be implemented by classes that want to be notified 
 * about state changes of an {@link Endpoint}.  
 * @author sven
 * @version 1.0.5
 */
public interface EndpointStateListener {

	/**
	 * Notify this listener that the endpoint changed it state 
	 * to <code>newState</code>. 
	 * 
	 * @param newState The new state of the endpoint. 
	 */
	public void notify(int newState);

}
