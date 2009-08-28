/***************************************************************************
 *                                                                         *
 *                            ConsoleException.java                        *
 *                            -------------------                          *
 *   date                 : 16. Mai 2003, 18:26                            *
 *   copyright            : (C) 2004 Distributed and Mobile Systems Group  *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
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

package de.uniba.wiai.lspi.util.console;

/**
 *
 * @author  sven
 * @version 1.0.5
 */
public class ConsoleException extends Exception {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 633088623740052662L;

	/** Creates a new instance of ConsoleException
     * @param message
     */
    public ConsoleException(String message) {
        super(message);
    }
    
    /**
     * @param message
     * @param cause
     */
    public ConsoleException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
