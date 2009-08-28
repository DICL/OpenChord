/***************************************************************************
 *                                                                         *
 *                        ShowFingerTableNetwork.java                      *
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

import de.uniba.wiai.lspi.chord.service.Report;
import de.uniba.wiai.lspi.util.console.Command;

/**
 * <p>
 * {@link Command} to show the finger table of a chord node of the local chord
 * network.
 * </p>
 * 
 * To get a description of this command type <code>finger -help</code> into
 * the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author sven
 * @version 1.0.5
 */
public class ShowFingerTableNetwork extends Command {

	/**
	 * The name of this {@link Command}.
	 */
	public static final String COMMAND_NAME = "refsN";

	/** Creates a new instance of ShowFingerTable 
	 * @param toCommand1 
	 * @param out1 */
	public ShowFingerTableNetwork(Object[] toCommand1, java.io.PrintStream out1) {
		super(toCommand1, out1);
	}
	
	/**
	 * @param node
	 */
	private void printFingerTableForEndpoint(Report node) {
    	this.out.println(node.printReferences());
	}
    
	public void exec() {
		this.printFingerTableForEndpoint((Report)((RemoteChordNetworkAccess) this.toCommand[1]).getChordInstance());
	}

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void printOutHelp() {
		this.out
				.println("This command displays the finger table of the chord node");
	}

}
