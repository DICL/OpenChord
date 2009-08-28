/***************************************************************************
 *                                                                         *
 *                       SystemOutPrintlnLogger.java                       *
 *                            -------------------                          *
 *   date                 : 09.09.2004                                     *
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
package de.uniba.wiai.lspi.util.logging;

/**
 * Logger, that logs to <code>System.out</code>. 
 * 
 * @author karsten
 * @version 1.0.5
 */
public class SystemOutPrintlnLogger extends Logger {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2574986690956442182L;

	/**
	 * @param name
	 */
	SystemOutPrintlnLogger(String name) {
		super(name);
	}

	/**
	 * Log a message object with the <code>DEBUG</code> level.
	 *  
	 * @param msg The message object to log.
	 */
	public void debug(Object msg) {
		System.out.println(
			"DEBUG "
				+ this.name
				+ " "
				+ ": "
				+ msg);
	}

	/**
	 * Log a message object with the <code>DEBUG</code> level including the
	 * stack trace of the Throwable t passed as parameter.
	 * 
	 * @param msg The message object to log.
	 * @param t The exception to log, including its stack trace.
	 */
	public void debug(Object msg, Throwable t) {
		System.out.println(
			"DEBUG "
				+ this.name
				+ " "
				+ ": "
				+ msg);
		t.printStackTrace();
	}

	/**
	 * Log a message object with the <code>INFO</code> level.
	 *  
	 * @param msg The message object to log.
	 */
	public void info(Object msg) {
		System.out.println(
			"INFO  "
				+ this.name
				+ " "
				+ ": "
				+ msg);
	}

	/**
	 * Log a message object with the <code>INFO</code> level including the
	 * stack trace of the Throwable t passed as parameter.
	 * 
	 * @param msg The message object to log.
	 * @param t The exception to log, including its stack trace.
	 */
	public void info(Object msg, Throwable t) {
		System.out.println(
			"INFO  "
				+ this.name
				+ " "
				+ ": "
				+ msg);
		t.printStackTrace();
	}

	/**
	 * Log a message object with the <code>WARN</code> level.
	 *  
	 * @param msg The message object to log.
	 */
	public void warn(Object msg) {
		System.out.println(
			"WARN  "
				+ this.name
				+ " "
				+ ": "
				+ msg);
	}

	/**
	 * Log a message object with the <code>WARN</code> level including the
	 * stack trace of the Throwable t passed as parameter.
	 * 
	 * @param msg The message object to log.
	 * @param t The exception to log, including its stack trace.
	 */
	public void warn(Object msg, Throwable t) {
		System.out.println(
			"WARN  "
				+ this.name
				+ " "
				+ ": "
				+ msg);
		t.printStackTrace();
	}

	/**
	 * Log a message object with the <code>ERROR</code> level.
	 *  
	 * @param msg The message object to log.
	 */
	public void error(Object msg) {
		System.out.println(
			"ERROR "
				+ this.name
				+ " "
				+ ": "
				+ msg);
	}

	/**
	 * Log a message object with the <code>ERROR</code> level including the
	 * stack trace of the Throwable t passed as parameter.
	 * 
	 * @param msg The message object to log.
	 * @param t The exception to log, including its stack trace.
	 */
	public void error(Object msg, Throwable t) {
		System.out.println(
			"ERROR "
				+ this.name
				+ " "
				+ ": "
				+ msg);
		t.printStackTrace();
	}

	/**
	 * Log a message object with the <code>FATAL</code> level.
	 *  
	 * @param msg The message object to log.
	 */
	public void fatal(Object msg) {
		System.out.println(
			"FATAL "
				+ this.name
				+ " "
				+ ": "
				+ msg);
	}

	/**
	 * Log a message object with the <code>FATAL</code> level including the
	 * stack trace of the Throwable t passed as parameter.
	 * 
	 * @param msg The message object to log.
	 * @param t The exception to log, including its stack trace.
	 */
	public void fatal(Object msg, Throwable t) {
		System.out.println(
			"FATAL "
				+ this.name
				+ " "
				+ ": "
				+ msg);
		t.printStackTrace();
	}

	@Override
	public boolean isEnabledFor(LogLevel l) {
		return true; 
	}
}