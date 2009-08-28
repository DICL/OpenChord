/***************************************************************************
 *                                                                         *
 *                             Test.java                                *
 *                            -------------------                          *
 *   date                 : 26.02.2008, 13:37:22                               *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : {sven.kaffille}@uni-bamberg.de                 *
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
package de.uniba.wiai.lspi.chord.com;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;

public class Test {

	private Test() {
	}

	static final String URL1 = "ocrmi://localhost:4245/";

	static final String URL2 = "ocrmi://localhost/";

	public static void main(String[] args) throws MalformedURLException,
			CommunicationException {
		PropertiesLoader.loadPropertyFile();
		try {
			if (args[0] != null) {
				NodeImpl node = new NodeImpl(URL2);
				Endpoint ep = Endpoint.createEndpoint(node, node.nodeURL);
				ep.listen();
				ep.acceptEntries();
			}
		} catch (Exception e) {
			// TODO: handle exception

			NodeImpl node = new NodeImpl(URL1);
			Endpoint ep = Endpoint.createEndpoint(node, node.nodeURL);
			ep.listen();
			ep.acceptEntries();

			Node proxy = Proxy.createConnection(new URL(URL1), new URL(URL2));

			List<Long> millis = new LinkedList<Long>();

			long start = System.currentTimeMillis();
			proxy.findSuccessor(node.nodeID);
			long end = System.currentTimeMillis();
			System.out.println("findSuccessor took " + (end - start) + "ms");
			millis.add((end - start));

			start = System.currentTimeMillis();
			proxy.getNodeID();
			end = System.currentTimeMillis();
			System.out.println("getNodeID took " + (end - start) + "ms");
			millis.add((end - start));

			start = System.currentTimeMillis();
			proxy.insertEntry(new Entry(node.nodeID, "test"));
			end = System.currentTimeMillis();
			System.out.println("insertEntry took " + (end - start) + "ms");
			millis.add((end - start));

			start = System.currentTimeMillis();
			proxy.insertReplicas(new HashSet<Entry>());
			end = System.currentTimeMillis();
			System.out.println("insertReplicas took " + (end - start) + "ms");
			millis.add((end - start));

			start = System.currentTimeMillis();
			proxy.removeEntry(new Entry(node.nodeID, "test"));
			end = System.currentTimeMillis();
			System.out.println("removeEntry took " + (end - start) + "ms");
			millis.add((end - start));

			start = System.currentTimeMillis();
			proxy.leavesNetwork(node);
			end = System.currentTimeMillis();
			System.out.println("leavesNetwork took " + (end - start) + "ms");
			millis.add((end - start));

			start = System.currentTimeMillis();
			proxy.removeReplicas(node.nodeID, new HashSet<Entry>());
			end = System.currentTimeMillis();
			System.out.println("removeReplicas took " + (end - start) + "ms");
			millis.add((end - start));

			start = System.currentTimeMillis();
			proxy.notify(node);
			end = System.currentTimeMillis();
			System.out.println("notify took " + (end - start) + "ms");
			millis.add((end - start));

			proxy.notifyAndCopyEntries(node);
			end = System.currentTimeMillis();
			System.out.println("notifyAndCopyEntries took " + (end - start)
					+ "ms");
			millis.add((end - start));

			start = System.currentTimeMillis();
			proxy.retrieveEntries(node.nodeID);
			end = System.currentTimeMillis();
			System.out.println("retrieveEntries took " + (end - start) + "ms");
			millis.add((end - start));

			start = System.currentTimeMillis();
			proxy.ping();
			end = System.currentTimeMillis();
			System.out.println("ping took " + (end - start) + "ms");
			millis.add((end - start));

			long calls = 0;
			long total = 0;
			for (Long time : millis) {
				total += time;
				calls++;
			}
			System.out
					.println("Average duration of a call: " + (total / calls));

			proxy.disconnect();

			ep.disconnect();
		}
	}

	private static class NodeImpl extends Node {

		NodeImpl(String url) {
			try {
				this.nodeURL = new URL(url);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
			this.nodeID = new ID(this.nodeURL.toString().getBytes());
		}

		@Override
		public void disconnect() {
			// TODO Auto-generated method stub

		}

		@Override
		public Node findSuccessor(ID key) throws CommunicationException {
			return this;
		}

		@Override
		public void insertEntry(Entry entryToInsert)
				throws CommunicationException {
			// TODO Auto-generated method stub

		}

		@Override
		public void insertReplicas(Set<Entry> entries)
				throws CommunicationException {
			// TODO Auto-generated method stub

		}

		@Override
		public void leavesNetwork(Node predecessor)
				throws CommunicationException {
			// TODO Auto-generated method stub

		}

		@Override
		public List<Node> notify(Node potentialPredecessor)
				throws CommunicationException {
			return new LinkedList<Node>();
		}

		@Override
		public RefsAndEntries notifyAndCopyEntries(Node potentialPredecessor)
				throws CommunicationException {
			return new RefsAndEntries(new LinkedList<Node>(),
					new HashSet<Entry>());
		}

		@Override
		public void ping() throws CommunicationException {
			// TODO Auto-generated method stub

		}

		@Override
		public void removeEntry(Entry entryToRemove)
				throws CommunicationException {
			// TODO Auto-generated method stub

		}

		@Override
		public void removeReplicas(ID sendingNode, Set<Entry> replicasToRemove)
				throws CommunicationException {
			// TODO Auto-generated method stub

		}

		@Override
		public Set<Entry> retrieveEntries(ID id) throws CommunicationException {
			return new HashSet<Entry>();
		}

	}
}
