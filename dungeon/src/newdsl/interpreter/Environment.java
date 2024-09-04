package newdsl.interpreter;

import java.util.HashMap;

public class Environment {

    private Environment enclosing;
    private HashMap<String, Object> values = new HashMap<>();

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        this.values.put(name, value);
    }

    public Object get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        } else if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new Error("undefined variable " + name);
    }

    public HashMap<String, Object> getValues(){
        return values;
    }

}
