package org.hsbi.dungeon.intellijplugin;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Representation of the dungeon dsl language server for lsp4ij defining the server start command.
 */
public class DngDslLanguageServer extends ProcessStreamConnectionProvider {
    /**
     * Creating a new instance of the dungeon dsl language server by defining the server start command.
     */
    public DngDslLanguageServer() {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(
                PluginId.getId("org.hsbi.intellij-plugin"));
        if (plugin != null) {
            Path pluginInstallDirPath = plugin.getPluginPath();
            if (pluginInstallDirPath != null) {
                File[] possibleLspServerJars = pluginInstallDirPath.resolve("lib").toFile()
                        .listFiles(pathname -> pathname.getName()
                                .startsWith("lsp-server"));
                if (possibleLspServerJars != null && possibleLspServerJars.length > 0 ) {
                    String serverJarPath = Arrays.stream(possibleLspServerJars)
                            .findFirst()
                            .map(File::getPath)
                            .orElse(null);
                    String pathToJavaExe = Path.of(System.getenv("JAVA_HOME")).resolve("bin").resolve("java").toString();
                    String qualifiedMainClass = "lsp.DslLanguageServerLauncher";
                    super.setCommands(
                            List.of(pathToJavaExe, "-cp", serverJarPath, qualifiedMainClass));
                } else {
                    throw new RuntimeException("LSP Server Jar not found in plugin install directory");
                }                
            } else {
                throw new RuntimeException("Plugin install directory not found");
            }
        }
        else {
            throw new RuntimeException("Plugin not found");
        }        
    }
}