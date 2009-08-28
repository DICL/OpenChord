/***************************************************************************
 *                                                                         *
 *                             CreateNodes.java                            *
 *                            -------------------                          *
 *   date                 : 09.09.2004, 11:16                              *
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
/*
 * CreateNodes.java
 *
 * Created on 9. September 2004, 11:16
 */

package de.uniba.wiai.lspi.chord.console.command;

import java.util.List;
import java.io.PrintStream;

import de.uniba.wiai.lspi.util.console.ConsoleException;
import de.uniba.wiai.lspi.chord.com.local.Registry;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
//import de.uniba.wiai.lspi.chord.service.impl.TaskExecutor;

/**
 * Command to create a number of nodes. 
 * To get a description of this command type <code>create -help</code> 
 * into the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author  sven
 * @version 1.0.5
 */
public class CreateNodes extends de.uniba.wiai.lspi.util.console.Command {
    
	/**
	 * Name of this commmand. 
	 */
    public static final String COMMAND_NAME = "create";
    
    /**
     * Name of parameter that defines the names of the nodes to create. 
     */
    public static final String NAMES_PARAM = "names";
    
    /**
     * Names of the bootstrap nodes to use. 
     */
    public static final String BOOTSTRAP_PARAM = "bootstraps";
    
    
//    public static final String EXECUTOR_PARAM = "executor";
    
    /** Creates a new instance of CreateNodes 
     * @param toCommand1 
     * @param out1 */
    public CreateNodes(Object[] toCommand1, PrintStream out1) {
        super(toCommand1, out1);
    }
    
    public void exec() throws ConsoleException {
        if (!this.parameters.containsKey(NAMES_PARAM)){
            throw new ConsoleException("Not enough parameters. Provide at "
                    + "least one node name with help of '" + NAMES_PARAM
                    + "' parameter.");
        }
        String namesString = this.parameters.get(NAMES_PARAM);
        
        ListParameter namesParam = new ListParameter(NAMES_PARAM, namesString, false);
        List<String> names = namesParam.getList();
        
        String bootstrapsString = this.parameters.get(BOOTSTRAP_PARAM);
        ListParameter bootsNames = new ListParameter(BOOTSTRAP_PARAM, bootstrapsString, true);
        List<String> bootstraps = bootsNames.getList();
        
//        String executorClass = this.parameters.get(EXECUTOR_PARAM);
//        TaskExecutor executor = null; 
//        if (executorClass != null && executorClass.length() > 0) {
//            this.out.println("Trying to use executor " + executorClass);
//            try {
//                Class klass = Class.forName(executorClass);
//                executor =(TaskExecutor)klass.newInstance();
//            } catch (Exception e) {
//                this.out.println("Could not create executor " + executorClass);
//                throw new ConsoleException("", e);
//            }
//        }
        
        
        if ( (names.size() > 1) && (bootstraps.size() == 0) ){
            throw new ConsoleException("Cannot start more than one node without at least "
                    + "one bootstrap node.");
        }
        if ( (names.size() == 1) && (bootstraps.size() == 0) ){
            this.out.println("Creating new chord network.");
            /* test if there is already a chord network */
            Registry reg = Registry.getRegistryInstance();
            if (reg.lookupAll().size() > 0){
                throw new ConsoleException("There is already a chord network present in "
                        + "this JVM. Try to start node " + names.get(0)
                        + " with one of the existing nodes as bootstrap node.");
            }
            
            Chord node = null; 
//            if (executor != null) {
//                node = ChordImpl.createChordNode(executor);
//            } else {
                node = new ChordImpl();
//            }
            try {
                URL url = new URL(URL.KNOWN_PROTOCOLS.get(URL.LOCAL_PROTOCOL) + "://" + names.get(0) + "/");
                node.create(url);
            } catch (Throwable t){
                t.printStackTrace(this.out);
                throw new ConsoleException("Exception during creation of node " + names.get(0)
                + ". Message : " + t.getMessage(), t);
            }
        } else {
            for (int i = 0; i < names.size(); i++){
                this.out.print("Starting node with name '" + names.get(i) + "'");
                String nodeToCreate = names.get(i);
                String bootstrap = "";
                if (i < bootstraps.size()){
                    bootstrap = bootstraps.get(i);
                } else if (bootstraps.size() >= 1){
                    bootstrap = bootstraps.get(bootstraps.size()-1);
                }
                this.out.println(" with bootstrap node '" + bootstrap +"'");
                
                /* test if there is already a chord network */
                Registry reg = Registry.getRegistryInstance();
                URL url = null; 
                try {
                	url = new URL(URL.KNOWN_PROTOCOLS.get(URL.LOCAL_PROTOCOL) + "://" + nodeToCreate + "/");
                } catch (Exception e) {
                	throw new ConsoleException(e.getMessage()); 
                }
                if (reg.lookup(url) != null){
                    this.out.println("There is already a node in chord network present with name " + nodeToCreate);
                    this.out.println("Ignoring node. Node " + nodeToCreate + " NOT started!");
                } else {
                    Chord node = null;
//                    if (executor != null) {
//                        node = ChordImpl.createChordNode(executor);
//                    } else {
                        node = new ChordImpl();
//					 }
                    try {
                     
                        URL bootstrapURL = new URL(URL.KNOWN_PROTOCOLS.get(URL.LOCAL_PROTOCOL) + "://" + bootstrap + "/");
                        node.join(url, bootstrapURL);
                    } catch (Throwable t){
                    	t.printStackTrace(); 
                    	try {
                    		node.leave();
                    	} catch (ServiceException e) {
                    		/*
                    		 * does not matter. 
                    		 */
                    	}
                        throw new ConsoleException("Exception during join of node " + nodeToCreate
                                + ". Message : " + t.getMessage(), t);
                    }
                }
            }
        }
    }
    
    public String getCommandName() {
        return COMMAND_NAME;
    }
    
    public void printOutHelp() {
        this.out.println("The " + COMMAND_NAME + " command creates one or more \n"
                + "nodes of a chord network. The first node created must be \n"
                + "the only node provides with help of parameter '"
                + NAMES_PARAM + "'.");
        this.out.println("___________________"); 
        this.out.println("Parameters: ");
        this.out.println("'" + NAMES_PARAM + "' takes a list of names of nodes. The names \n"
                + "must be separated with help of '_' and without a space. ");
        this.out.println("'" + BOOTSTRAP_PARAM + "' takes a list of names of nodes. The names \n"
                + "must be separated with help of '_' and without a space. ");
        this.out.println("There must always be a bootstrap node except in the case \n"
                + "that the first node of a chord network is created.");
    }
    
}
