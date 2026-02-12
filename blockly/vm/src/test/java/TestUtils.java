import blockly.vm.dgir.core.ir.Block;
import blockly.vm.dgir.core.ir.Operation;
import blockly.vm.dgir.dialect.builtin.ProgramOp;
import blockly.vm.dgir.dialect.func.FuncOp;
import blockly.vm.dgir.dialect.func.types.FuncType;
import org.apache.commons.lang3.tuple.Pair;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtils {
  private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

  public static String compareSerializedOperations(ObjectMapper mapper, Operation op1, String op2Json) {
    return compareSerializedOperations(mapper, op1, mapper.readValue(op2Json, Operation.class));
  }

  public static String compareSerializedOperations(ObjectMapper mapper, Operation op1, Operation op2) {
    try {
      String json1 = mapper.writeValueAsString(op1);
      String json2 = mapper.writeValueAsString(op2);

      String normalizedJson1 = normalizeJson(json1);
      String normalizedJson2 = normalizeJson(json2);

      if (normalizedJson1.equals(normalizedJson2)) {
        return "";
      }
      return diffStrings(normalizedJson1, normalizedJson2);
    } catch (Exception e) {
      return "Error during serialization comparison: " + e.getMessage();
    }
  }

  private static String normalizeJson(String json) {
    AtomicInteger uuidCounter = new AtomicInteger();
    Map<UUID, String> uuidMap = new HashMap<>();

    Matcher matcher = UUID_PATTERN.matcher(json);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      UUID uuid = UUID.fromString(matcher.group());
      String replacement = uuidMap.computeIfAbsent(uuid, u -> "UUID_" + uuidCounter.getAndIncrement());
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
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

  /**
   * Create a new ProgramOp with a func.func op inside with the symbol_name "main"
   * @return a pair of the created ProgramOp and the block contained in the func.func op
   */
  public static Pair<ProgramOp, FuncOp> createProgramOpWithEntryFunc(){
    ProgramOp programOp = new ProgramOp();
    FuncOp funcOp = programOp.addOperation(new FuncOp("main"));
    return Pair.of(programOp, funcOp);
  }
}
