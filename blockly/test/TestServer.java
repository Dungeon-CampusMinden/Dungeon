import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import server.Server;

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
