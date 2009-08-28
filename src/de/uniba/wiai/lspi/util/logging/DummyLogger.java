/***************************************************************************
 *                                                                         *
 *                             DummyLogger.java                            *
 *                            -------------------                          *
 *   date                 : 26. Januar 2004, 18:35                         *
 *   copyright            : (C) 2004 Distributed and Mobile Systems Group  *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : {jens.bruhn|sven.kaffille}@uni-bamberg.de *
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

package de.uniba.wiai.lspi.util.logging;

/** This is a dummy logger that does no logging. 
 * All methods are empty. 
 *
 * @author  sven
 * @version 1.0.5
 */
public class DummyLogger extends Logger {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -4992475317252236790L;

	/** Creates a new instance of DummyLogger 
     * @param _class */
    DummyLogger(String _class) {
        super(_class);
    }
    
    /** Creates a new instance of DummyLogger 
     * @param _class */
    DummyLogger(Class _class) {
        this(_class.getName());
    }
    
    public void debug(Object msg) {
    	/*
    	 * This logger does nothing. 
    	 */
    }
    
    public void debug(Object msg, Throwable t) {
    	/*
    	 * This logger does nothing. 
    	 */
    }
    
    public void error(Object msg) {
    	/*
    	 * This logger does nothing. 
    	 */
    }
    
    public void error(Object msg, Throwable t) {
    	/*
    	 * This logger does nothing. 
    	 */
    }
    
    public void fatal(Object msg) {
    	/*
    	 * This logger does nothing. 
    	 */
    }
    
    public void fatal(Object msg, Throwable t) {
    	/*
    	 * This logger does nothing. 
    	 */
    }
    
    public void info(Object msg) {
    	/*
    	 * This logger does nothing. 
    	 */
    }
    
    public void info(Object msg, Throwable t) {
    	/*
    	 * This logger does nothing. 
    	 */
    }
    
    public void warn(Object msg) {
    	/*
    	 * This logger does nothing. 
    	 */
    }
    
    public void warn(Object msg, Throwable t) {
    	/*
    	 * This logger does nothing. 
    	 */
    }

	@Override
	public boolean isEnabledFor(LogLevel l) {
		return false;
	}
    
}
