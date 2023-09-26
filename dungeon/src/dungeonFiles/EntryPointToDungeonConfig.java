package dungeonFiles;

public class EntryPointToDungeonConfig {

    public static DungeonConfig dungeonConfigFor(DSLEntryPoint entryPoint) {
        // @malte-r dsl magic
        return new DungeonConfig(null, null);
    }
}
