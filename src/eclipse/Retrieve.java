package eclipse;

import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.data.URL;

import java.net.MalformedURLException;
import java.io.Serializable;
import java.util.Set;

public class Retrieve {
  public static void main (String[] args) {
    PropertiesLoader.loadPropertyFile();
    String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
    URL localURL = null;

    try {
      localURL = new URL(protocol + "://localhost:8080/");

    } catch (MalformedURLException e ) {
      throw new RuntimeException(e);
    }

    URL bootstrapURL = null;

    try {
      bootstrapURL = new URL (protocol + "://localhost:8080/") ;
    } catch (MalformedURLException e) {
      throw new RuntimeException (e) ;
    }

    Chord chord = new ChordImpl();

    try {
      chord.join (localURL, bootstrapURL) ;

      // Remove key
      StringKey myKey = new StringKey (args[0]);
      Set<Serializable> data = chord.retrieve (myKey);

    } catch (ServiceException e) {
      throw new RuntimeException (" Could not join DHT !", e ) ;
    }

  }
} 
