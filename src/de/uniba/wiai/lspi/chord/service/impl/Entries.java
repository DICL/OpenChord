/***************************************************************************
 *                                                                         *
 *                               Entries.java                              *
 *                            -------------------                          *
 *   date                 : 28.02.2005                                     *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *   			    		karsten.loesing@uni-bamberg.de                 *
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

package de.uniba.wiai.lspi.chord.service.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.util.logging.Logger;

/**
 * Stores entries for the local node in a local hash table and provides methods
 * for accessing them. It IS allowed, that multiple objects of type
 * {@link Entry} with same {@link ID} are stored!
 * 
 * @author Karsten Loesing, Sven Kaffille
 * @version 1.0.5
 * 
 */

/*
 * 23.12.2006. Fixed synchronization. The Map<ID, Set<Entry>> entries must be
 * synchronized with a synchronized statement, when executing several methods
 * that depend on each other. This would also apply to the internal Set<Entry>
 * if it were not only used in the same synchronized statements for entries,
 * which than functions as a synchronization point. It must also be locked by a
 * synchronized statement, when iterating over it. TODO: What about fairness?
 * sven
 */
final class Entries {

	/**
	 * Object logger.
	 */
	private final static Logger logger = Logger.getLogger(Entries.class);

	private final static boolean debugEnabled = logger
			.isEnabledFor(Logger.LogLevel.DEBUG);

	/**
	 * Local hash table for entries. Is synchronized, st. methods do not have to
	 * be synchronized.
	 */
	private Map<ID, Set<Entry>> entries = null;

	/**
	 * Creates an empty repository for entries.
	 */
	Entries(){ 
		this.entries = Collections
				.synchronizedMap(new TreeMap<ID, Set<Entry>>());
	}

	/**
	 * Stores a set of entries to the local hash table.
	 * 
	 * @param entriesToAdd
	 *            Set of entries to add to the repository.
	 * @throws NullPointerException
	 *             If set reference is <code>null</code>.
	 */
	final void addAll(Set<Entry> entriesToAdd) {

		if (entriesToAdd == null) {
			NullPointerException e = new NullPointerException(
					"Set of entries to be added to the local hash table may "
							+ "not be null!");
			Entries.logger.error("Null pointer", e);
			throw e;
		}

		for (Entry nextEntry : entriesToAdd) {
			this.add(nextEntry);
		}

		if (debugEnabled) {
			Entries.logger.debug("Set of entries of length "
					+ entriesToAdd.size() + " was added.");
		}
	}

	/**
	 * Stores one entry to the local hash table.
	 * 
	 * @param entryToAdd
	 *            Entry to add to the repository.
	 * @throws NullPointerException
	 *             If entry to add is <code>null</code>.
	 */
	final void add(Entry entryToAdd) {
		
		if (entryToAdd == null) {
			NullPointerException e = new NullPointerException(
					"Entry to add may not be null!");
			Entries.logger.error("Null pointer", e);
			throw e;
		}

		Set<Entry> values;
		synchronized (this.entries) {
			if (this.entries.containsKey(entryToAdd.getId())) {
				values = this.entries.get(entryToAdd.getId());
			} else {
				values = new HashSet<Entry>();
				this.entries.put(entryToAdd.getId(), values);
			}
			values.add(entryToAdd);
		}
		if (debugEnabled) {
			Entries.logger.debug("Entry was added: " + entryToAdd);
		}
	}

	/**
	 * Removes the given entry from the local hash table.
	 * 
	 * @param entryToRemove
	 *            Entry to remove from the hash table.
	 * @throws NullPointerException
	 *             If entry to remove is <code>null</code>.
	 */
	final void remove(Entry entryToRemove) {
		
		if (entryToRemove == null) {
			NullPointerException e = new NullPointerException(
					"Entry to remove may not be null!");
			Entries.logger.error("Null pointer", e);
			throw e;
		}

		synchronized (this.entries) {
			if (this.entries.containsKey(entryToRemove.getId())) {
				Set<Entry> values = this.entries.get(entryToRemove.getId());
				values.remove(entryToRemove);
				if (values.size() == 0) {
					this.entries.remove(entryToRemove.getId());
				}
			}
		}
		if (debugEnabled) {
			Entries.logger.debug("Entry was removed: " + entryToRemove);
		}
	}

