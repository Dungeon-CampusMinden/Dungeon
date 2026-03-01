package dgir.vm;

import core.Dialect;
import core.serialization.Utils;
import dgir.vm.api.OpRunnerRegistry;
import dgir.vm.api.VM;
import dgir.vm.dap.DapServer;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static dialect.builtin.BuiltinOps.ProgramOp;

/**
 * Entry-point for the DGIR VM DAP debug server.
 *
 * <p>Usage:
 *
 * <pre>
 *   java -jar dgir-vm.jar [--program &lt;path&gt;] [--dap-port &lt;port&gt;]
 * </pre>
 *
 * <p>When {@code --program} is supplied the server loads the given {@code .dgir} JSON file and
 * executes it inside each debug session. Without {@code --program} the server starts but accepts no
 * sessions (useful for testing that the port opens).
 *
 * <p>The server runs forever; send SIGTERM / Ctrl-C to stop it.
 */
public class DgirDebugServer {

  private static final Logger LOG = Logger.getLogger(DgirDebugServer.class.getName());

  /** Default DAP TCP port. Must match the VS Code extension default. */
  public static final int DEFAULT_DAP_PORT = DapServer.DEFAULT_PORT;

  public static void main(String[] args) throws Exception {
    String programPath = null;
    int port = DEFAULT_DAP_PORT;

    // Simple arg parsing
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--program" -> {
          if (i + 1 < args.length) {
            programPath = args[++i];
          } else {
            System.err.println("--program requires a path argument");
            System.exit(1);
          }
        }
        case "--dap-port" -> {
          if (i + 1 < args.length) {
            try {
              port = Integer.parseInt(args[++i]);
            } catch (NumberFormatException e) {
              System.err.println("--dap-port requires an integer argument");
              System.exit(1);
            }
          } else {
            System.err.println("--dap-port requires an integer argument");
            System.exit(1);
          }
        }
        case "--help", "-h" -> {
          printUsage();
          System.exit(0);
        }
        default -> {
          System.err.println("Unknown argument: " + args[i]);
          printUsage();
          System.exit(1);
        }
      }
    }

    // Register all dialects and op runners (once, globally)
    Dialect.registerAllDialects();
    OpRunnerRegistry.registerAllRunners();

    // Load the program if provided
    final ProgramOp program = programPath != null ? loadProgram(programPath) : null;

    if (program == null) {
      System.err.println(
          "Warning: no --program supplied; the DAP server will start but each session will immediately exit.");
    }

    // Build the DAP server; each session gets its own VM instance
    DapServer server =
        new DapServer(
            port,
            () -> {
              VM vm = new VM();
              if (program != null) {
                vm.init(program);
              }
              return vm;
            });

    server.start();
    LOG.info("DGIR DAP server listening on port " + port + ". Press Ctrl-C to stop.");

    // Block the main thread indefinitely
    Thread.currentThread().join();
  }

  // =========================================================================
  // Helpers
  // =========================================================================

  /**
   * Deserialize a {@link ProgramOp} from a JSON {@code .dgir} file.
   *
   * @param filePath path to the {@code .dgir} / {@code .json} file.
   * @return the deserialized program, or {@code null} on error.
   */
  private static @NotNull ProgramOp loadProgram(@NotNull String filePath) throws IOException {
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      throw new IOException("DGIR program file not found: " + filePath);
    }

    ObjectMapper mapper = Utils.getMapper(false);
    String json = Files.readString(path);
    ProgramOp program = mapper.readValue(json, ProgramOp.class);
    if (program == null) {
      throw new IOException("Failed to deserialize DGIR program from: " + filePath);
    }
    return program;
  }

  private static void printUsage() {
    System.out.println("Usage: java -jar dgir-vm.jar [--program <path.dgir>] [--dap-port <port>]");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  --program <path>   Path to the .dgir JSON program file to debug.");
    System.out.println(
        "  --dap-port <port>  TCP port for the DAP server (default: " + DEFAULT_DAP_PORT + ").");
    System.out.println("  --help, -h         Print this help message.");
  }
}
