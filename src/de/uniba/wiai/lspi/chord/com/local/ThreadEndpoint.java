/***************************************************************************
 *                                                                         *
 *                            ThreadEndpoint.java                          *
 *                            -------------------                          *
 *   date                 : 12.08.2004                                     *
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

package de.uniba.wiai.lspi.chord.com.local;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Endpoint;
import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.RefsAndEntries;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import de.uniba.wiai.lspi.util.logging.Logger;

/**
 * This represents the {@link Endpoint} for the protocol that can be used to
 * build a (local) chord network within one JVM.
 * 
 * @author sven
 * @version 1.0.5
 */
public final class ThreadEndpoint extends Endpoint {

	/**
	 * The logger for this instance.
	 */
	private Logger logger = null;

	/**
	 * Constant indicating that this has crashed.
	 */
	private final static int CRASHED = Integer.MAX_VALUE;

	/**
	 * The {@link Registry registry}of local endpoints.
	 */
	protected final Registry registry;

	/**
	 * Object to synchronize threads at. Used to block and wake up threads that
	 * are waiting for this endpoint to get into a state.
	 */
	private Object lock = new Object();

	/**
	 * {@link List}of {@link InvocationListener listeners}that want to be
	 * notified if a method is invoked on this endpoint.
	 */
	protected List<InvocationListener> invocationListeners = null;

	/**
	 * Creates a new Endpoint for communication via Java Threads.
	 * 
	 * @param node1
	 *            The {@link Node}this endpoint invocates methods on.
	 * @param url1
	 *            The {@link URL}of this endpoint. The hostname of url is the
	 *            name of the node.
	 */
	public ThreadEndpoint(Node node1, URL url1) {
		super(node1, url1);
		this.logger = Logger.getLogger(ThreadEndpoint.class.getName() + "."
				+ node1.getNodeID());
		this.invocationListeners = new LinkedList<InvocationListener>();
		this.registry = Registry.getRegistryInstance();
		this.logger.info(this + " initialised.");
	}

	/**
	 * @return Implementation of {@link Node#notify(Node)}. See documentation
	 *         of {@link Node}.
	 */
	public ID getNodeID() {
		return this.node.getNodeID();
	}

	/**
	 * @param listener
	 */
	public void register(InvocationListener listener) {
		this.logger.debug("register(" + listener + ")");
		// synchronized (this.invocationListeners) {
		this.invocationListeners.add(listener);
		// }
		this.logger.debug("No. of invocation listeners "
				+ this.invocationListeners.size());
	}

	/**
	 * @param method
	 */
	private void notifyInvocationListeners(int method) {
		for (InvocationListener l : this.invocationListeners) {
			l.notifyInvocationOf(method);
		}
	}

	/**
	 * @param method
	 */
	private void notifyInvocationListenersFinished(int method) {
		for (InvocationListener l : this.invocationListeners) {
			l.notifyInvocationOfFinished(method);
		}
	}

	/**
	 * @param key
	 * @return The successor of <code>key</code>.
	 * @throws CommunicationException
	 */
	public Node findSuccessor(ID key) throws CommunicationException {
		this.checkIfCrashed();
		this.waitFor(Endpoint.LISTENING);
		/* delegate invocation to node. */
		this.notifyInvocationListeners(InvocationListener.FIND_SUCCESSOR);
		Node n = this.node.findSuccessor(key);
		if (n == this.node) {
			this.logger
					.debug("Returned node is local node. Converting to 'remote' reference. ");
			ThreadProxy t = new ThreadProxy(this.url, this.url);
			t.reSetNodeID(n.getNodeID());
			n = t;
		}
		this
				.notifyInvocationListenersFinished(InvocationListener.FIND_SUCCESSOR);
		return n;
	}

	/**
	 * @param entry
	 * @throws CommunicationException
	 */
	public void insertEntry(Entry entry) throws CommunicationException {
		this.checkIfCrashed();
		this.waitFor(Endpoint.ACCEPT_ENTRIES);
		/* delegate invocation to node. */
		this.notifyInvocationListeners(InvocationListener.INSERT_ENTRY);
		this.node.insertEntry(entry);
		this.notifyInvocationListenersFinished(InvocationListener.INSERT_ENTRY);
	}

	/**
	 * @param entry
	 * @throws CommunicationException
	 */
	public void removeEntry(Entry entry) throws CommunicationException {
		this.checkIfCrashed();
		this.waitFor(Endpoint.ACCEPT_ENTRIES);
		/* delegate invocation to node. */
		this.notifyInvocationListeners(InvocationListener.REMOVE_ENTRY);
		this.node.removeEntry(entry);
		this.notifyInvocationListenersFinished(InvocationListener.REMOVE_ENTRY);
	}

