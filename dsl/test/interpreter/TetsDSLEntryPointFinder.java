package interpreter;

import dslToGame.DSLEntryPoint;
import helpers.Helpers;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TetsDSLEntryPointFinder {

    @Test
    public void testReadEntrtyPoints() {
        List<DSLEntryPoint> entryPoints = new ArrayList<>();
        DSLEntryPointFinder finder = new DSLEntryPointFinder();
        URL resource1 = getClass().getClassLoader().getResource("config1.dng");
        assert resource1 != null;
        Path firstPath = null;
        try {
            firstPath = Path.of(resource1.toURI());
            var entryPointsFromFile = finder.getEntryPoints(firstPath).get();
            entryPoints.addAll(entryPointsFromFile);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        URL resource2 = getClass().getClassLoader().getResource("config2.dng");
        assert resource2 != null;
        Path secondPath = null;
        try {
            secondPath = Path.of(resource2.toURI());
            var entryPointsFromFile = finder.getEntryPoints(secondPath).get();
            entryPoints.addAll(entryPointsFromFile);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(4, entryPoints.size());

        DSLEntryPoint firstEntryPoint = entryPoints.get(0);
        Assert.assertEquals("This is my config 1", firstEntryPoint.displayName());
        Assert.assertEquals("my_config", firstEntryPoint.configName());
        Assert.assertEquals(firstPath, firstEntryPoint.filePath());

        DSLEntryPoint secondEntryPoint = entryPoints.get(1);
        Assert.assertEquals("my_other_config", secondEntryPoint.displayName());
        Assert.assertEquals("my_other_config", secondEntryPoint.configName());
        Assert.assertEquals(firstPath, secondEntryPoint.filePath());

        DSLEntryPoint thirdEntryPoint = entryPoints.get(2);
        Assert.assertEquals("This is my config 2", thirdEntryPoint.displayName());
        Assert.assertEquals("my_other_config", thirdEntryPoint.configName());
        Assert.assertEquals(secondPath, thirdEntryPoint.filePath());

        DSLEntryPoint forthEntryPoint = entryPoints.get(3);
        Assert.assertEquals("my_completely_other_config", forthEntryPoint.displayName());
        Assert.assertEquals("my_completely_other_config", forthEntryPoint.configName());
        Assert.assertEquals(secondPath, forthEntryPoint.filePath());
    }
}
