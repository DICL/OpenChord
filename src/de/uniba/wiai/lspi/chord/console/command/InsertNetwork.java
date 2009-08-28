/***************************************************************************
 *                                                                         *
 *                            InsertNetwork.java                           *
 *                            -------------------                          *
 *   date                 : 15.09.2004                                     *
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

import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.console.command.entry.Value;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.util.console.Command;
import de.uniba.wiai.lspi.util.console.ConsoleException;



import java.io.PrintStream;

/**
 * <p>
 * This command can be used to insert a value into the remote chord network.
 * </p> 
 * 
 * To get a description of this command type <code>insertN -help</code> 
 * into the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author  sven
 * @version 1.0.5
 */
public class InsertNetwork extends Command {
    
	/**
	 * The name of this {@link Command}. 
	 */
    public static final String COMMAND_NAME = "insertN";
    
    /**
     * The name of the parameter, that defines the key of the value to insert. 
     */
    protected static final String KEY_PARAM = "key";
    
    /**
     * The name of the parameter, that defines the value to insert. 
     */
    protected static final String VALUE_PARAM = "value";
    
    /** Creates a new instance of Insert 
     * @param toCommand1 
     * @param out1 */
    public InsertNetwork(Object[] toCommand1, PrintStream out1) {
        super(toCommand1, out1);
    }
    
    public void exec() throws ConsoleException {
        String key = this.parameters.get(KEY_PARAM);
        String value = this.parameters.get(VALUE_PARAM);
        if ( (key == null) || (key.length() == 0) ){
            throw new ConsoleException("Not enough parameters! " + KEY_PARAM + " is missing.");
        }
        if ( (value == null) || (value.length() == 0) ){
            throw new ConsoleException("Not enough parameters! " + VALUE_PARAM + " is missing.");
        }
        Chord chord = ((RemoteChordNetworkAccess)this.toCommand[1]).getChordInstance(); 
        
        Key keyObject = new Key(key);
        Value valueObject = new Value(value);
        try {
            chord.insert(keyObject, valueObject);
        }
        catch (Throwable t){
            ConsoleException e 
                    = new ConsoleException("Exception during execution of command. " 
                    + t.getMessage(), t);
            throw e;
        }
    }
    
    public String getCommandName() {
        return COMMAND_NAME;
    }
    
    public void printOutHelp() {
        this.out.println("This command inserts a value with a provided key into the (remote) chord network.");
        this.out.println("The key is inserted starting from the node provided as parameter.");
        this.out.println("Required parameters: ");
        this.out.println("\t" + KEY_PARAM + ": The key for the value.");
        this.out.println("\t" + VALUE_PARAM + ": The value to insert.");
        this.out.println();
    }
    
}
