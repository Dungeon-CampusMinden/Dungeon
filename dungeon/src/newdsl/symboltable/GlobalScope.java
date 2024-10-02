package newdsl.symboltable;

import java.util.HashMap;

public class GlobalScope extends BaseScope {
    public GlobalScope(Scope enclosingScope, HashMap<String, Symbol> symbols) {
        super(enclosingScope, symbols, null);
    }

    @Override
    public String getName() {
        return "global";
    }
}
