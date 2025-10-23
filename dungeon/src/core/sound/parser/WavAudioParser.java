package core.sound.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Implementation of {@link IAudioParser} for parsing WAV file metadata. Reads the WAV header to
 * calculate duration based on sample rate, channels, bit depth, and data size. Assumes standard
 * 44-byte PCM WAV headers; returns empty on parsing errors or non-standard files.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * WavAudioParser parser = new WavAudioParser();
 * Optional<Long> duration = parser.parseDuration(Path.of("sound.wav"));
 * if (duration.isPresent()) {
 *     System.out.println("Duration: " + duration.get() + " ms");
 * }
 * }</pre>
 *
 * @see IAudioParser
 */
public class WavAudioParser implements IAudioParser {
  private static final String[] SUPPORTED_EXTENSIONS = {"wav"};

  /**
   * Parses the duration of a WAV file by reading its header. Calculates duration as (data size /
   * bytes per second) * 1000.
   *
   * @param file the WAV file path
   * @return the duration in milliseconds, or empty if parsing fails
   */
  @Override
  public Optional<Long> parseDuration(Path file) {
    try {
      byte[] header = Files.readAllBytes(file);
      if (header.length < 44) {
        return Optional.empty();
      }
      // Sample rate at 24-27 (little endian)
      int sampleRate =
          (header[27] & 0xFF) << 24
              | (header[26] & 0xFF) << 16
              | (header[25] & 0xFF) << 8
              | (header[24] & 0xFF);
      // Channels at 22-23
      int channels = (header[23] & 0xFF) << 8 | (header[22] & 0xFF);
      // Bits per sample at 34-35
      int bitsPerSample = (header[35] & 0xFF) << 8 | (header[34] & 0xFF);
      // Data size at 40-43
      int dataSize =
          (header[43] & 0xFF) << 24
              | (header[42] & 0xFF) << 16
              | (header[41] & 0xFF) << 8
              | (header[40] & 0xFF);
      long bytesPerSecond = (long) sampleRate * channels * (bitsPerSample / 8);
      long durationMs = dataSize * 1000L / bytesPerSecond;
      return Optional.of(durationMs);
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  @Override
  public String[] supportedExtensions() {
    return SUPPORTED_EXTENSIONS;
  }
}
