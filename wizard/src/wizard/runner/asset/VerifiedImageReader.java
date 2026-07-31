package wizard.runner.asset;

import foundation.room.model.VerifiedAsset;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import wizard.runner.contract.ContractCapabilities;

/** Shared bounded reader for one flat custom PNG or JPEG asset. */
public final class VerifiedImageReader {
  private static final String LOGICAL_PREFIX = "assets/custom/";
  private static final int BUFFER_SIZE = 8192;

  private VerifiedImageReader() {}

  /**
   * Reads, hashes, and completely decodes one custom image.
   *
   * @param assetRoot resolved non-link {@code assets/custom} directory
   * @param logicalPath portable flat path beginning with {@code assets/custom/}
   * @param mediaType expected PNG or JPEG media type
   * @param maximumBytes remaining applicable byte limit
   * @return immutable verified asset containing the exact bytes read
   */
  public static VerifiedAsset read(
      final Path assetRoot,
      final String logicalPath,
      final String mediaType,
      final long maximumBytes) {
    Path root = Objects.requireNonNull(assetRoot, "assetRoot").toAbsolutePath().normalize();
    String filename = filename(logicalPath);
    String expectedType = requireMediaType(mediaType);
    if (maximumBytes < 0) {
      throw failure(Reason.TOO_LARGE, "asset byte limit is exhausted");
    }

    Path candidate = root.resolve(filename).normalize();
    if (!candidate.getParent().equals(root)) {
      throw failure(Reason.UNSAFE_PATH, "path escapes the custom asset directory");
    }
    BasicFileAttributes attributes = attributes(candidate);
    if (attributes.isSymbolicLink()) {
      throw failure(Reason.UNSAFE_PATH, "file must be a non-link regular file");
    }
    if (attributes.isOther() || !attributes.isRegularFile() || !Files.isReadable(candidate)) {
      throw failure(Reason.MISSING, "file is missing or is not a readable regular file");
    }
    if (attributes.size() > maximumBytes) {
      throw failure(
          Reason.TOO_LARGE,
          "file exceeds the remaining byte limit: " + attributes.size() + " > " + maximumBytes);
    }

    Path realRoot = realPath(root, Reason.UNSAFE_PATH, "custom asset directory is not readable");
    Path realFile = realPath(candidate, Reason.MISSING, "file is not readable");
    if (!realFile.getParent().equals(realRoot)
        || !realFile.getFileName().toString().equals(filename)) {
      throw failure(
          Reason.UNSAFE_PATH, "filename case or Unicode normalization differs or path escapes");
    }

    byte[] bytes = readBytes(candidate, maximumBytes);
    String actualHash = sha256(bytes);
    if (!mediaTypeForFilename(filename).equals(expectedType)) {
      throw failure(Reason.TYPE_MISMATCH, "filename extension and media type differ");
    }
    decode(bytes, expectedType);
    return new VerifiedAsset(logicalPath, actualHash, expectedType, bytes);
  }

  /**
   * Returns the one portable filename represented by a logical custom-asset path.
   *
   * @param logicalPath portable flat custom-asset path
   * @return contained filename
   */
  public static String filename(final String logicalPath) {
    Objects.requireNonNull(logicalPath, "logicalPath");
    if (!logicalPath.startsWith(LOGICAL_PREFIX)
        || logicalPath.indexOf('\\') >= 0
        || logicalPath.indexOf(':') >= 0) {
      throw failure(Reason.UNSAFE_PATH, "logical path is not portable");
    }
    String filename = logicalPath.substring(LOGICAL_PREFIX.length());
    if (filename.isEmpty()
        || filename.equals(".")
        || filename.equals("..")
        || filename.indexOf('/') >= 0
        || !Normalizer.normalize(filename, Normalizer.Form.NFKC).equals(filename)) {
      throw failure(Reason.UNSAFE_PATH, "logical path must contain one normalized flat filename");
    }
    Path parsed;
    try {
      parsed = Path.of(filename);
    } catch (RuntimeException exception) {
      throw failure(Reason.UNSAFE_PATH, "logical path is not a valid filename");
    }
    if (parsed.isAbsolute()
        || parsed.getNameCount() != 1
        || !parsed.getFileName().toString().equals(filename)) {
      throw failure(Reason.UNSAFE_PATH, "logical path must contain one portable flat filename");
    }
    return filename;
  }

  /**
   * Derives the supported image media type from one portable filename.
   *
   * @param filename portable image filename
   * @return supported media type
   */
  public static String mediaTypeForFilename(final String filename) {
    String lower = Objects.requireNonNull(filename, "filename").toLowerCase(Locale.ROOT);
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    throw failure(Reason.TYPE_MISMATCH, "only PNG and JPEG assets are supported");
  }

