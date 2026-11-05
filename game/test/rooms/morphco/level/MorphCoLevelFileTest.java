package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Checks the level file against the named points the room code expects. */
class MorphCoLevelFileTest {

  private static JsonNode level() throws IOException {
    try (InputStream in =
        MorphCoLevelFileTest.class
            .getClassLoader()
            .getResourceAsStream("levels/morphCo/morphco_1.level")) {
      return JsonMapper.builder()
          .build()
          .readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  private static char tile(JsonNode level, JsonNode point) {
    List<String> rows = new ArrayList<>();
    level.get("layout").forEach(row -> rows.add(row.asString()));
    int x = (int) Math.floor(point.get("x").asDouble());
    int y = (int) Math.floor(point.get("y").asDouble());
    return rows.get(rows.size() - 1 - y).charAt(x);
  }

  @Test
  void everyPointUsedByTheRoomExists() throws IOException {
    JsonNode points = level().get("namedPoints");
    Pattern literal = Pattern.compile("(?:point|door)\\(\"([a-z0-9_]+)\"\\)");
    List<String> missing = new ArrayList<>();
    try (Stream<Path> files = Files.walk(Path.of("src/rooms/morphco"))) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
        Matcher matcher = literal.matcher(Files.readString(file));
        while (matcher.find()) {
          if (!points.has(matcher.group(1))) missing.add(matcher.group(1));
        }
      }
    }
    for (String indexed : List.of("crate", "conveyor_word", "boxes")) {
      assertTrue(points.has(indexed + "0"), indexed);
    }
    assertTrue(missing.isEmpty(), "Missing points: " + missing);
  }

  @Test
  void doorsLieOnDoorTilesAndTheStartOnTheFloor() throws IOException {
    JsonNode level = level();
    level
        .get("namedPoints")
        .properties()
        .forEach(
            entry -> {
              if (entry.getKey().startsWith("door_")) {
                assertEquals('D', tile(level, entry.getValue()), entry.getKey());
              }
            });
    assertEquals('F', tile(level, level.get("startTiles").get(0)));
    assertEquals("SYSTEM_RECOVERY", level.get("design").asString());
  }
}
