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

// importing all required classes from symbolTable will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport
import parser.AST.*;
import semanticAnalysis.types.BuiltInType;
import semanticAnalysis.types.IType;
// CHECKSTYLE:ON: AvoidStarImport

/** Creates symbols for definition nodes (graph, object) and binds these nodes to those symbols */
public class VariableBinder implements AstVisitor<Void> {
    SymbolTable symbolTable;
    IScope parentScope;
    StringBuilder errorStringBuilder;

    /**
     * Visits each child node of the passed rootNode and binds definition nodes.
     *
     * @param symbolTable The symbolTable to use for creation of relations between AST nodes and
     *     Symbols
     * @param parentScope The scope, in which the binding should be performed
     * @param rootNode The node to visit the children of
     * @param errorStringBuilder A string builder to append error messages to
     */
    public void bindVariables(
            SymbolTable symbolTable,
            IScope parentScope,
            Node rootNode,
            StringBuilder errorStringBuilder) {
        this.symbolTable = symbolTable;
        this.parentScope = parentScope;
        this.errorStringBuilder = errorStringBuilder;
        visitChildren(rootNode);
    }

    @Override
    public Void visit(Node node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(IdNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(StringNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(BinaryNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(DotDefNode node) {
        var id = node.getIdNode();
        assert id.type == Node.Type.Identifier;

        // Bind assignee in current scope
        var idNode = (IdNode) id;
        var idName = idNode.getName();

        // check, if assignee is already bound in current scope
        // if not, create it and bind it
        var objectSymbol = parentScope.resolve(idName);

        if (Symbol.NULL == objectSymbol) {
            objectSymbol = new Symbol(idName, parentScope, BuiltInType.graphType);
            if (parentScope.bind(objectSymbol)) {
                symbolTable.addSymbolNodeRelation(objectSymbol, node);
            }
        } else {
            errorStringBuilder.append("Already defined object of name...");
        }
        return null;
    }

    @Override
    public Void visit(ObjectDefNode node) {
        var id = node.getId();
        assert id.type == Node.Type.Identifier;

        // Bind assignee in current scope
        var idNode = (IdNode) id;
        var idName = idNode.getName();

        // check, if assignee is already bound in current scope
        // if not, create it and bind it
        var objectSymbol = parentScope.resolve(idName);

        if (Symbol.NULL == objectSymbol) {
            // resolve type name
            var typeName = node.getTypeSpecifierName();
            var type = this.parentScope.resolve(typeName);
            assert type != null;
            assert type instanceof IType;

            objectSymbol = new Symbol(idName, parentScope, (IType) type);
            if (parentScope.bind(objectSymbol)) {
                symbolTable.addSymbolNodeRelation(objectSymbol, node);
            }
        } else {
            errorStringBuilder.append("Already defined object of name...");
        }
        return null;
    }
}
