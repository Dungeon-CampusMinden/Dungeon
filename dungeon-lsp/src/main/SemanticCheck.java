package main;

import org.eclipse.lsp4j.DiagnosticSeverity;

public class SemanticCheck {
  public String cypherQuery;
  public int amountParamsFromQuery;
  public String msgFmt;
  public DiagnosticSeverity sev;

  public SemanticCheck(String cypherQuery, int amountParamsFromQuery, String msgFmt, DiagnosticSeverity sev) {
    this.cypherQuery = cypherQuery;
    this.amountParamsFromQuery = amountParamsFromQuery;
    this.msgFmt = msgFmt;
    this.sev = sev;
  }
}
