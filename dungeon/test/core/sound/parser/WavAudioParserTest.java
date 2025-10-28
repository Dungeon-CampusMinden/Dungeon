package core.sound.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Tests for the {@link WavAudioParser} class. */
public class WavAudioParserTest {

  private final WavAudioParser parser = new WavAudioParser();

  private Path getTestFile(String filename) throws Exception {
    String path = "/sounds/" + filename;
    URL url = getClass().getResource(path);
    if (url == null) {
      throw new RuntimeException("Test resource not found: " + path);
    }
    return Path.of(url.toURI());
  }

  @Test
  void testParseDurationWithInvalidFile() throws Exception {
    Path invalidPath = getTestFile("invalid.wav");
    Optional<Long> result = parser.parseDuration(invalidPath);
    assertFalse(result.isPresent());
  }

  @Test
  void testParseDurationWithShortHeader() throws Exception {
    Path shortPath = getTestFile("invalid.wav");
    Optional<Long> result = parser.parseDuration(shortPath);
    assertFalse(result.isPresent());
  }

  @Test
  void testParseDurationWithValidWavFile() throws Exception {
    Path validWavPath = getTestFile("test.wav");
    Optional<Long> result = parser.parseDuration(validWavPath);
    assertTrue(result.isPresent());
    assertEquals(1962L, result.get());
  }

  @Test
  void testSupportedExtensions() {
    String[] extensions = parser.supportedExtensions();
    assertArrayEquals(new String[] {"wav"}, extensions);
  }
}
