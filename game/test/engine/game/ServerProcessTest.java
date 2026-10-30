package engine.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import org.junit.jupiter.api.Test;

/** Tests the check that keeps the main menu from joining a server left over on its port. */
class ServerProcessTest {

  @Test
  void portIsBusyWhileAnotherProgramListens() throws IOException {
    try (ServerSocket other = new ServerSocket()) {
      other.bind(new InetSocketAddress(0));
      assertFalse(ServerProcess.portFree(other.getLocalPort()));
    }
  }

  @Test
  void portIsFreeOnceTheOtherProgramStops() throws IOException {
    int port;
    try (ServerSocket other = new ServerSocket()) {
      other.bind(new InetSocketAddress(0));
      port = other.getLocalPort();
    }
    assertTrue(ServerProcess.portFree(port));
  }
}
