package dsl.antlr;

import dsl.semanticanalysis.environment.IEnvironment;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

public abstract class DungeonDSLLexerWithEnvironment extends Lexer {
  protected IEnvironment environment;

  public DungeonDSLLexerWithEnvironment(CharStream input, IEnvironment environment) {
    super(input);
    this.environment = environment;
  }

  public DungeonDSLLexerWithEnvironment(CharStream input) {
    this(input, null);
  }

  public boolean isStrTypeName(String str) {
    if (this.environment == null) {
      return true;
    }

    return environment.isTypeName(str);
  }
}
