/***************************************************************************
 *                                                                         *
 *                             SocketProxy.java                            *
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
package de.uniba.wiai.lspi.chord.com.socket;

import static de.uniba.wiai.lspi.util.logging.Logger.LogLevel.DEBUG;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Endpoint;
import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.Proxy;
import de.uniba.wiai.lspi.chord.com.RefsAndEntries;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.util.logging.Logger;

/**
 * This is the implementation of {@link Proxy} for the socket protocol. This
 * connects to the {@link SocketEndpoint endpoint} of the node it represents by
 * means of <code>Sockets</code>.
 * 
 * @author sven
 * @version 1.0.5
 */
public final class SocketProxy extends Proxy implements Runnable {

	/**
	 * The logger for instances of this class.
	 */
	private final static Logger logger = Logger.getLogger(SocketProxy.class);

	/**
	 * Map of existing proxies. Key: {@link String}, Value: {@link SocketProxy}.
	 * changed on 21.03.2006 by sven. See documentation of method
	 * {@link #createProxyKey(URL, URL)}
	 * 
	 */
	private static Map<String, SocketProxy> proxies = new HashMap<String, SocketProxy>();

	/**
	 * The {@link URL}of the node that uses this proxy to connect to the node,
	 * which is represented by this proxy.
	 * 
	 */
	private URL urlOfLocalNode = null;

	/**
	 * Counter for requests that have been made by this proxy. Also required to
	 * create unique identifiers for {@link Request requests}.
	 */
	private long requestCounter = -1;

	/**
	 * The socket that provides the connection to the node that this is the
	 * Proxy for. This is transient as a proxy can be transferred over the
	 * network. After transfer this socket has to be restored by reconnecting to
	 * the node.
	 */
	private transient Socket mySocket;

	/**
	 * The {@link ObjectOutputStream}this Proxy writes objects to. This is
	 * transient as a proxy can be transferred over the network. After transfer
	 * this stream has to be restored.
	 */
	private transient ObjectOutputStream out;

	/**
	 * The {@link ObjectInputStream}this Proxy reads objects from. This is
	 * transient as a proxy can be transferred over the network. After transfer
	 * this stream has to be restored.
	 */
	private transient ObjectInputStream in;

	/**
	 * The {@link ObjectInputStream} this Proxy reads objects from. This is
	 * transient as a proxy can be transferred over the network. After transfer
	 * this stream has to be restored.
	 */
	private transient Map<String, Response> responses;

	/**
	 * {@link Map} where threads are put in that are waiting for a repsonse.
	 * Key: identifier of the request (same as for the response). Value: The
	 * Thread itself.
	 */
	private transient Map<String, WaitingThread> waitingThreads;

	/**
	 * This indicates that an exception occured while waiting for responses and
	 * that the connection to the {@link Node node}, that this is the proxy
	 * for, could not be reestablished.
	 */
	private volatile boolean disconnected = false;

	/**
	 * Establishes a connection from <code>urlOfLocalNode</code> to
	 * <code>url</code>. The connection is represented by the returned
	 * <code>SocketProxy</code>.
	 * 
	 * @param url
	 *            The {@link URL} to connect to.
	 * @param urlOfLocalNode
	 *            {@link URL} of local node that establishes the connection.
	 * @return <code>SocketProxy</code> representing the established
	 *         connection.
	 * @throws CommunicationException
	 *             Thrown if establishment of connection to <code>url</code>
	 *             failed.
	 */
	public static SocketProxy create(URL urlOfLocalNode, URL url)
			throws CommunicationException {
		synchronized (proxies) {
			/*
			 * added on 21.03.2006 by sven. See documentation of method
			 * createProxyKey(URL, URL);
			 */
			String proxyKey = SocketProxy.createProxyKey(urlOfLocalNode, url);
			logger.debug("Known proxies " + SocketProxy.proxies.keySet());
			if (proxies.containsKey(proxyKey)) {
				logger.debug("Returning existing proxy for " + url);
				return proxies.get(proxyKey);
			} else {
				logger.debug("Creating new proxy for " + url);
				SocketProxy newProxy = new SocketProxy(url, urlOfLocalNode);
				proxies.put(proxyKey, newProxy);
				return newProxy;
			}
		}
	}

	/**
	 * Closes all outgoing connections to other peers. Allows the local peer to
	 * shutdown cleanly.
	 * 
	 */
	static void shutDownAll() {
		Set<String> keys = proxies.keySet();
		for (String key : keys) {
			proxies.get(key).disconnect();
		}
		proxies.clear();
	}

