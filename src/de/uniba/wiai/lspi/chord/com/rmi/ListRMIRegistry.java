/***************************************************************************
 *                                                                         *
 *                             ListRMIRegistry.java                        *
 *                            -------------------                          *
 *   date                 : 04.03.2008, 15:20:11                           *
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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ListRMIRegistry {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		if (System.getSecurityManager() == null) {
//			System.setSecurityManager(new SecurityManager()); 
//		}
		String host = args[0];
		int port = 0;
		try {
			port = Integer.parseInt(args[1]);
		} catch (Exception e) {

		}
		Registry r = null;
		try {
			if (port != 0) {
				r = LocateRegistry.getRegistry(host, port);
			} else {
				r = LocateRegistry.getRegistry(host);
			}
			String[] names = r.list();
			System.out.println("Registered services in RMIRegistry at " + host + ":" + port); 
			for (String name : names) {
				System.out.print("- " + name + ", Stub: "); 
				try {
					Remote stub = r.lookup(name);
					System.out.println(stub.toString()); 
				} catch (Exception e) {
					System.out.println("UNKNOWN");
				} 
			}
		} catch (RemoteException e) {
			System.err.println("Could not connect to RMIRegistry at " + host
					+ " on port " + port);
			System.err.println(e.getMessage());
			e.printStackTrace(); 
			System.exit(-1);
		}

	}
}
