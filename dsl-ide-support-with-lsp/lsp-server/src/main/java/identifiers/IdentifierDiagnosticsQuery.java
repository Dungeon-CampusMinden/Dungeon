package identifiers;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

/** Query to get diagnostics for redefined, undefined and unused ids. */
public class IdentifierDiagnosticsQuery {

  /**
   * Gets diagnostics for redefined, undefined and unused ids.
   *
   * @param definitions the identifier definitions.
   * @param usages the identifier usages.
   * @return diagnostics for redefined, undefined and unused ids.
   */
  public static ArrayList<Diagnostic> getDiagnostics(
      Hashtable<String, ArrayList<Range>> definitions, Hashtable<String, ArrayList<Range>> usages) {
    ArrayList<Diagnostic> diagnostics = new ArrayList<>();

    addRedefinitions(diagnostics, definitions);
    addDiagnosticsForUndefinedIds(diagnostics, definitions, usages);
    addDiagnosticsForUnusedIds(diagnostics, definitions, usages);
    return diagnostics;
  }

  private static void addRedefinitions(
      ArrayList<Diagnostic> diagnostics, Hashtable<String, ArrayList<Range>> definitions) {
    for (Map.Entry<String, ArrayList<Range>> definitionWithRanges : definitions.entrySet()) {
      List<Range> rangesOfRedefinition = definitionWithRanges.getValue().stream().skip(1).toList();
      String name = definitionWithRanges.getKey();
      for (Range rangeOfRedefinition : rangesOfRedefinition) {
        diagnostics.add(new Diagnostic(rangeOfRedefinition, name + " already defined"));
      }
    }
  }

  private static void addDiagnosticsForUnusedIds(
      ArrayList<Diagnostic> diagnostics,
      Hashtable<String, ArrayList<Range>> definitions,
      Hashtable<String, ArrayList<Range>> usages) {
    for (Map.Entry<String, ArrayList<Range>> definition : definitions.entrySet()) {
      String definitionName = definition.getKey();
      if (!usages.containsKey(definitionName)) {
        for (Range range : definition.getValue()) {
          diagnostics.add(new Diagnostic(range, definitionName + " not used"));
        }
      }
    }
  }

  private static void addDiagnosticsForUndefinedIds(
      ArrayList<Diagnostic> diagnostics,
      Hashtable<String, ArrayList<Range>> definitions,
      Hashtable<String, ArrayList<Range>> usages) {
    for (Map.Entry<String, ArrayList<Range>> usage : usages.entrySet()) {
      if (!definitions.containsKey(usage.getKey())) {
        for (Range range : usage.getValue()) {
          diagnostics.add(new Diagnostic(range, usage.getKey() + " not found"));
        }
      }
    }
  }
}
