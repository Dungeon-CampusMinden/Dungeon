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

package semanticAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import parser.AST.Node;

/** The results of semantic analysis done by SymbolTableParser */
public class SymbolTable {
    /** The global scope of the program */
    IScope globalScope;
    /** Store all symbols in a key-value store for easy referencing by their index */
    private final HashMap<Integer, Symbol> symbolIdxToSymbol;

    /** Store all referenced astNodes in a key-value store for easy referencing by their index */
    private final HashMap<Integer, Node> astNodeIdxToAstNode;

    /**
     * Creates an association between a specific AST node (by index) and a symbol (by index) -> e.g.
     * "which symbol is referenced by the identifier in a specific AST node?"
     */
    private final HashMap<Integer, ArrayList<Integer>> astNodeSymbolRelation;

    /**
     * Creates an association between a specific symbol (by Idx) and an ast node (by index) -> e.g.
     * "by which ast node was this symbol created?"
     */
    private final HashMap<Integer, Integer> symbolToAstNodeRelation;

    /**
     * Getter for the global {@link IScope}, which is the topmost scope in the scope stack
     *
     * @return the global {@link IScope}
     */
    public IScope getGlobalScope() {
        return globalScope;
    }

    /**
     * Add an association between symbol and AST node. The nodeOfSymbol passed to this method with a
     * symbol, which was not previously passed, will be treated as the node, which created the
     * symbol
     *
     * @param symbol The symbol
     * @param nodeOfSymbol The AST Node, which references the symbol
     */
    public void addSymbolNodeRelation(Symbol symbol, Node nodeOfSymbol) {
        if (!astNodeSymbolRelation.containsKey(nodeOfSymbol.getIdx())) {
            astNodeSymbolRelation.put(nodeOfSymbol.getIdx(), new ArrayList<>());
        }

        // TODO: are there situations, in which multiple symbols are associated with the same
        // AST-Node?
        //  if not, this could be simplified
        astNodeSymbolRelation.get(nodeOfSymbol.getIdx()).add(symbol.getIdx());

        if (!symbolIdxToSymbol.containsKey(symbol.getIdx())) {
            symbolToAstNodeRelation.put(symbol.getIdx(), nodeOfSymbol.getIdx());
        }

        symbolIdxToSymbol.put(symbol.getIdx(), symbol);
        astNodeIdxToAstNode.put(nodeOfSymbol.getIdx(), nodeOfSymbol);
    }

    /**
     * Try to get the Symbol referenced by a specific AST node
     *
     * @param node The AST node
     * @return The Symbol referenced by node, or Symbol.NULL, if no Symbol could be found
     */
    public ArrayList<Symbol> getSymbolsForAstNode(Node node) {
        if (!astNodeSymbolRelation.containsKey(node.getIdx())) {
            // TODO: just empty list?
            var list = new ArrayList<Symbol>();
            list.add(Symbol.NULL);
            return list;
        }

        var symbolIdxs = astNodeSymbolRelation.get(node.getIdx());
        var returnList = new ArrayList<Symbol>();
        for (int idx : symbolIdxs) {
            if (symbolIdxToSymbol.containsKey(idx)) {
                var symbol = symbolIdxToSymbol.get(idx);
                // TODO: why is this a list? if every AST-Node is ever associated with at most one
                //  symbol, this could be simplified -> not to be confused with the likely reference
                // of one symbol by
                //  multiple AST-Nodes
                returnList.add(symbol);
            }
        }
        return returnList;
    }

    /**
     * Gets the AST Node, which was passed to {@link #addSymbolNodeRelation(Symbol, Node)} the first
     * time the symbol was passed to that method, which will be treated as the AST Node, which
     * creates the Symbol
     *
     * @param symbol The symbol to get the creation AST node for
     * @return The creation AST node or Node.NONE, if none could be found for the passed symbol
     */
    public Node getCreationAstNode(Symbol symbol) {
        if (!symbolToAstNodeRelation.containsKey(symbol.getIdx())) {
            return Node.NONE;
        }

        var astNodeIdx = symbolToAstNodeRelation.get(symbol.getIdx());
        if (!astNodeIdxToAstNode.containsKey(astNodeIdx)) {
            return Node.NONE;
        }

        return astNodeIdxToAstNode.get(astNodeIdx);
    }

    /**
     * Get the {@link Symbol} with passed index
     *
     * @param idx the index of the {@link Symbol} to get
     * @return the {@link Symbol} with passed index or Symbol.NULL, if no symbol with given index
     *     exists
     */
    public Symbol getSymbolByIdx(int idx) {
        return symbolIdxToSymbol.getOrDefault(idx, Symbol.NULL);
    }

    /**
     * Constructor
     *
     * @param globalScope the global scope to use for this symbol Table
     */
    public SymbolTable(IScope globalScope) {
        this.globalScope = globalScope;
        astNodeSymbolRelation = new HashMap<>();
        symbolIdxToSymbol = new HashMap<>();
        astNodeIdxToAstNode = new HashMap<>();
        symbolToAstNodeRelation = new HashMap<>();
    }
}
