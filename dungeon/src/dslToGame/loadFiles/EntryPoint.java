package dslToGame.loadFiles;

import java.nio.file.Path;

/**
 * Used to store an entry point into the DSL.
 *
 * <p>In the future, the parameters will be used for the {@link
 * interpreter.DSLInterpreter#getQuestConfig(String)}.
 *
 * @param name The name used by the game for selection preview.
 * @param path The path to the DSL file.
 */
public record EntryPoint(String name, Path path) {}
