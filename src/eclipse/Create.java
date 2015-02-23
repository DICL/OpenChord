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
            System.out.println ("OpenChord network created at " + localURL);

            BufferedReader br = 
                new BufferedReader (new InputStreamReader(System.in));
            String input;

            while ((input=br.readLine())!=null) { 
                String[] arrayin = input.split("[ ]");

                String command   = arrayin[0];
                String skey      = new String(arrayin[1]);
                String svalue;

                if (command.equals("insert")) {
                    //svalue       = arrayin[2];

                    System.err.println ("Inserting: " + skey + " " );
                    StringKey myKey = new StringKey (skey) ;

                    try {
                        chord.insert (myKey , skey);

                    } catch (ServiceException e) {
                        System.err.println ("insertion failed");
                    }

                } else if (command.equals("retrieve")) {
                    svalue       = arrayin[2];
                    System.out.println("Retrieve mock");

                } else if (command.equals("close")) {
                    System.out.println ("Close mock");
                }

                System.out.println ("Cmd: [" + command + "] key: [" + skey + "]"); 
            }

        } catch (MalformedURLException e ) {
            throw new RuntimeException(e);

        } catch (IOException io) {
            io.printStackTrace();

        } catch (ServiceException e) {
            throw new RuntimeException("Could not create DHT!", e);
        }
    }
} 
