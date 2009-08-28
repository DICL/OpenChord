/***************************************************************************
 *                                                                         *
 *                            SocketEndpoint.java                          *
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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import de.uniba.wiai.lspi.chord.com.Endpoint;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.util.logging.Logger;
import static de.uniba.wiai.lspi.util.logging.Logger.LogLevel.*;

/**
 * This class represents an {@link Endpoint} for communication over socket
 * protocol. It provides a <code>ServerSocket</code> to that clients can
 * connect and starts for each incoming connection a
 * {@link de.uniba.wiai.lspi.chord.com.socket.RequestHandler} that handles
 * {@link de.uniba.wiai.lspi.chord.com.socket.Request}s for method invocations
 * from remote nodes. These {@link de.uniba.wiai.lspi.chord.com.socket.Request}s
 * are sent by one {@link SocketProxy} representing the node, that this is the
 * endpoint for, at another node.
 * 
 * @author sven
 * @version 1.0.5
 */
public final class SocketEndpoint extends Endpoint implements Runnable {

	/**
	 * Logger for this endpoint.
	 */
	private final static Logger logger = Logger.getLogger(SocketEndpoint.class);

	private final static boolean debug = logger.isEnabledFor(DEBUG);

	/**
	 * {@link Set} containing all {@link RequestHandler}s created by this
	 * endpoint.
	 */
	private Set<RequestHandler> handlers = new HashSet<RequestHandler>();

	/**
	 * The Socket this endpoint listens to for connections.
	 */
	private ServerSocket mySocket = null;

	/**
	 * The {@link java.util.concurrent.Executor} responsible for carrying out
	 * executions of methods with help of an instance of
	 * {@link InvocationThread}.
	 */
	private final ThreadPoolExecutor invocationExecutor = InvocationThread
			.createInvocationThreadPool();

	/**
	 * Creates a new <code>SocketEndpoint</code> for the given {@link Node}
	 * with {@link URL url}. <code>url</code> must have the protocol indexed
	 * by <code>{@link URL#SOCKET_PROTOCOL}</code> in the
	 * <code>{@link URL#KNOWN_PROTOCOLS}</code> array.
	 * 
	 * @param node1
	 *            The {@link Node} node this endpoint provides connections to.
	 * @param url1
	 *            The {@link URL} of this endpoint.
	 */
	public SocketEndpoint(Node node1, URL url1) {
		super(node1, url1);
		SocketEndpoint.logger.info("Initialisation finished.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniba.wiai.lspi.chord.com.Endpoint#openConnections()
	 */
	protected void openConnections() {
		/* Open server socket on port specified by url */
		try {
			if (debug) {
				SocketEndpoint.logger
						.debug("Trying to open server socket on port "
								+ this.url.getPort());
			}
			this.mySocket = new ServerSocket(this.url.getPort());
			this.setState(LISTENING);
			if (debug) {
				SocketEndpoint.logger.debug("Server socket opened on port "
						+ this.url.getPort() + ". Starting listener thread.");
			}
			/* and start thread to listen for incoming connections. */
			Thread listenerThread = new Thread(this, "SocketEndpoint_"
					+ this.url + "_Thread");
			listenerThread.start();
			if (debug) {
				SocketEndpoint.logger.debug("Listener Thread " + listenerThread
						+ "started. ");
			}
		} catch (IOException e) {
			/* TODO: change type of exception */
			throw new RuntimeException(
					"SocketEndpoint could not listen on port "
							+ this.url.getPort() + " " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniba.wiai.lspi.chord.com.Endpoint#entriesAcceptable()
	 */
	protected void entriesAcceptable() {
		if (debug) {
			SocketEndpoint.logger.debug("entriesAcceptable() called");
		}
		this.setState(ACCEPT_ENTRIES);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniba.wiai.lspi.chord.com.Endpoint#closeConnections()
	 */
	protected void closeConnections() {
		this.setState(STARTED);
		/* try to close socket */
		try {
			this.mySocket.close();
		} catch (IOException e) {
			/* should not occur */
			if (debug) {
				SocketEndpoint.logger.debug("Could not close socket "
						+ this.mySocket, e);
			}
		}
		this.invocationExecutor.shutdownNow();
		/*
		 * Close outgoing connections.
		 */
		SocketProxy.shutDownAll(); 
	}

	/**
	 * Run method from {@link Runnable} to accept connections from clients. This
	 * method runs until {@link #closeConnections()} is called. It creates
	 * threads responsible for the handling of requests from other nodes.
	 */
	public void run() {

		// Cleaner cleaner = new Cleaner();
		while (this.getState() > STARTED) {
			if (debug) {
				SocketEndpoint.logger.debug("Waiting for incoming connection.");
			}
			Socket incomingConnection = null; 
			try {
				incomingConnection = this.mySocket.accept();
				if (debug) {
					SocketEndpoint.logger.debug("Incoming connection "
							+ incomingConnection);
				}
				/*
				 * Create a handler for requests that come in over the newly
				 * created socket.
				 */
				if (debug) {
					SocketEndpoint.logger
							.debug("Creating request handler for incoming connection.");
				}
				RequestHandler handler = new RequestHandler(this.node,
						incomingConnection, this);
				/*
				 * Remember handler to be able to close its connection (shut it
				 * down.
				 */
				this.handlers.add(handler);
				/* Start handler thread */
				if (debug) {
					SocketEndpoint.logger
							.debug("Request handler created. Starting thread.");
				}
				handler.start();
				if (debug) {
					SocketEndpoint.logger
							.debug("Request handler thread started.");
				}
			} catch (IOException e) {
				/* Can this happen? */
				if ((this.getState() > STARTED)) {
					if (debug) {
						SocketEndpoint.logger.debug(
								"Could not accept connection from other node!",
								e);
					}
					if (incomingConnection != null) {
						try {
							incomingConnection.close();
						} catch (IOException e1) {
							// can be ignored, as incoming Connection is no longer needed. 
						} 
						incomingConnection = null; 
					}
				} else {
					/* Socket has been closed */
				}
				/* TODO: go on or notify some one? */
			}
		}
		SocketEndpoint.logger.info("Listener thread stopped.");
		/* Disconnect all */
		for (RequestHandler handler : this.handlers) {
			handler.disconnect();
		}
		this.handlers.clear();
	}

	/**
	 * Schedule an invocation of a local method to be executed.
	 * 
	 * @param invocationThread
	 */
	void scheduleInvocation(InvocationThread invocationThread) {
		if (debug) {
			logger.debug("Scheduling invocation: " + invocationThread);
		}
		this.invocationExecutor.execute(invocationThread);
		if (debug) {
			logger.debug("Current jobs: "
					+ this.invocationExecutor.getQueue().size());
			logger.debug("Active jobs: "
					+ this.invocationExecutor.getActiveCount());
			logger.debug("Completed jobs: "
					+ this.invocationExecutor.getCompletedTaskCount());
		}

	}

}
