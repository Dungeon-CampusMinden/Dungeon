package dslToGame;

import java.nio.file.Path;

public record DSLEntryPoint (Path filePath, String displayName, String configName) { }