	/**
	 * Creates a <code>SocketProxy</code> representing the connection from
	 * <code>urlOfLocalNode</code> to <code>url</code>. The connection is
	 * established when the first (remote) invocation with help of the
	 * <code>SocketProxy</code> occurs.
	 * 
	 * @param url
	 *            The {@link URL} of the remote node.
	 * @param urlOfLocalNode
	 *            The {@link URL} of local node.
	 * @param nodeID
	 *            The {@link ID} of the remote node.
	 * @return SocketProxy
	 */
	protected static SocketProxy create(URL url, URL urlOfLocalNode, ID nodeID) {
		synchronized (proxies) {
			/*
			 * added on 21.03.2006 by sven. See documentation of method
			 * createProxyKey(String, String);
			 */
			String proxyKey = SocketProxy.createProxyKey(urlOfLocalNode, url);
			logger.debug("Known proxies " + SocketProxy.proxies.keySet());
			if (proxies.containsKey(proxyKey)) {
				logger.debug("Returning existing proxy for " + url);
				return proxies.get(proxyKey);
			} else {
				logger.debug("Creating new proxy for " + url);
				SocketProxy proxy = new SocketProxy(url, urlOfLocalNode, nodeID);
				proxies.put(proxyKey, proxy);
				return proxy;
			}
		}
	}

	/**
	 * Method that creates a unique key for a SocketProxy to be stored in
	 * {@link #proxies}.
	 * 
	 * This is important for the methods {@link #create(URL, URL)},
	 * {@link #create(URL, URL, ID)}, and {@link #disconnect()}, so that
	 * socket communication also works when it is used within one JVM.
	 * 
	 * Added by sven 21.03.2006, as before SocketProxy were stored in
	 * {@link #proxies} with help of their remote URL as key, so that they were
	 * a kind of singleton for that URL. But the key has to consist of the URL
	 * of the local peer, that uses the proxy, and the remote URL as
	 * SocketProxies must only be (kind of) a singleton per local and remote
	 * URL.
	 * 
	 * @param localURL
	 * @param remoteURL
	 * @return The key to store the SocketProxy
	 */
	private static String createProxyKey(URL localURL, URL remoteURL) {
		return localURL.toString() + "->" + remoteURL.toString();
	}

	/**
	 * Corresponding constructor to factory method {@link #create(URL, URL, ID)}.
	 * 
	 * @see #create(URL, URL, ID)
	 * @param url
	 * @param urlOfLocalNode1
	 * @param nodeID1
	 */
	protected SocketProxy(URL url, URL urlOfLocalNode1, ID nodeID1) {
		super(url);
		if (url == null || urlOfLocalNode1 == null || nodeID1 == null) {
			throw new IllegalArgumentException("null");
		}
		this.urlOfLocalNode = urlOfLocalNode1;
		this.nodeID = nodeID1;
	}

	/**
	 * Corresponding constructor to factory method {@link #create(URL, URL)}.
	 * 
	 * @see #create(URL, URL)
	 * @param url
	 * @param urlOfLocalNode1
	 * @throws CommunicationException
	 */
	private SocketProxy(URL url, URL urlOfLocalNode1)
			throws CommunicationException {
		super(url);
		if (url == null || urlOfLocalNode1 == null) {
			throw new IllegalArgumentException(
					"URLs must not be null!");
		}
		this.urlOfLocalNode = urlOfLocalNode1;
		this.initializeNodeID();
		logger.info("SocketProxy for " + url + " has been created.");
	}

	/**
	 * Private method to send requests over the socket. This method is
	 * synchronized to ensure that no other thread concurrently accesses the
	 * {@link ObjectOutputStream output stream}<code>out</code> while sending
	 * {@link Request request}.
	 * 
	 * @param request
	 *            The {@link Request}to be sent.
	 * @throws CommunicationException
	 *             while writing to {@link ObjectOutputStream output stream}.
	 */
	private synchronized void send(Request request)
			throws CommunicationException {
		try {
			logger.debug("Sending request " + request.getReplyWith());
			this.out.writeObject(request);
			this.out.flush();
			this.out.reset();
		} catch (IOException e) {
			throw new CommunicationException("Could not connect to node "
					+ this.nodeURL, e);
		}
	}

