package dsl.antlr;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ParseTracerForTokenType implements ParseTreeListener {
  private final Parser parser;

  public ParseTracerForTokenType(Parser parser) {
    this.parser = parser;
  }

  @Override
  public void visitTerminal(TerminalNode terminalNode) {}

  @Override
  public void visitErrorNode(ErrorNode errorNode) {}

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
    var LA1 = this.parser.getInputStream().LA(1);
    String displayName = parser.getVocabulary().getDisplayName(LA1);
    System.out.println(
        "enter   "
            + this.parser.getRuleNames()[ctx.getRuleIndex()]
            + ", LA(1) type='"
            + displayName
            + "'");
  }

  @Override
  public void exitEveryRule(ParserRuleContext parserRuleContext) {}
}
