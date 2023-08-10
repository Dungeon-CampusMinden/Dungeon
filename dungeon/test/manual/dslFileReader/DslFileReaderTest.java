package manual.dslFileReader;

import core.Game;
import core.hud.UITools;

import dslToGame.DslFileLoader;
import dslToGame.DummyDSLFunctions;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class DslFileReaderTest {

    public static void main(String[] args) throws IOException {
        Game.initBaseLogger();
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class);
        Game.disableAudio(true);
        Game.userOnLevelLoad(
                loadFirstTime -> {
                    Set<File> files = DslFileLoader.dslFiles();
                    Set<String> fileContents =
                            files.stream()
                                    .map(DslFileLoader::fileToString)
                                    .collect(Collectors.toSet());
                    Set<Map<String, String>> configs =
                            fileContents.stream()
                                    .map(DummyDSLFunctions::getConfigs)
                                    .collect(Collectors.toSet());

                    AtomicReference<String> f = new AtomicReference<>("");
                    files.forEach(v -> f.set(f.get() + v + System.lineSeparator()));
                    UITools.generateNewTextDialog(f.get(), "Ok", "Files");

                    // for the start: print on console
                    configs.forEach(map -> map.values().forEach(System.out::println));
                });

        Game.run();
    }
}
