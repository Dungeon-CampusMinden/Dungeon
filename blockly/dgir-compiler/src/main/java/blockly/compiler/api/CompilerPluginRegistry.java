package blockly.compiler.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple registry to support multiple compiler backends (e.g., java, python, etc.).
 * Backends are hardcoded at build-time; lookup is by id.
 */
public final class CompilerPluginRegistry {
  private static final Map<String, CompilerPlugin> PLUGINS;

  static {
    Map<String, CompilerPlugin> map = new HashMap<>();
    // Register built-in backends here. Additional backends can be added by code changes.
    registerBuiltin(map, new blockly.compiler.java.JavaCompilerPlugin());
    PLUGINS = Collections.unmodifiableMap(map);
  }

  private CompilerPluginRegistry() {}

  private static void registerBuiltin(Map<String, CompilerPlugin> map, CompilerPlugin plugin) {
    map.put(plugin.getId(), plugin);
  }

  /**
   * @param id backend identifier (e.g., "java").
   * @return the backend instance or null if not present.
   */
  public static CompilerPlugin byId(String id) {
    return PLUGINS.get(id);
  }

  /**
   * @return immutable view of all registered backends.
   */
  public static Map<String, CompilerPlugin> all() {
    return PLUGINS;
  }
}

