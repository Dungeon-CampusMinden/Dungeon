package dsl.antlr;

import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

public class TreeUtils {

  /** Platform dependent end-of-line marker */
  public static final String Eol = System.lineSeparator();

  private static final String terminalPrefix = "$";

  /** The literal indent char(s) used for pretty-printing */
  public static final String Indents = "  ";

  private static int level;

  private TreeUtils() {}

  public static String toPrettyTree(final Tree t, final List<String> ruleNames) {
    level = 0;
    return process(t, ruleNames).replaceAll("(?m)^\\s+$", "").replaceAll("\\r?\\n\\r?\\n", Eol);
  }

  private static String process(final Tree t, final List<String> ruleNames) {
    if (t.getChildCount() == 0) {
      StringBuilder sb = new StringBuilder(lead(level) + terminalPrefix);
      String nodeText = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false);
      if (t instanceof ErrorNodeImpl) {
        sb.append(String.format("'%s'[ERROR_NODE]", nodeText));
        return sb.toString();
      }
      if (t instanceof ParserRuleContext ctx) {
        if (ctx.exception != null) {
          sb.append(String.format("'%s'[EXCEPTION IN NODE]", nodeText));
          return sb.toString();
        }
      }
      sb.append(String.format("'%s'", nodeText));
      return sb.toString();
    }
    StringBuilder sb = new StringBuilder();
    sb.append(lead(level));
    level++;
    String s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false);
    sb.append(s + ' ');
    if (t instanceof ParserRuleContext ctx) {
      if (ctx.exception != null) {
        sb.append("[EXCEPTION IN NODE] ");
      }
    }
    for (int i = 0; i < t.getChildCount(); i++) {
      sb.append(process(t.getChild(i), ruleNames));
    }
    level--;
    sb.append(lead(level));
    return sb.toString();
  }

  private static String lead(int level) {
    StringBuilder sb = new StringBuilder();
    if (level > 0) {
      sb.append(Eol);
      for (int cnt = 0; cnt < level; cnt++) {
        sb.append(Indents);
      }
    }
    return sb.toString();
  }
}
