import dgir.core.DgirCoreUtils;
import dgir.core.analysis.DotCFG;
import dgir.core.debug.Location;
import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import dgir.core.serialization.Utils;
import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.apache.commons.lang3.tuple.Pair;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.func.FuncOps.FuncOp;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtils {
  public static ObjectMapper mapper = Utils.getMapper(true);
  public static boolean printResult = true;
  public static boolean saveResult = true;
  public static boolean printCfg = true;
  public static boolean saveCfg = true;
  public static boolean saveCfgImage = true;
  // The file path for saved files (cfg and image)
  public static String savePath = "test_results/";

  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

  public static boolean testValidityAndSerialization(Op op) {
    String result = mapper.writeValueAsString(op);
    if (printResult) System.out.println(result);

    assertEquals(
        "",
        TestUtils.compareSerializedOperations(mapper, op.getOperation(), result),
        "Serialization mismatch for op: " + op.getClass().getSimpleName());

    String callerName =
        DgirCoreUtils.STACK_WALKER.walk(
            stream ->
                stream
                    .skip(1)
                    .findFirst()
                    .map(
                        stackFrame ->
                            stackFrame.getDeclaringClass().getSimpleName()
                                + "."
                                + stackFrame.getMethodName())
                    .orElse("unknown"));

    // Ensure the output directory exists before writing files
    try {
      Files.createDirectories(Paths.get(savePath));
    } catch (IOException e) {
      System.out.println("Failed to create output directory '" + savePath + "': " + e);
    }

    if (saveResult) {
      String filePath = savePath + callerName + ".json";
      try {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), UTF_8);
        writer.write(result);
        writer.close();
        System.out.println("Saved result to " + filePath);
      } catch (IOException e) {
        System.out.println("Failed to save result to " + filePath + ": " + e.getMessage());
      }
    }

    // Check that this is a valid op, otherwise we can't generate a cfg
    if (!op.verify(true)) {
      System.out.println(
          "Skipping cfg generation for invalid op: "
              + op.getClass().getSimpleName()
              + " for test "
              + callerName);
      return false;
    }

    if (printCfg || saveCfg || saveCfgImage) {
      DotCFG.Cluster cfg = DotCFG.buildCfgCluster(op.getOperation());

      // Print the cfg to console
      if (printCfg) System.out.println(cfg);

      // Save the cfg to a file with the caller name as the file name
      if (saveCfg) {
        String filePath = savePath + callerName + ".dot";
        try {
          BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), UTF_8);
          writer.write(cfg.toString());
          writer.close();
          System.out.println("Saved CFG to " + filePath);
        } catch (IOException e) {
          System.out.println("Failed to save CFG to " + filePath + ": " + e.getMessage());
        }
      }

      // Generate an image of the cfg and save it to a file with the caller name as the file name
      if (saveCfgImage) {
        String filePath = savePath + callerName + ".png";
        try {
          Graphviz.fromString(cfg.toString())
              .engine(Engine.DOT)
              .render(Format.PNG)
              .toFile(new File(filePath));
        } catch (IOException e) {
          System.out.println("Failed to save CFG image to " + filePath + ": " + e.getMessage());
        }
      }
    }
    return true;
  }

  public static String compareSerializedOperations(
      ObjectMapper mapper, Operation op1, String op2Json) {
    return compareSerializedOperations(mapper, op1, mapper.readValue(op2Json, Operation.class));
  }

  public static String compareSerializedOperations(
      ObjectMapper mapper, Operation op1, Operation op2) {
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
      String replacement =
          uuidMap.computeIfAbsent(uuid, u -> "UUID_" + uuidCounter.getAndIncrement());
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  /**
   * Create a new string based on the initial string. The string is printed in whole and line by
   * line the difference is printed next to the original line if there is a difference. The
   * difference is separated by a " | " symbol.
   *
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
   *
   * @return a pair of the created ProgramOp and the block contained in the func.func op
   */
  public static Pair<ProgramOp, FuncOp> createProgramOpWithEntryFunc() {
    ProgramOp programOp = new ProgramOp(Location.UNKNOWN);
    FuncOp funcOp = programOp.addOperation(new FuncOp(Location.UNKNOWN, "main"));
    return Pair.of(programOp, funcOp);
  }
}
