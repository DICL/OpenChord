/***************************************************************************
 *                                                                         *
 *                             ChordFuture.java                            *
 *                            -------------------                          *
 *   date                 : 16.08.2005                                     *
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

import static de.uniba.wiai.lspi.util.logging.Logger.LogLevel.DEBUG;
import static de.uniba.wiai.lspi.util.logging.Logger.LogLevel.INFO;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.Proxy;
import de.uniba.wiai.lspi.chord.com.RefsAndEntries;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.AsynChord;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ChordCallback;
import de.uniba.wiai.lspi.chord.service.ChordFuture;
import de.uniba.wiai.lspi.chord.service.ChordRetrievalFuture;
import de.uniba.wiai.lspi.chord.service.Key;
import de.uniba.wiai.lspi.chord.service.Report;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.util.logging.Logger;

/**
 * Implements all operations which can be invoked on the local node.
 * 
 * @author Karsten Loesing
 * @version 1.0.5
 */
public final class ChordImpl implements Chord, Report, AsynChord {

	/**
	 * Number of threads to allow concurrent invocations of asynchronous
	 * methods. e.g. {@link ChordImpl#insertAsync(Key, Serializable)}.
	 */
	private static final int ASYNC_CALL_THREADS = Integer.parseInt(System
			.getProperty(ChordImpl.class.getName() + ".AsyncThread.no"));

	/**
	 * Time in seconds until the stabilize task is started for the first time.
	 */
	private static final int STABILIZE_TASK_START = Integer
			.parseInt(System
					.getProperty("de.uniba.wiai.lspi.chord.service.impl.ChordImpl.StabilizeTask.start"));

	/**
	 * Time in seconds between two invocations of the stabilize task.
	 */
	private static final int STABILIZE_TASK_INTERVAL = Integer
			.parseInt(System
					.getProperty("de.uniba.wiai.lspi.chord.service.impl.ChordImpl.StabilizeTask.interval"));

	/**
	 * Time in seconds until the fix finger task is started for the first time.
	 */
	private static final int FIX_FINGER_TASK_START = Integer
			.parseInt(System
					.getProperty("de.uniba.wiai.lspi.chord.service.impl.ChordImpl.FixFingerTask.start"));

	/**
	 * Time in seconds between two invocations of the fix finger task.
	 */
	private static final int FIX_FINGER_TASK_INTERVAL = Integer
			.parseInt(System
					.getProperty("de.uniba.wiai.lspi.chord.service.impl.ChordImpl.FixFingerTask.interval"));

	/**
	 * Time in seconds until the check predecessor task is started for the first
	 * time.
	 */
	private static final int CHECK_PREDECESSOR_TASK_START = Integer
			.parseInt(System
					.getProperty("de.uniba.wiai.lspi.chord.service.impl.ChordImpl.CheckPredecessorTask.start"));

	/**
	 * Time in seconds between two invocations of the check predecessor task.
	 */
	private static final int CHECK_PREDECESSOR_TASK_INTERVAL = Integer
			.parseInt(System
					.getProperty("de.uniba.wiai.lspi.chord.service.impl.ChordImpl.CheckPredecessorTask.interval"));

	/**
	 * Number of references in the successor list.
	 */
	private static final int NUMBER_OF_SUCCESSORS = (Integer
			.parseInt(System
					.getProperty("de.uniba.wiai.lspi.chord.service.impl.ChordImpl.successors")) < 1) ? 1
			: Integer
					.parseInt(System
							.getProperty("de.uniba.wiai.lspi.chord.service.impl.ChordImpl.successors"));

	/**
	 * Object logger.
	 */
	protected Logger logger;

	/**
	 * Reference on that part of the node implementation which is accessible by
	 * other nodes; if <code>null</code>, this node is not connected
	 */
	private NodeImpl localNode;

	/**
	 * Entries stored at this node, including replicas.
	 */
	private Entries entries;

	/**
	 * Executor service for local maintenance tasks.
	 */
	private ScheduledExecutorService maintenanceTasks;

