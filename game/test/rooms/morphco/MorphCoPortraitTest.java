package rooms.morphco;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import engine.Entity;
import feature.components.CharacterClassComponent;
import feature.entities.CharacterClass;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import rooms.morphco.util.MorphCoText;

/** Checks which portrait the arrival dialog shows for the chosen character. */
class MorphCoPortraitTest {

  private static final Pattern SPEAKER_IMAGE = Pattern.compile("\\[speaker img=([^ ]+) ");

  private static Entity player(CharacterClass characterClass) {
    Entity player = new Entity();
    player.add(new CharacterClassComponent(characterClass));
    return player;
  }

  @Test
  void everyCharacterGetsItsPortrait() {
    assertEquals(
        "rooms/morphCo/technician_portrait.png",
        MorphCoText.playerImage(player(CharacterClass.TECHNICIAN)));
    assertEquals(
        "rooms/morphCo/rogue_portrait.png",
        MorphCoText.playerImage(player(CharacterClass.THE_LAST_HOUR_ROGUE)));
    assertEquals(
        "rooms/morphCo/player_portrait.png",
        MorphCoText.playerImage(player(CharacterClass.THE_LAST_HOUR_CHAR03)));
    assertEquals("rooms/morphCo/player_portrait.png", MorphCoText.playerImage(new Entity()));
  }

  @Test
  void everyPortraitExists() {
    List<String> portraits =
        List.of(
            MorphCoText.SHIFT_LEAD_IMAGE,
            MorphCoText.playerImage(player(CharacterClass.TECHNICIAN)),
            MorphCoText.playerImage(player(CharacterClass.THE_LAST_HOUR_ROGUE)),
            MorphCoText.playerImage(player(CharacterClass.THE_LAST_HOUR_CHAR03)),
            MorphCoText.playerImage(new Entity()));
    for (String portrait : portraits) {
      assertNotNull(getClass().getClassLoader().getResource(portrait), portrait);
    }
  }

  @Test
  void arrivalShowsTheGivenPlayerPortrait() {
    String portrait = MorphCoText.playerImage(player(CharacterClass.TECHNICIAN));
    List<String> images =
        SPEAKER_IMAGE
            .matcher(MorphCoText.arrival(portrait))
            .results()
            .map(m -> m.group(1))
            .toList();
    assertEquals(
        List.of(MorphCoText.SHIFT_LEAD_IMAGE, portrait, MorphCoText.SHIFT_LEAD_IMAGE, portrait),
        images);
  }
}
