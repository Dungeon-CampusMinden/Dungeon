package dsl.antlr;

import dsl.semanticanalysis.environment.IEnvironment;
import org.antlr.v4.runtime.Parser;
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

  public boolean isTypeName() {
    if (this.environment == null) {
      return true;
    }

    var currentToken = this.getCurrentToken();
    var lt1 = _input.LT(1);
    var lt2 = _input.LT(2);
    var lt3 = _input.LT(3);
    String currentTokenText = currentToken.getText();
    String lt1Text = lt1.getText();
    String lt2Text = lt2.getText();
    String lt3Text = lt3.getText();
    return environment.isTypeName(lt1Text);
  }
}
