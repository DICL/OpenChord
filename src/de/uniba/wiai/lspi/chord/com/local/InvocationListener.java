/***************************************************************************
 *                                                                         *
 *                          InvocationListener.java                        *
 *                            -------------------                          *
 *   date                 : 06.10.2004, 13:42                              *
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

/**
 * This interface has to be implemented by classes that want to be notified 
 * about invocations made to a {@link ThreadEndpoint}. 
 *
 * @author  sven
 * @version 1.0.5 
 */
public interface InvocationListener {
    
    /**
     * 
     */
    public final static int FIND_SUCCESSOR = 0;
    
    /**
     * 
     */
    public final static int INSERT_ENTRY = 1;
    
    /**
     * 
     */
    public final static int INSERT_REPLICAS = 2; 
    
    /**
     * 
     */
    public final static int LEAVES_NETWORK = 3; 
    
    /**
     * 
     */
    public final static int NOTIFY = 4;
    
    /**
     * 
     */
    public final static int NOTIFY_AND_COPY = 5; 
    
    /**
     * 
     */
    public final static int PING = 6; 
    
    /**
     * 
     */
    public final static int REMOVE_ENTRY = 7;
    
    /**
     * 
     */
    public final static int REMOVE_REPLICAS = 8; 
    
    /**
     * 
     */
    public final static int RETRIEVE_ENTRIES = 9;
    
//    public final static String[] METHOD_NAMES = new String[] {
//                "findSuccessor", 
//                "insertEntry", 
//                "insertReplicas", 
//                "leavesNetwork", 
//                "notify", 
//                "notifyAndCopyEntries", 
//                "ping", 
//                "removeEntry", 
//                "removeReplicas",
//                "retrieveEntries"
//    };
    
    /**
     * @param method
     */
    public void notifyInvocationOf(int method);
    
    /**
     * @param method
     */
    public void notifyInvocationOfFinished(int method);
    
}
