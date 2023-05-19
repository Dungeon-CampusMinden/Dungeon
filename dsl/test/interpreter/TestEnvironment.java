package interpreter;

import runtime.GameEnvironment;

import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;
import semanticAnalysis.types.IType;

public class TestEnvironment extends GameEnvironment {
    public TestEnvironment() {
        super();
    }

    @Override
    protected void bindBuiltIns() {
        for (IType type : BUILT_IN_TYPES) {
            // load custom QuestConfig
            if (!type.getName().equals("quest_config") && !type.getName().equals("game_object")) {
                globalScope.bind((Symbol) type);
            }
        }

        var questConfigType =
                this.getTypeBuilder()
                        .createTypeFromClass(
                                Scope.NULL, TestDSLInterpreter.CustomQuestConfig.class);
        loadTypes(new semanticAnalysis.types.IType[] {questConfigType});

        for (Symbol func : NATIVE_FUNCTIONS) {
            globalScope.bind(func);
        }
    }
}
