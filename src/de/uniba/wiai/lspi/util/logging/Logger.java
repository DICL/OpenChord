/***************************************************************************
 *                                                                         *
 *                               Logger.java                               *
 *                            -------------------                          *
 *   date                 : 26. Januar 2004, 18:32                         *
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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the abstract class to be implemented by a logger that should be
 * used within the framework. <br/> A reference to a logger can be obtained by
 * {@link #getLogger(java.lang.String)} or {@link #getLogger(java.lang.Class)}.
 * <br/> You can use a custom logger by extending this class and setting the
 * system property de.uniba.wiai.lspi.util.logging.logger.class to the FQN of
 * your logger class. <br/> {@link #getLogger(java.lang.String)} or
 * {@link #getLogger(java.lang.Class)} then try to instantiate your logger. If
 * that fails one of the standard loggers is created. The standard logger is
 * {@link Log4jLogger}. If log4j is not availalbe from the classpath
 * {@link DummyLogger} is the standard logger.
 * 
 * If you set the system property de.uniba.wiai.lspi.util.logging.off to true
 * {@link DummyLogger} is the standard logger.
 * 
 * <br/> When implementing a custom logger you have to consider that the logger
 * may be transferred over the network when classes are passed as parameters to
 * remote methods. The custom logger must have a public constructor with
 * {@link java.lang.String} as parameter.
 * 
 * @author sven
 * @version 1.0.5
 */
public abstract class Logger implements java.io.Serializable {

	public static enum LogLevel{ DEBUG, INFO, WARN, ERROR, FATAL }
	
	/**
	 * Name of property that specifies the name of the logger class to be used.
	 */
	public final static String LOGGER_CLASS_NAME_PROPERTY_NAME = "de.uniba.wiai.lspi.util.logging.logger.class";

	/**
	 * Name of property that defines if logging is off.
	 */
	public final static String LOGGING_OFF_PROPERTY_NAME = "de.uniba.wiai.lspi.util.logging.off";

	/**
	 * The name of the standard logger class.
	 */
	private final static String STANDARD_LOGGER_CLASS = Log4jLogger.class
			.getName();

	/**
	 * The name of the class, for which this is the logger.
	 */
	protected String name = "";

	/**
	 * Map containing instances of loggers. Key: name of the logger. Value: The
	 * logger itself.
	 */
	private static final Map<String, Logger> loggerInstances = new HashMap<String, Logger>();

	/**
	 * Creates a new instance of Logger
	 * 
	 * @param name
	 */
	protected Logger(String name) {
		this.name = name;
	}

	/**
	 * @param _class
	 * @return The logger for the given class.
	 */
	public static Logger getLogger(Class _class) {
		return getLogger(_class.getName());
	}

	/**
	 * @param name
	 * @return The logger with the given name.
	 */
	public synchronized static Logger getLogger(String name) {

		String loggingOff = System.getProperty(LOGGING_OFF_PROPERTY_NAME);
		boolean logOff = false; 

		if ((loggingOff != null) && (loggingOff.equalsIgnoreCase("true"))) {
			name = Logger.class.getName();
			logOff = true; 
		}

		Logger logger = Logger.loggerInstances.get(name);
		if (logger != null) {
			return logger;
		} else {

			if (!logOff) {
				String loggerClassName = System
						.getProperty(LOGGER_CLASS_NAME_PROPERTY_NAME);
				if ((loggerClassName == null) || (loggerClassName.equals(""))) {
					loggerClassName = STANDARD_LOGGER_CLASS;
				}
				try {
					Class loggerClass = Class.forName(loggerClassName);
		
					Constructor cons = loggerClass
							.getConstructor(new Class[] { java.lang.String.class });
					logger = (Logger) cons.newInstance(new Object[] { name });
				} catch (Throwable t) {
					/*
					 * Exception occured during instantiation of custom logger or
					 * Log4jLogger. Create dummy logger.
					 */
					System.setProperty(LOGGING_OFF_PROPERTY_NAME, "true"); 
					logger = getLogger(name); 
				}
			} else {
				logger = new DummyLogger(name); 
			}
			Logger.loggerInstances.put(name, logger);
			return logger;
		}
	}

	public abstract boolean isEnabledFor(LogLevel l); 
	/**
	 * @param msg
	 */
	public abstract void debug(Object msg);

	/**
	 * @param msg
	 * @param t
	 */
	public abstract void debug(Object msg, Throwable t);

	/**
	 * @param msg
	 */
	public abstract void info(Object msg);

	/**
	 * @param msg
	 * @param t
	 */
	public abstract void info(Object msg, Throwable t);

	/**
	 * @param msg
	 */
	public abstract void warn(Object msg);

	/**
	 * @param msg
	 * @param t
	 */
	public abstract void warn(Object msg, Throwable t);

	/**
	 * @param msg
	 */
	public abstract void error(Object msg);

	/**
	 * @param msg
	 * @param t
	 */
	public abstract void error(Object msg, Throwable t);

	/**
	 * @param msg
	 */
	public abstract void fatal(Object msg);

	/**
	 * @param msg
	 * @param t
	 */
	public abstract void fatal(Object msg, Throwable t);

}
