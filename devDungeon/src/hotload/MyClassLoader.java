package hotload;

import java.io.*;
import java.net.*;

public class MyClassLoader extends URLClassLoader {

  public MyClassLoader(URL[] urls) {
    super(urls, ClassLoader.getSystemClassLoader()); // System ClassLoader als Parent verwenden
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    // Wenn die Klasse nicht im aktuellen ClassLoader gefunden wird, gehe auf den Parent ClassLoader
    try {
      return findClass(name); // Versuche, sie selbst zu finden

    } catch (ClassNotFoundException e) {
      return super.loadClass(name);
    }
  }
}
