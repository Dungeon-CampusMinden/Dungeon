package core.sound.parser;

import java.util.Arrays;
import java.util.Optional;

/**
 * Parser for WAV audio files that extracts the audio duration by reading the file header.
 *
 * <p>Supports standard PCM WAV files with a canonical 44-byte header layout. For other WAV variants
 * (e.g., extended headers or metadata chunks), parsing may fail.
 */
public class WavAudioParser implements IAudioParser {
  private static final String[] SUPPORTED_EXTENSIONS = {"wav"};
  private static final int MIN_HEADER_SIZE = 44;

  /**
   * Parses the duration of a WAV file from its byte array representation.
   *
   * <p>It validates key "RIFF" and "WAVE" markers, the presence of the "data" chunk, and reads the
   * byte rate and data chunk size as unsigned little-endian values. Duration is calculated as
   * (dataSize / byteRate) * 1000 to get milliseconds.
   *
   * @param fileBytes the WAV file content as a byte array
   * @return Optional containing the duration in milliseconds if parsing is successful; empty
   *     otherwise
   */
  @Override
  public Optional<Long> parseDuration(byte[] fileBytes) {
    if (fileBytes == null || fileBytes.length < 44) {
      return Optional.empty();
    }

    // Validate RIFF header
    if (!(fileBytes[0] == 'R'
        && fileBytes[1] == 'I'
        && fileBytes[2] == 'F'
        && fileBytes[3] == 'F')) {
      return Optional.empty();
    }

    // Validate WAVE format
    if (!(fileBytes[8] == 'W'
        && fileBytes[9] == 'A'
        && fileBytes[10] == 'V'
        && fileBytes[11] == 'E')) {
      return Optional.empty();
    }

    // Read byte rate from fmt chunk (offset 28-31)
    long byteRate =
        ((fileBytes[31] & 0xFFL) << 24)
            | ((fileBytes[30] & 0xFFL) << 16)
            | ((fileBytes[29] & 0xFFL) << 8)
            | (fileBytes[28] & 0xFFL);

    if (byteRate == 0L) {
      return Optional.empty();
    }

    // Locate the "data" chunk dynamically, starting after byte 12
    int pointer = 12;
    while (pointer + 8 <= fileBytes.length) {
      // Read chunk ID (4 bytes)
      String chunkId = new String(fileBytes, pointer, 4);
      // Read chunk size (4 bytes, little endian)
      int chunkSize =
          (fileBytes[pointer + 7] & 0xFF) << 24
              | (fileBytes[pointer + 6] & 0xFF) << 16
              | (fileBytes[pointer + 5] & 0xFF) << 8
              | (fileBytes[pointer + 4] & 0xFF);

      if ("data".equals(chunkId)) {
        // Found data chunk, calculate duration
        long dataSize = Integer.toUnsignedLong(chunkSize);
        long durationMs = (dataSize * 1000L) / byteRate;
        return Optional.of(durationMs);
      }

      // Move pointer to next chunk (chunk header + chunk data)
      pointer += 8 + chunkSize;
    }
    // Data chunk not found
    return Optional.empty();
  }

  /**
   * Returns the list of supported audio file extensions.
   *
   * @return an array of supported file extensions (lowercase, without dot)
   */
  @Override
  public String[] supportedExtensions() {
    return Arrays.copyOf(SUPPORTED_EXTENSIONS, SUPPORTED_EXTENSIONS.length);
  }
}
