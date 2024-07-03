package de.fwatermann.dungine.resource;

import de.fwatermann.dungine.utils.Disposable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class Resource implements Disposable {

  private static final Map<String, Resource> cache = new HashMap<>();

  public static Resource load(String path) {
    return cache.computeIfAbsent(
        path,
        k -> {
          FileSystem fs = FileSystems.getDefault();
          Path p = fs.getPath(k);
          if (Files.exists(p)) {
            return new FileResource(p);
          } else {
            return new ClasspathResource(k);
          }
        });
  }

  public abstract ByteBuffer readBytes() throws IOException;

  public abstract long size() throws IOException;
}
