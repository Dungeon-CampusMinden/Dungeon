package core.utils.files;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public interface MySimpleFileVisitor extends FileVisitor<Path> {
  SimpleFileVisitor<Path> INSTANCE = new SimpleFileVisitor<>() {};

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