	/**
	 * Private method to create an identifier that enables this to associate a
	 * {@link Response response}with a {@link Request request}made before.
	 * This method is synchronized to protect {@link #requestCounter}from race
	 * conditions.
	 * 
	 * @param methodIdentifier
	 *            Integer identifying the method this method is called from.
	 * @return Unique Identifier for the request.
	 */
	private synchronized String createIdentifier(int methodIdentifier) {
		/* Create unique identifier from */
		StringBuilder uid = new StringBuilder();
		/* Time stamp */
		uid.append(System.currentTimeMillis());
		uid.append("-");
		/* counter and */
		uid.append(this.requestCounter++);
		/* methodIdentifier */
		uid.append("-");
		uid.append(methodIdentifier);
		return uid.toString();
	}

	/**
	 * Called in a method that is delegated to the {@link Node node}, that this
	 * is the proxy for. This method blocks the thread that calls the particular
	 * method until a {@link Response response} is received.
	 * 
	 * @param request
	 * @return The {@link Response} for <code>request</code>.
	 * @throws CommunicationException
	 */
	private Response waitForResponse(Request request)
			throws CommunicationException {

		String responseIdentifier = request.getReplyWith();
		Response response = null;
		logger.debug("Trying to wait for response with identifier "
				+ responseIdentifier + " for method "
				+ MethodConstants.getMethodName(request.getRequestType()));

		synchronized (this.responses) {
			logger.debug("No of responses " + this.responses.size());
			/* Test if we got disconnected while waiting for lock on object */
			if (this.disconnected) {
				throw new CommunicationException("Connection to remote host "
						+ " is broken down. ");
			}
			/*
			 * Test if response is already available (Maybe response arrived
			 * before we reached this point).
			 */
			response = this.responses.remove(responseIdentifier);
			if (response != null) {
				return response;
			}

			/* WAIT FOR RESPONSE */
			/* add current thread to map of threads waiting for a response */
			WaitingThread wt = new WaitingThread(Thread.currentThread());
			this.waitingThreads.put(responseIdentifier, wt);
			while (!wt.hasBeenWokenUp()) {
				try {
					/*
					 * Wait until notified or time out is reached.
					 */
					logger.debug("Waiting for response to arrive.");
					this.responses.wait();
				} catch (InterruptedException e) {
					/*
					 * does not matter as this is intended Thread is interrupted
					 * if response arrives
					 */
				}
			}
			logger.debug("Have been woken up from waiting for response.");

			/* remove thread from map of threads waiting for a response */
			this.waitingThreads.remove(responseIdentifier);
			/* try to get the response if available */
			response = this.responses.remove(responseIdentifier);
			logger.debug("Response for request with identifier "
					+ responseIdentifier + " for method "
					+ MethodConstants.getMethodName(request.getRequestType())
					+ " received.");
			/* if no response availabe */
			if (response == null) {
				logger.debug("No response received.");
				/* we have been disconnected */
				if (this.disconnected) {
					logger.info("Connection to remote host lost.");
					throw new CommunicationException(
							"Connection to remote host " + " is broken down. ");
				}
				/* or time out has elapsed */
				else {
					logger.error("There is no result, but we have not been "
							+ "disconnected. Something went seriously wrong!");
					throw new CommunicationException(
							"Did not receive a response!");
				}
			}
		}
		return response;
	}

	/**
	 * This method is called by {@link #run()}when it receives a
	 * {@link Response}. The {@link Thread thread}waiting for response is
	 * woken up and the response is put into {@link Map responses}.
	 * 
	 * @param response
	 */
	private void responseReceived(Response response) {
		synchronized (this.responses) {
			/* Try to fetch thread waiting for this response */
			logger.debug("No of waiting threads " + this.waitingThreads);
			WaitingThread waitingThread = this.waitingThreads.get(response
					.getInReplyTo());
			logger.debug("Response with id " + response.getInReplyTo()
					+ "received.");
			/* save response */
			this.responses.put(response.getInReplyTo(), response);
			/* if there is a thread waiting for this response */
			if (waitingThread != null) {
				/* wake up the thread */
				logger.debug("Waking up thread!");
				waitingThread.wakeUp();
			} else {
				// TODO what else? why 'else' anyway?
			}
		}
	}

	/**
	 * Method to indicate that connection to remote {@link Node node} is broken
	 * down.
	 */
	private void connectionBrokenDown() {
		if (this.responses == null) {
			/*
			 * Nothing to do!
			 */
			return;
		}
		/* synchronize on responses, as all threads accessing this proxy do so */
		synchronized (this.responses) {
			logger.info("Connection broken down!");
			this.disconnected = true;
			/* wake up all threads */
			for (WaitingThread thread : this.waitingThreads.values()) {
				logger.debug("Interrupting waiting thread " + thread);
				thread.wakeUp();
			}
		}
	}

