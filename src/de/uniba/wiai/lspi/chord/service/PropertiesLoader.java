/***************************************************************************
 *                                                                         *
 *                           PropertiesLoader.java                         *
 *                            -------------------                          *
 *   date                 : 10.08.2005                                     *
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

import java.io.IOException;
import java.util.Properties;

/**
 * This class is responsible for loading all properties of a given property file
 * and for setting them as Java properties for this JVM. Unless otherwise stated
 * this class tries to load the property file named
 * {@link #STANDARD_PROPERTY_FILE} which must be located in the classpath. If
 * another file should be loaded, its file name has to be set as value for
 * property {@link #PROPERTY_WHERE_TO_FIND_PROPERTY_FILE}.
 * 
 * @author karsten
 * @version 1.0.5
 */
public final class PropertiesLoader {

	/**
	 * Flag that indicates if the properties have been loaded. 
	 */
	private static boolean loaded = false; 
	
	/**
	 * Prevent instantiation of this class by private constructor. 
	 */
	private PropertiesLoader() {
		// not instantiable
	}

	/**
	 * Name of property which has to be set for loading a specific property
	 * file.
	 */
	public final static String PROPERTY_WHERE_TO_FIND_PROPERTY_FILE = "chord.properties.file";

	/**
	 * File name of property file which is loaded, if no other file is
	 * specified.
	 */
	public final static String STANDARD_PROPERTY_FILE = "chord.properties";

	/**
	 * Reads the properties from property file. Either uses the standard
	 * property file or, if specified, a given property file. In either case the
	 * property file must be located in the classpath.
	 */
	public static void loadPropertyFile() {
		
		if (loaded) {
			throw new IllegalStateException("Properties have already been loaded!"); 
		}
		loaded = true; 
		
		// if property file was specified, use it instead of standard property
		// file

		String file = STANDARD_PROPERTY_FILE;

		if (System.getProperty(PROPERTY_WHERE_TO_FIND_PROPERTY_FILE) != null
				&& System.getProperty(PROPERTY_WHERE_TO_FIND_PROPERTY_FILE)
						.length() != 0) {
			file = System.getProperty(PROPERTY_WHERE_TO_FIND_PROPERTY_FILE);
		}

		// load property file
		try {
			Properties props = System.getProperties();
			props.load(ClassLoader.getSystemResourceAsStream(file));
			System.setProperties(props);
		} catch (IOException e) {
			throw new RuntimeException("Property file was not found: " + file
					+ "! It must be located in the CLASSPATH and "
					+ "either be named 'chord.properties' or its name "
					+ "be specified by -Dchord.properties.file='filename'", e);
		} catch (NullPointerException e) {
			throw new RuntimeException("Property file was not found: " + file
					+ "! It must be located in the CLASSPATH and "
					+ "either be named 'chord.properties' or its name "
					+ "be specified by -Dchord.properties.file='filename'", e);
		}

	}
}
