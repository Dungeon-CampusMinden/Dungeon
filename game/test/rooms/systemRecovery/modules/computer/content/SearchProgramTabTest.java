package rooms.systemRecovery.modules.computer.content;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SearchProgramTabTest {

  @Test
  void buildsTheFixedSearchBodyAroundTheSingleInnerLoopInput() {
    assertEquals(
        "for (int row = 0; row < map.length; row++) {\n"
            + "    for (int column = 0; column < map[row].length; column++) {\n"
            + "        if (map[row][column] == 1) {\n"
            + "            roboter.collect();\n"
            + "        }\n"
            + "    }\n"
            + "}",
        SearchProgramTab.buildSource("map[row].length"));
  }
}
