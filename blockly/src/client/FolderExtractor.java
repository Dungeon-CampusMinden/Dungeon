package client;

import coderunner.BlocklyCodeRunner;
import core.utils.logging.DungeonLogger;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Map;

/**
 * This class has functions for managing a temporary folder that the blockly code is saved in. In
 * addition, there are also function for creating a copy of the jar file in case the windows
 * operating system is used. Otherwise a loft of temporary folders would be created because the
 * blocklyCodeRunner and libgdx access the jar file at the same time.
 */
public class FolderExtractor {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(BlocklyCodeRunner.class);

  /**
   * The name of the temporary folder where the compiled code will be stored. This folder is created
   * in the system's temporary directory. (default: "blockly")
   */
  public static String TEMP_FOLDER_NAME = "blockly";

  /**
   * Returns the path to the BlocklyCodeRunner's temporary folder.
   *
   * <p>If no temporary folder is found it will generate a new one in the system's temporary
   * directory.
   *
   * @return Path to the temporary folder. If no folder is found or cannot be created, returns null.
   */
  public static Path tempFolder() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    System.out.println("Found temp file: " + tempDir.getAbsolutePath());
    File[] tempFiles = tempDir.listFiles();
    if (tempFiles != null) {
      for (File file : tempFiles) {

        if (file.isDirectory() && file.getName().equals(TEMP_FOLDER_NAME)) {
          return file.toPath();
        }
      }
    }

    // If no temp folder found, create a new one
    try {
      tempDir = Files.createDirectory(Paths.get(tempDir.getPath(), TEMP_FOLDER_NAME)).toFile();
    } catch (IOException e) {
      LOGGER.error("Error creating temp folder: " + e.getMessage());
      return null;
    }
    return tempDir.toPath();
  }

  /** Creates a copy of the jar file and stores it in the temp directory. */
  public static void prepareCompilerResources() throws Exception {
    // create directory
    Path tempDir = tempFolder();
    Path libFolder = Files.createDirectories(tempDir.resolve("unpacked_libs"));
    URI jarUri = FolderExtractor.class.getProtectionDomain().getCodeSource().getLocation().toURI();

    try (FileSystem zipFs = FileSystems.newFileSystem(URI.create("jar:" + jarUri), Map.of())) {
      Path root = zipFs.getPath("/");

      // go through the jar and process each file
      Files.walk(root)
          .filter(Files::isRegularFile) // only files
          .forEach(source -> copyToTemp(source, root, libFolder));
    }
  }

  /** Deletes the copy of the jar file. */
  public static void deleteCompilerResources() throws Exception {

    System.out.println("Deleting temp folder");

    Path tempDir = tempFolder();
    Path libFolder = tempDir.resolve("unpacked_libs");

    if (Files.exists(libFolder)) {
      Files.walk(libFolder)
          .sorted(Comparator.reverseOrder())
          .forEach(
              path -> {
                try {
                  Files.delete(path);
                } catch (IOException e) {
                  // Fehlerbehandlung, falls eine Datei gesperrt ist
                  System.err.println("Could not delete: " + path + " - " + e.getMessage());
                }
              });
    }
  }

  private static void copyToTemp(Path source, Path root, Path targetDir) {
    try {
      // path relativ to jar file
      Path target = targetDir.resolve(root.relativize(source).toString());

      Files.createDirectories(target.getParent());
      Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      System.err.println("Skip: " + source + " (" + e.getMessage() + ")");
    }
  }
}
