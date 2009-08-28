/***************************************************************************
 *                                                                         *
 *                             ThreadProxy.java                            *
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

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.Proxy;
import de.uniba.wiai.lspi.chord.com.RefsAndEntries;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.util.logging.Logger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class represents a {@link Proxy} for the protocol that allows 
 * to be build a (local) chord network within one JVM. 
 * 
 * @author sven
 * @version 1.0.5
 */
public final class ThreadProxy extends Proxy {

	/**
	 * The logger for instances of this. 
	 */
	private static final Logger logger = Logger
			.getLogger(ThreadProxy.class.getName());

	/**
	 * Reference to the {@link Registry registry}singleton.
	 */
	protected Registry registry = null;

	/**
	 * The {@link URL}of the node that created this proxy.
	 */
	protected URL creatorURL;

	/**
	 * Indicates if this proxy can be used for communication;
	 */
	protected boolean isValid = true;

	/**
	 * Indicates if this proxy has been used to make a invocation.
	 */
	protected boolean hasBeenUsed = false;

	/**
	 * The endpoint, to which this delegates method invocations. 
	 */
	private ThreadEndpoint endpoint = null;

	/**
	 * 
	 * @param creatorURL1
	 * @param url
	 * @param nodeID1
	 */
	private ThreadProxy(URL creatorURL1, URL url, ID nodeID1) {
		super(url);
		this.registry = Registry.getRegistryInstance();
		this.nodeID = nodeID1;
		this.creatorURL = creatorURL1;
	}

	/**
	 * Creates a Proxy for the <code>jchordlocal</code> protocol. The host
	 * name part of {@link URL url}is the name of the node in the
	 * <code>jchordlocal</code> protocol.
	 * @param creatorURL1 
	 * 
	 * @param url
	 *            The {@link URL}of the node this proxy represents.
	 * @throws CommunicationException 
	 */
	public ThreadProxy(URL creatorURL1, URL url) throws CommunicationException {
		super(url);
		this.registry = Registry.getRegistryInstance();
		this.creatorURL = creatorURL1;
		logger.debug("Trying to get id of node.");
		ThreadEndpoint endpoint_ = this.registry.lookup(this.nodeURL);
		logger.debug("Found endpoint " + endpoint_);
		if (endpoint_ == null) {
			throw new CommunicationException();
		}
		this.nodeID = endpoint_.getNodeID();
	}
	
	void reSetNodeID(ID id){
		this.setNodeID(id); 
	}

	/**
	 * Method to check if this proxy is valid.
	 * 
	 * @throws CommunicationException
	 */
	private void checkValidity() throws CommunicationException {

		if (!this.isValid) {
			throw new CommunicationException("No valid connection!");
		}

		if (this.endpoint == null) {
			this.endpoint = this.registry.lookup(this.nodeURL);
			if (this.endpoint == null) {
				throw new CommunicationException();
			}
		}

		/*
		 * Ensure that node id is set, if has not been set before.
		 */
		this.getNodeID();

		if (!this.hasBeenUsed) {
			this.hasBeenUsed = true;
			Registry.getRegistryInstance().addProxyUsedBy(
					this.creatorURL, this);
		}
	}

	/**
	 * Test if this Proxy is valid.
	 * 
	 * @return <code>true</code> if this Proxy is valid.
	 */
	public boolean isValid() {
		return this.isValid;
	}

	/**
	 * Invalidates this proxy.
	 * 
	 */
	public void invalidate() {
		this.isValid = false;
		this.endpoint = null;
	}

	/**
	 * Get a reference to the {@link ThreadEndpoint   endpoint} this proxy
	 * delegates methods to. If there is no endpoint a
	 * {@link CommunicationException   exception} is thrown.
	 * 
	 * @return Reference to the {@link ThreadEndpoint   endpoint} this proxy
	 *         delegates methods to.
	 * @throws CommunicationException
	 *             If there is no endpoint this exception is thrown.
	 */
	public ThreadEndpoint getEndpoint() throws CommunicationException {
		ThreadEndpoint ep = this.registry.lookup(this.nodeURL);
		if (ep == null) {
			throw new CommunicationException();
		}
		return ep;
	}

	public Node findSuccessor(ID key) throws CommunicationException {
		this.checkValidity();
		// ThreadEndpoint endpoint = this.registry.lookup(this.nodeName);
		// if (endpoint == null) {
		// throw new CommunicationException();
		// }
		Node succ = this.endpoint.findSuccessor(key);
		try {
			logger.debug("Creating clone of proxy " + succ);
			ThreadProxy temp = (ThreadProxy) succ;
			logger.debug("Clone created");
			return temp.cloneMeAt(this.creatorURL);
		} catch (Throwable t) {
			logger.debug("Exception during clone of proxy.", t);
			throw new CommunicationException(t);
		}
	}

