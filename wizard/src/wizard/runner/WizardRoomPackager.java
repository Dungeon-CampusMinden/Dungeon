package wizard.runner;

import escaperoom.foundation.room.model.VerifiedAsset;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import wizard.runner.contract.ContractCapabilities;
import wizard.runner.validation.ValidationResult;

/** Packages a validated finalized project into the generic executable Wizard room template. */
public final class WizardRoomPackager {
  private static final String PROJECT_PREFIX = "wizard/embedded-project/";

  private WizardRoomPackager() {}

  /**
   * CLI used by Gradle: {@code template project output}.
   *
   * @param arguments template, project, and output paths
   */
  public static void main(final String[] arguments) {
    if (arguments.length != 3) {
      throw new IllegalArgumentException("Expected template, project, and output paths");
    }
    Path project = Path.of(arguments[1]);
    ProjectValidationService.Outcome outcome = new ProjectValidationService().validate(project);
    System.out.println(outcome.report().canonicalJson());
    if (!outcome.report().valid()) {
      System.exit(1);
    }
    packageValidatedProject(
        Path.of(arguments[0]), project, Path.of(arguments[2]), outcome.validation().orElseThrow());
  }

  /**
   * Atomically creates or replaces one player JAR without changing the source project.
   *
   * @param template generic project-free player JAR
   * @param project finalized project directory
   * @param destination output player JAR
   */
  public static void packageProject(
      final Path template, final Path project, final Path destination) {
    packageProject(template, project, destination, null);
  }

  /**
   * Atomically packages a project only when its validated {@code deer.json} matches the expected
   * finalized bytes.
   *
   * @param template generic project-free player JAR
   * @param project finalized project directory
   * @param destination output player JAR
   * @param expectedDeerSha256 host-confirmed hash stored with the draft finalization
   */
  public static void packageProject(
      final Path template,
      final Path project,
      final Path destination,
      final String expectedDeerSha256) {
    ProjectValidationService.Outcome outcome = new ProjectValidationService().validate(project);
    if (!outcome.report().valid()) {
      throw new IllegalArgumentException(outcome.report().canonicalJson());
    }
    ValidationResult validation = outcome.validation().orElseThrow();
    if (expectedDeerSha256 != null
        && !validation.rawDeerSha256().filter(expectedDeerSha256::equals).isPresent()) {
      throw new IllegalStateException(
          "Finalized deer.json no longer matches this draft's saved finalization");
    }
    packageValidatedProject(template, project, destination, validation);
  }

  /**
   * Packages a project from the exact closed input returned by production validation.
   *
   * @param template generic project-free player JAR
   * @param project finalized project directory
   * @param destination output player JAR
   * @param validation exact successful production validation input
   */
  public static void packageValidatedProject(
      final Path template,
      final Path project,
      final Path destination,
      final ValidationResult validation) {
    byte[] deer = readValidatedDeer(project, validation);
    Path output = destination.toAbsolutePath().normalize();
    try {
      Path parent = output.getParent();
      if (parent == null) {
        throw new IllegalArgumentException("Output JAR must have a parent directory");
      }
      Files.createDirectories(parent);
      Path temporary = Files.createTempFile(parent, output.getFileName().toString(), ".tmp");
      try {
        writeJar(template.toAbsolutePath().normalize(), validation, deer, temporary);
        atomicReplace(temporary, output);
      } finally {
        Files.deleteIfExists(temporary);
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Wizard room JAR could not be packaged", exception);
    }
  }

  private static void writeJar(
      final Path template, final ValidationResult validation, final byte[] deer, final Path output)
      throws IOException {
    Set<String> entries = new HashSet<>();
    try (InputStream fileInput = Files.newInputStream(template);
        ZipInputStream input = new ZipInputStream(new BufferedInputStream(fileInput));
        OutputStream fileOutput = Files.newOutputStream(output);
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(fileOutput))) {
      ZipEntry entry;
      while ((entry = input.getNextEntry()) != null) {
        if (entry.isDirectory() || entry.getName().startsWith(PROJECT_PREFIX)) {
          continue;
        }
        put(zip, entries, entry.getName(), input.readAllBytes());
      }

      List<VerifiedAsset> assets =
          validation.assets().stream()
              .sorted(Comparator.comparing(VerifiedAsset::logicalPath))
              .toList();
      List<String> projectFiles = new ArrayList<>();
      projectFiles.add("deer.json");
      put(zip, entries, PROJECT_PREFIX + "deer.json", deer);
      for (VerifiedAsset asset : assets) {
        projectFiles.add(asset.logicalPath());
        put(zip, entries, PROJECT_PREFIX + asset.logicalPath(), asset.bytes());
      }
      projectFiles.sort(Comparator.naturalOrder());
      put(
          zip,
          entries,
          PROJECT_PREFIX + "files.list",
          (String.join("\n", projectFiles) + "\n").getBytes(StandardCharsets.UTF_8));
    }
  }

  private static byte[] readValidatedDeer(final Path project, final ValidationResult validation) {
    Path deer = project.toAbsolutePath().normalize().resolve("deer.json");
    try {
      BasicFileAttributes before =
          Files.readAttributes(deer, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      if (before.isSymbolicLink()
          || before.isOther()
          || !before.isRegularFile()
          || before.size() > ContractCapabilities.MAX_DEER_BYTES) {
        throw new IllegalStateException("Validated deer.json is no longer a bounded regular file");
      }
      byte[] bytes = readBounded(deer);
      BasicFileAttributes after =
          Files.readAttributes(deer, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      if (after.isSymbolicLink()
          || after.isOther()
          || !after.isRegularFile()
          || before.size() != after.size()
          || !before.lastModifiedTime().equals(after.lastModifiedTime())
          || !java.util.Objects.equals(before.fileKey(), after.fileKey())) {
        throw new IllegalStateException("Validated deer.json changed before packaging");
      }
      String validatedHash = validation.rawDeerSha256().orElseThrow();
      if (!sha256(bytes).equals(validatedHash)) {
        throw new IllegalStateException("Validated deer.json changed before packaging");
      }
      return bytes;
    } catch (IOException | SecurityException exception) {
      throw new IllegalStateException(
          "Validated deer.json cannot be read for packaging", exception);
    }
  }

  private static byte[] readBounded(final Path deer) throws IOException {
    Set<OpenOption> options = Set.of(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
    try (SeekableByteChannel channel = Files.newByteChannel(deer, options);
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      ByteBuffer buffer = ByteBuffer.allocate(8192);
      int total = 0;
      int read;
      while ((read = channel.read(buffer)) >= 0) {
        if (read == 0) {
          continue;
        }
        total += read;
        if (total > ContractCapabilities.MAX_DEER_BYTES) {
          throw new IllegalStateException("Validated deer.json exceeds the packaging byte limit");
        }
        output.write(buffer.array(), 0, read);
        buffer.clear();
      }
      return output.toByteArray();
    }
  }

  private static String sha256(final byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  private static void put(
      final ZipOutputStream output,
      final Set<String> entries,
      final String name,
      final byte[] bytes)
      throws IOException {
    if (!entries.add(name)) {
      return;
    }
    ZipEntry entry = new ZipEntry(name);
    entry.setTime(0L);
    output.putNextEntry(entry);
    output.write(bytes);
    output.closeEntry();
  }

  private static void atomicReplace(final Path source, final Path target) throws IOException {
    Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
  }
}