	/**
	 * Creates a request for the method identified by
	 * <code>methodIdentifier</code> with the parameters
	 * <code>parameters</code>. Sets also field
	 * {@link Request#getReplyWith()}of created {@link Request request}.
	 * 
	 * @param methodIdentifier
	 *            The identifier of the method to request.
	 * @param parameters
	 *            The parameters for the request.
	 * @return The {@link Request request}created.
	 */
	private Request createRequest(int methodIdentifier,
			Serializable[] parameters) {
		if (logger.isEnabledFor(DEBUG)) {
			logger.debug("Creating request for method "
					+ MethodConstants.getMethodName(methodIdentifier)
					+ " with parameters "
					+ java.util.Arrays.deepToString(parameters));
		}
		String responseIdentifier = this.createIdentifier(methodIdentifier);
		Request request = new Request(methodIdentifier, responseIdentifier);
		request.setParameters(parameters);
		logger.debug("Request " + request + " created.");
		return request;
	}

	/**
	 * @param key
	 * @return The successor of <code>key</code>.
	 * @throws CommunicationException
	 */
	public Node findSuccessor(ID key) throws CommunicationException {
		this.makeSocketAvailable();

		logger.debug("Trying to find successor for ID " + key);

		/* prepare request for method findSuccessor */
		Request request = this.createRequest(MethodConstants.FIND_SUCCESSOR,
				new Serializable[] { key });
		/* send request */
		try {
			logger.debug("Trying to send request " + request);
			this.send(request);
		} catch (CommunicationException ce) {
			logger.debug("Connection failed!");
			throw ce;
		}
		/* wait for response */
		logger.debug("Waiting for response for request " + request);
		Response response = this.waitForResponse(request);
		logger.debug("Response " + response + " arrived.");
		if (response.isFailureResponse()) {
			throw new CommunicationException(response.getFailureReason());
		} else {
			try {
				RemoteNodeInfo nodeInfo = (RemoteNodeInfo) response.getResult();
				if (nodeInfo.getNodeURL().equals(this.urlOfLocalNode)) {
					return Endpoint.getEndpoint(this.urlOfLocalNode).getNode();
				} else {
					return create(nodeInfo.getNodeURL(), this.urlOfLocalNode,
							nodeInfo.getNodeID());
				}
			} catch (ClassCastException e) {
				/*
				 * This should not occur as all nodes should have the same
				 * classes!
				 */
				String message = "Could not understand result! "
						+ response.getResult();
				logger.fatal(message);
				throw new CommunicationException(message, e);
			}
		}
	}

	/**
	 * @return The id of the node represented by this proxy.
	 * @throws CommunicationException
	 */
	private void initializeNodeID() throws CommunicationException {
		if (this.nodeID == null) {
			this.makeSocketAvailable();

			logger.debug("Trying to get node ID ");

			/* prepare request for method findSuccessor */
			Request request = this.createRequest(MethodConstants.GET_NODE_ID,
					new Serializable[0]);
			/* send request */
			try {
				logger.debug("Trying to send request " + request);
				this.send(request);
			} catch (CommunicationException ce) {
				logger.debug("Connection failed!");
				throw ce;
			}
			/* wait for response */
			logger.debug("Waiting for response for request " + request);
			Response response = this.waitForResponse(request);
			logger.debug("Response " + response + " arrived.");
			if (response.isFailureResponse()) {
				throw new CommunicationException(response.getFailureReason());
			} else {
				try {
					this.nodeID = (ID) response.getResult();
				} catch (ClassCastException e) {
					/*
					 * This should not occur as all nodes should have the same
					 * classes!
					 */
					String message = "Could not understand result! "
							+ response.getResult();
					logger.fatal(message);
					throw new CommunicationException(message);
				}
			}
		}
	}

