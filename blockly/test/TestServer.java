import org.junit.jupiter.api.BeforeEach;
import server.Server;

import java.io.IOException;

/** Test the server class. */
public class TestServer {
  private Server server;

  /** Reset all global values before each test. */
  @BeforeEach
  public void setUp() throws IOException {
    server = Server.instance();
    server.clearGlobalValues();
  }
}
