/***************************************************************************
 *                                                                         *
 *                            RequestHandler.java                          *
 *                            -------------------                          *
 *   date                 : 02.09.2004, 14:02                              *
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

/*
 * RequestHandler.java
 *
 * Created on 2. September 2004, 14:02
 */

package de.uniba.wiai.lspi.chord.com.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Endpoint;
import de.uniba.wiai.lspi.chord.com.EndpointStateListener;
import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.RefsAndEntries;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.util.logging.Logger;
import static de.uniba.wiai.lspi.util.logging.Logger.LogLevel.*;

/**
 * This class handles {@link Request requests} for a single incoming connection
 * from another node sent through a {@link SocketProxy proxy} that represents
 * the local node at the remote node.
 * 
 * @author sven
 * @version 1.0.5
 */
final class RequestHandler extends Thread implements EndpointStateListener {

	/**
	 * Logger for this class.
	 */
	private static Logger logger = Logger.getLogger(RequestHandler.class);

	/**
	 * The node this RequestHandler invokes methods on.
	 */
	private Node node;

	/**
	 * The socket over that this RequestHandler receives requests.
	 */
	private Socket connection;

	/**
	 * {@link ObjectOutputStream}to write answers to.
	 */
	private ObjectOutputStream out;

	/**
	 * {@link ObjectInputStream}to read {@link Request requests}from.
	 */
	private ObjectInputStream in;

	/**
	 * Indicates if this RequestHandler is connected. Used in {@link #run()} to
	 * determine if this is still listening for requests.
	 */
	boolean connected = true;

	/**
	 * The state that the {@link SocketEndpoint endpoint}, that started this
	 * request handler, is currently in. See constants of class
	 * {@link de.uniba.wiai.lspi.chord.com.Endpoint}.
	 */
	private int state;

	/**
	 * The {@link SocketEndpoint endpoint}that started this handler.
	 */
	private SocketEndpoint endpoint;

	/**
	 * This {@link Vector}contains {@link Thread threads}waiting for a state
	 * of the {@link SocketEndpoint endpoint}that permits the execution of the
	 * methods the threads are about to execute. This is also used as
	 * synchronization variable for these threads.
	 */
	private Set<Thread> waitingThreads = new HashSet<Thread>();

	/**
	 * Creates a new instance of RequestHandler
	 * 
	 * @param node_
	 *            The {@link Node node}to delegate requested methods to.
	 * @param connection_
	 *            The {@link Socket}over which this receives requests.
	 * @param ep
	 * @throws IOException 
	 * 
	 * @throws IOException
	 *             Thrown if the establishment of a connection over the provided
	 *             socket fails.
	 */
	RequestHandler(Node node_, Socket connection_, SocketEndpoint ep) throws IOException {
		super("RequestHandler_" + ep.getURL());

		if (RequestHandler.logger.isEnabledFor(INFO)) {
			RequestHandler.logger.info("Initialising RequestHandler. Socket "
					+ connection_ + ", " + ", Endpoint " + ep);
		}
		// logger = Logger.getLogger(this.getClass().toString() +
		// connection.toString());
		this.node = node_;
		this.connection = connection_;
		this.out = new ObjectOutputStream(this.connection.getOutputStream());
		try {
			this.in = new ObjectInputStream(this.connection.getInputStream());
		} catch (IOException e1) {
			out.close(); 
			throw e1; 
		}
		try {
			Request r = (Request) this.in.readObject();
			if (r.getRequestType() != MethodConstants.CONNECT) {
				Response resp = new Response(Response.REQUEST_FAILED, r
						.getRequestType(), r.getReplyWith());
				try {
					out.writeObject(resp);
				} catch (IOException e) {
				}
				try {
					out.close();
				} catch (IOException e) {}
				try {
					in.close();
				} catch (IOException e) {} 
				throw new IOException("Unexpected Message received! " + r);
			} else {
				Response resp = new Response(Response.REQUEST_SUCCESSFUL, r
						.getRequestType(), r.getReplyWith());
				out.writeObject(resp); 
			}
		} catch (ClassNotFoundException e) {
			throw new IOException("Unexpected class type received! " + e.getMessage()); 
		}
		this.endpoint = ep;
		this.state = this.endpoint.getState();
		this.endpoint.register(this);
		logger.info("RequestHandler initialised.");
	}

	/**
	 * Returns a reference to the endpoint this {@link RequestHandler} belongs
	 * to.
	 * 
	 * @return Reference to the endpoint this {@link RequestHandler} belongs to.
	 */
	SocketEndpoint getEndpoint() {
		return this.endpoint;
	}