	/**
	 * @param potentialPredecessor
	 * @return List of references for the node invoking this method. See
	 *         {@link Node#notify(Node)}.
	 */
	public List<Node> notify(Node potentialPredecessor)
			throws CommunicationException {
		this.makeSocketAvailable();

		RemoteNodeInfo nodeInfoToSend = new RemoteNodeInfo(potentialPredecessor
				.getNodeURL(), potentialPredecessor.getNodeID());

		Request request = this.createRequest(MethodConstants.NOTIFY,
				new Serializable[] { nodeInfoToSend });

		/* send request to remote node. */
		try {
			this.send(request);
		} catch (CommunicationException e) {
			throw e;
		}

		/* wait for response to arrive */
		Response response = this.waitForResponse(request);
		if (response.isFailureResponse()) {
			throw new CommunicationException(response.getFailureReason());
		} else {
			try {
				List<RemoteNodeInfo> references = (List<RemoteNodeInfo>) response
						.getResult();
				List<Node> nodes = new LinkedList<Node>();
				for (RemoteNodeInfo nodeInfo : references) {
					if (nodeInfo.getNodeURL().equals(this.urlOfLocalNode)) {
						nodes.add(Endpoint.getEndpoint(this.urlOfLocalNode)
								.getNode());
					} else {
						nodes.add(create(nodeInfo.getNodeURL(),
								this.urlOfLocalNode, nodeInfo.getNodeID()));
					}
				}
				return nodes;
			} catch (ClassCastException cce) {
				throw new CommunicationException(
						"Could not understand result! " + response.getResult(),
						cce);
			}
		}
	}

	/**
	 * @throws CommunicationException
	 */
	public void ping() throws CommunicationException {
		this.makeSocketAvailable();

		boolean debugEnabled = SocketProxy.logger.isEnabledFor(DEBUG);

		if (debugEnabled) {
			logger.debug("Trying to ping remote node " + this.nodeURL);
		}

		/* prepare request for method findSuccessor */
		Request request = this.createRequest(MethodConstants.PING,
				new Serializable[0]);
		/* send request */
		try {
			logger.debug("Trying to send request " + request);
			this.send(request);
		} catch (CommunicationException ce) {
			logger.debug("Connection failed!");
			throw ce;
		}
		/* wait for response */
		if (debugEnabled) {
			logger.debug("Waiting for response for request " + request);
		}
		Response response = this.waitForResponse(request);
		if (debugEnabled) {
			logger.debug("Response " + response + " arrived.");
		}
		if (response.isFailureResponse()) {
			throw new CommunicationException(response.getFailureReason());
		} else {
			return;
		}

	}

	/**
	 * @param entry
	 * @throws CommunicationException
	 */
	public void insertEntry(Entry entry) throws CommunicationException {
		this.makeSocketAvailable();

		logger.debug("Trying to insert entry " + entry + ".");

		/* prepare request for method insertEntry */
		Request request = this.createRequest(MethodConstants.INSERT_ENTRY,
				new Serializable[] { entry });
		/* send request */
		try {
			logger.debug("Trying to send request " + request);
			this.send(request);
		} catch (CommunicationException ce) {
			logger.debug("Connection failed!");
			throw ce;
		}
		/* wait for response */
		logger.debug("Waiting for response for request " + request);
		Response response = this.waitForResponse(request);
		logger.debug("Response " + response + " arrived.");
		if (response.isFailureResponse()) {
			throw new CommunicationException(response.getFailureReason());
		} else {
			/* No result here */
			return;
		}
	}

	/**
	 * @param replicas
	 * @throws CommunicationException
	 */
	public void insertReplicas(Set<Entry> replicas)
			throws CommunicationException {
		this.makeSocketAvailable();

		logger.debug("Trying to insert replicas " + replicas + ".");

		/* prepare request for method insertEntry */
		Request request = this.createRequest(MethodConstants.INSERT_REPLICAS,
				new Serializable[] { (Serializable) replicas });
		/* send request */
		try {
			logger.debug("Trying to send request " + request);
			this.send(request);
		} catch (CommunicationException ce) {
			logger.debug("Connection failed!");
			throw ce;
		}
		/* wait for response */
		logger.debug("Waiting for response for request " + request);
		Response response = this.waitForResponse(request);
		logger.debug("Response " + response + " arrived.");
		if (response.isFailureResponse()) {
			throw new CommunicationException(response.getFailureReason());
		} else {
			/* No result here */
			return;
		}
	}

	/**
	 * @param predecessor
	 * @throws CommunicationException
	 */
	public void leavesNetwork(Node predecessor) throws CommunicationException {
		this.makeSocketAvailable();

		logger.debug("Trying to insert notify node that " + predecessor
				+ " leaves network.");

		RemoteNodeInfo nodeInfo = new RemoteNodeInfo(predecessor.getNodeURL(),
				predecessor.getNodeID());

		/* prepare request for method insertEntry */
		Request request = this.createRequest(MethodConstants.LEAVES_NETWORK,
				new Serializable[] { nodeInfo });
		/* send request */
		try {
			logger.debug("Trying to send request " + request);
			this.send(request);
		} catch (CommunicationException ce) {
			logger.debug("Connection failed!");
			throw ce;
		}
		/* wait for response */
		logger.debug("Waiting for response for request " + request);
		Response response = this.waitForResponse(request);
		logger.debug("Response " + response + " arrived.");
		if (response.isFailureResponse()) {
			throw new CommunicationException(response.getFailureReason());
		} else {
			/* No result here */
			return;
		}
	}

