package dsl.parser.ast;

import java.util.ArrayList;
import java.util.List;

/** WTF? . */
public class FuncDefNode extends Node {
  /** WTF? . */
  public final int idIdx = 0;

  /** WTF? . */
  public final int paramListIdx = 1;

  /** WTF? . */
  public final int retTypeIdx = 2;

  /** WTF? . */
  public final int stmtBlockIdx = 3;

  /**
   * Getter for the AstNode corresponding to the identifier of the defined function.
   *
   * @return AstNode corresponding to the identifier of the defined function
   */
  public Node getId() {
    return this.getChild(idIdx);
  }

  /**
   * Getter for the name of the defined function.
   *
   * @return Name of the defined function as String
   */
  public String getIdName() {
    return ((IdNode) this.getChild(idIdx)).getName();
  }

  /**
   * Getter for the AstNode corresponding to the return type of the function definition.
   *
   * @return AstNode corresponding to the return type of the function definition
   */
  public Node getRetTypeId() {
    return this.getChild(retTypeIdx);
  }

  /**
   * Getter for the AstNode corresponding to the stmtBlock of the function definition.
   *
   * @return AstNode corresponding to the stmtBlock of the function definition
   */
  public Node getStmtBlock() {
    return this.getChild(stmtBlockIdx);
  }

  /**
   * Getter for the name of return type of the function definition.
   *
   * @return Name of the return type as String
   */
  public String getRetTypeName() {
    return ((IdNode) this.getChild(retTypeIdx)).getName();
  }

  /**
   * Getter for the AstNodes corresponding to the stmts of the function definition.
   *
   * @return List of the AstNodes corresponding to the stmts of the function definition
   */
  public List<Node> getStmts() {
    var block = this.getStmtBlock();
    return block.getChild(0).getChildren();
  }

  /**
   * Getter for the AstNodes corresponding to the parameters of the function call.
   *
   * @return List of the AstNodes corresponding to the parameters of the function call
   */
  public List<Node> getParameters() {
    return this.getChild(paramListIdx).getChildren();
  }

  /**
   * Constructor. WTF? .
   *
   * @param id The AstNode corresponding to the identifier of the called function
   * @param paramList The AstNode corresponding to the parameter list of the function call
   * @param retType foo
   * @param stmtBlock foo
   */
  public FuncDefNode(Node id, Node paramList, Node retType, Node stmtBlock) {
    super(Type.FuncDef, new ArrayList<>(4));

    this.addChild(id);
    this.addChild(paramList);
    this.addChild(retType);
    this.addChild(stmtBlock);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
