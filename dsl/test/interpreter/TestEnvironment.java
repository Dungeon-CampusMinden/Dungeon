package interpreter;

import runtime.GameEnvironment;

import semanticanalysis.Scope;
import semanticanalysis.Symbol;
import semanticanalysis.types.IType;

public class TestEnvironment extends GameEnvironment {

    public TestEnvironment() {
        super();
    }

    @Override
    protected void bindBuiltIns() {
        for (IType type : BUILT_IN_TYPES) {
            // load custom QuestConfig
            if (!type.getName().equals("quest_config") && !type.getName().equals("entity")) {
                globalScope.bind((Symbol) type);
            }
        }

        var questConfigType =
                this.getTypeBuilder().createTypeFromClass(Scope.NULL, CustomQuestConfig.class);
        loadTypes(questConfigType);

        for (Symbol func : NATIVE_FUNCTIONS) {
            globalScope.bind(func);
        }
    }
}
