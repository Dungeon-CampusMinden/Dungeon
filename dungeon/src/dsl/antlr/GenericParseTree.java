package dsl.antlr;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public class GenericParseTree implements ParseTree {
  public static GenericParseTree NONE = new GenericParseTree("NONE", "NONE");

  public GenericParseTree(String text, String nodename) {
    this.text = text;
    this.nodeName = nodename;
  }

  public GenericParseTree() {}

  public GenericParseTree(
      String text, String nodeName, ParseTree parent, List<ParseTree> children) {
    this.text = text;
    this.nodeName = nodeName;
    this.parent = parent;
    this.children = children;
  }

  private ParseTree parent = GenericParseTree.NONE;
  private String nodeName = "";
  private String text = "";
  private List<ParseTree> children = new ArrayList<>();

  @Override
  public ParseTree getParent() {
    return parent;
  }

  @Override
  public Object getPayload() {
    return text;
  }

  @Override
  public ParseTree getChild(int i) {
    if (i > this.children.size()) {
      return NONE;
    }
    return this.children.get(i);
  }

  public void addChild(GenericParseTree child) {
    this.children.add(child);
    child.setParent(this);
  }

  @Override
  public int getChildCount() {
    return this.children.size();
  }

  @Override
  public String toStringTree() {
    StringBuilder str = new StringBuilder("( ");
    if (!this.nodeName.isEmpty()) {
      str.append("name: ");
      str.append(this.nodeName);
      str.append(" ");
    }
    if (!this.text.isEmpty()) {
      str.append("text: '");
      str.append(this.text);
      str.append("' ");
    }
    if (this.children.size() > 0) {
      str.append("c: ");
    }
    for (int i = 0; i < this.children.size(); i++) {
      var child = this.children.get(i);
      str.append(child.toStringTree());
      if (i < this.children.size() - 1) {
        str.append(", ");
      }
    }
    str.append(")");
    return str.toString();
  }

  @Override
  public void setParent(RuleContext ruleContext) {
    this.parent = ruleContext;
  }

  public void setParent(GenericParseTree parent) {
    this.parent = parent;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setNodeName(String name) {
    this.nodeName = name;
  }

  @Override
  public <T> T accept(ParseTreeVisitor<? extends T> parseTreeVisitor) {
    return null;
  }

  @Override
  public String getText() {
    return this.text;
  }

  public String getNodeName() {
    return this.nodeName;
  }

  @Override
  public String toStringTree(Parser parser) {
    return "";
  }

  @Override
  public Interval getSourceInterval() {
    return null;
  }

  @Override
  public String toString() {
    String str = this.getNodeName();
    if (!this.getText().isEmpty()) {
      str = str + " " + this.getText();
    }
    return str;
  }
}
