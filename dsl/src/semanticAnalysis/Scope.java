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
import java.util.List;

public class Scope implements IScope {

    public static Scope NULL = new Scope();

    protected IScope parent;
    protected HashMap<String, Symbol> symbols;

    /**
     * Constructor
     *
     * @param parentScope the parent scope of the new scope
     */
    public Scope(IScope parentScope) {
        parent = parentScope;
        symbols = new HashMap<>();
    }

    /** Constructor */
    public Scope() {
        parent = NULL;
        symbols = new HashMap<>();
    }

    /**
     * Bind a new symbol in this scope
     *
     * @param symbol The symbol to bind
     * @return True, if no symbol with the same name exists in this scope, false otherwise
     */
    public boolean bind(Symbol symbol) {
        var name = symbol.getName();
        if (symbols.containsKey(name)) {
            return false;
        } else {
            symbols.put(name, symbol);
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
}