  private static String requireMediaType(final String mediaType) {
    return switch (Objects.requireNonNull(mediaType, "mediaType")) {
      case "image/png", "image/jpeg" -> mediaType;
      default -> throw failure(Reason.TYPE_MISMATCH, "only PNG and JPEG assets are supported");
    };
  }

  private static BasicFileAttributes attributes(final Path path) {
    try {
      return Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
    } catch (IOException | SecurityException exception) {
      throw failure(Reason.MISSING, "file is missing or unreadable");
    }
  }

  private static Path realPath(final Path path, final Reason reason, final String message) {
    try {
      return path.toRealPath();
    } catch (IOException | SecurityException exception) {
      throw failure(reason, message);
    }
  }

  private static byte[] readBytes(final Path path, final long maximumBytes) {
    Set<OpenOption> options = Set.of(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
    try (SeekableByteChannel channel = Files.newByteChannel(path, options);
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
      long total = 0;
      int read;
      while ((read = channel.read(buffer)) >= 0) {
        if (read == 0) {
          continue;
        }
        total += read;
        if (total > maximumBytes) {
          throw failure(Reason.TOO_LARGE, "file exceeds the remaining byte limit while reading");
        }
        output.write(buffer.array(), 0, read);
        buffer.clear();
      }
      return output.toByteArray();
    } catch (Failure failure) {
      throw failure;
    } catch (IOException | SecurityException exception) {
      throw failure(Reason.MISSING, "file could not be read");
    }
  }

  private static void decode(final byte[] bytes, final String expectedType) {
    try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      if (input == null) {
        throw failure(Reason.TYPE_MISMATCH, "image content cannot be decoded");
      }
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) {
        throw failure(Reason.TYPE_MISMATCH, "image content cannot be decoded");
      }
      ImageReader reader = readers.next();
      try {
        reader.setInput(input, true, true);
        String decodedType =
            switch (reader.getFormatName().toLowerCase(Locale.ROOT)) {
              case "png" -> "image/png";
              case "jpeg", "jpg" -> "image/jpeg";
              default -> "";
            };
        if (!decodedType.equals(expectedType)) {
          throw failure(Reason.TYPE_MISMATCH, "image content does not match its extension");
        }
        int width = reader.getWidth(0);
        int height = reader.getHeight(0);
        if (width <= 0 || height <= 0) {
          throw failure(Reason.TYPE_MISMATCH, "image dimensions must be positive");
        }
        if (width > ContractCapabilities.MAX_IMAGE_DIMENSION
            || height > ContractCapabilities.MAX_IMAGE_DIMENSION
            || (long) width * height > ContractCapabilities.MAX_IMAGE_PIXELS) {
          throw imageCapacityFailure(width, height);
        }
        if (reader.read(0) == null) {
          throw failure(Reason.TYPE_MISMATCH, "image content cannot be decoded");
        }
      } finally {
        reader.dispose();
      }
    } catch (Failure failure) {
      throw failure;
    } catch (IOException | RuntimeException exception) {
      throw failure(Reason.TYPE_MISMATCH, "image content cannot be decoded");
    }
  }

  private static String sha256(final byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  private static Failure failure(final Reason reason, final String message) {
    return new Failure(reason, message, -1, -1);
  }

  private static Failure imageCapacityFailure(final int width, final int height) {
    return new Failure(
        Reason.IMAGE_CAPACITY, "image dimension or pixel limit exceeded", width, height);
  }

  /** Stable failure category mapped by the host and join boundaries. */
  public enum Reason {
    /** Logical or resolved path violates the flat custom-asset boundary. */
    UNSAFE_PATH,
    /** File is absent, linked, irregular, or unreadable. */
    MISSING,
    /** File exceeds the applicable byte limit. */
    TOO_LARGE,
    /** Decoded image dimensions exceed the published dimension or pixel limits. */
    IMAGE_CAPACITY,
    /** Extension, declared media type, or decoded image content differs. */
    TYPE_MISMATCH
  }

  /** Bounded shared read failure with a stable mapping category. */
  public static final class Failure extends IllegalArgumentException {
    private final Reason reason;
    private final int imageWidth;
    private final int imageHeight;

    private Failure(
        final Reason reason, final String message, final int imageWidth, final int imageHeight) {
      super(message);
      this.reason = Objects.requireNonNull(reason, "reason");
      this.imageWidth = imageWidth;
      this.imageHeight = imageHeight;
    }

    /**
     * Returns the stable failure category.
     *
     * @return failure category
     */
    public Reason reason() {
      return reason;
    }

    int imageWidth() {
      return imageWidth;
    }

    int imageHeight() {
      return imageHeight;
    }
  }
}
