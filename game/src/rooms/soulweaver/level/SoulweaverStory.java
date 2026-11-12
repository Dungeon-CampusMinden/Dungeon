package rooms.soulweaver.level;

import engine.utils.Tuple;
import java.util.List;
import rooms.soulweaver.util.SoulweaverText;

/** Workshop records explain Nox's work and the blocked exit without spoiling the accident. */
final class SoulweaverStory {
  private SoulweaverStory() {}

  static List<Tuple<String, Integer>> intro() {
    return List.of(Tuple.of(SoulweaverText.key("story.intro"), 30));
  }

  static List<Tuple<String, Integer>> ending() {
    return List.of(
        Tuple.of(SoulweaverText.key("story.ending.heartfire"), 30),
        Tuple.of(SoulweaverText.key("story.ending.done"), 30));
  }

  static String letter() {
    return SoulweaverText.key("story.letter");
  }

  static String archive() {
    return SoulweaverText.key("story.archive");
  }

  static String maintenance() {
    return SoulweaverText.key("story.maintenance");
  }

  static String workshop() {
    return SoulweaverText.key("story.workshop");
  }

  static String workshopJournal() {
    return SoulweaverText.key("story.workshop-journal");
  }
}