	/**
	 * Returns a set of entries matching the given ID. If no entries match the
	 * given ID, an empty set is returned.
	 * 
	 * @param id
	 *            ID of entries to be returned.
	 * @throws NullPointerException
	 *             If given ID is <code>null</code>.
	 * @return Set of matching entries. Empty Set if no matching entries are
	 *         available.
	 */
	final Set<Entry> getEntries(ID id) {

		if (id == null) {
			NullPointerException e = new NullPointerException(
					"ID to find entries for may not be null!");
			Entries.logger.error("Null pointer", e);
			throw e;
		}
		synchronized (this.entries) {
			/*
			 * This has to be synchronized as the test if the map contains a set
			 * associated with id can succeed and then the thread may hand
			 * control over to another thread that removes the Set belonging to
			 * id. In that case this.entries.get(id) would return null which
			 * would break the contract of this method.
			 */
			if (this.entries.containsKey(id)) {
				Set<Entry> entriesForID = this.entries.get(id);
				/*
				 * Return a copy of the set to avoid modification of Set stored
				 * in this.entries from outside this class. (Avoids also
				 * modifications concurrent to iteration over the Set by a
				 * client of this class.
				 */
				if (debugEnabled) {
					Entries.logger.debug("Returning entries " + entriesForID);
				}
				return new HashSet<Entry>(entriesForID);
			}
		}
		if (debugEnabled) {
			Entries.logger.debug("No entries available for " + id
					+ ". Returning empty set.");
		}
		return new HashSet<Entry>();
	}

	/**
	 * Returns all entries in interval, excluding lower bound, but including
	 * upper bound
	 * 
	 * @param fromID
	 *            Lower bound of IDs; entries matching this ID are NOT included
	 *            in result.
	 * @param toID
	 *            Upper bound of IDs; entries matching this ID ARE included in
	 *            result.
	 * @throws NullPointerException
	 *             If either or both of the given ID references have value
	 *             <code>null</code>.
	 * @return Set of matching entries.
	 */
	final Set<Entry> getEntriesInInterval(ID fromID, ID toID) {

		if (fromID == null || toID == null) {
			NullPointerException e = new NullPointerException(
					"Neither of the given IDs may have value null!");
			Entries.logger.error("Null pointer", e);
			throw e;
		}

		Set<Entry> result = new HashSet<Entry>();

		synchronized (this.entries) {
			for (ID nextID : this.entries.keySet()) {
				if (nextID.isInInterval(fromID, toID)) {
					Set<Entry> entriesForID = this.entries.get(nextID);
					for (Entry entryToAdd : entriesForID) {
						result.add(entryToAdd);
					}
				}
			}
		}

		// add entries matching upper bound
		result.addAll(this.getEntries(toID));

		return result;
	}

	/**
	 * Removes the given entries from the local hash table.
	 * 
	 * @param toRemove
	 *            Set of entries to remove from local hash table.
	 * @throws NullPointerException
	 *             If the given set of entries is <code>null</code>.
	 */
	final void removeAll(Set<Entry> toRemove) {

		if (toRemove == null) {
			NullPointerException e = new NullPointerException(
					"Set of entries may not have value null!");
			Entries.logger.error("Null pointer", e);
			throw e;
		}

		for (Entry nextEntry : toRemove) {
			this.remove(nextEntry);
		}

		if (debugEnabled) {
			Entries.logger.debug("Set of entries of length " + toRemove.size()
					+ " was removed.");
		}
	}

	/**
	 * Returns an unmodifiable map of all stored entries.
	 * 
	 * @return Unmodifiable map of all stored entries.
	 */
	final Map<ID, Set<Entry>> getEntries() {
		return Collections.unmodifiableMap(this.entries);
	}

	/**
	 * Returns the number of stored entries.
	 * 
	 * @return Number of stored entries.
	 */
	final int getNumberOfStoredEntries() {
		return this.entries.size();
	}

	/**
	 * Returns a formatted string of all entries stored in the local hash table.
	 * 
	 * @return String representation of all stored entries.
	 */
	public final String toString() {
		StringBuilder result = new StringBuilder("Entries:\n");
		for (Map.Entry<ID, Set<Entry>> entry : this.entries.entrySet()) {
			result.append("  key = " + entry.getKey().toString()
					+ ", value = " + entry.getValue() + "\n");
		}
		return result.toString();
	}
}