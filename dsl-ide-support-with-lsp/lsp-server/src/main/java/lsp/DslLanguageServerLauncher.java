package lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Class that provides runnable main method. It launches the {@code DslLanguageServer} listening on
 * the standard streams.
 */
public final class DslLanguageServerLauncher {

  private DslLanguageServerLauncher() {}

  /**
   * Runnable main method launching the {@code DslLanguageServer} that will listen on the standard
   * streams.
   *
   * @param args the command line parameters.
   * @throws InterruptedException is thrown when the server is interrupted.
   * @throws ExecutionException if the computation threw an exception.
   * @throws IOException when the socket connection throws an IO Error
   */
  public static void main(final String[] args)
      throws InterruptedException, ExecutionException, IOException {

    if (Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("debug"))) {
      try (Socket clientSocket = new Socket("127.0.0.1", 9925)) {
        startServer(clientSocket.getInputStream(), clientSocket.getOutputStream());
      }
    } else {
      startServer(System.in, System.out);
    }
  }

  /**
   * Starts the lsp server.
   *
   * @param in the {@code InputStream} the server listens on.
   * @param out the {@code OutputStream} the server writes to.
   * @throws InterruptedException is thrown when the server is interrupted.
   * @throws ExecutionException if the computation threw an exception.
   */
  public static void startServer(final InputStream in, final OutputStream out)
      throws InterruptedException, ExecutionException {
    DslLanguageServer server = new DslLanguageServer();
    Launcher<LanguageClient> launcher =
        Launcher.createLauncher(server, LanguageClient.class, in, out);
    LanguageClient client = launcher.getRemoteProxy();
    server.connect(client);
    Future<?> startListening = launcher.startListening();
    startListening.get();
  }
}
