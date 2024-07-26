package org.hsbi.dungeon.intellijplugin;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.lsp4ij.LanguageServerFactory;
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider;
import org.jetbrains.annotations.NotNull;

/** Factory for the dungeon dsl language server. */
public class DngDslLanguageServerFactory implements LanguageServerFactory {
  @NotNull
  @Override
  public StreamConnectionProvider createConnectionProvider(@NotNull Project project) {
    return new DngDslLanguageServer();
  }
}
