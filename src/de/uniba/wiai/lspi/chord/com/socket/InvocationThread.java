/***************************************************************************
 *                                                                         *
 *                           InvocationThread.java                         *
 *                            -------------------                          *
 *   date                 : 01.09.2004                                    *
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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.uniba.wiai.lspi.util.logging.Logger;
import static de.uniba.wiai.lspi.util.logging.Logger.LogLevel.*;

/**
 * This <code>Thread</code> is used to make a method invocation on a node that
 * is accessible through sockets over its {@link SocketEndpoint}.
 * 
 * @author sven
 * @version 1.0.5
 */
class InvocationThread implements Runnable {

	/**
	 * Name of property which defines the number of threads in pool created by
	 * {@link #createInvocationThreadPool()}.
	 */
	protected static final String CORE_POOL_SIZE_PROPERTY_NAME = InvocationThread.class
			.getName()
			+ ".corepoolsize";

	/**
	 * Name of property which defines the maximum number of threads in pool
	 * created by {@link #createInvocationThreadPool()}.
	 */
	protected static final String MAX_POOL_SIZE_PROPERTY_NAME = InvocationThread.class
			.getName()
			+ ".maxpoolsize";

	/**
	 * Name of property which defines the time the threads in pool created by
	 * {@link #createInvocationThreadPool()} can stay idle before being
	 * terminated.
	 */
	protected static final String KEEP_ALIVE_TIME_PROPERTY_NAME = InvocationThread.class
			.getName()
			+ ".keepalivetime";

	/**
	 * The number of core threads in ThreadPool created by
	 * {@link #createInvocationThreadPool()}.
	 */
	private static final int CORE_POOL_SIZE = Integer.parseInt(System
			.getProperty(CORE_POOL_SIZE_PROPERTY_NAME));

	/**
	 * The maximum number of threads in ThreadPool created by
	 * {@link #createInvocationThreadPool()}.
	 */
	private static final int MAX_POOL_SIZE = Integer.parseInt(System
			.getProperty(MAX_POOL_SIZE_PROPERTY_NAME));

	/**
	 * The time threads in ThreadPool created by
	 * {@link #createInvocationThreadPool()} can be idle before being
	 * terminated.
	 */
	private static final int KEEP_ALIVE_TIME = Integer.parseInt(System
			.getProperty(KEEP_ALIVE_TIME_PROPERTY_NAME));

	/**
	 * The logger for instances of this class.
	 */
	private static final Logger logger = Logger
			.getLogger(InvocationThread.class);

	private static final boolean debug = logger.isEnabledFor(DEBUG);

	/**
	 * The request that has to be handled by this InvocationThread. Represents
	 * the method to be invoked.
	 */
	private Request request;

	/**
	 * The {@link RequestHandler} that started this thread.
	 */
	private RequestHandler handler;

	/**
	 * The {@link ObjectOutputStream} to write the results of the invocation to.
	 */
	private ObjectOutputStream out;

	/**
	 * 
	 * @param handler1
	 *            Reference to {@link RequestHandler} that started this.
	 * @param request1
	 *            The {@link Request} that caused this invocation to be started.
	 * @param out1
	 *            The stream to which to write the result of the invocation.
	 */
	InvocationThread(RequestHandler handler1, Request request1,
			ObjectOutputStream out1) {
		this.handler = handler1;
		this.request = request1;
		this.out = out1;
		// schedule this for execution
		this.handler.getEndpoint().scheduleInvocation(this);
		if (debug) {
			logger.debug("InvocationThread scheduled for request " + request1);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Invocation of ");
		sb.append(MethodConstants.getMethodName(this.request.getRequestType()));
		sb.append("] Request: ");
		sb.append(this.request);
		return sb.toString();
	}

	/**
	 * This <code>run</code>-method invokes the Method that is assigned to it
	 * by {@link Request} provided in its
	 * {@link #InvocationThread(RequestHandler, Request, ObjectOutputStream) constructor}.
	 */
	public void run() {
		if (debug) {
			logger.debug(this + " started");
		}
		int requestType = this.request.getRequestType();
		String methodName = MethodConstants.getMethodName(requestType);
		if (debug) {
			logger.debug("Request received. Requested method: " + methodName);
		}
		/* and try to execute the requested method */
		try {
			if (debug) {
				logger.debug("Trying to invoke method " + methodName);
			}
			Serializable result = this.handler.invokeMethod(requestType,
					this.request.getParameters());
			/* Send result of requested method back to requestor. */
			Response response = new Response(Response.REQUEST_SUCCESSFUL,
					requestType, this.request.getReplyWith());
			response.setResult(result);
			synchronized (this.out) {
				this.out.writeObject(response);
				this.out.flush();
				this.out.reset();
			}
			logger.debug("Method invoked and result has been sent.");
		} catch (IOException e) {
			if (this.handler.connected) {
				logger.warn("Could not send response. Disconnecting!", e);
				this.handler.disconnect();
			}
			/* else socket has been closed */
		} catch (Exception t) {
			if (debug) {
				logger.debug("Throwable occured during execution of request "
						+ MethodConstants.getMethodName(requestType) + "!");
			}
			this.handler.sendFailureResponse(t, "Could not execute request! "
					+ "Reason unknown! Maybe this helps: " + t.getMessage(),
					this.request);
		}
//		this.request = null;
		this.handler = null;
		this.out = null;
		if (debug) {
			logger.debug(this + " finished");
		}
	}

	/**
	 * Creates a ThreadPool that is used by the {@link SocketEndpoint} to
	 * execute instances of this class.
	 * 
	 * @return A ThreadPool that is used by the {@link SocketEndpoint} to
	 *         execute instances of this class.
	 */
	static ThreadPoolExecutor createInvocationThreadPool() {
		return new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
				KEEP_ALIVE_TIME, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

					private static final String name = "InvocationExecution-";

					public Thread newThread(Runnable r) {
						Thread newThread = new Thread(r);
						newThread.setName(name + newThread.getName());
						return newThread;
					}

				});
	}
}