	/**
	 * Executor service for asynch requests.
	 */
	private ExecutorService asyncExecutor;

	/**
	 * ThreadFactory used with Executor services.
	 * 
	 * @author sven
	 * 
	 */
	private static class ChordThreadFactory implements
			java.util.concurrent.ThreadFactory {

		private String executorName;

		ChordThreadFactory(String executorName) {
			this.executorName = executorName;
		}

		public Thread newThread(Runnable r) {
			Thread newThread = new Thread(r);
			newThread.setName(this.executorName + "-" + newThread.getName());
			return newThread;
		}

	}

	/**
	 * References to remote nodes.
	 */
	protected References references;

	/**
	 * Reference on hash function (singleton instance).
	 */
	private HashFunction hashFunction;

	/**
	 * This node's URL.
	 */
	private URL localURL;

	/**
	 * This node's ID.
	 */
	private ID localID;

	/* constructor */

	/**
	 * Creates a new instance of ChordImpl which initially is disconnected.
	 * Constructor is hidden. Only constructor.
	 */
	public ChordImpl() {
		this.logger = Logger.getLogger(ChordImpl.class.getName()
				+ ".unidentified");
		this.logger.debug("Logger initialized.");

		this.maintenanceTasks = new ScheduledThreadPoolExecutor(3,
				new ChordThreadFactory("MaintenanceTaskExecution"));
		this.asyncExecutor = Executors.newFixedThreadPool(
				ChordImpl.ASYNC_CALL_THREADS, new ChordThreadFactory(
						"AsynchronousExecution"));
		this.hashFunction = HashFunction.getHashFunction();
		logger.info("ChordImpl initialized!");
	}

	/**
	 * @return The Executor executing asynchronous request.
	 */
	final Executor getAsyncExecutor() {
		if (this.asyncExecutor == null) {
			throw new NullPointerException("ChordImpl.asyncExecutor is null!");
		}
		return this.asyncExecutor;
	}

	/* implementation of Chord interface */

	public final URL getURL() {
		return this.localURL;
	}

