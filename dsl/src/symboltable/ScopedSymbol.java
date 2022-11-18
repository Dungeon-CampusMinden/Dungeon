/*
 * MIT License
 *
 * Copyright (c) 2022 Malte Reinsch, Florian Warzecha, Sebastian Steinmeyer, BC George, Carsten Gips
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package symboltable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScopedSymbol extends Symbol implements IScope {

    public static Scope NULL = new Scope();

    protected HashMap<String, Symbol> symbols;

    public ScopedSymbol(String name, IScope parentScope, IType type) {
        super(name, parentScope, type);
        symbolType = Type.Scoped;
        symbols = new HashMap<>();
    }

    public boolean Bind(Symbol symbol) {
        var name = symbol.getName();
        if (symbols.containsKey(name)) {
            return false;
        } else {
            symbols.put(name, symbol);
            return true;
        }
    }

    public Symbol Resolve(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        } else if (scope != null) {
            return scope.Resolve(name);
        } else {
            return Symbol.NULL;
        }
    }

    @Override
    public IScope GetParent() {
        return scope;
    }

    @Override
    public List<Symbol> GetSymbols() {
        return new ArrayList<>(symbols.values());
    }
}
