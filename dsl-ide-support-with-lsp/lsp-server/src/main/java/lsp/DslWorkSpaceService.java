package lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.RenameFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

/** Implements {@code WorkspaceService} to provide workspace specific capabilities. */
public class DslWorkSpaceService implements WorkspaceService {
  private final ClientLogger clientLogger;

  /** Creates a new {@code DslWorkSpaceService} instance. */
  public DslWorkSpaceService() {
    this.clientLogger = ClientLogger.getInstance();
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {
    this.clientLogger.logMessage("Operation 'workspace/didChangeConfiguration' Ack");
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {
    this.clientLogger.logMessage("Operation 'workspace/didChangeWatchedFiles' Ack");
  }

  @Override
  public void didRenameFiles(RenameFilesParams params) {
    this.clientLogger.logMessage("Operation 'workspace/didRenameFiles' Ack");
  }
}