	/**
	 * @param potentialPredecessor
	 * @return Implementation of {@link Node#notify(Node)}. See documentation
	 *         of {@link Node}.
	 * @throws CommunicationException
	 */
	public List<Node> notify(Node potentialPredecessor)
			throws CommunicationException {
		this.checkIfCrashed();
		this.waitFor(Endpoint.LISTENING);
		this.notifyInvocationListeners(InvocationListener.NOTIFY);
		this.logger.debug("Invoking notify on local node " + this.node);
		List<Node> n = this.node.notify(potentialPredecessor);
		this.logger.debug("Notify resulted in " + n);

		for (Node current : n) {
			if (current == this.node) {
				n.remove(current);

				this.logger
						.debug("Returned node is local node. Converting to 'remote' reference. ");
				n.add(new ThreadProxy(this.url, this.url));
			}
		}
		this.notifyInvocationListenersFinished(InvocationListener.NOTIFY);
		return n;
	}

	/**
	 * @throws CommunicationException
	 */
	public void ping() throws CommunicationException {
		this.checkIfCrashed();
		this.waitFor(Endpoint.LISTENING);
		this.notifyInvocationListeners(InvocationListener.PING);
		this.node.ping();
		this.notifyInvocationListenersFinished(InvocationListener.PING);
	}

	/**
	 * @param id
	 * @return The retrieved entries.
	 * @throws CommunicationException
	 */
	public Set<Entry> retrieveEntries(ID id) throws CommunicationException {
		this.checkIfCrashed();
		this.waitFor(Endpoint.ACCEPT_ENTRIES);
		this.notifyInvocationListeners(InvocationListener.RETRIEVE_ENTRIES);
		Set<Entry> s = this.node.retrieveEntries(id);
		this
				.notifyInvocationListenersFinished(InvocationListener.RETRIEVE_ENTRIES);
		return s;
	}

	/**
	 * @param predecessor
	 * @throws CommunicationException
	 */
	public void leavesNetwork(Node predecessor) throws CommunicationException {
		this.checkIfCrashed();
		this.notifyInvocationListeners(InvocationListener.LEAVES_NETWORK);
		this.node.leavesNetwork(predecessor);
		this
				.notifyInvocationListenersFinished(InvocationListener.LEAVES_NETWORK);
	}

	/**
	 * @param sendingNodeID
	 * @param entriesToRemove
	 * @throws CommunicationException
	 */
	public void removeReplicas(ID sendingNodeID, Set<Entry> entriesToRemove)
			throws CommunicationException {
		this.checkIfCrashed();
		this.waitFor(Endpoint.LISTENING);
		this.notifyInvocationListeners(InvocationListener.REMOVE_REPLICAS);
		this.node.removeReplicas(sendingNodeID, entriesToRemove);
		this
				.notifyInvocationListenersFinished(InvocationListener.REMOVE_REPLICAS);
	}

	/**
	 * @param entries
	 * @throws CommunicationException
	 */
	public void insertReplicas(Set<Entry> entries)
			throws CommunicationException {
		this.checkIfCrashed();
		this.waitFor(Endpoint.LISTENING);
		this.notifyInvocationListeners(InvocationListener.INSERT_REPLICAS);
		this.node.insertReplicas(entries);
		this
				.notifyInvocationListenersFinished(InvocationListener.INSERT_REPLICAS);
	}

	/**
	 * @param potentialPredecessor
	 * @return Implementation of {@link Node#notify(Node)}. See documentation
	 *         of {@link Node}.
	 * @throws CommunicationException
	 */
	public RefsAndEntries notifyAndCopyEntries(Node potentialPredecessor)
			throws CommunicationException {
		this.checkIfCrashed();
		this.waitFor(Endpoint.ACCEPT_ENTRIES);
		this.notifyInvocationListeners(InvocationListener.NOTIFY_AND_COPY);
		RefsAndEntries refs = this.node
				.notifyAndCopyEntries(potentialPredecessor);
		List<Node> nodes = refs.getRefs();

		for (Node current : nodes) {
			if (current == this.node) {
				nodes.remove(current);

				this.logger
						.debug("Returned node is local node. Converting to 'remote' reference. ");
				nodes.add(new ThreadProxy(this.url, this.url));
			}
		}
		this
				.notifyInvocationListenersFinished(InvocationListener.NOTIFY_AND_COPY);
		return new RefsAndEntries(nodes, refs.getEntries());
	}

