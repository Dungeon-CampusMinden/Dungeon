module.exports = function(babel) {
    const { types: t } = babel;

    return {
        visitor: {
            // Inserting command after variable declaration
            // Todo - Only works for declarations with number, not for declaration with expression
            VariableDeclaration(path) {
                path.node.declarations.forEach(declaration => {
                    const { id, init } = declaration;
                    const name = id.name;
                    const value = init ? init.value : 'undefined';

                    const websocketSendStatement = t.expressionStatement(
                        t.callExpression(
                            t.identifier('websocketSend'),
                            [t.stringLiteral(`var ${name} = ${value}`)]
                        )
                    );
                    path.insertAfter(websocketSendStatement);
                });
            }
        }
    };
};
