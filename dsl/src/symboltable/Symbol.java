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

/** Represents a symbol in a program */
public class Symbol {
    public enum Type {
        Base,
        Scoped // for handling of datatypes for object definitions
    }

    // running index, used as unique identifier
    protected static int s_idx;

    protected String name;
    // the parent scope of the symbol
    protected IScope scope;
    protected Type symbolType;

    private int idx;

    public static Symbol NULL = new Symbol("NULL SYMBOL", null);

    public String getName() {
        return name;
    }

    public IScope getScope() {
        return scope;
    }

    public int getIdx() {
        return idx;
    }

    public Type getSymbolType() {
        return symbolType;
    }

    public Symbol(String symbolName, IScope parentScope) {
        idx = s_idx++;
        scope = parentScope;
        name = symbolName;
        symbolType = Type.Base;
    }

    public Symbol() {
        scope = null;
        name = "no name";
        symbolType = Type.Base;
    }
}
