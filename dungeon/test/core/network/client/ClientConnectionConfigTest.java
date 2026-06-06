package core.network.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests parsing and validation for {@link ClientConnectionConfig}. */
public class ClientConnectionConfigTest {

  /** Empty dialog fields use the visible default values. */
  @Test
  public void emptyFieldsUseDefaults() {
    ClientConnectionConfig config = ClientConnectionConfig.fromFields("", "");

    assertEquals("127.0.0.1", config.host());
    assertEquals(7777, config.port());
  }

  /** Host input is trimmed before validation. */
  @Test
  public void hostIsTrimmed() {
    ClientConnectionConfig config = ClientConnectionConfig.fromFields("  example.org  ", "");

    assertEquals("example.org", config.host());
  }

  /** A valid port input is parsed and used. */
  @Test
  public void validPortIsUsed() {
    ClientConnectionConfig config = ClientConnectionConfig.fromFields("", " 12345 ");

    assertEquals(12345, config.port());
  }

  /** Non-numeric port input is rejected. */
  @Test
  public void nonNumericPortIsRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> ClientConnectionConfig.fromFields("", "abc"));
  }

  /** Port zero is rejected. */
  @Test
  public void portZeroIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> ClientConnectionConfig.fromFields("", "0"));
  }

  /** Negative ports are rejected. */
  @Test
  public void negativePortIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> ClientConnectionConfig.fromFields("", "-1"));
  }

  /** Port values above 65535 are rejected. */
  @Test
  public void portAboveMaximumIsRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> ClientConnectionConfig.fromFields("", "65536"));
  }

  /** Empty host input is rejected when the configured default host is invalid. */
  @Test
  public void emptyHostWithInvalidDefaultIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ClientConnectionConfig.fromFields("", "", "", ClientConnectionConfig.DEFAULT_PORT));
  }
}
