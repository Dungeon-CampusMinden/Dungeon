package lsp;

import identifiers.IdentifierRangesCollector;

/** The information of a document. */
public class DocumentInformation {
  private String fileContent;
  private final IdentifierRangesCollector definitionIdCollector;
  private final IdentifierRangesCollector usageIdCollector;

  /**
   * Initializes a new DocumentInformation instance.
   *
   * @param fileContent the file content of the document.
   */
  DocumentInformation(String fileContent) {
    this.fileContent = fileContent;
    definitionIdCollector = new IdentifierRangesCollector();
    usageIdCollector = new IdentifierRangesCollector();
  }

  /**
   * Update the file content and clear collected information.
   *
   * @param fileContent the new file content.
   */
  public void updateFileContent(String fileContent) {
    this.fileContent = fileContent;
    definitionIdCollector.clear();
    usageIdCollector.clear();
  }

  /**
   * Get the usage id collector.
   *
   * @return the usage id collector.
   */
  public IdentifierRangesCollector getUsageIdCollector() {
    return usageIdCollector;
  }

  /**
   * Gets the definition id collector.
   *
   * @return the definition id collector.
   */
  public IdentifierRangesCollector getDefinitionIdCollector() {
    return definitionIdCollector;
  }

  /**
   * Gets the file content.
   *
   * @return the file content.
   */
  public String getFileContent() {
    return fileContent;
  }
}
