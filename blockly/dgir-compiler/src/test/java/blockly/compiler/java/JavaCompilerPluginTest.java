package blockly.compiler.java;

import static org.junit.jupiter.api.Assertions.*;

import blockly.compiler.api.CompilerPlugin;
import org.junit.jupiter.api.Test;

class JavaCompilerPluginTest {
  @Test
  void returnsIdAndDescription() {
    CompilerPlugin plugin = new JavaCompilerPlugin();
    assertEquals("java", plugin.getId());
    assertTrue(plugin.getDescription().toLowerCase().contains("java"));
  }

  @Test
  void producesIrString() throws Exception {
    CompilerPlugin plugin = new JavaCompilerPlugin();
    var response = plugin.compile(new blockly.compiler.api.CompilerRequest("System.out.println(\"hi\");", "Main.java"));
    assertEquals("java", response.backendId());
    assertTrue(response.ir().contains("IR for unit"));
    assertTrue(response.ir().contains("length"));
  }
}
