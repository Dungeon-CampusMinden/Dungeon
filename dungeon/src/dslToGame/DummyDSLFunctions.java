package dslToGame;

import java.util.HashMap;
import java.util.Map;

// @malte-r you have to place this methods to the right place
public class DummyDSLFunctions {

    public static Map<String, String> getConfigs(String dslFile) {
        // dummy siehe   https://github.com/Programmiermethoden/Dungeon/issues/869
        HashMap<String, String> map = new HashMap<>();
        map.put(dslFile, dslFile);
        return map;
    }
}
