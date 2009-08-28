/***************************************************************************
 *                                                                         *
 *                             LeaveNetwork.java                           *
 *                            -------------------                          *
 *   date                 : 09.09.2004                                     *
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

package de.uniba.wiai.lspi.chord.console.command;

import de.uniba.wiai.lspi.util.console.Command;
import de.uniba.wiai.lspi.util.console.ConsoleException;
import java.io.PrintStream;

/**
 * {@link Command} to leave the remote chord network. 
 * 
 * To get a description of this command type <code>leaveN -help</code> 
 * into the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author  sven
 * @version 1.0.5
 */
public class LeaveNetwork extends Command {
    
	/**
	 * The name of this {@link Command}. 
	 */
    public static final String COMMAND_NAME = "leaveN";
    
    /** Creates a new instance of Exit 
     * @param toCommand1 
     * @param out1 */
    public LeaveNetwork(Object[] toCommand1, PrintStream out1) {
        super(toCommand1, out1);
    }
    
    public void exec() throws ConsoleException {
        this.out.println("Leaving network.");
        try {
            ((RemoteChordNetworkAccess)this.toCommand[1]).leave();
        } catch (Exception e) {
            throw new ConsoleException("Leave failed! Reason: " 
                    + e.getMessage(), e); 
        }
    }
    
    public String getCommandName() {
        return COMMAND_NAME;
    }
    
    public void printOutHelp() {
    	this.out.println("Causes the node that is connected to a remote network \n " +
    			"to leave the network and notify the affected nodes.");
    }
    
}
