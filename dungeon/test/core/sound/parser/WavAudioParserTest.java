package core.sound.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Tests for the {@link WavAudioParser} class. */
public class WavAudioParserTest {

  private final WavAudioParser parser = new WavAudioParser();

  private byte[] getTestFile(String filename) throws Exception {
    String path = "/sounds/" + filename;
    URL url = getClass().getResource(path);
    if (url == null) {
      throw new RuntimeException("Test resource not found: " + path);
    }
    return Files.readAllBytes(Path.of(url.toURI()));
  }

  @Test
  void testParseDurationWithInvalidFile() throws Exception {
    Optional<Long> result = parser.parseDuration(getTestFile("invalid.wav"));
    assertFalse(result.isPresent());
  }

  @Test
  void testParseDurationWithShortHeader() throws Exception {
    Optional<Long> result = parser.parseDuration(getTestFile("invalid.wav"));
    assertFalse(result.isPresent());
  }

  @Test
  void testParseDurationWithValidWavFile() throws Exception {
    Optional<Long> result = parser.parseDuration(getTestFile("test.wav"));
    assertTrue(result.isPresent());
    assertEquals(1962L, result.get());
  }

  @Test
  void testSupportedExtensions() {
    String[] extensions = parser.supportedExtensions();
    assertArrayEquals(new String[] {"wav"}, extensions);
  }
}