	/**
	 * @param entry
	 * @throws CommunicationException
	 */
	public void removeEntry(Entry entry) throws CommunicationException {
		this.makeSocketAvailable();

		logger.debug("Trying to remove entry " + entry + ".");

		/* prepare request for method findSuccessor */
		Request request = this.createRequest(MethodConstants.REMOVE_ENTRY,
				new Serializable[] { entry });
		/* send request */
		try {
			logger.debug("Trying to send request " + request);
			this.send(request);
		} catch (CommunicationException ce) {
			logger.debug("Connection failed!");
			throw ce;
		}
		/* wait for response */
		logger.debug("Waiting for response for request " + request);
		Response response = this.waitForResponse(request);
		logger.debug("Response " + response + " arrived.");
		if (response.isFailureResponse()) {
			throw new CommunicationException(response.getFailureReason());
		} else {
			/* No result here */
			return;
		}

	}

	/**
	 * @param sendingNodeID
	 * @param replicas
	 * @throws CommunicationException
	 */
	public void removeReplicas(ID sendingNodeID, Set<Entry> replicas)
			throws CommunicationException {
		this.makeSocketAvailable();

		logger.debug("Trying to remove replicas " + replicas + ".");

		/* prepare request for method insertEntry */
		Request request = this.createRequest(MethodConstants.REMOVE_REPLICAS,
				new Serializable[] { sendingNodeID, (Serializable) replicas });
		/* send request */
		try {
			logger.debug("Trying to send request " + request);
			this.send(request);
		} catch (CommunicationException ce) {
			logger.debug("Connection failed!");
			throw ce;
		}
		/* wait for response */
		logger.debug("Waiting for response for request " + request);
		Response response = this.waitForResponse(request);
		logger.debug("Response " + response + " arrived.");
		if (response.isFailureResponse()) {
			throw new CommunicationException(response.getFailureReason());
		} else {
			/* No result here */
			return;
		}
	}

	public Set<Entry> retrieveEntries(ID id) throws CommunicationException {
		this.makeSocketAvailable();

		logger.debug("Trying to retrieve entries for ID " + id);

		/* prepare request for method findSuccessor */
		Request request = this.createRequest(MethodConstants.RETRIEVE_ENTRIES,
				new Serializable[] { id });
		/* send request */
		try {
			logger.debug("Trying to send request " + request);
			this.send(request);
		} catch (CommunicationException ce) {
			logger.debug("Connection failed!");
			throw ce;
		}
		/* wait for response */
		logger.debug("Waiting for response for request " + request);
		Response response = this.waitForResponse(request);
		logger.debug("Response " + response + " arrived.");
		if (response.isFailureResponse()) {
			throw new CommunicationException(response.getFailureReason(),
					response.getThrowable());
		} else {
			try {
				Set<Entry> result = (Set<Entry>) response.getResult();
				return result;
			} catch (ClassCastException cce) {
				throw new CommunicationException(
						"Could not understand result! " + response.getResult());
			}
		}
	}

