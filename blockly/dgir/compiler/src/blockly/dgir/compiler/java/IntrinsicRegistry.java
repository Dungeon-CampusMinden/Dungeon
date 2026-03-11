package blockly.dgir.compiler.java;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Retreive information about the intrinsic methods specified for blockly. These are place in
 * "/assets" and marked using the @Intrinsic annotation.
 */
public class IntrinsicRegistry {
  static final @NotNull Logger logger = Logger.getLogger(IntrinsicRegistry.class.getName());

  static boolean initialized = false;

  /**
   * A mapping from method signature to opcode name for all methods annotated with @Intrinsic in the
   * Dungeon files. Populated by {@link #init()}.
   */
  public static final @NotNull Map<@NotNull String, @NotNull String> signatureToOpCode =
      new HashMap<>();

  /**
   * A mapping from opcode name to method signature for all methods annotated with @Intrinsic in the
   * Dungeon files. Populated by {@link #init()}.
   */
  public static final @NotNull Map<@NotNull String, @NotNull Set<@NotNull String>>
      opcodeToIntrinsic = new HashMap<>();

  /**
   * A mapping from module name to a mapping from method signature to opcode name for all methods in
   * that module annotated with @Intrinsic. Populated by {@link #init()}.
   */
  public static final @NotNull Map<@NotNull String, @NotNull Map<@NotNull String, @NotNull String>>
      intrinsicsByModule = new HashMap<>();

  public static @NotNull @Unmodifiable List<String> listDungeonFiles() throws IOException {
    InputStream indexStream = openDungeonFile("_index.txt");
    if (indexStream == null) throw new IOException("Dungeon index not found");

    try (indexStream) {
      return new String(indexStream.readAllBytes(), StandardCharsets.UTF_8)
          .lines()
          .filter(l -> !l.isBlank())
          .toList();
    }
  }

  public static @Nullable InputStream openDungeonFile(String filename) {
    return ClassLoader.getSystemClassLoader().getResourceAsStream("imports/Dungeon/" + filename);
  }

  /**
   * Load all the Dungeon files specified in the index and return a mapping from their package name
   * to their source code.
   *
   * @return A mapping from package name to source code for all Dungeon files.
   * @throws IOException If any of the files specified in the index cannot be found or read.
   */
  public static @NotNull Map<String, String> loadAllDungeonFiles() throws IOException {
    Map<String, String> result = new LinkedHashMap<>();

    for (String filename : listDungeonFiles()) {
      try (InputStream is = openDungeonFile(filename)) {
        if (is == null) throw new IOException("Indexed file missing: " + filename);
        String packageName = "Dungeon/" + filename.substring(0, filename.lastIndexOf(".java"));
        packageName = packageName.replace('/', '.');
        result.put(packageName, new String(is.readAllBytes(), StandardCharsets.UTF_8));
      }
    }

    return result;
  }

  /** Init the intrinsics lookup so that we can just look at cached results later on. */
  public static void init() {
    if (initialized) return;
    initialized = true;

    // Load the intrinsics sources
    Map<String, String> intrinsicSources;
    try {
      intrinsicSources = loadAllDungeonFiles();
    } catch (IOException e) {
      System.err.println("Failed to load dungeon files: " + e.getMessage());
      return;
    }

    // Parse the files one at a time and extract the method declarations which are annotated with
    // the @Intrinsic annotation. Store the mapping from opcode name to method declaration in a
    // global map for later use.
    for (Map.Entry<String, String> entry : intrinsicSources.entrySet()) {
      String moduleName = entry.getKey();
      String source = entry.getValue();
      CompilationUnit result;
      try {
        result = StaticJavaParser.parse(source);
      } catch (ParseProblemException e) {
        logger.severe(
            "Failed to parse Java source code for module " + moduleName + ": " + e.getMessage());
        return;
      } catch (RuntimeException e) {
        logger.severe("Unexpected error while parsing Java source code: " + e.getMessage());
        return;
      }
      IntrinsicsEmitter emitter = new IntrinsicsEmitter();
      var moduleIntrinsics = intrinsicsByModule.computeIfAbsent(moduleName, s -> new HashMap<>());
      emitter.visit(result, moduleIntrinsics);
      signatureToOpCode.putAll(moduleIntrinsics);

      // Log the intrinsics for this module
      logger.info(
          "Intrinsics for module "
              + moduleName
              + ": "
              + moduleIntrinsics.keySet().stream()
                  .map(s -> s + " -> " + moduleIntrinsics.get(s))
                  .toList());
    }

    opcodeToIntrinsic.putAll(
        signatureToOpCode.entrySet().stream()
            .collect(
                Collectors.groupingBy(
                    Map.Entry::getValue,
                    Collectors.mapping(Map.Entry::getKey, Collectors.toSet()))));
  }

  /**
   * Visitor that looks for method declarations annotated with @Intrinsic and adds their signature
   * and opcode name to the provided collector map.
   */
  private static final class IntrinsicsEmitter extends VoidVisitorAdapter<Map<String, String>> {
    @Override
    public void visit(MethodDeclaration md, Map<String, String> collector) {
      md.getAnnotationByName("Intrinsic")
          .ifPresent(
              annotation -> {
                String opcodeName =
                    annotation
                        .asSingleMemberAnnotationExpr()
                        .getMemberValue()
                        .asStringLiteralExpr()
                        .getValue();
                collector.put(opcodeName, md.getDeclarationAsString());
              });
    }
  }
}
