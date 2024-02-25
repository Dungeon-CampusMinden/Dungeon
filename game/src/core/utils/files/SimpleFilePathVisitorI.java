package core.utils.files;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Class that extends {@link FileVisitor} and implements {@link SimpleFileVisitor} for use in {@link
 * core.utils.files.FileSystemUtil}. You only have to implement {@link #visitFile(Path,
 * BasicFileAttributes)}
 */
public interface SimpleFilePathVisitorI extends FileVisitor<Path> {
  SimpleFileVisitor<Path> INSTANCE = new SimpleFileVisitor<>() {};

  /**
   * Decide what to do with a file found in the file system.
   *
   * @param file a reference to the file
   * @param attrs the file's basic attributes
   * @return see {@link FileVisitor#visitFile(Object, BasicFileAttributes)}
   * @throws IOException see {@link FileVisitor#visitFile(Object, BasicFileAttributes)}
   */
  @Override
  FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException;

  @Override
  default FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
      throws IOException {
    return INSTANCE.preVisitDirectory(dir, attrs);
  }

  @Override
  default FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    return INSTANCE.visitFileFailed(file, exc);
  }

  @Override
  default FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    return INSTANCE.postVisitDirectory(dir, exc);
  }
}
