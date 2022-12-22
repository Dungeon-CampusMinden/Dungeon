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

import java.util.List;

/** Represents the capabilities of a scope. */
public interface IScope {
    /**
     * Binds the symbol to this scope
     *
     * @param symbol The symbol to bind
     * @return True on success (the symbols was not previously bound to this scope) and false
     *     otherwise
     */
    boolean bind(Symbol symbol);

    /**
     * Try to resolve a name in this scope or the parent scopes.
     *
     * @param name The name to resolve
     * @return The symbol with the name or Symbol.NULL, if the symbol could not be resolved
     */
    Symbol resolve(String name);

    /**
     * Try to resolve a name in this scope or the parent scopes.
     *
     * @param name The name to resolve
     * @return The symbol with the name or Symbol.NULL, if the symbol could not be resolved
     */
    Symbol resolve(String name, boolean resolveInParent);

    /**
     * Get all symbols of this scope in a List
     *
     * @return List containing all symbols in this scope.
     */
    List<Symbol> getSymbols();

    /**
     * Get the parent scope of this scope.
     *
     * @return The parent scope.
     */
    IScope getParent();
}
