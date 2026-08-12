package escaperoom.foundation.room.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/** Immutable verified custom-asset bytes retained by each locally derived room. */
public final class VerifiedAsset {
  private final String logicalPath;
  private final String sha256;
  private final String mediaType;
  private final byte[] bytes;

  /**
   * Creates one immutable verified custom asset.
   *
   * @param logicalPath exact portable {@code assets/custom/...} path
   * @param sha256 lowercase SHA-256 of the exact bytes
   * @param mediaType verified media type
   * @param bytes exact custom-asset bytes
   */
  public VerifiedAsset(
      final String logicalPath, final String sha256, final String mediaType, final byte[] bytes) {
    this.logicalPath = RoomModelChecks.requireCustomAssetPath(logicalPath);
    this.sha256 = RoomModelChecks.requireSha256(sha256, "custom asset SHA-256");
    this.mediaType = RoomModelChecks.requireText(mediaType, "custom asset media type");
    this.bytes = Objects.requireNonNull(bytes, "bytes").clone();
    if (!this.sha256.equals(hash(this.bytes))) {
      throw new IllegalArgumentException("custom asset SHA-256 must match its exact bytes");
    }
  }

  /**
   * Returns the exact portable logical asset path.
   *
   * @return {@code assets/custom/...} path
   */
  public String logicalPath() {
    return logicalPath;
  }

  /**
   * Returns the lowercase content SHA-256.
   *
   * @return exact byte identity
   */
  public String sha256() {
    return sha256;
  }

  /**
   * Returns the verified media type retained with the local room.
   *
   * @return media type
   */
  public String mediaType() {
    return mediaType;
  }

  /**
   * Returns a defensive copy of the exact custom-asset bytes.
   *
   * @return copied bytes
   */
  public byte[] bytes() {
    return bytes.clone();
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof VerifiedAsset asset)) {
      return false;
    }
    return logicalPath.equals(asset.logicalPath)
        && sha256.equals(asset.sha256)
        && mediaType.equals(asset.mediaType)
        && Arrays.equals(bytes, asset.bytes);
  }

  @Override
  public int hashCode() {
    return 31 * Objects.hash(logicalPath, sha256, mediaType) + Arrays.hashCode(bytes);
  }

  @Override
  public String toString() {
    return "VerifiedAsset[logicalPath="
        + logicalPath
        + ", sha256="
        + sha256
        + ", mediaType="
        + mediaType
        + ", bytes="
        + bytes.length
        + "]";
  }

  private static String hash(final byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
