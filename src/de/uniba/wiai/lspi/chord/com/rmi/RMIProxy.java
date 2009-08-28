/***************************************************************************
 *                                                                         *
 *                             RMIProxy.java                               *
 *                            -------------------                          *
 *   date                 : 22.02.2008, 14:10:04                           *
 *   copyright            : (C) 2008 Distributed and                       *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
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
package de.uniba.wiai.lspi.chord.com.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Endpoint;
import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.Proxy;
import de.uniba.wiai.lspi.chord.com.RefsAndEntries;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;

public final class RMIProxy extends Proxy {

	private static final String NAME_IN_REGISTRY = RMIEndpoint.NAME_IN_REGISTRY;  
	
	/**
	 * 
	 */
	private RemoteNode remoteNode;
	
	/**
	 * 
	 */
	private URL localURL; 
	
	private volatile boolean connected; 
	
	private RMIEndpoint localEndpoint; 

	/**
	 * 
	 * @param rNode
	 * @param url
	 */
	RMIProxy(RemoteNodeInfo rNode, URL url) {
		super(rNode.getUrl());
		if (url == null) {
			throw new IllegalArgumentException("URL of local node must not be null!"); 
		}
		this.remoteNode = rNode.getRemoteNode();
		this.nodeID = rNode.getNodeID();
		this.localURL = url; 
		this.connected = true;
		this.localEndpoint = (RMIEndpoint)Endpoint.getEndpoint(this.localURL); 
	}

	/**
	 * 
	 * @param localURL
	 * @param url
	 * @throws RemoteException
	 * @throws CommunicationException
	 */
	RMIProxy(URL localURL, URL url) throws RemoteException, CommunicationException {
		super(url);
		if (url == null) {
			throw new IllegalArgumentException("URL of local node must not be null!"); 
		}
		this.localURL = localURL; 
		try {
			this.remoteNode = (RemoteNode) LocateRegistry.getRegistry(
					url.getHost(), url.getPort()).lookup(NAME_IN_REGISTRY+url.toString());
		} catch (NotBoundException e) {
			throw new CommunicationException("Cannot find stub with name " + url.getHost(), e); 
		}
		this.nodeID = this.remoteNode.getNodeID();
		this.connected = true; 
		this.localEndpoint = (RMIEndpoint)Endpoint.getEndpoint(this.localURL);
	}

	/**
	 * 
	 * @param localURL
	 * @param url
	 * @return
	 * @throws CommunicationException
	 */
	public static RMIProxy create(URL localURL, URL url) throws CommunicationException {
		try {
			return new RMIProxy(localURL, url);
		} catch (RemoteException e) {
			throw new CommunicationException(
					"Connection cannot be established!", e);
		}
	}

	/**
	 * 
	 * @return
	 */
	RemoteNode getRemoteNode() {
		return this.remoteNode;
	}

	@Override
	public void disconnect() {
		this.connected = false; 
	}
	
	public void testConnection() throws CommunicationException{
		if (this.connected == false) {
			throw new CommunicationException("Not connected!"); 
		}
	}

	@Override
	public Node findSuccessor(ID key) throws CommunicationException {
		this.testConnection();
		try {
			RemoteNodeInfo info = this.remoteNode.findSuccessor(key);
			return new RMIProxy(info, this.localURL);
		} catch (RemoteException e) {
			throw new CommunicationException("Could not connect to "
					+ this.nodeURL + "!", e);
		}
	}

	@Override
	public void insertEntry(Entry entryToInsert) throws CommunicationException {
		this.testConnection();
		try {
			this.remoteNode.insertEntry(entryToInsert);
		} catch (RemoteException e) {
			throw new CommunicationException("Could not connect to "
					+ this.nodeURL + "!", e);
		}
	}

	@Override
	public void insertReplicas(Set<Entry> entries)
			throws CommunicationException {
		this.testConnection();
		try {
			this.remoteNode.insertReplicas(entries);
		} catch (RemoteException e) {
			throw new CommunicationException("Could not connect to "
					+ this.nodeURL + "!", e);
		}

	}

	@Override
	public void leavesNetwork(Node predecessor) throws CommunicationException {
		this.testConnection();
		try {
			RemoteNodeInfo info = null; 
			if (this.localURL.equals(predecessor.getNodeURL())) {
				info = new RemoteNodeInfo(
						this.localEndpoint.getRemoteNode(),
						predecessor.getNodeID(), predecessor.getNodeURL()); 
			} else {
				info = new RemoteNodeInfo(
						((RMIProxy)predecessor).remoteNode,
						predecessor.getNodeID(), predecessor.getNodeURL());
			}
			this.remoteNode.leavesNetwork(info);
		} catch (RemoteException e) {
			throw new CommunicationException("Could not connect to "
					+ this.getNodeURL() + "!", e);
		}
	}

	@Override
	public List<Node> notify(Node predecessor) throws CommunicationException {
		this.testConnection();
		try {
			RemoteNodeInfo info = null; 
			if (this.localURL.equals(predecessor.getNodeURL())) {
				info = new RemoteNodeInfo(
						this.localEndpoint.getRemoteNode(),
						predecessor.getNodeID(), predecessor.getNodeURL()); 
			} else {
				info = new RemoteNodeInfo(
						((RMIProxy)predecessor).remoteNode,
						predecessor.getNodeID(), predecessor.getNodeURL());
			}
			
			List<RemoteNodeInfo> infos = this.remoteNode
					.notify(info);
			List<Node> nodes = new LinkedList<Node>();
			for (RemoteNodeInfo i : infos) {
				nodes.add(new RMIProxy(i, this.localURL));
			}
			return nodes;
		} catch (RemoteException e) {
			throw new CommunicationException("Could not connect to "
					+ this.nodeURL + "!", e);
		}
	}

	@Override
	public RefsAndEntries notifyAndCopyEntries(Node predecessor)
			throws CommunicationException {
		this.testConnection();
		try {
			RemoteNodeInfo info = null; 
			if (this.localURL.equals(predecessor.getNodeURL())) {
				info = new RemoteNodeInfo(
						this.localEndpoint.getRemoteNode(),
						predecessor.getNodeID(), predecessor.getNodeURL()); 
			} else {
				info = new RemoteNodeInfo(
						((RMIProxy)predecessor).remoteNode,
						predecessor.getNodeID(), predecessor.getNodeURL());
			}
			
			RemoteRefsAndEntries rraes = this.remoteNode
					.notifyAndCopyEntries(info);
			
			List<Node> nodes = new LinkedList<Node>();
			List<RemoteNodeInfo> infos = rraes.getNodeInfos(); 
			for (RemoteNodeInfo i : infos) {
				nodes.add(new RMIProxy(i, this.localURL));
			}
			RefsAndEntries raes = new RefsAndEntries(nodes, rraes.getEntries()); 
			return raes;
		} catch (RemoteException e) {
			throw new CommunicationException("Could not connect to "
					+ this.nodeURL + "!", e);
		}
	}

	@Override
	public void ping() throws CommunicationException {
		this.testConnection();
		try {
			this.remoteNode.ping();
		} catch (RemoteException e) {
			throw new CommunicationException("Could not connect to "
					+ this.nodeURL + "!", e);
		} 
	}

	@Override
	public void removeEntry(Entry entryToRemove) throws CommunicationException {
		this.testConnection();
		try {
			this.remoteNode.removeEntry(entryToRemove); 
		} catch (RemoteException e) {
			throw new CommunicationException("Could not connect to "
					+ this.nodeURL + "!", e);
		} 
	}

	@Override
	public void removeReplicas(ID sendingNode, Set<Entry> replicasToRemove)
			throws CommunicationException {
		this.testConnection();
		try {
			this.remoteNode.removeReplicas(sendingNode, replicasToRemove);  
		} catch (RemoteException e) {
			throw new CommunicationException("Could not connect to "
					+ this.nodeURL + "!", e);
		} 
	}

	@Override
	public Set<Entry> retrieveEntries(ID id) throws CommunicationException {
		this.testConnection();
		try {
			return this.remoteNode.retrieveEntries(id); 
		} catch (RemoteException e) {
			throw new CommunicationException("Could not connect to "
					+ this.nodeURL + "!", e);
		} 
	}

}
