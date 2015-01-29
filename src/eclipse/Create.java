package eclipse;

import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.data.URL;

import java.net.MalformedURLException;

public class Launcher {
  public static void main (String[] args) {
    PropertiesLoader.loadPropertyFile();
    String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
    URL localURL = null;

    try {
      localURL = new URL(protocol + "://localhost:8080/");

    } catch (MalformedURLException e ) {
      throw new RuntimeException(e);
    }

    Chord chord = new ChordImpl();

    try {
      chord.create(localURL);
      
    } catch (ServiceException e) {
      throw new RuntimeException("Could not create DHT!", e);
    }
  }
} 
