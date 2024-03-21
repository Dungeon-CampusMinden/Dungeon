package dsl.antlr;

import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

import java.util.HashSet;

public abstract class DungeonDSLLexerWithEnvironment extends Lexer {
  private IEnvironment environment;
  protected HashSet<String> globalTypeNames = new HashSet<>();

  public DungeonDSLLexerWithEnvironment(CharStream input, IEnvironment environment) {
    super(input);
    setEnvironment(environment);
  }

  protected void setEnvironment(IEnvironment environment) {
    if (environment != null) {
      this.environment = environment;

      // the built-in types are not registered in the typebuilder of the environment explicitly, because
      // they are not built.
      // we need to put them in a separate HashSet; the globalScope of the environment will contain all built-in
      // types at this point
      this.globalTypeNames.clear();
      for (var symbol : environment.getGlobalScope().getSymbols()) {
        if (symbol instanceof IType) {
          globalTypeNames.add(symbol.getName());
        }
      }
    }
  }

  public DungeonDSLLexerWithEnvironment(CharStream input) {
    this(input, null);
  }

  public boolean isStrTypeName(String str) {
    if (this.environment == null) {
      return true;
    }

    // try to resolve str in global scope, which will contain all built-in types
    if (this.globalTypeNames.contains(str)) {
      return true;
    }

    return environment.isTypeName(str);
  }
}
