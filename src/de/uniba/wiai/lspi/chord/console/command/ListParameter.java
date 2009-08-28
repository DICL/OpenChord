/***************************************************************************
 *                                                                         *
 *                            ListParameter.java                           *
 *                            -------------------                          *
 *   date                 : 10.09.2004, 07:51                              *
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

package de.uniba.wiai.lspi.chord.console.command;

import java.util.List;
import java.util.LinkedList;

import de.uniba.wiai.lspi.chord.console.Main;
import de.uniba.wiai.lspi.util.console.ConsoleException;

/**
 * This class represents a list of names, that are provided to some commands as
 * for example for {@link CreateNodes} with parameter
 * {@link CreateNodes#NAMES_PARAM}.
 * 
 * @author sven
 * @version 1.0.5
 */
class ListParameter {

	/**
	 * Character (as String) that separates the entries of the list, that is
	 * represented as a String.
	 */
	protected static final String SEPARATOR = "_";

	/**
	 * The name of the list parameter that is represented by this.
	 */
	private String paramName;

	/**
	 * The String representing the list. Entries are separated by
	 * {@link #SEPARATOR}.
	 */
	private String list;

	/**
	 * <code>true</code> if the list may be empty. 
	 */
	private boolean mayBeEmpty;

	/**
	 * Creates a new instance of ListParameter. This class represents a
	 * parameter that has been passed to a command in {@link Main} and takes a
	 * list of values as value for the parameter. The values have to be
	 * separated by {@link #SEPARATOR}
	 * 
	 * @param paramName1
	 * @param list1
	 * @param mayBeEmpty1
	 */
	ListParameter(String paramName1, String list1, boolean mayBeEmpty1) {
		this.paramName = paramName1;
		this.list = list1;
		this.mayBeEmpty = mayBeEmpty1;
	}

	/**
	 * Returns the list represented by {@link #list} , that was provided as
	 * parameter to the constructor, as a {@link List} that contains the single
	 * values, that are in the list, as Strings.
	 * 
	 * @return {@link List} containing the single values contained in
	 *         {@link #list} as Strings. An empty List is returned if no value
	 *         could be found.
	 * @throws ConsoleException
	 */
	List<String> getList() throws ConsoleException {
		if ((!this.mayBeEmpty)
				&& ((this.list == null) || (this.list.length() == 0))) {
			throw new ConsoleException("No value provided for "
					+ this.paramName + " parameter.");
		}

		if ((this.list == null) || (this.list.length() == 0)) {
			return new LinkedList<String>();
		}

		List<String> stringList = new LinkedList<String>();
		int separatorIndex = this.list.indexOf(SEPARATOR);
		while (separatorIndex != -1) {
			String name = this.list.substring(0, separatorIndex);
			if (!(name == null) && !(name.length() == 0)) {
				stringList.add(name);
			}
			/* remove name and separator */
			this.list = this.list.substring(separatorIndex + 1);
			separatorIndex = this.list.indexOf(SEPARATOR);
		}
		/* do not forget rest of String */
		if (!(this.list == null) && !(this.list.length() == 0)) {
			stringList.add(this.list);
		}

		if ((!this.mayBeEmpty) && (stringList.size() == 0)) {
			throw new ConsoleException("No value provided for "
					+ this.paramName + " parameter.");
		}

		return stringList;
	}

	/**
	 * @return The String representation of the list contained by this. 
	 */
	String getStringList() {
		return this.list;
	}

	/**
	 * @param toAdd
	 */
	void add(String toAdd) {
		if ((this.list == null) || (this.list.length() == 0)) {
			this.list = toAdd;
		} else {
			this.list += SEPARATOR + toAdd;
		}
	}

}
