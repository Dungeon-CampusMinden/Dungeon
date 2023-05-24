package interpreter;

import runtime.GameEnvironment;

import semanticanalysis.Scope;
import semanticanalysis.Symbol;
import semanticanalysis.types.IType;

import java.util.List;

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
        var typesToLoad = new semanticanalysis.types.IType[] {questConfigType};
        loadTypes(List.of(typesToLoad));

        for (Symbol func : NATIVE_FUNCTIONS) {
            globalScope.bind(func);
        }
    }
}
