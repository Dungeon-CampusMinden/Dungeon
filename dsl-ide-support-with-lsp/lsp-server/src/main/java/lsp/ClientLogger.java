package lsp;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * This class is used to log to the lsp client. With the vscode extension it is visible in the
 * vscode output when selecting the extension from the available outputs.
 */
public final class ClientLogger {

  private static ClientLogger instance;
  private LanguageClient client;
  private boolean isInitialized;

  private ClientLogger() {}

  /**
   * Initializes this logger with the {@code languageClient}.
   *
   * @param languageClient the {@code LanguageClient} to initialize this logger with.
   */
  public void initialize(final LanguageClient languageClient) {
    if (!Boolean.TRUE.equals(isInitialized)) {
      this.client = languageClient;
    }
    isInitialized = true;
  }

  /**
   * Gets the current instance of the {@code ClientLogger}. Creates a new instance if none
   * available.
   *
   * @return an instance of the {@code ClientLogger}.
   */
  public static ClientLogger getInstance() {
    if (instance == null) {
      instance = new ClientLogger();
    }
    return instance;
  }

  /**
   * Logs an info message to the client. With the vscode extension it is visible in the vscode
   * output when selecting the extension from the available outputs.
   *
   * @param message what will be logged.
   */
  public void logMessage(final String message) {
    if (!isInitialized) {
      return;
    }
    client.logMessage(new MessageParams(MessageType.Info, message));
  }
}
