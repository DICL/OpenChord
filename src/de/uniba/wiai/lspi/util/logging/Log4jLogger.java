/***************************************************************************
 *                                                                         *
 *                             Log4jLogger.java                            *
 *                            -------------------                          *
 *   date                 : 26. März 2004, 12:37                           *
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Level;

/**
 * This is the standard logger for this framework.
 * 
 * It uses <a href="http://logging.apache.org/log4j/docs/">log4j </a> for
 * logging.
 * 
 * <br/>To configure this logger you have to set the system property
 * log4j.properties.file that points to a file containing log4j properties.
 * 
 * See log4j manual for details.
 * 
 * If the file cannot be found logging is set to ON for all classes.
 * 
 * @author sven
 * @version 1.0.5
 */
public class Log4jLogger extends Logger {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6061557202317126907L;

	/**
	 * The fully qualified class name of this logger.
	 */
	private String myFQN = this.getClass().getName();

	/**
	 * Reference to underlying log4j logger.
	 */
	private transient org.apache.log4j.Logger logger = null;

	/**
	 * Name of property to set the reference to the property file containing
	 * log4j properties.
	 */
	public final static String PROPERTIES_FILE_PROPERTY = "log4j.properties.file";

	/**
	 * Flag indicating if log4j has been configured before.
	 */
	private static boolean configured = false;

	/**
	 * Creates a new instance of Log4JLogger
	 * 
	 * @param _class
	 */
	public Log4jLogger(String _class) {
		super(_class);
		this.logger = org.apache.log4j.Logger.getLogger(_class);
		if (!configured) {
			configure();
		}
	}

	/**
	 * Configures log4j.
	 */
	private static void configure() {
		if (!configured) {
			configured = true;
			boolean usefile = false;
			java.net.URL configURL = null;
			try {
				configURL = ClassLoader.getSystemClassLoader().getResource(
						System.getProperty(PROPERTIES_FILE_PROPERTY));
				if (configURL == null) {
					java.io.File f = new java.io.File(System
							.getProperty(PROPERTIES_FILE_PROPERTY));
					usefile = f.exists();
					configURL = f.toURL();
				} else {
					usefile = true;
				}
			} catch (Exception e) {
				usefile = false;
			}
			if (usefile) {
				System.out.println("[" + Thread.currentThread().getName()
						+ "] " + "INFO " + Log4jLogger.class.getName()
						+ " - Configuring log4j with '"
						+ System.getProperty(PROPERTIES_FILE_PROPERTY) + "'.");
				try {
					if (System.getProperty(PROPERTIES_FILE_PROPERTY)
							.toLowerCase().endsWith(".xml")) {
						org.apache.log4j.xml.DOMConfigurator
								.configure(configURL);
					} else // usual properties file
					{
						org.apache.log4j.PropertyConfigurator
								.configure(configURL);
					}
					System.out.println("[" + Thread.currentThread().getName()
							+ "] " + "INFO " + Log4jLogger.class.getName()
							+ " - log4j configured with '"
							+ System.getProperty(PROPERTIES_FILE_PROPERTY)
							+ "'.");
					Logger.getLogger(Logger.class).debug("Logger initialized.");
				} catch (Throwable t) {
					System.out.println("[" + Thread.currentThread().getName()
							+ "] " + "ERROR " + Log4jLogger.class.getName()
							+ " - log4j could not be configured with '"
							+ System.getProperty(PROPERTIES_FILE_PROPERTY)
							+ "'.");
				}
			} else {
				// no log file was found.
				System.out
						.println("["
								+ Thread.currentThread().getName()
								+ "] "
								+ "INFO "
								+ Log4jLogger.class.getName()
								+ " - Could not find log4j properties file with filename '"
								+ System.getProperty(PROPERTIES_FILE_PROPERTY)
								+ "'.");
				System.out.println("[" + Thread.currentThread().getName()
						+ "] " + "INFO " + Log4jLogger.class.getName()
						+ " - Logging is On.");
				// configure with basic configurator
				org.apache.log4j.BasicConfigurator.configure();
				// and set logging to off.
				org.apache.log4j.Level level = org.apache.log4j.Level.ALL;
				org.apache.log4j.Logger.getRootLogger().setLevel(level);
			}
		}
	}

	@Override
	public void debug(Object msg) {
		this.logger.log(this.myFQN, org.apache.log4j.Level.DEBUG, msg, null);
	}

	@Override
	public void debug(Object msg, Throwable t) {
		this.logger.log(this.myFQN, org.apache.log4j.Level.DEBUG, msg, t);
	}

	@Override
	public void info(Object msg) {
		this.logger.log(this.myFQN, org.apache.log4j.Level.INFO, msg, null);
	}

	@Override
	public void info(Object msg, Throwable t) {
		this.logger.log(this.myFQN, org.apache.log4j.Level.INFO, msg, t);

	}

	@Override
	public void warn(Object msg) {
		this.logger.log(this.myFQN, org.apache.log4j.Level.WARN, msg, null);
	}

	@Override
	public void warn(Object msg, Throwable t) {
		this.logger.log(this.myFQN, org.apache.log4j.Level.WARN, msg, t);
	}

	@Override
	public void error(Object msg) {
		this.logger.log(this.myFQN, org.apache.log4j.Level.ERROR, msg, null);
	}

	@Override
	public void error(Object msg, Throwable t) {
		this.logger.log(this.myFQN, org.apache.log4j.Level.ERROR, msg, t);
	}

	@Override
	public void fatal(Object msg) {
		this.logger.log(this.myFQN, org.apache.log4j.Level.FATAL, msg, null);

	}

	@Override
	public void fatal(Object msg, Throwable t) {
		this.logger.log(this.myFQN, org.apache.log4j.Level.FATAL, msg, t);
	}

	@Override
	public boolean isEnabledFor(LogLevel l) {
		switch (l) {
		case DEBUG:
			return this.logger.isEnabledFor(Level.DEBUG);
		case INFO:
			return this.logger.isEnabledFor(Level.INFO);
		case WARN:
			return this.logger.isEnabledFor(Level.WARN);
		case ERROR:
			return this.logger.isEnabledFor(Level.ERROR);
		case FATAL:
			return this.logger.isEnabledFor(Level.FATAL);
		default:
			return false;
		}
	}

	private void readObject(ObjectInputStream inputStream)
			throws ClassNotFoundException, IOException {
		inputStream.defaultReadObject();
		this.logger = org.apache.log4j.Logger.getLogger(super.name);
		if (!configured) {
			configure();
		}
	}

	private void writeObject(ObjectOutputStream outputStream)
			throws IOException {
		outputStream.defaultWriteObject();
	}

}