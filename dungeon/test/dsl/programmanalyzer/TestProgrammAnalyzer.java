package dsl.programmanalyzer;

import org.junit.Test;

public class TestProgrammAnalyzer {
  @Test
  public void testUpdate() {
    String programState1 =
        """
      fn test(entity ent, int x) {
          if x {

          }
      }
      """;

    String programState2 =
        """
      fn test(entity ent, int x) {
          if x {
            e
          }
      }
      """;

    ProgrammAnalyzer analyzer = new ProgrammAnalyzer(false);
    String mockFileUri = "asdf";
    var result1 = analyzer.analyze(programState1, mockFileUri);
    var creations1 = result1.symboltable().currentSymbolCreations();
    var references1 = result1.symboltable().currentSymbolReferences();

    var result2 = analyzer.analyze(programState2, mockFileUri);
    var creations2 = result2.symboltable().currentSymbolCreations();
    var references2 = result2.symboltable().currentSymbolReferences();

    boolean b = true;
  }
}
