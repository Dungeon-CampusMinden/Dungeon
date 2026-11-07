package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Checks the level file against the named points and objects the room code expects. */
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

  private static List<JsonNode> objects(JsonNode level) {
    List<JsonNode> objects = new ArrayList<>();
    level
        .get("prefabs")
        .forEach(
            prefab -> {
              if (MorphCoObjectPrefab.TYPE.equals(prefab.get("type").asString())) {
                objects.add(prefab);
              }
            });
    return objects;
  }

  @Test
  void everyObjectHasAKnownKind() throws IOException {
    List<String> unknown = new ArrayList<>();
    for (JsonNode object : objects(level())) {
      String kind = object.get("properties").get("kind").asString();
      if (!MorphCoObjects.kinds().contains(kind)) unknown.add(object.get("name").asString());
    }
    assertTrue(unknown.isEmpty(), "Objects of unknown kind: " + unknown);
  }

  @Test
  void everyObjectTheRoomUsesIsPlaced() throws IOException {
    Set<String> placed = new HashSet<>();
    objects(level()).forEach(object -> placed.add(object.get("name").asString()));
    placed.add("phone");
    placed.add("coffee");
    Pattern literal = Pattern.compile("\\b(?:on|poster|sign)\\(\\s*\"([a-z0-9_-]+)\"\\s*,");
    List<String> missing = new ArrayList<>();
    try (Stream<Path> files = Files.walk(Path.of("src/rooms/morphco/level"))) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
        Matcher matcher = literal.matcher(Files.readString(file));
        while (matcher.find()) {
          if (!placed.contains(matcher.group(1))) missing.add(matcher.group(1));
        }
      }
    }
    for (String indexed :
        List.of("crate4", "conveyor-word3", "boxes1", MorphCoLighting.ALARM_LIGHT)) {
      if (!placed.contains(indexed)) missing.add(indexed);
    }
    assertTrue(missing.isEmpty(), "Objects without an instance in the level: " + missing);
  }

  @Test
  void everyArtFileOfTheObjectsExists() throws IOException {
    String source = Files.readString(Path.of("src/rooms/morphco/level/MorphCoObjects.java"));
    Matcher matcher = Pattern.compile("\"([a-z0-9_/]+\\.png)\"").matcher(source);
    List<String> missing = new ArrayList<>();
    while (matcher.find()) {
      String file = matcher.group(1);
      Path path =
          file.contains("/") ? Path.of("assets", file) : Path.of("assets/rooms/morphCo/art", file);
      if (!Files.exists(path)) missing.add(file);
    }
    for (String picture :
        List.of("form", "warning", "noeat", "luggage", "summer", "safety", "clock", "marmot")) {
      if (!Files.exists(Path.of("assets/rooms/morphCo/art", "wall_" + picture + ".png"))) {
        missing.add("wall_" + picture + ".png");
      }
    }
    assertTrue(missing.isEmpty(), "Missing art: " + missing);
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
    assertEquals("MORPH_CO", level.get("design").asString());
  }
}