	public void insertEntry(Entry entry) throws CommunicationException {
		this.checkValidity();
		logger.debug("Trying to execute insert().");
		// ThreadEndpoint endpoint = this.registry.lookup(this.nodeName);
		logger.debug("Found endpoint " + this.endpoint);
		// if (endpoint == null) {
		// throw new CommunicationException();
		// }
		this.endpoint.insertEntry(entry);
		logger.debug("insert() executed");
	}

	public void removeEntry(Entry entry) throws CommunicationException {
		this.checkValidity();
		this.endpoint.removeEntry(entry);
	}

	/**
	 * 
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("[ThreadProxy ");
		buffer.append(this.nodeURL);
		buffer.append("]");
		return buffer.toString();
	}

	public List<Node> notify(Node potentialPredecessor)
			throws CommunicationException {
		this.checkValidity();

		ThreadProxy potentialPredecessorProxy = new ThreadProxy(
				this.creatorURL, potentialPredecessor.getNodeURL());

		logger.debug("Trying to execute notify().");
		// ThreadEndpoint endpoint = this.registry.lookup(this.nodeName);
		logger.debug("Found endpoint " + this.endpoint);
		// if (endpoint == null) {
		// throw new CommunicationException();
		// }
		List<Node> nodes = this.endpoint.notify(potentialPredecessorProxy);
		Node[] proxies = new Node[nodes.size()];
		try {
			int currentIndex = 0;
			// TODO Document why ThreadProxy instead of Node
			for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
				Object o = i.next();
				ThreadProxy current = (ThreadProxy) o;
				proxies[currentIndex++] = current.cloneMeAt(this.creatorURL);
			}
		} catch (Throwable t) {
			throw new CommunicationException(t);
		}
		return Arrays.asList(proxies);
	}

	public void ping() throws CommunicationException {
		this.checkValidity();
		logger.debug("Trying to execute ping().");
		logger.debug("Found endpoint " + this.endpoint);
		this.endpoint.ping();
	}

	public Set<Entry> retrieveEntries(ID id) throws CommunicationException {
		this.checkValidity();
		logger.debug("Trying to execute retrieve().");
		logger.debug("Found endpoint " + this.endpoint);
		return this.endpoint.retrieveEntries(id);
	}

	/**
	 * Creates a copy of this. 
	 * 
	 * @param creatorUrl The url of the node where this is being copied. 
	 * @return The copy of this. 
	 */
	private ThreadProxy cloneMeAt(URL creatorUrl) {
		return new ThreadProxy(creatorUrl, this.nodeURL, this.nodeID);
	}

	public void leavesNetwork(Node predecessor) throws CommunicationException {
		this.checkValidity();

		ThreadProxy predecessorProxy = new ThreadProxy(this.creatorURL,
				predecessor.getNodeURL());

		logger.debug("Trying to execute leavesNetwork(" + predecessor + ").");
		// ThreadEndpoint endpoint = this.registry.lookup(this.nodeName);
		logger.debug("Found endpoint " + this.endpoint);
		// if (endpoint == null) {
		// throw new CommunicationException();
		// }
		this.endpoint.leavesNetwork(predecessorProxy);
	}

	public void removeReplicas(ID sendingNodeID, Set<Entry> entriesToRemove)
			throws CommunicationException {
		this.checkValidity();
		logger.debug("Trying to execute removeReplicas(" + entriesToRemove
				+ ").");
		// ThreadEndpoint endpoint = this.registry.lookup(this.nodeName);
		logger.debug("Found endpoint " + this.endpoint);
		// if (endpoint == null) {
		// throw new CommunicationException();
		// }
		this.endpoint.removeReplicas(sendingNodeID, entriesToRemove);
	}

	public void insertReplicas(Set<Entry> entries)
			throws CommunicationException {
		this.checkValidity();
		logger.debug("Trying to execute insertReplicas(" + entries + ").");
		// ThreadEndpoint endpoint = this.registry.lookup(this.nodeName);
		logger.debug("Found endpoint " + this.endpoint);
		// if (endpoint == null) {
		// throw new CommunicationException();
		// }
		this.endpoint.insertReplicas(entries);
	}

	public RefsAndEntries notifyAndCopyEntries(Node potentialPredecessor)
			throws CommunicationException {
		this.checkValidity();

		ThreadProxy potentialPredecessorProxy = new ThreadProxy(
				this.creatorURL, potentialPredecessor.getNodeURL());

		logger.debug("Trying to execute notifyAndCopyEntries().");
		// ThreadEndpoint endpoint = this.registry.lookup(this.nodeName);
		logger.debug("Found endpoint " + this.endpoint);
		// if (endpoint == null) {
		// throw new CommunicationException();
		// }
		return this.endpoint.notifyAndCopyEntries(potentialPredecessorProxy);
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

}