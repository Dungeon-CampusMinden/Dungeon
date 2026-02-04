import blockly.vm.dgir.core.Operation;
import tools.jackson.databind.ObjectMapper;

public class TestUtils {
  public static String compareSerializedOperations(ObjectMapper mapper, Operation op1, Operation op2) {
    try {
      String json1 = mapper.writeValueAsString(op1);
      String json2 = mapper.writeValueAsString(op2);
      /*
      Replace all UUIDs in the form of "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" with a standardized string "UUID".
       */
      String normalizedJson1 = json1.replaceAll("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "UUID");
      String normalizedJson2 = json2.replaceAll("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "UUID");
      if (normalizedJson1.equals(normalizedJson2)) {
        return "";
      }
      return diffStrings(normalizedJson1, normalizedJson2);
    } catch (Exception e) {
      return "Error during serialization comparison: " + e.getMessage();
    }
  }

  /**
   * Create a new string based on the initial string.
   * The string is printed in whole and line by line the difference is printed next to the original line if there is a difference.
   * The difference is separated by a " | " symbol.
   * @param base The original string
   * @param modified The modified string
   * @return the diff string
   */
  public static String diffStrings(String base, String modified) {
    base = base.replaceAll("\r", "");
    modified = modified.replaceAll("\r", "");
    StringBuilder diff = new StringBuilder();
    String[] baseLines = base.split("\n", -1);
    String[] modifiedLines = modified.split("\n", -1);
    int maxLines = Math.max(baseLines.length, modifiedLines.length);
    int maxBaseLength = 0;
    for (String line : baseLines) {
      maxBaseLength = Math.max(maxBaseLength, line.length());
    }
    for (int i = 0; i < maxLines; i++) {
      String baseLine = i < baseLines.length ? baseLines[i] : "";
      String modifiedLine = i < modifiedLines.length ? modifiedLines[i] : "";
      // Pad the base line to the maximum length of the base lines for better alignment.
      diff.append(String.format("%-" + maxBaseLength + "s", baseLine));
      diff.append(" | ");
      if (!baseLine.trim().equals(modifiedLine.trim())) {
        diff.append("\u001B[33m").append(modifiedLine).append("\u001B[0m");
      } else {
        diff.append("\u001B[32m").append(modifiedLine).append("\u001B[0m");
      }
      diff.append("\n");
    }
    return diff.toString();
  }
}