	/**
	 * This method has to be called at first in every method that uses the
	 * socket to connect to the node this is the proxy for. This method
	 * establishes the connection if not already done. This method has to be
	 * called as this proxy can be serialized and the reference to the socket is
	 * transient. So by calling this method after a transfer the connection to
	 * the node is reestablished. The same applies for {@link #logger}and
	 * {@link #responses}.
	 * 
	 * @throws CommunicationException
	 */
	private void makeSocketAvailable() throws CommunicationException {
		if (this.disconnected) {
			throw new CommunicationException("Connection from "
					+ this.urlOfLocalNode + " to remote host " + this.nodeURL
					+ " is broken down. ");
		}

		logger.debug("makeSocketAvailable() called. "
				+ "Testing for socket availability");

		if (this.responses == null) {
			this.responses = new HashMap<String, Response>();
		}
		if (this.waitingThreads == null) {
			this.waitingThreads = new HashMap<String, WaitingThread>();
		}
		if (this.mySocket == null) {
			try {
				logger.info("Opening new socket to " + this.nodeURL);
				this.mySocket = new Socket(this.nodeURL.getHost(), this.nodeURL
						.getPort());
				logger.debug("Socket created: " + this.mySocket);
				this.mySocket.setSoTimeout(5000);
				this.out = new ObjectOutputStream(this.mySocket
						.getOutputStream());
				this.in = new ObjectInputStream(this.mySocket.getInputStream());
				logger.debug("Sending connection request!");
				out.writeObject(new Request(MethodConstants.CONNECT,
						"Initial Connection"));
				try {
					// set time out, in case the other side does not answer!
					Response resp = null;
					boolean timedOut = false;
					try {
						logger.debug("Waiting for connection response!");
						resp = (Response) in.readObject();
					} catch (SocketTimeoutException e) {
						logger.info("Connection timed out!");
						timedOut = true;
					}
					this.mySocket.setSoTimeout(0);
					if (timedOut) {
						throw new CommunicationException(
								"Connection to remote host timed out!");
					}
					if (resp != null
							&& resp.getStatus() == Response.REQUEST_SUCCESSFUL) {
						Thread t = new Thread(this, "SocketProxy_Thread_"
								+ this.nodeURL);
						t.start();
					} else {
						throw new CommunicationException(
								"Establishing connection failed!");
					}
				} catch (ClassNotFoundException e) {
					throw new CommunicationException(
							"Unexpected result received! " + e.getMessage(), e);
				} catch (ClassCastException e) {
					throw new CommunicationException(
							"Unexpected result received! " + e.getMessage(), e);
				}
			} catch (UnknownHostException e) {
				throw new CommunicationException("Unknown host: "
						+ this.nodeURL.getHost());
			} catch (IOException ioe) {
				throw new CommunicationException("Could not set up IO channel "
						+ "to host " + this.nodeURL.getHost(), ioe);
			}
		}
		logger.debug("makeSocketAvailable() finished. Socket " + this.mySocket);
	}

	/**
	 * Finalization ensures that the socket is closed if this proxy is not
	 * needed anymore.
	 * 
	 * @throws Throwable
	 */
	protected void finalize() throws Throwable {
		logger.debug("Finalization running.");
	}

	/**
	 * Tells this proxy that it is not needed anymore.
	 */
	public void disconnect() {

		logger.info("Destroying connection from " + this.urlOfLocalNode
				+ " to " + this.nodeURL);

		synchronized (proxies) {
			/*
			 * added on 21.03.2006 by sven. See documentation of method
			 * createProxyKey(String, String);
			 */
			String proxyKey = SocketProxy.createProxyKey(this.urlOfLocalNode,
					this.nodeURL);
			Object o = proxies.remove(proxyKey);
		}
		this.disconnected = true;
		try {
			if (this.out != null) {
				try {
					/*
					 * notify endpoint this is connected to, about shut down of
					 * this proxy
					 */
					logger.debug("Sending shutdown notification to endpoint.");
					Request request = this.createRequest(
							MethodConstants.SHUTDOWN, new Serializable[0]);
					logger.debug("Notification send.");
					this.out.writeObject(request);
					this.out.close();
					this.out = null;
					logger.debug("OutputStream " + this.out + " closed.");
				} catch (IOException e) {
					/* should not occur */
					logger.debug(this
							+ ": Exception during closing of output stream "
							+ this.out, e);
				}
			}
			if (this.in != null) {
				try {
					this.in.close();
					logger.debug("InputStream " + this.in + " closed.");
					this.in = null;
				} catch (IOException e) {
					/* should not occur */
					logger.debug("Exception during closing of input stream"
							+ this.in);
				}
			}
			if (this.mySocket != null) {
				try {
					logger.info("Closing socket " + this.mySocket + ".");
					this.mySocket.close();
				} catch (IOException e) {
					/* should not occur */
					logger.debug("Exception during closing of socket "
							+ this.mySocket);
				}
				this.mySocket = null;
			}
		} catch (Throwable t) {
			logger.warn("Unexpected exception during disconnection of SocketProxy", t);
		}
		this.connectionBrokenDown();
	}

