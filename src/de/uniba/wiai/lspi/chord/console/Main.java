/***************************************************************************
 *                                                                         *
 *                                 Main.java                               *
 *                            -------------------                          *
 *   date                 : 09.09.2004, 15:58							   *
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

package de.uniba.wiai.lspi.chord.console;

import java.util.Hashtable;

import de.uniba.wiai.lspi.util.console.CommandFactory;
import de.uniba.wiai.lspi.util.console.ConsoleThread;
import de.uniba.wiai.lspi.util.console.ExecuteMacro;
import de.uniba.wiai.lspi.util.console.MemoryOutputStream;
import de.uniba.wiai.lspi.util.console.ShowOutputCommand;

import de.uniba.wiai.lspi.chord.com.local.Registry;
import de.uniba.wiai.lspi.chord.console.command.*;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;

/**
 * Main class to start a console, that allows manual testing of chord. There are
 * two possibilities to create a chord network.
 * <ul>
 * <li> Create a chord network that completely runs within the local VM. The
 * console provides commmands to access all nodes and to retrieve/insert entries
 * from/to nodes.
 * <li> Create a single chord node, that connects to a remote chord network
 * {@link Chord#join(URL, URL)} or creates a new chord network ({@link Chord#create(URL)}),
 * which can be entered by other remote nodes.
 * </ul>
 * 
 * @author sven
 * @version 1.0.5
 */
public class Main {

	/** Creates a new instance of Console */
	private Main() {
		/*
		 * No instances of Main allowed.
		 */
	}

	/**
	 * @param args
	 *            the command line arguments
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {

		// read property file by invoking static method on ChordImpl
		PropertiesLoader.loadPropertyFile();

		System.out
				.println("This program is free software; you can redistribute "
						+ "\n"
						+ "it and/or modify it under the terms of the GNU General "
						+ "\n"
						+ "Public License as published by the Free Software "
						+ "\n"
						+ "Foundation; either version 2 of the License, or (at "
						+ "\n" + "your option) any later version.");
		System.out.println();
		System.out
				.println("A copy of the license can be found in the license.txt "
						+ "\n"
						+ "file supplied with this software or at: "
						+ "\n" + "http://www.gnu.org/copyleft/gpl.html");

		System.out.println();
		/*
		 * Create mapping from command name to command class
		 */
		Hashtable<String, String> commandMapping = new Hashtable<String, String>();
		commandMapping.put(ExecuteMacro.COMMAND_NAME, ExecuteMacro.class
				.getName());
		commandMapping.put(Exit.COMMAND_NAME, Exit.class.getName());
		commandMapping.put(Help.COMMAND_NAME, Help.class.getName());
		commandMapping.put(ShowOutputCommand.COMMAND_NAME,
				ShowOutputCommand.class.getName());
		commandMapping.put(Wait.COMMAND_NAME, Wait.class.getName());

		/*
		 * Commands to create a local chord network
		 */
		commandMapping.put(CrashNodes.COMMAND_NAME, CrashNodes.class.getName());
		commandMapping.put(CreateNodes.COMMAND_NAME, CreateNodes.class
				.getName());

		commandMapping.put(Insert.COMMAND_NAME, Insert.class.getName());
		commandMapping.put(Remove.COMMAND_NAME, Remove.class.getName());
		commandMapping.put(Retrieve.COMMAND_NAME, Retrieve.class.getName());
		commandMapping.put(ShowFingerTable.COMMAND_NAME, ShowFingerTable.class
				.getName());
		commandMapping.put(ShowNodes.COMMAND_NAME, ShowNodes.class.getName());
		commandMapping.put(ShowSuccessorList.COMMAND_NAME,
				ShowSuccessorList.class.getName());
		commandMapping.put(ShutdownNodes.COMMAND_NAME, ShutdownNodes.class
				.getName());

		commandMapping.put(ShowEntries.COMMAND_NAME, ShowEntries.class
				.getName());

		/*
		 * Commands to create a node that connects to a remote chord network.
		 */
		commandMapping.put(JoinNetwork.COMMAND_NAME, JoinNetwork.class
				.getName());
		commandMapping.put(LeaveNetwork.COMMAND_NAME, LeaveNetwork.class
				.getName());
		commandMapping.put(InsertNetwork.COMMAND_NAME, InsertNetwork.class
				.getName());
		commandMapping.put(RetrieveNetwork.COMMAND_NAME, RetrieveNetwork.class
				.getName());
		commandMapping.put(RemoveNetwork.COMMAND_NAME, RemoveNetwork.class
				.getName());
		commandMapping.put(ShowEntriesNetwork.COMMAND_NAME,
				ShowEntriesNetwork.class.getName());
		commandMapping.put(ShowFingerTableNetwork.COMMAND_NAME,
				ShowFingerTableNetwork.class.getName());
		commandMapping.put(ChangeProtocol.COMMAND_NAME, ChangeProtocol.class
				.getName());

		/*
		 * Get the registry for thread communication as this is the object
		 * commands need to be executed
		 */
		Object toCommand = Registry.getRegistryInstance();

		CommandFactory factory = new CommandFactory(new Object[] { toCommand,
				RemoteChordNetworkAccess.getUniqueInstance() }, System.out,	commandMapping);
		ConsoleThread t = ConsoleThread.getConsole("oc", factory,new MemoryOutputStream());
		t.setWelcomeText("Welcome to Open Chord test environment." + "\n"
				+ "(C) 2004-2008 Distributed and Mobile Systems Group" + "\n"
				+ "University of Bamberg" + "\n" + "\n"
				+ "Type 'help' for a list of available commands");
		t.setExitCommand(Exit.COMMAND_NAME);
		t.start();

		// execute commands from args list
		for (int i = 0; i < args.length; i++) {
			factory.createCommand(args[i]).exec();
		}
	}
}
