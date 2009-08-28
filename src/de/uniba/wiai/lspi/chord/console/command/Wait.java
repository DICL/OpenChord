/***************************************************************************
 *                                                                         *
 *                                Wait.java                                *
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

import java.io.PrintStream;

import de.uniba.wiai.lspi.util.console.Command;
import de.uniba.wiai.lspi.util.console.ConsoleException;

/**
 * <p>
 * {@link Command} to block the console for a provided time. 
 * </p>
 * To get a description of this command type <code>wait -help</code> 
 * into the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author  sven
 * @version 1.0.5
 */
public class Wait extends Command {
    
	/**
	 * The name of this {@link Command}. 
	 */
    public static final String COMMAND_NAME = "wait";
    
    /**
     * The parameter, that defines the time to wait in milliseconds. 
     */
    public static final String MILLIS_PARAM = "millis";
    
    /** Creates a new instance of Wait 
     * @param toCommand1 
     * @param out1 */
    public Wait(Object[] toCommand1, PrintStream out1) {
        super(toCommand1, out1);
    }
    
    public void exec() throws ConsoleException {
        if (!this.parameters.containsKey(MILLIS_PARAM)){
            throw new ConsoleException("Not enough parameters. Provide the "
                    + "wait time in millis using the parameter '" + MILLIS_PARAM + "'.");
        }
        String millisString = this.parameters.get(MILLIS_PARAM);
        
        try {
            long waitTime = Long.parseLong(millisString);
            
            Thread.sleep(waitTime);
            
        } catch (NumberFormatException e) {
            throw new ConsoleException("Parameter '" + MILLIS_PARAM + "' has "
                    + "wrong format.");
        } catch (InterruptedException e) {
            /*
             * nothing to do here. 
             */
        }
        
        
    }
    
    public String getCommandName() {
        return COMMAND_NAME;
    }
    
    public void printOutHelp() {
        this.out.println("The " + COMMAND_NAME + " command makes the console wait "
                   + "a given time until the next step is done. This is meant "
                   + "to be used in macros.");
        this.out.println("Parameters: ");
        this.out.println(MILLIS_PARAM + " takes the number of millis to wait for.");
    }
    
}
