package core.sound.parser;

import java.util.Optional;

/**
 * Interface for parsing metadata from audio files, such as duration. Implementations handle
 * specific audio formats (e.g., WAV, OGG) to extract details needed for sound management. Used
 * during asset loading to populate {@link core.sound.SoundAsset} metadata.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * IAudioParser parser = new WavAudioParser();
 * Optional<Long> duration = parser.parseDuration(Path.of("sound.wav"));
 * }</pre>
 *
 * @see WavAudioParser
 * @see core.sound.SoundAsset
 */
public interface IAudioParser {
  /**
   * Parses the duration of the audio file in milliseconds.
   *
   * @param fileBytes the audio file bytes
   * @return the duration in milliseconds, or empty if parsing fails or is unsupported
   */
  Optional<Long> parseDuration(byte[] fileBytes);

  /**
   * Returns the supported file extensions for this parser.
   *
   * @return array of supported extensions (e.g., "wav")
   */
  String[] supportedExtensions();
}
