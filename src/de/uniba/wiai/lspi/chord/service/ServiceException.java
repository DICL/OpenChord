/***************************************************************************
 *                                                                         *
 *                          ServiceException.java                          *
 *                            -------------------                          *
 *   date                 : 15.08.2004                                     *
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

/**
 * Whenever this exception is thrown, an error has occured which cannot be
 * resolved by the service layer.
 * 
 * @author Sven Kaffille, Karsten Loesing
 * @version 1.0.5
 */
public final class ServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1039630030458301201L;

	/**
	 * Creates a new service exception with the given description.
	 * 
	 * @param message Description for the user.
	 */
	public ServiceException(String message) {
        super(message);
    }
    
	/**
	 * Creates a new service exception with the given description.
	 * 
	 * @param message Description for the user.
	 * @param cause Throwable which led to throwing this exception.
	 */
	public ServiceException(String message, Throwable cause) {
        super(message, cause); 
    }

}
