/***************************************************************************
 *                                                                         *
 *                           ShowOutputCommand.java                        *
 *                            -------------------                          *
 *   date                 : 9. September 2004, 09:04                       *
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

import java.io.PrintStream;

/**
 * @author   sven
 * @version 1.0.5
 */
public class ShowOutputCommand extends Command {
    
    /**
     * 
     */
    public static final String COMMAND_NAME = "displaySystemOut";
    
    /**
     * 
     */
    public static final String CLEAR_PARAM = "clear";
    
    /**
     * 
     */
    private MemoryOutputStream memOut;
    
    /** Creates a new instance of ShowOutputCommand 
     * @param objects 
     * @param out */
    public ShowOutputCommand(Object[] objects, PrintStream out) {
        super(objects, out);
    }
    
    public void exec() throws ConsoleException{
        /* Get refernce to memory output stream */
        if (this.memOut == null){
            try {
                ConsoleThread console = ConsoleThread.getConsole();
                this.memOut = (MemoryOutputStream)console.getSystemOutputStream();
            }
            catch (ClassCastException e){
                throw new ConsoleException("Current System.out does not print " 
                            + " to a MemoryOutputStream. " + e.getMessage());
            }
        }
        try {
            this.memOut.printOutputTo(this.out);
            if ( this.parameters.containsKey(CLEAR_PARAM) ) {
                this.memOut.clearBuffer();
            }
        }
        catch (Throwable t){
            //t.printStackTrace();
            throw new ConsoleException("Error while printing saved System.out. "
                        + t.getMessage() + " Maybe current OutputStream is no " 
                        + "MemoryOutputStream.");
        }
    }
    
    public String getCommandName() {
        return COMMAND_NAME;
    }
    
    public void printOutHelp() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("The command '");
        buffer.append(this.getCommandName());
        buffer.append("' can be used to print output, that has been ");
        buffer.append("printed to System.out, which has been redirected to ");
        buffer.append("memory with help of a MemoryOutputStream.");
        buffer.append("\n");
        buffer.append("Parameters: ");
        buffer.append("\n");
        buffer.append("-h/-help");
        buffer.append("\t");
        buffer.append("Displays this message.");
        buffer.append("\n");
        buffer.append("-clear");
        buffer.append("\t");
        buffer.append("The output buffer is cleared after output has been displayed.");
        this.out.println(buffer.toString());
    }
    
}
