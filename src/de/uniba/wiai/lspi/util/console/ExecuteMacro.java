/***************************************************************************
 *                                                                         *
 *                             ExecuteMacro.java                           *
 *                            -------------------                          *
 *   date                 : 10. September 2004, 12:29                      *
 *   copyright            : (C) 2004 Distributed and Mobile Systems Group  *
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
package de.uniba.wiai.lspi.util.console;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * This is an implementation of {@link Command} that allows the execution of
 * commands stored in a <code>.txt</code> file. Every line of the file must be
 * a valid command for the console, from that this command is executed. The file
 * must not contain empty lines.
 * 
 * The file is provided with help of the <code>file</code> parameter. To make
 * sure the file is found the complete path has to be provided. On Windows
 * systems the \ must be replaced by /.
 * 
 * @author sven, karsten
 * @version 1.0.5
 */
public class ExecuteMacro extends Command {

	/**
	 * The name of this command.
	 */
	public static final String COMMAND_NAME = "executeMacro";

	/**
	 * The name of the parameter for the file name.
	 */
	public static final String FILE_PARAM = "file";

	/**
	 * The number of times that the Macro is executed.
	 */
	public static final String TIMES_PARAM = "times";

	/** Creates a new instance of ExecuteMacro 
	 * @param toCommand 
	 * @param out */
	public ExecuteMacro(Object[] toCommand, PrintStream out) {
		super(toCommand, out);
	}

	public void printOutHelp() {
		this.out.println("This command executes commands saved in a txt file.");
		this.out.println("Each command must be placed in a separate line.");
		this.out.println("There must be no empty line within the file.");
		this.out.println("The file name has to be provided as value of parameter '"
				+ FILE_PARAM + "'.");
		this.out.println("The path to the file can be specified relative to the "
				+ "directory from where this console has been started or as "
				+ "an absoulte path. As path separator '/' has to be used.");
	}

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void exec() throws ConsoleException {
		String filename = this.parameters.get(FILE_PARAM);
		if ((filename == null) || (filename.length() == 0)) {
			throw new ConsoleException(FILE_PARAM + " parameter is missing!");
		}
		String timesString = this.parameters.get(TIMES_PARAM);
		int times = 1;
		if (timesString != null && timesString.length() != 0) {
			try {
				times = Integer.parseInt(timesString);
			} catch (NumberFormatException e) {
				throw new ConsoleException(TIMES_PARAM + " is not a valid number!");
			}
		}
		if (times < 1) {
			times = 1;
		}
		this.out.println("Trying to open macro file '" + filename + "'.");
		int linesRead = 0;
		try {
			this.out.println("Executing macro " + times + " time(s).");
			for (int i = 0; i < times; i++) {
				this.out.println(i);
				FileReader fileInput = new FileReader(filename);
				BufferedReader reader = new BufferedReader(fileInput);
				String line = reader.readLine();
				linesRead++;
				while (line != null) {
					CommandFactory factory = ConsoleThread.getConsole()
							.getCommandFactory();
					this.out.println("MACRO EXECUTION: Line read from file: '"
							+ line + "'.");
					Command cmd = factory.createCommand(line);
					this.out.println("MACRO EXECUTION: Executing command "
							+ cmd.getCommandName());
					cmd.execute();
					line = reader.readLine();
					linesRead++;
				}
				linesRead = 0;
				reader.close();
				fileInput.close();
			}

		} catch (IOException e) {
			throw new ConsoleException(
					"IO error while reading macro file. Lines "
							+ "read successfully: " + linesRead + ". In round "
							+ times + ".Message: " + e.getMessage());
		}
	}

}
