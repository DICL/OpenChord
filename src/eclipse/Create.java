package eclipse;
// vim: set autoindent : set smartindent

import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.data.URL;

import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.*;
import org.json.*;
import java.util.*;

public class Create {
    public static void main (String[] args) {
        PropertiesLoader.loadPropertyFile();
        String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
        URL localURL = null;

        try {
            if (args.length == 0) 
                localURL = new URL(protocol + "://localhost:8080/");
            else 
                localURL = new URL(protocol + "://" + args[0] + ":8080/");

            Chord chord = new ChordImpl();
            chord.create (localURL);
//System.out.println ("OpenChord network created at " + localURL);

            BufferedReader br = 
                new BufferedReader (new InputStreamReader(System.in));

            while (true) { 
                String input = br.readLine();
                if (input == null) continue;
                JSONObject obj = new JSONObject(input);

                String command   = obj.getString("command");
                String skey      = obj.getString("key");
                String svalue    = obj.getString("value");

                System.err.println("Cmd: " + command +" key: " + skey + " value " + svalue);

                if (command.equals("exit")) {
                  break;
                }

                if (command.equals("insert")) {
                    StringKey myKey = new StringKey (skey) ;

                    try {
                        chord.insert (myKey , svalue);

                    } catch (ServiceException e) {
                        System.err.println ("insertion failed");
                    }

                } else if (command.equals("retrieve")) {
                    StringKey myKey = new StringKey (skey) ;
                    try {
                      Set<Serializable> ss = chord.retrieve (myKey);
                      for (Serializable aux : ss) {
System.err.println ("{\"key\":\""+ myKey + "\",\"data\":\"" + aux + "\"}");
                        System.out.println ("{\"key\":\""+ myKey + "\",\"data\":\"" + aux + "\"}");
  //                      System.err.println ("done");
                      }

                    } catch (ServiceException e) {
                        System.err.println ("insertion failed");
                    }
                } else if (command.equals("close")) {
                    System.err.println ("Close mock");
                }
            }
            //System.err.println ("Bye dude!");

        } catch (MalformedURLException e ) {
            throw new RuntimeException(e);

        } catch (IOException io) {
            io.printStackTrace();

        } catch (ServiceException e) {
            throw new RuntimeException("Could not create DHT!", e);
        }

    }
} 
