package dsl.antlr;

import dsl.semanticanalysis.environment.IEnvironment;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

public abstract class DungeonDSLParserWithEnvironment extends Parser {
  protected IEnvironment environment;

  public DungeonDSLParserWithEnvironment(TokenStream input, IEnvironment environment) {
    super(input);
    this.environment = environment;
  }

  public DungeonDSLParserWithEnvironment(TokenStream input) {
    this(input, null);
  }

  public boolean isTypeName(Token token) {
    String tokenText = token.getText();
    return this.isStrTypeName(tokenText);
  }

  public boolean isStrTypeName(String str) {
    if (this.environment == null) {
      return true;
    }

    return environment.isTypeName(str);
  }
}