	/**
	 * The task of this Thread. Listens for incoming requests send over the
	 * {@link #connection}of this thread. The thread can be stopped by invoking
	 * {@link #disconnect()}.
	 */
	@Override
	public void run() {
		/*
		 * As long as this is connected
		 */
		while (this.connected) {
			Request request = null;
			try {
				/* wait for incoming requests */
				logger.debug("Waiting for request...");
				request = (Request) this.in.readObject();
				if (request.getRequestType() == MethodConstants.SHUTDOWN) {
					logger.debug("Received shutdown request");
					this.disconnect();
				} else {
					logger.debug("Received request " + request);
					new InvocationThread(this, request, /*
														 * "Request_" +
														 * MethodConstants.getMethodName(request
														 * .getRequestType()) +
														 * "_" +
														 * request.getReplyWith(),
														 */this.out);
				}
			} catch (IOException e) {
				/*
				 * This can also occur if disconnect() is called, as the socket
				 * is closed then
				 */
				logger.debug("Exception occured while receiving a request. "
						+ "Maybe socket has been closed.");
				/* cannot do anything here but disconnect */
				this.disconnect();
			} catch (ClassNotFoundException cnf) {
				/* Should not occur as all nodes should have the same classes */
				logger.error("Exception occured while receiving a request ",
						cnf);
				/* cannot do anything here */
				this.disconnect();
			} catch (Throwable t) {
				logger
						.fatal("Unexpected throwable while receiving message!",
								t);
				this.disconnect();
			}
		}
	}

	/**
	 * Method to create failure responses and send them to the requestor.
	 * 
	 * @param t
	 * @param failure
	 * @param request
	 */
	void sendFailureResponse(Throwable t, String failure, Request request) {
		if (!this.connected) {
			return;
		}
		logger.debug("Trying to send failure response. Failure reason "
				+ failure);
		Response failureResponse = new Response(Response.REQUEST_FAILED,
				request.getRequestType(), request.getReplyWith());
		failureResponse.setFailureReason(failure);
		failureResponse.setThrowable(t);
		try {
			synchronized (this.out) {
				this.out.writeObject(failureResponse);
				this.out.flush();
				this.out.reset();
			}
			logger.debug("Response send.");
		} catch (IOException e) {
			if (this.connected) {
				logger.debug("Connection seems to be broken down. Could not "
						+ "send failure response. Connection is closed. ", e);
				this.disconnect();
			}
		}
	}

	/**
	 * Invokes methods on {@link #node}.
	 * 
	 * @param methodType
	 *            The type of the method to invoke. See {@link MethodConstants}.
	 * @param parameters
	 *            The parameters to pass to the method.
	 * @return The result of the invoked method. May be <code>null</code> if
	 *         method is void.
	 * @throws Exception
	 */
	Serializable invokeMethod(int methodType, Serializable[] parameters)
			throws Exception {

		String method = MethodConstants.getMethodName(methodType);
		this.waitForMethod(method);
		/* If we got disconnected while waiting */
		if (!this.connected) {
			/* throw an Exception */
			throw new CommunicationException("Connection closed.");
		}
		Serializable result = null;
		logger.debug("Trying to invoke method " + methodType
				+ " with parameters: ");
		for (Serializable parameter : parameters) {
			logger.debug(parameter);
		}
		switch (methodType) {
		case MethodConstants.FIND_SUCCESSOR: {
			Node chordNode = this.node.findSuccessor((ID) parameters[0]);
			result = new RemoteNodeInfo(chordNode.getNodeURL(), chordNode.getNodeID());
			break;
		}
		case MethodConstants.GET_NODE_ID: {
			result = this.node.getNodeID();
			break;
		}
		case MethodConstants.INSERT_ENTRY: {
			this.node.insertEntry((Entry) parameters[0]);
			break;
		}
		case MethodConstants.INSERT_REPLICAS: {
			this.node.insertReplicas((Set<Entry>) parameters[0]);
			break;
		}
		case MethodConstants.LEAVES_NETWORK: {
			RemoteNodeInfo nodeInfo = (RemoteNodeInfo) parameters[0];
			this.node.leavesNetwork(SocketProxy.create(nodeInfo.getNodeURL(),
					this.node.getNodeURL(), nodeInfo.getNodeID()));
			break;
		}
		case MethodConstants.NOTIFY: {
			RemoteNodeInfo nodeInfo = (RemoteNodeInfo) parameters[0];
			List<Node> l = this.node.notify(SocketProxy.create(nodeInfo
					.getNodeURL(), this.node.getNodeURL(), nodeInfo.getNodeID()));
			List<RemoteNodeInfo> nodeInfos = new LinkedList<RemoteNodeInfo>();
			for (Node current : l) {
				nodeInfos.add(new RemoteNodeInfo(current.getNodeURL(),
						current.getNodeID()));
			}
			result = (Serializable) nodeInfos;
			break;
		}
		case MethodConstants.NOTIFY_AND_COPY: {
			RemoteNodeInfo nodeInfo = (RemoteNodeInfo) parameters[0];
			RefsAndEntries refs = this.node.notifyAndCopyEntries(SocketProxy
					.create(nodeInfo.getNodeURL(), this.node.getNodeURL(), nodeInfo
							.getNodeID()));
			List<Node> l = refs.getRefs();
			List<RemoteNodeInfo> nodeInfos = new LinkedList<RemoteNodeInfo>();
			for (Node current : l) {
				nodeInfos.add(new RemoteNodeInfo(current.getNodeURL(),
						current.getNodeID()));
			}
			RemoteRefsAndEntries rRefs = new RemoteRefsAndEntries(refs
					.getEntries(), nodeInfos);
			result = rRefs;
			break;
		}
		case MethodConstants.PING: {
			logger.debug("Invoking ping()");
			this.node.ping();
			logger.debug("ping() invoked.");
			break;
		}
		case MethodConstants.REMOVE_ENTRY: {
			this.node.removeEntry((Entry) parameters[0]);
			break;
		}
		case MethodConstants.REMOVE_REPLICAS: {
			this.node.removeReplicas((ID) parameters[0],
					(Set<Entry>) parameters[1]);
			break;
		}
		case MethodConstants.RETRIEVE_ENTRIES: {
			result = (Serializable) this.node
					.retrieveEntries((ID) parameters[0]);
			break;
		}
		default: {
			logger.warn("Unknown method requested " + method);
			throw new Exception("Unknown method requested " + method);
		}
		}
		logger.debug("Returning result.");
		return result;
	}

