package dsl.parser;

import dsl.antlr.*;
import dsl.antlr.ParseTreeValidationLexer;
import dsl.antlr.ParseTreeValidationListener;
import dsl.antlr.ParseTreeValidationParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.*;

public class ParseTreeValidator implements ParseTreeValidationListener {
  private static String DONT_CARE = "$DC$";
  private GenericParseTree reconstructedTree;

  private Stack<Integer> indentStack = new Stack<>();
  private Stack<GenericParseTree> nodeStack = new Stack<>();
  private List<String> errorList = new ArrayList<>();
  private List<String> ruleNames;

  public List<String> validate(String prettyPrintedParseTree, Tree t, List<String> ruleNames) {
    this.ruleNames = ruleNames;
    var reconstructedTreeExpected = reconstructTree(prettyPrintedParseTree);
    var treeToCheck = TreeUtils.toPrettyTree(t, ruleNames);
    var reconstructedTreeToCheck = reconstructTree(treeToCheck);

    // TODO: match trees
    var firstRealNodeExpected = reconstructedTreeExpected.getChild(0);
    var firstRealNodeToCheck = reconstructedTreeToCheck.getChild(0);
    var errorList = matchNode(firstRealNodeExpected, firstRealNodeToCheck);

    return errorList;
  }

  private List<String> matchNode(ParseTree expectedTree, ParseTree treeToCheck) {
    List<String> mismatches = new ArrayList<>();
    // do the names/types of the nodes match
    // does the text of the nodes match
    String expectedTreeStr = expectedTree.toString();
    // String treeToCheckStr = Trees.getNodeText(treeToCheck, ruleNames);
    String treeToCheckStr = expectedTree.toString();

    boolean match = expectedTreeStr.equals(treeToCheckStr);
    if (!match) {
      String msg =
          "Nodes do not match, expected: '" + expectedTreeStr + "' tree: '" + treeToCheckStr + "'";
      mismatches.add(msg);
    }

    // TODO: do the children of the nodes match
    boolean careAboutChildren =
        expectedTree.getChildCount() != 1 || !expectedTree.getChild(0).toString().equals(DONT_CARE);

    if (careAboutChildren && expectedTree.getChildCount() != treeToCheck.getChildCount()) {
      // check for don't care node
      String msg =
          String.format(
              "Childcount of expected node '%s'(%x) and given node '%s'(%x) is different!",
              expectedTreeStr,
              expectedTree.getChildCount(),
              treeToCheckStr,
              treeToCheck.getChildCount());
      mismatches.add(msg);
    }

    if (careAboutChildren) {
      for (int i = 0; i < expectedTree.getChildCount() && i < treeToCheck.getChildCount(); i++) {
        var expectedChild = expectedTree.getChild(i);
        var childToCheck = treeToCheck.getChild(i);
        List<String> msgs = matchNode(expectedChild, childToCheck);
        mismatches.addAll(msgs);
      }
    }

    return mismatches;
  }

  public GenericParseTree reconstructTree(String prettyPrintedTree) {
    var cs = CharStreams.fromString(prettyPrintedTree);
    ParseTreeValidationLexer lexer = new ParseTreeValidationLexer(cs);
    var tokenStream = new CommonTokenStream(lexer);
    ParseTreeValidationParser parser = new ParseTreeValidationParser(tokenStream);
    var parsedTree = parser.tree();

    ParseTreeWalker.DEFAULT.walk(this, parsedTree);
    return this.reconstructedTree;
  }

  @Override
  public void enterTree(ParseTreeValidationParser.TreeContext ctx) {
    indentStack.clear();
    nodeStack.clear();
    this.reconstructedTree = new GenericParseTree("", "root");
    this.nodeStack.push(reconstructedTree);
    this.indentStack.push(-1);
  }

  @Override
  public void exitTree(ParseTreeValidationParser.TreeContext ctx) {}

  @Override
  public void enterBranch(ParseTreeValidationParser.BranchContext ctx) {
    // if the indentStack is empty, create and push a new node and push indent level to stack
    int indents = ctx.INDENT().size();
    if (indentStack.empty()) {
      indentStack.push(indents);
      nodeStack.push(new GenericParseTree());
    }

    // if the top of indentStack is smaller than current indent, create new node, add it as child to
    // nodeStack-top, push new indent to stack
    else if (indentStack.peek() < indents) {
      var node = new GenericParseTree();
      nodeStack.peek().addChild(node);
      nodeStack.push(node);
      indentStack.push(indents);
    }

    // if the top of indentStack is bigger or equal to the current indent, pop the top (of both
    // indentStack and
    // nodeStack) until top is equal to current indent
    // then create new node for current branch, add it to child of top of nodeStack
    else if (indentStack.peek() >= indents) {
      boolean x = true;
      while (indentStack.peek() >= indents) {
        indentStack.pop();
        nodeStack.pop();
      }

      var node = new GenericParseTree();
      this.indentStack.push(indents);
      this.nodeStack.peek().addChild(node);
      this.nodeStack.push(node);
    }
  }

  @Override
  public void exitBranch(ParseTreeValidationParser.BranchContext ctx) {}

  @Override
  public void enterDont_care_branch(ParseTreeValidationParser.Dont_care_branchContext ctx) {
    // set node name and text to "$DC$"
    this.nodeStack.peek().setNodeName(DONT_CARE);
  }

  @Override
  public void exitDont_care_branch(ParseTreeValidationParser.Dont_care_branchContext ctx) {}

  @Override
  public void enterSingle_symbol_branch(ParseTreeValidationParser.Single_symbol_branchContext ctx) {
    // set rule name of node on top of nodeStack to matched text
    String nodeName = ctx.SYMBOL().getText();
    this.nodeStack.peek().setNodeName(nodeName);
  }

  @Override
  public void exitSingle_symbol_branch(ParseTreeValidationParser.Single_symbol_branchContext ctx) {}

  @Override
  public void enterComplex_branch(ParseTreeValidationParser.Complex_branchContext ctx) {
    // set rule name of node on top of nodeStack to matched text of symbol with space
    // add matched text to text of node on top of nodeStack
    String nodeName = ctx.rule_.getText();
    StringBuilder matchedText = new StringBuilder();
    for (int i = 0; i < ctx.matched_text().SYMBOL().size(); i++) {
      var symbol = ctx.matched_text().SYMBOL(i);
      var space = ctx.matched_text().SPACE(i);
      if (symbol != null) {
        matchedText.append(symbol.getText());
      }
      if (space != null) {
        matchedText.append(space.getText());
      }
    }

    this.nodeStack.peek().setText(matchedText.toString());
    this.nodeStack.peek().setNodeName(nodeName);
  }

  @Override
  public void exitComplex_branch(ParseTreeValidationParser.Complex_branchContext ctx) {}

  @Override
  public void enterMatched_text(ParseTreeValidationParser.Matched_textContext ctx) {}

  @Override
  public void exitMatched_text(ParseTreeValidationParser.Matched_textContext ctx) {}

  @Override
  public void enterSymbol_with_space(ParseTreeValidationParser.Symbol_with_spaceContext ctx) {}

  @Override
  public void exitSymbol_with_space(ParseTreeValidationParser.Symbol_with_spaceContext ctx) {}

  @Override
  public void visitTerminal(TerminalNode terminalNode) {}

  @Override
  public void visitErrorNode(ErrorNode errorNode) {}

  @Override
  public void enterEveryRule(ParserRuleContext parserRuleContext) {}

  @Override
  public void exitEveryRule(ParserRuleContext parserRuleContext) {}
}
