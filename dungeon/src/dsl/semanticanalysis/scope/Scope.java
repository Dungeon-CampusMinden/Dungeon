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

import dsl.IndexGenerator;
import dsl.parser.ast.Node;
import dsl.programmanalyzer.Relatable;
import dsl.programmanalyzer.Relate;
import dsl.programmanalyzer.RelationshipRecorder;
import dsl.semanticanalysis.symbol.Symbol;
import java.util.*;
import org.neo4j.ogm.annotation.*;

@NodeEntity
public class Scope implements IScope, Relatable {

  @Id @GeneratedValue private Long id;
  @Property public Long internalId = IndexGenerator.getUniqueIdx();
  // @Id public final Long uuid = IndexGenerator.getIdx();
  @Property private final String name;

  @Override
  public String getName() {
    return name;
  }

  public static Scope NULL = new Scope("NULL_SCOPE");

  @Relate(type = "PARENT_SCOPE")
  @Transient
  protected IScope parent;

  @Transient protected HashSet<IScope> childScopes;

  @Transient protected HashMap<String, Symbol> symbols;

  @Relate(type = "CONTAINS")
  @Transient
  protected List<Symbol> symbolList;

  @Transient
  // TODO: direction = incoming, maybe this is redundant anyway..!!!
  @Relate(type = "CREATES", direction = Relate.Direction.INCOMING)
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

    RelationshipRecorder.instance.addRelatable(this);
  }

  public Scope(IScope parentScope, String name) {
    parent = parentScope;
    this.name = name + " p: " + parentScope.getName();
    this.symbols = new HashMap<>();
    this.symbolList = new ArrayList<>();
    this.childScopes = new HashSet<>();
    this.parent.getChildScopes().add(this);

    RelationshipRecorder.instance.addRelatable(this);
  }

  public Scope(IScope parentScope, Node node) {
    parent = parentScope;
    this.name = "p: " + parentScope.getName();
    this.symbols = new HashMap<>();
    this.symbolList = new ArrayList<>();
    this.relatedASTNode = node;
    this.childScopes = new HashSet<>();
    this.parent.getChildScopes().add(this);

    RelationshipRecorder.instance.addRelatable(this);
  }

  /** Constructor */
  public Scope() {
    parent = NULL;
    this.name = "";
    this.symbols = new HashMap<>();
    this.symbolList = new ArrayList<>();
    this.childScopes = new HashSet<>();

    RelationshipRecorder.instance.addRelatable(this);
  }

  public Scope(String name) {
    parent = NULL;
    this.name = name;
    this.symbols = new HashMap<>();
    this.symbolList = new ArrayList<>();
    this.childScopes = new HashSet<>();

    RelationshipRecorder.instance.addRelatable(this);
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

  @Override
  public Long getId() {
    return this.internalId;
  }
}