	/**
	 * This method is used to block threads that want to make a method call
	 * until the method invocation is permitted by the endpoint. Invocation of a
	 * method depends on the state of the endpoint.
	 * 
	 * @param method
	 *            The name of the method to invoke. TODO: change this to another
	 *            type.
	 */
	private void waitForMethod(String method) {

		logger
				.debug(method
						+ " allowed? "
						+ !(Collections.binarySearch(
								Endpoint.METHODS_ALLOWED_IN_ACCEPT_ENTRIES,
								method) >= 0));
		synchronized (this.waitingThreads) {
			while ((!(this.state == Endpoint.ACCEPT_ENTRIES))
					&& (this.connected)
					&& ((Collections.binarySearch(
							Endpoint.METHODS_ALLOWED_IN_ACCEPT_ENTRIES, method) >= 0))) {

				Thread currentThread = Thread.currentThread();
				boolean debug = logger.isEnabledFor(DEBUG);
				if (debug) {
					logger.debug("HERE!!!" + currentThread
							+ " waiting for permission to " + "execute "
							+ method);
				}
				this.waitingThreads.add(currentThread);
				try {
					this.waitingThreads.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
				if (debug) {
					logger.debug("HERE!!!" + currentThread
							+ " has been notified.");
				}
				this.waitingThreads.remove(currentThread);
			}
		}
		logger.debug("waitForMethod(" + method + ") returns!");
	}

	/**
	 * Disconnect this RequestHandler. Forces the socket, which this
	 * RequestHandler is bound to, to be closed and {@link #run()}to be
	 * stopped.
	 */
	public void disconnect() {

		logger.info("Disconnecting.");
		if (this.connected) {
			/* cause the while loop in run() method to be finished */
			/* and notify all threads waiting for execution of a method */
			synchronized (this.waitingThreads) {
				this.connected = false;
				this.waitingThreads.notifyAll();
			}
			/* release reference to node. */
			this.node = null;
			/* try to close the socket */
			try {
				synchronized (this.out) {
					this.out.close();
					this.out = null;
				}
			} catch (IOException e) {
				/* should not occur */
				/* if closing of socket fails, that does not matter!??? */
				logger.debug("Exception while closing output stream "
						+ this.out);
			}
			try {
				this.in.close();
				this.in = null;
			} catch (IOException e) {
				/* should not occur */
				/* if closing of socket fails, that does not matter!??? */
				logger.debug("Exception while closing input stream" + this.in);
			}
			try {
				logger.info("Closing socket " + this.connection);
				this.connection.close();
				this.connection = null;
				logger.info("Socket closed.");
			} catch (IOException e) {
				/* should not occur */
				/* if closing of socket fails, that does not matter!??? */
				logger.debug("Exception while closing socket "
						+ this.connection);
			}
			this.endpoint.deregister(this);
		}
		logger.debug("Disconnected.");
	}

	/**
	 * Test if this RequestHandler is disconnected
	 * 
	 * @return <code>true</code> if this is still connected to its remote end.
	 */
	public boolean isConnected() {
		return this.connected;
	}

	public void notify(int newState) {
		logger.debug("notify(" + newState + ") called.");
		this.state = newState;
		/* notify all threads waiting for a state change */
		synchronized (this.waitingThreads) {
			logger.debug("HERE!!! Notifying waiting threads. "
					+ this.waitingThreads);
			this.waitingThreads.notifyAll();
		}
	}

}
