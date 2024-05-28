/*
 * MIT License
 *
 * Copyright (c) 2022 Malte Reinsch, Florian Warzecha, Sebastian Steinmeyer, BC George, Carsten Gips
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dsl.semanticanalysis.scope;

import dsl.parser.ast.Node;
import dsl.semanticanalysis.symbol.Symbol;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.neo4j.ogm.annotation.*;

@NodeEntity
public class Scope implements IScope {
  @Id @GeneratedValue public Long id;
  @Property private final String name;

  @Override
  public String getName() {
    return name;
  }

  public static Scope NULL = new Scope("NULL_SCOPE");

  @Relationship(type = "PARENT_SCOPE", direction = Relationship.Direction.OUTGOING)
  protected IScope parent;

  protected HashSet<IScope> childScopes;

  @Transient protected HashMap<String, Symbol> symbols;

  @Relationship(type = "CONTAINS", direction = Relationship.Direction.OUTGOING)
  protected List<Symbol> symbolList;

  @Relationship(type = "CREATES", direction = Relationship.Direction.INCOMING)
  protected Node relatedASTNode;

  /**
   * Constructor
   *
   * @param parentScope the parent scope of the new scope
   */
  public Scope(IScope parentScope) {
    parent = parentScope;
    this.name = "p: " + parentScope.getName();
    this.symbols = new HashMap<>();
    this.symbolList = new ArrayList<>();
    this.childScopes = new HashSet<>();
    this.parent.getChildScopes().add(this);
  }

  public Scope(IScope parentScope, String name) {
    parent = parentScope;
    this.name = name + " p: " + parentScope.getName();
    this.symbols = new HashMap<>();
    this.symbolList = new ArrayList<>();
    this.childScopes = new HashSet<>();
    this.parent.getChildScopes().add(this);
  }

  public Scope(IScope parentScope, Node node) {
    parent = parentScope;
    this.name = "p: " + parentScope.getName();
    this.symbols = new HashMap<>();
    this.symbolList = new ArrayList<>();
    this.relatedASTNode = node;
    this.childScopes = new HashSet<>();
    this.parent.getChildScopes().add(this);
  }

  /** Constructor */
  public Scope() {
    parent = NULL;
    this.name = "";
    this.symbols = new HashMap<>();
    this.symbolList = new ArrayList<>();
    this.childScopes = new HashSet<>();
  }

  public Scope(String name) {
    parent = NULL;
    this.name = name;
    this.symbols = new HashMap<>();
    this.symbolList = new ArrayList<>();
    this.childScopes = new HashSet<>();
  }

  /**
   * Bind a new symbol in this scope
   *
   * @param symbol The symbol to bind
   * @return True, if no symbol with the same name exists in this scope, false otherwise
   */
  public boolean bind(Symbol symbol) {
    if (this == NULL) {
      throw new RuntimeException("Binding in NULL scope!");
    }
    var name = symbol.getName();
    if (symbols.containsKey(name)) {
      return false;
    } else {
      symbols.put(name, symbol);
      symbolList.add(symbol);
      return true;
    }
  }

  /**
   * Try to resolve the passed name in this scope (or the parent scope).
   *
   * @param name the name of the symbol to resolvle
   * @param resolveInParent if set to true, and the name could not be resolved in this scope,
   *     resolve it in the parent scope
   * @return the resolved symbol or Symbol.NULL, if the name could not be resolved
   */
  public Symbol resolve(String name, boolean resolveInParent) {
    if (symbols.containsKey(name)) {
      return symbols.get(name);
    } else if (parent != null && resolveInParent) {
      return parent.resolve(name);
    } else {
      return Symbol.NULL;
    }
  }

  /**
   * Try to resolve the passed name in this scope (or the parent scope).
   *
   * @param name the name of the symbol to resolvle
   * @return the resolved symbol or Symbol.NULL, if the name could not be resolved
   */
  public Symbol resolve(String name) {
    return resolve(name, true);
  }

  /**
   * Getter for a List of all bound symbols
   *
   * @return a List of all bound symbols
   */
  @Override
  public List<Symbol> getSymbols() {
    return new ArrayList<>(symbols.values());
  }

  /**
   * Getter for the parent scope of this scope
   *
   * @return the parent of this scope
   */
  @Override
  public IScope getParent() {
    return parent;
  }

  @Override
  public HashSet<IScope> getChildScopes() {
    return this.childScopes;
  }
}