	/**
	 * Wait for the endpoint to get into given state.
	 * 
	 * @param state_
	 *            The state to wait for.
	 * @throws CommunicationException
	 */
	private void waitFor(int state_) throws CommunicationException {
		synchronized (this.lock) {
			while (this.getState() < state_) {
				try {
					this.logger.debug(Thread.currentThread()
							+ " waiting for state: " + state_);
					this.lock.wait();
					if (state_ == CRASHED) {
						throw new CommunicationException(
								"Connection destroyed!");
					}
				} catch (InterruptedException t) {
					this.logger.warn("Unexpected exception while waiting!", t);
				}
			}
		}
	}

	/**
	 * Notify threads waiting for monitor on lock.
	 */
	private void notifyWaitingThreads() {
		synchronized (this.lock) {
			this.logger.debug("Notifying waiting threads.");
			this.lock.notifyAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniba.wiai.lspi.chord.com.Endpoint#openConnections()
	 */
	protected void openConnections() {
		/** state has changed. notify waiting threads */
		this.logger.debug("openConnections()");
		this.notifyWaitingThreads();
		this.registry.bind(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniba.wiai.lspi.chord.com.Endpoint#closeConnections()
	 */
	protected void closeConnections() {
		this.registry.unbind(this);
		this.registry.removeProxiesInUseBy(this.getURL());
		/** state has changed. notify waiting threads */
		this.notifyWaitingThreads();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniba.wiai.lspi.chord.com.Endpoint#entriesAcceptable()
	 */
	protected void entriesAcceptable() {
		/** state has changed. notify waiting threads */
		this.notifyWaitingThreads();
	}

	/**
	 * Method to emulate a crash of the node that this is the endpoint for. This
	 * method heavily relise on the internal structure of service layer
	 * implementation to make it possible to emulate a chord overlay network
	 * within one JVM.
	 * 
	 * This method may cause problems at runtime.
	 */
	public void crash() {
		this.logger.debug("crash() invoked!");
		this.registry.unbind(this);
		List<ThreadProxy> proxies = this.registry.getProxiesInUseBy(this
				.getURL());
		if (proxies != null) {
			for (ThreadProxy p : proxies) {
				p.invalidate();
			}
		}
		
		this.registry.removeProxiesInUseBy(this.getURL());
		this.setState(CRASHED);
		this.notifyWaitingThreads();
		/* kill threads of node (gefrickelt) */
		ChordImpl impl = ChordImplAccess.fetchChordImplOfNode(this.node);
		Field[] fields = impl.getClass().getDeclaredFields();
		this.logger.debug(fields.length + " fields obtained from class "
				+ impl.getClass());
		for (Field field : fields) {
			this.logger.debug("Examining field " + field + " of node "
					+ this.node);
			try {
				if (field.getName().equals("maintenanceTasks")) {
					field.setAccessible(true);

					Object executor = field.get(impl);
					this.logger.debug("Shutting down TaskExecutor " + executor
							+ ".");
					Method m = executor.getClass().getMethod("shutdown",
							new Class[0]);
					m.setAccessible(true);
					m.invoke(executor, new Object[0]);
				}
			} catch (Throwable t) {
				this.logger.warn("Could not kill threads of node " + this.node,
						t);
				t.printStackTrace();
			}
		}
		Endpoint.endpoints.remove(this.url);
		this.invocationListeners = null;
	}

	/**
	 * Checks if this has crashed.
	 * 
	 * @throws CommunicationException
	 */
	private void checkIfCrashed() throws CommunicationException {
		if ((this.getState() == CRASHED)
				|| (this.getState() < Endpoint.LISTENING)) {
			this.logger.debug(this + " has crashed. Throwing Exception.");
			throw new CommunicationException();
		}
	}

	/** ********************************************************** */
	/* START: Methods overwritten from java.lang.Object */
	/** ********************************************************** */

	/**
	 * Overwritten from {@link java.lang.Object}. Two ThreadEndpoints A and B
	 * are equal if they are endpoints for the node with the same name. (A.name ==
	 * B.name).
	 * 
	 * @param obj
	 * @return <code>true</code> if this equals the provided <code>obj</code>.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ThreadEndpoint) {
			ThreadEndpoint ep = (ThreadEndpoint) obj;
			URL epURL = ep.getURL();
			return ((epURL.equals(this.getURL())) && (ep.hashCode() == this
					.hashCode()));
		} else {
			return false;
		}
	}

	/**
	 * Overwritten from {@link java.lang.Object}.
	 * 
	 * @return Overwritten from {@link java.lang.Object}.
	 */
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Overwritten from {@link java.lang.Object}.
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("[ThreadEndpoint for node ");
		buffer.append(this.node);
		buffer.append(" with URL ");
		buffer.append(this.url);
		buffer.append("]");
		return buffer.toString();
	}

	/** ********************************************************** */
	/* END: Methods overwritten from java.lang.Object */
	/** ********************************************************** */

}