	/**
	 * The run methods waits for incoming
	 * {@link de.uniba.wiai.lspi.chord.com.socket.Response} made by this proxy
	 * and puts them into a datastructure from where the can be collected by the
	 * associated method call that made a
	 * {@link de.uniba.wiai.lspi.chord.com.socket.Request} to the {@link Node},
	 * that this is the proxy for.
	 */
	public void run() {
		while (!this.disconnected) {
			try {
				Response response = (Response) this.in.readObject();
				logger.debug("Response " + response + "received!");
				this.responseReceived(response);
			} catch (ClassNotFoundException cnfe) {
				/* should not occur, as all classes must be locally available */
				logger
						.fatal(
								"ClassNotFoundException occured during deserialization "
										+ "of response. There is something seriously wrong "
										+ " here! ", cnfe);
			} catch (IOException e) {
				if (!this.disconnected) {
					logger.warn("Could not read response from stream!", e);
				} else {
					logger.debug(this + ": Connection has been closed!");
				}
				this.connectionBrokenDown();
			}
		}
	}

	/**
	 * @param potentialPredecessor
	 * @return See {@link Node#notifyAndCopyEntries(Node)}.
	 * @throws CommunicationException
	 */
	public RefsAndEntries notifyAndCopyEntries(Node potentialPredecessor)
			throws CommunicationException {
		this.makeSocketAvailable();

		RemoteNodeInfo nodeInfoToSend = new RemoteNodeInfo(potentialPredecessor
				.getNodeURL(), potentialPredecessor.getNodeID());

		/* prepare request for method notifyAndCopyEntries */
		Request request = this.createRequest(MethodConstants.NOTIFY_AND_COPY,
				new Serializable[] { nodeInfoToSend });
		/* send request */
		try {
			logger.debug("Trying to send request " + request);
			this.send(request);
		} catch (CommunicationException ce) {
			logger.debug("Connection failed!");
			throw ce;
		}
		/* wait for response */
		logger.debug("Waiting for response for request " + request);
		Response response = this.waitForResponse(request);
		logger.debug("Response " + response + " arrived.");
		if (response.isFailureResponse()) {
			throw new CommunicationException(response.getFailureReason(),
					response.getThrowable());
		} else {
			try {
				RemoteRefsAndEntries result = (RemoteRefsAndEntries) response
						.getResult();
				List<Node> newReferences = new LinkedList<Node>();
				List<RemoteNodeInfo> references = result.getNodeInfos();
				for (RemoteNodeInfo nodeInfo : references) {
					if (nodeInfo.getNodeURL().equals(this.urlOfLocalNode)) {
						newReferences.add(Endpoint.getEndpoint(
								this.urlOfLocalNode).getNode());
					} else {
						newReferences.add(create(nodeInfo.getNodeURL(),
								this.urlOfLocalNode, nodeInfo.getNodeID()));
					}
				}
				return new RefsAndEntries(newReferences, result.getEntries());
			} catch (ClassCastException cce) {
				throw new CommunicationException(
						"Could not understand result! " + response.getResult());
			}
		}
	}

	/**
	 * The string representation of this proxy. Created when {@link #toString()}
	 * is invoked for the first time.
	 */
	private String stringRepresentation = null;

	/**
	 * @return String representation of this.
	 */
	public String toString() {
		if (this.nodeID == null || this.mySocket == null) {
			return "Unconnected SocketProxy from " + this.urlOfLocalNode + " to " + this.nodeURL; 
		}
		if (this.stringRepresentation == null) {
			StringBuilder builder = new StringBuilder();
			builder.append("Connection from Node[url=");
			builder.append(this.urlOfLocalNode);
			builder.append(", socket=");
			builder.append(this.mySocket);
			builder.append("] to Node[id=");
			builder.append(this.nodeID);
			builder.append(", url=");
			builder.append(this.nodeURL);
			builder.append("]");
			this.stringRepresentation = builder.toString();
		}
		return this.stringRepresentation;
	}

	/**
	 * Wraps a thread, which is waiting for a response.
	 * 
	 * @author sven
	 * 
	 */
	private static class WaitingThread {

		private boolean hasBeenWokenUp = false;

		private Thread thread;

		private WaitingThread(Thread thread) {
			this.thread = thread;
		}

		/**
		 * Returns <code>true</code> when the thread has been woken up by
		 * invoking {@link #wakeUp()}
		 * 
		 * @return
		 */
		boolean hasBeenWokenUp() {
			return this.hasBeenWokenUp;
		}

		/**
		 * Wake up the thread that is waiting for a response.
		 * 
		 */
		void wakeUp() {
			this.hasBeenWokenUp = true;
			this.thread.interrupt();
		}

		public String toString() {
			return this.thread.toString() + ": Waiting? "
					+ !this.hasBeenWokenUp();
		}
	}

}
