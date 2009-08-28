/***************************************************************************
 *                                                                         *
 *                                Exit.java                                *
 *                            -------------------                          *
 *   date                 : 09.09.2004, 16:33                              *
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

import java.io.PrintStream;

import de.uniba.wiai.lspi.chord.com.local.Registry;
import de.uniba.wiai.lspi.util.console.Command;

/**
 * The command to close the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author sven
 * @version 1.0.5
 */
public class Exit extends Command {

	/**
	 * The name of this command, that can be typed into the console. 
	 */
	public static final String COMMAND_NAME = "exit";

	/** Creates a new instance of Exit 
	 * @param toCommand1 
	 * @param out1 */
	public Exit(Object[] toCommand1, PrintStream out1) {
		super(toCommand1, out1);
	}

	public void exec() {
		try {
			((Registry)this.toCommand[0]).shutdown();
		} catch (Exception e) {
			// do nothing
		}
		try {
			((RemoteChordNetworkAccess) this.toCommand[1]).getChordInstance()
					.leave();
		} catch (Exception e) {
			// do nothing
		}
		this.out.println("Bye, bye!");
		// System.exit(0);
	}

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void printOutHelp() {
		// do nothing
	}

}
