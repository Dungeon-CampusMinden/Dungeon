package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.sound.SoundSpec;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import rooms.morphco.petrinet.MorphCoMilestone;

/**
 * The noises of the factory: every machine loops a sound that tells whether it is broken or
 * repaired, and a printer in the ventilation shaft dies once the alarm is off.
 *
 * <p>The loops are positional and grow quieter towards the edge of their range, so players mostly
 * hear the machines of their own hall and can tell where each machine stands. A sound played before
 * a client is connected never reaches it, so every player gets their own loops when the level
 * welcomes them, like the siren.
 */
final class MorphCoAmbience {

  /** A machine with its loops; {@code entity} is the instance name in the level file. */
  private enum Machine {
    FLEXION(
        "flexion-machine",
        MorphCoMilestone.F04_REPAIRED,
        10,
        "morphco_f04_squeak",
        0.35f,
        "morphco_f04_hum",
        0.3f),
    F04_BELT(
        "conveyor",
        MorphCoMilestone.F04_REPAIRED,
        8,
        "morphco_conveyor_jerky",
        0.2f,
        "morphco_conveyor_run",
        0.2f),
    AFFIX_SORTER(
        "affix-sorter",
        MorphCoMilestone.A12_REPAIRED,
        8,
        "morphco_a12_clatter",
        0.25f,
        "morphco_a12_plop",
        0.4f),
    A12_BELT(
        "a12-conveyor",
        MorphCoMilestone.A12_REPAIRED,
        6,
        "morphco_conveyor_jerky",
        0.15f,
        "morphco_conveyor_run",
        0.2f);

    private final String entity;
    private final MorphCoMilestone repaired;
    private final float range;
    private final String brokenSound;
    private final float brokenVolume;
    private final String repairedSound;
    private final float repairedVolume;

    Machine(
        String entity,
        MorphCoMilestone repaired,
        float range,
        String brokenSound,
        float brokenVolume,
        String repairedSound,
        float repairedVolume) {
      this.entity = entity;
      this.repaired = repaired;
      this.range = range;
      this.brokenSound = brokenSound;
      this.brokenVolume = brokenVolume;
      this.repairedSound = repairedSound;
      this.repairedVolume = repairedVolume;
    }
  }

  private record Loop(boolean repaired, long instance) {}

  private final MorphCoState state;
  private final Map<Integer, Map<Machine, Loop>> loops = new HashMap<>();

  MorphCoAmbience(MorphCoState state) {
    this.state = state;
  }

  // Starts the machine loops for a player who has just joined.
  void welcome(int playerId) {
    if (loops.putIfAbsent(playerId, new EnumMap<>(Machine.class)) == null) update();
  }

  // Reacts to progress: the printer dies when the alarm is off, and a repaired machine changes its
  // loop for every player.
  void completed(MorphCoMilestone milestone) {
    if (milestone == MorphCoMilestone.ALARM_OFF) {
      Game.audio().playGlobal(SoundSpec.builder("morphco_printer_dying").volume(0.3f));
    }
    update();
  }

  private void update() {
    Map<Machine, Entity> sources = new EnumMap<>(Machine.class);
    for (Machine machine : Machine.values()) {
      // Every machine is one entity, a belt too; its sound comes from the belt's left end.
      Game.levelEntities()
          .filter(entity -> machine.entity.equals(entity.name()))
          .findFirst()
          .ifPresent(entity -> sources.put(machine, entity));
    }
    loops.forEach((playerId, playing) -> {
      for (Map.Entry<Machine, Entity> entry : sources.entrySet()) {
        Machine machine = entry.getKey();
        boolean repaired = state.done(machine.repaired);
        Loop loop = playing.get(machine);
        if (loop != null && loop.repaired() == repaired) continue;
        if (loop != null) Game.audio().stopInstance(loop.instance());
        long instance =
            Game.audio()
                .playOnEntity(
                    entry.getValue(),
                    SoundSpec.builder(repaired ? machine.repairedSound : machine.brokenSound)
                        .volume(repaired ? machine.repairedVolume : machine.brokenVolume)
                        .looping(true)
                        .maxDistance(machine.range)
                        .targets(playerId));
        playing.put(machine, new Loop(repaired, instance));
      }
    });
  }
}
