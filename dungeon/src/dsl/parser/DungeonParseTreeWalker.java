package dsl.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.RuleNode;

public class DungeonParseTreeWalker extends ParseTreeWalker {
  public static final DungeonParseTreeWalker DEFAULT = new DungeonParseTreeWalker();

  @Override
  protected void exitRule(ParseTreeListener listener, RuleNode r) {
    ParserRuleContext ctx = (ParserRuleContext) r.getRuleContext();
    boolean specificExit = true;
    if (listener instanceof DungeonASTConverter astConverter) {
      specificExit = astConverter.preExitEveryRule(ctx);
    }
    if (specificExit) {
      ctx.exitRule(listener);
    }
    listener.exitEveryRule(ctx);
  }
}
