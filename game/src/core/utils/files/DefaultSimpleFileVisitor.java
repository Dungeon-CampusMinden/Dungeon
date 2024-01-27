package core.utils.files;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultSimpleFileVisitor extends SimpleFileVisitor<Path> {
  private final String ending;
  private final Map<String, List<String>> dirSubdirMap;

  public DefaultSimpleFileVisitor(
      final String ending, final Map<String, List<String>> dirSubdirMap) {
    this.ending = ending;
    this.dirSubdirMap = dirSubdirMap;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
    if (Files.isRegularFile(file) && file.toString().endsWith(ending)) {
      String parentDirName = file.getParent().getFileName().toString();
      String halfAbsPath = file.toString();
      dirSubdirMap.computeIfAbsent(parentDirName, k -> new ArrayList<>()).add(halfAbsPath);
    }
    return FileVisitResult.CONTINUE;
  }
}