	public final void setURL(URL nodeURL) {

		if (nodeURL == null) {
			NullPointerException e = new NullPointerException(
					"Cannot set URL to null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		if (this.localNode != null) {
			IllegalStateException e = new IllegalStateException(
					"URL cannot be set after creating or joining a network!");
			this.logger.error("Illegal state.", e);
			throw e;
		}

		this.localURL = nodeURL;

		this.logger.info("URL was set to " + nodeURL);
	}

	public final ID getID() {
		return this.localID;
	}

	public final void setID(ID nodeID) {

		if (nodeID == null) {
			NullPointerException e = new NullPointerException(
					"Cannot set ID to null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		if (this.localNode != null) {
			IllegalStateException e = new IllegalStateException(
					"ID cannot be set after creating or joining a network!");
			this.logger.error("Illegal state.", e);
			throw e;
		}

		this.localID = nodeID;
		this.logger = Logger.getLogger(ChordImpl.class.getName() + "."
				+ this.localID);
	}

	public final void create() throws ServiceException {

		// is node already connected?
		if (this.localNode != null) {
			throw new ServiceException(
					"Cannot create network; node is already connected!");
		}

		// has nodeURL been set?
		if (this.localURL == null) {
			throw new ServiceException("Node URL is not set yet!");
		}

		// if necessary, generate nodeID out of nodeURL
		if (this.getID() == null) {
			this.setID(this.hashFunction.createUniqueNodeID(this.localURL));
		}

		// establish connection
		this.createHelp();

	}

	public final void create(URL localURL1) throws ServiceException {

		// check if parameters are valid
		if (localURL1 == null) {
			throw new NullPointerException(
					"At least one parameter is null which is not permitted!");
		}

		// is node already connected?
		if (this.localNode != null) {
			throw new ServiceException(
					"Cannot create network; node is already connected!");
		}

		// set nodeURL
		this.localURL = localURL1;

		// if necessary, generate nodeID out of nodeURL
		if (this.getID() == null) {
			this.setID(this.hashFunction.createUniqueNodeID(this.localURL));
		}

		// establish connection
		this.createHelp();

	}

	public final void create(URL localURL1, ID localID1)
			throws ServiceException {

		// check if parameters are valid
		if (localURL1 == null || localID1 == null) {
			throw new IllegalArgumentException(
					"At least one parameter is null which is not permitted!");
		}

		// is node already connected?
		if (this.localNode != null) {
			throw new ServiceException(
					"Cannot create network; node is already connected!");
		}

		// set nodeURL
		this.localURL = localURL1;

		// set nodeID
		this.setID(localID1);

		// establish connection
		this.createHelp();

	}

	/**
	 * Performs all necessary tasks for creating a new Chord ring. Assumes that
	 * localID and localURL are correctly set. Is invoked by the methods
	 * {@link #create()}, {@link #create(URL)}, and {@link #create(URL, ID)}
	 * only.
	 * 
	 * @throws RuntimeException
	 */
	private final void createHelp() {

		this.logger.debug("Help method for creating a new Chord ring invoked.");

		// create local repository for entries
		this.entries = new Entries();

		// create local repository for node references
		if (NUMBER_OF_SUCCESSORS >= 1) {
			this.references = new References(this.getID(), this.getURL(), 
					NUMBER_OF_SUCCESSORS, this.entries);
		} else {
			throw new RuntimeException(
					"NUMBER_OF_SUCCESSORS intialized with wrong value! "
							+ NUMBER_OF_SUCCESSORS);
		}

		// create NodeImpl instance for communication
		this.localNode = new NodeImpl(this, this.getID(), this.localURL,
				this.references, this.entries);

		// create tasks for fixing finger table, checking predecessor and
		// stabilizing
		this.createTasks();

		// accept content requests from outside
		this.localNode.acceptEntries();

	}

	/**
	 * Creates the tasks that must be executed periodically to maintain the
	 * Chord overlay network and schedules them with help of a
	 * {@link ScheduledExecutorService}.
	 */
	private final void createTasks() {

		// start thread which periodically stabilizes with successor
		this.maintenanceTasks.scheduleWithFixedDelay(new StabilizeTask(
				this.localNode, this.references, this.entries),
				ChordImpl.STABILIZE_TASK_START,
				ChordImpl.STABILIZE_TASK_INTERVAL, TimeUnit.SECONDS);

		// start thread which periodically attempts to fix finger table
		this.maintenanceTasks.scheduleWithFixedDelay(new FixFingerTask(
				this.localNode, this.getID(), this.references),
				ChordImpl.FIX_FINGER_TASK_START,
				ChordImpl.FIX_FINGER_TASK_INTERVAL, TimeUnit.SECONDS);

		// start thread which periodically checks whether predecessor has
		// failed
		this.maintenanceTasks.scheduleWithFixedDelay(new CheckPredecessorTask(
				this.references), ChordImpl.CHECK_PREDECESSOR_TASK_START,
				ChordImpl.CHECK_PREDECESSOR_TASK_INTERVAL, TimeUnit.SECONDS);
	}

	public final void join(URL bootstrapURL) throws ServiceException {

		// check if parameters are valid
		if (bootstrapURL == null) {
			throw new NullPointerException(
					"At least one parameter is null which is not permitted!");
		}

		// is node already connected?
		if (this.localNode != null) {
			throw new ServiceException(
					"Cannot join network; node is already connected!");
		}

		// has nodeURL been set?
		if (this.localURL == null) {
			throw new ServiceException("Node URL is not set yet! Please "
					+ "set URL with help of setURL(URL) "
					+ "before invoking join(URL)!");
		}

		// if necessary, generate nodeID out of nodeURL
		if (this.getID() == null) {
			this.setID(this.hashFunction.createUniqueNodeID(this.localURL));
		}

		// establish connection
		this.joinHelp(bootstrapURL);

	}

	public final void join(URL localURL1, URL bootstrapURL)
			throws ServiceException {

		// check if parameters are valid
		if (localURL1 == null || bootstrapURL == null) {
			throw new NullPointerException(
					"At least one parameter is null which is not permitted!");
		}

		// is node already connected?
		if (this.localNode != null) {
			throw new ServiceException(
					"Cannot join network; node is already connected!");
		}

		// set nodeURL
		this.localURL = localURL1;

		// if necessary, generate nodeID out of nodeURL
		if (this.getID() == null) {
			this.setID(this.hashFunction.createUniqueNodeID(this.localURL));
		}

		// establish connection
		this.joinHelp(bootstrapURL);

	}

	public final void join(URL localURL1, ID localID1, URL bootstrapURL)
			throws ServiceException {

		// check if parameters are valid
		if (localURL1 == null || localID1 == null || bootstrapURL == null) {
			throw new NullPointerException(
					"At least one parameter is null which is not permitted!");
		}

		// is node already connected?
		if (this.localNode != null) {
			throw new ServiceException(
					"Cannot join network; node is already connected!");
		}

		// set nodeURL
		this.localURL = localURL1;

		// set nodeID
		this.setID(localID1);

		// establish connection
		this.joinHelp(bootstrapURL);

	}

	/**
	 * Performs all necessary tasks for joining an existing Chord ring. Assumes
	 * that localID and localURL are correctly set. Is invoked by the methods
	 * {@link #join(URL)}, {@link #join(URL, URL)}, and
	 * {@link #join(URL, ID, URL)} only.
	 * 
	 * @param bootstrapURL
	 *            URL of bootstrap node. Must not be null!.
	 * @throws ServiceException
	 *             If anything goes wrong during the join process.
	 * @throws RuntimeException
	 *             Length of successor list has not been initialized correctly.
	 * @throws IllegalArgumentException
	 *             <code>boostrapURL</code> is null!
	 */
	private final void joinHelp(URL bootstrapURL) throws ServiceException {

		// create local repository for entries
		this.entries = new Entries();

		// create local repository for node references
		if (NUMBER_OF_SUCCESSORS >= 1) {
			this.references = new References(this.getID(), this.getURL(), 
					NUMBER_OF_SUCCESSORS, this.entries);
		} else {
			throw new RuntimeException(
					"NUMBER_OF_SUCCESSORS intialized with wrong value! "
							+ NUMBER_OF_SUCCESSORS);
		}

		// create NodeImpl instance for communication
		this.localNode = new NodeImpl(this, this.getID(), this.localURL,
				this.references, this.entries);

		// create proxy for outgoing connection to bootstrap node
		Node bootstrapNode;
		try {
			bootstrapNode = Proxy.createConnection(this.localURL, bootstrapURL);
		} catch (CommunicationException e) {
			throw new ServiceException(
					"An error occured when creating a proxy for outgoing "
							+ "connection to bootstrap node! Join operation"
							+ "failed!", e);

		}

		// only an optimization: store reference on bootstrap node
		this.references.addReference(bootstrapNode);

		// Asking for my successor at node bootstrapNode.nodeID

		// find my successor
		Node mySuccessor;
		try {
			mySuccessor = bootstrapNode.findSuccessor(this.getID());
		} catch (CommunicationException e1) {
			throw new ServiceException("An error occured when trying to find "
					+ "the successor of this node using bootstrap node "
					+ "with url " + bootstrapURL.toString() + "! Join "
					+ "operation failed!", e1);
		}

		// store reference on my successor
		this.logger.info(this.localURL + " has successor "
				+ mySuccessor.getNodeURL());
		this.references.addReference(mySuccessor);

		// notify successor for the first time and copy keys from successor
		RefsAndEntries copyOfRefsAndEntries;
		try {
			copyOfRefsAndEntries = mySuccessor
					.notifyAndCopyEntries(this.localNode);
		} catch (CommunicationException e2) {
			throw new ServiceException("An error occured when contacting "
					+ "the successor of this node in order to "
					+ "obtain its references and entries! Join "
					+ "operation failed!", e2);
		}

		List<Node> refs = copyOfRefsAndEntries.getRefs();
		/*
		 * The first list item is the current predecessor of our successor. Now
		 * we are the predecessor, so we can assume, that it must be our
		 * predecessor. 10.06.2007 sven.
		 */
		boolean predecessorSet = false;
		int count = 0;
		while (!predecessorSet) {
			logger.debug("Size of refs: " + refs.size());
			// there is only one other peer in the network
			if (refs.size() == 1) {
				logger
						.info("Adding successor as predecessor as there are only two peers! "
								+ mySuccessor);
				this.references.addReferenceAsPredecessor(mySuccessor);
				predecessorSet = true;
				logger.debug("Actual predecessor: "
						+ this.references.getPredecessor());
			} else {
				// we got the right predecessor and successor
				if (this.getID().isInInterval(refs.get(0).getNodeID(),
						mySuccessor.getNodeID())) {
					this.references.addReferenceAsPredecessor(refs.get(0));
					predecessorSet = true;
				} else {
					/*
					 * if ID of potential predecessor is greater than ours it
					 * can be our successor...
					 */
					logger.info("Wrong successor found. Going backwards!!!");
					this.references.addReference(refs.get(0));
					try {
						copyOfRefsAndEntries = refs.get(0)
								.notifyAndCopyEntries(this.localNode);
						refs = copyOfRefsAndEntries.getRefs();
					} catch (CommunicationException e) {
						throw new ServiceException(
								"An error occured when contacting "
										+ "the successor of this node in order to "
										+ "obtain its references and entries! Join "
										+ "operation failed!", e);
					}
				}
			}
		}

		// add new references, if pings are successful //removed ping to new
		// references. 17.09.2007 sven
		for (Node newReference : copyOfRefsAndEntries.getRefs()) {
			if (newReference != null && !newReference.equals(this.localNode)
					&& !this.references.containsReference(newReference)) {

				ChordImpl.this.references.addReference(newReference);
				if (ChordImpl.this.logger.isEnabledFor(DEBUG)) {
					ChordImpl.this.logger.debug("Added reference on "
							+ newReference.getNodeID() + " which responded to "
							+ "ping request");
				}

			}
		}

		// add copied entries of successor
		this.entries.addAll(copyOfRefsAndEntries.getEntries());

		// accept content requests from outside
		this.localNode.acceptEntries();

		// create tasks for fixing finger table, checking predecessor and
		// stabilizing
		this.createTasks();
	}

	public final void leave() {

		if (this.localNode == null) {
			// ring has not been created or joined, st. leave has no effect
			return;
		}

		this.maintenanceTasks.shutdownNow();

		try {
			Node successor = this.references.getSuccessor();
			if (successor != null && this.references.getPredecessor() != null) {
				successor.leavesNetwork(this.references.getPredecessor());
			}
		} catch (CommunicationException e) {
			/*
			 * throw new ServiceException( "An unknown error occured when trying
			 * to contact the " + "successor of this node to inform it about " +
			 * "leaving! Leave operation failed!");
			 */

		}

		this.localNode.disconnect();
		this.asyncExecutor.shutdownNow();
		this.localNode = null;

	}

	public final void insert(Key key, Serializable s) {

		// check parameters
		if (key == null || s == null) {
			throw new NullPointerException(
					"Neither parameter may have value null!");
		}

		// determine ID for key
		ID id = this.hashFunction.getHashKey(key);
		Entry entryToInsert = new Entry(id, s);

		boolean debug = this.logger.isEnabledFor(DEBUG);
		if (debug) {
			this.logger.debug("Inserting new entry with id " + id);
		}
		boolean inserted = false;
		while (!inserted) {
			// find successor of id
			Node responsibleNode;
			// try {
			responsibleNode = this.findSuccessor(id);

			if (debug) {
				this.logger.debug("Invoking insertEntry method on node "
						+ responsibleNode.getNodeID());
			}

			// invoke insertEntry method
			try {
				responsibleNode.insertEntry(entryToInsert);
				inserted = true;
			} catch (CommunicationException e1) {
				if (debug) {
					this.logger
							.debug(
									"An error occured while invoking the insertEntry method "
											+ " on the appropriate node! Insert operation "
											+ "failed!", e1);
				}
				continue;
			}
		}
		this.logger.debug("New entry was inserted!");
	}

	public final Set<Serializable> retrieve(Key key) {

		// check parameters
		if (key == null) {
			NullPointerException e = new NullPointerException(
					"Key must not have value null!");
			this.logger.error("Null pointer", e);
			throw e;
		}

		// determine ID for key
		ID id = this.hashFunction.getHashKey(key);

		boolean debug = this.logger.isEnabledFor(DEBUG);
		if (debug) {
			this.logger.debug("Retrieving entries with id " + id);
		}
		Set<Entry> result = null;

		boolean retrieved = false;
		while (!retrieved) {
			// find successor of id
			Node responsibleNode = null;

			responsibleNode = findSuccessor(id);

			// invoke retrieveEntry method
			try {
				result = responsibleNode.retrieveEntries(id);
				// cause while loop to end.

				retrieved = true;
			} catch (CommunicationException e1) {
				if (debug) {
					this.logger
							.debug(
									"An error occured while invoking the retrieveEntry method "
											+ " on the appropriate node! Retrieve operation "
											+ "failed!", e1);
				}
				continue;
			}
		}
		Set<Serializable> values = new HashSet<Serializable>();

		if (result != null) {
			for (Entry entry : result) {
				values.add(entry.getValue());
			}
		}

		this.logger.debug("Entries were retrieved!");

		return values;

	}

	public final void remove(Key key, Serializable s) {

		// check parameters
		if (key == null || s == null) {
			throw new NullPointerException(
					"Neither parameter may have value null!");
		}

		// determine ID for key
		ID id = this.hashFunction.getHashKey(key);
		Entry entryToRemove = new Entry(id, s);

		boolean removed = false;
		while (!removed) {

			boolean debug = this.logger.isEnabledFor(DEBUG);
			if (debug) {
				this.logger.debug("Removing entry with id " + id
						+ " and value " + s);
			}

			// find successor of id
			Node responsibleNode;
			responsibleNode = findSuccessor(id);

			if (debug) {
				this.logger.debug("Invoking removeEntry method on node "
						+ responsibleNode.getNodeID());
			}
			// invoke removeEntry method
			try {
				responsibleNode.removeEntry(entryToRemove);
				removed = true;
			} catch (CommunicationException e1) {
				if (debug) {
					this.logger
							.debug(
									"An error occured while invoking the removeEntry method "
											+ " on the appropriate node! Remove operation "
											+ "failed!", e1);
				}
				continue;
			}
		}
		this.logger.debug("Entry was removed!");
	}

	/**
	 * Returns a human-readable string representation containing this node's
	 * node ID and URL.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return "Chord node: id = "
				+ (this.localID == null ? "null" : this.localID.toString())
				+ ", url = "
				+ (this.localURL == null ? "null" : this.localURL.toString()
						+ "\n");
	}

	/**
	 * Returns the Chord node which is responsible for the given key.
	 * 
	 * @param key
	 *            Key for which the successor is searched for.
	 * @throws NullPointerException
	 *             If given ID is <code>null</code>.
	 * @return Responsible node.
	 */
	final Node findSuccessor(ID key) {

		if (key == null) {
			NullPointerException e = new NullPointerException(
					"ID to find successor for may not be null!");
			this.logger.error("Null pointer.", e);
			throw e;
		}

		boolean debug = this.logger.isEnabledFor(DEBUG);

		// check if the local node is the only node in the network
		Node successor = this.references.getSuccessor();
		if (successor == null) {

			if (this.logger.isEnabledFor(INFO)) {
				this.logger
						.info("I appear to be the only node in the network, so I am "
								+ "my own "
								+ "successor; return reference on me: "
								+ this.getID());
			}
			return this.localNode;
		}
		// check if the key to look up lies between this node and its successor
		else if (key.isInInterval(this.getID(), successor.getNodeID())
				|| key.equals(successor.getNodeID())) {
			if (debug) {
				this.logger
						.debug("The requested key lies between my own and my "
								+ "successor's node id; therefore return my successor.");
			}

			// try to reach successor
			try {
				// successor.ping(); // if methods returns, successor is alive.
				// ping removed on 17.09.2007. sven
				if (debug) {
					this.logger.debug("Returning my successor "
							+ successor.getNodeID() + " of type "
							+ successor.getClass());
				}
				return successor;
			} catch (Exception e) {
				// not successful, delete node from successor list and finger
				// table, and set new successor, if available
				this.logger
						.warn("Successor did not respond! Removing it from all "
								+ "lists and retrying...");
				this.references.removeReference(successor);
				return findSuccessor(key);
			}
		}

		// ask closest preceding node found in local references for closest
		// preceding node concerning the key to look up
		else {

			Node closestPrecedingNode = this.references
					.getClosestPrecedingNode(key);

			try {
				if (debug) {
					this.logger
							.debug("Asking closest preceding node known to this node for closest preceding node "
									+ closestPrecedingNode.getNodeID()
									+ " concerning key " + key + " to look up");
				}
				return closestPrecedingNode.findSuccessor(key);
			} catch (CommunicationException e) {
				this.logger
						.error("Communication failure while requesting successor "
								+ "for key "
								+ key
								+ " from node "
								+ closestPrecedingNode.toString()
								+ " - looking up successor for failed node "
								+ closestPrecedingNode.toString());
				this.references.removeReference(closestPrecedingNode);
				return findSuccessor(key);
			}
		}
	}

	/* Implementation of Report interface */
	public final String printEntries() {
		return this.entries.toString();
	}

	public final String printFingerTable() {
		return this.references.printFingerTable();
	}

	public final String printSuccessorList() {
		return this.references.printSuccessorList();
	}

	public final String printReferences() {
		return this.references.toString();
	}

	public final String printPredecessor() {
		Node pre = this.references.getPredecessor();
		if (pre == null) {
			return "Predecessor: null";
		} else {
			return "Predecessor: " + pre.toString();
		}
	}

	public void retrieve(final Key key, final ChordCallback callback) {
		final Chord chord = this;
		this.asyncExecutor.execute(new Runnable() {
			public void run() {
				Throwable t = null;
				Set<Serializable> result = null;
				try {
					result = chord.retrieve(key);
				} catch (ServiceException e) {
					t = e;
				} catch (Throwable th) {
					t = th;
				}
				callback.retrieved(key, result, t);
			}
		});
	}

	public void insert(final Key key, final Serializable entry,
			final ChordCallback callback) {
		final Chord chord = this;
		this.asyncExecutor.execute(new Runnable() {
			public void run() {
				Throwable t = null;
				try {
					chord.insert(key, entry);
				} catch (ServiceException e) {
					t = e;
				} catch (Throwable th) {
					t = th;
				}
				callback.inserted(key, entry, t);
			}
		});
	}

	public void remove(final Key key, final Serializable entry,
			final ChordCallback callback) {
		final Chord chord = this;
		this.asyncExecutor.execute(new Runnable() {
			public void run() {
				Throwable t = null;
				try {
					chord.remove(key, entry);
				} catch (ServiceException e) {
					t = e;
				} catch (Throwable th) {
					t = th;
				}
				callback.removed(key, entry, t);
			}
		});
	}

	public ChordRetrievalFuture retrieveAsync(Key key) {
		return ChordRetrievalFutureImpl.create(this.asyncExecutor, this, key);
	}

	public ChordFuture insertAsync(Key key, Serializable entry) {
		return ChordInsertFuture.create(this.asyncExecutor, this, key, entry);
	}

	public ChordFuture removeAsync(Key key, Serializable entry) {
		return ChordRemoveFuture.create(this.asyncExecutor, this, key, entry);
	}

}