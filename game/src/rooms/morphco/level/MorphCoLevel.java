package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.rendering.Atmosphere;
import engine.rendering.Light;
import engine.sound.SoundSpec;
import engine.time.PlayClock;
import engine.utils.Point;
import engine.utils.Tuple;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.UIComponent;
import feature.credits.CreditsFeature;
import feature.entities.deco.Deco;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogFactory;
import feature.survey.SurveyFeature;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import rooms.morphco.MorphCo;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.petrinet.MorphCoProgressNet;
import rooms.morphco.save.MorphCoSave;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * Authoritative level of the Morph & Co. word factory.
 *
 * <p>The level owns the room state, the quest log and one controller per area: the control room,
 * the inflection line F-04, the affix sorter A-12, the circumfix station and the shift lead. Every
 * completed milestone updates the quest log and writes a checkpoint.
 */
public class MorphCoLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "morph-co";
  private static final float ENTER_RADIUS = 1.1f;

  private final MorphCoState state = new MorphCoState();
  private final MorphCoQuestLog questLog = new MorphCoQuestLog();
  private final Set<Integer> introduced = new HashSet<>();
  private final Set<Integer> playing = new HashSet<>();
  private ControlRoom controlRoom;
  private boolean continued;
  private boolean ending;
  private final Map<Integer, Long> alarmSounds = new HashMap<>();
  private MorphCoAutoplay autoplay;

  /**
   * Creates the level from the level file.
   *
   * @param layout tile layout
   * @param designLabel tile design
   * @param namedPoints named points of the level file
   * @param decorations static decorations of the level file
   */
  public MorphCoLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }

  @Override
  public Atmosphere atmosphere() {
    return MorphCoLighting.atmosphere();
  }

  @Override
  public void lights(List<Light> out) {
    MorphCoLighting.lights(layout(), out);
  }

  @Override
  protected void onFirstTick() {
    Game.playClock().playingParticipants(Set.of());
    ending = false;
    MorphCoProgressNet.reset();
    MorphCoProgressNet.initialize();
    questLog.initialize();

    Optional<MorphCoSave.SaveData> checkpoint = MorphCo.checkpoint();
    continued = checkpoint.isPresent();
    checkpoint.ifPresent(
        save -> {
          MorphCoProgressNet.restore(save.milestones());
          state.restore(save.flags(), save.assembled(), save.leverPulls(), save.affixAttempts());
        });

    MorphCoObjects.reset();
    controlRoom = new ControlRoom(this);
    controlRoom.spawn();
    new FlexionLine(this).spawn();
    new AffixSorter(this).spawn();
    new CircumfixStation(this).spawn();
    new ShiftLead(this).spawn();
    new Corridor(this).spawn();

    questLog.sync(state);
    if (!state.done(MorphCoMilestone.ALARM_OFF)) {
      MorphCoTracking.started(MorphCoTracking.Puzzle.ALARM);
    }
    autoplay = MorphCo.autoplay() ? new MorphCoAutoplay(this) : null;
  }

  @Override
  protected void onTick() {
    if (autoplay != null) autoplay.tick();
    if (ending) return;
    Game.allPlayers().filter(player -> introduced.add(player.id())).forEach(this::welcome);
    observeArea(
        "control_enter",
        MorphCoState.CONTROL_ENTERED,
        "world.titles.control",
        "world.control.enter");
    observeArea("f04_enter", MorphCoState.F04_ENTERED, "world.titles.f04", "world.f04.enter");
    observeArea("f04_enter_inner", MorphCoState.F04_ENTERED, "world.titles.f04", "world.f04.enter");
  }

  private void welcome(Entity player) {
    int id = player.id();
    startAlarm(id);
    if (continued) {
      playReady(id);
      return;
    }
    SurveyFeature.show(
        SurveyFeature.Phase.PRE,
        () ->
            BlackFadeCutscene.show(
                MorphCoText.introPages(), false, true, true, () -> arrival(player), id),
        id);
  }

  private void arrival(Entity player) {
    int playerId = player.id();
    playReady(playerId);
    DialogFactory.showDialogDialog(
        MorphCoText.arrival(MorphCoText.playerImage(player)),
        () -> {
          observe(MorphCoState.ARRIVAL);
          DialogFactory.showDialogDialog(MorphCoText.controls(), () -> {}, playerId);
        },
        playerId);
  }

  private void playReady(int playerId) {
    if (!playing.add(playerId)) return;
    Game.playClock().playingParticipants(playing);
    Game.playClock().ready();
  }

  private void observeArea(String point, String flag, String titleKey, String textKey) {
    if (state.has(flag)) return;
    Point center = getPoint(point);
    Game.allPlayers()
        .filter(player -> distance(player, center) < ENTER_RADIUS)
        .findFirst()
        .ifPresent(
            player -> {
              observe(flag);
              MorphCoProps.popup(player, titleKey, textKey);
            });
  }

  private static double distance(Entity entity, Point point) {
    Point target = point.translate(0.5f, 0.5f);
    return entity
        .fetch(PositionComponent.class)
        .map(position -> position.position().distance(target))
        .orElse(Double.MAX_VALUE);
  }

  /**
   * Returns the room state.
   *
   * @return mutable room state
   */
  MorphCoState state() {
    return state;
  }

  /**
   * Returns a named point of the level file.
   *
   * @param name point name
   * @return the point
   */
  Point point(String name) {
    return getPoint(name);
  }

  /**
   * Returns the door tile at a named point.
   *
   * @param name point name
   * @return the door
   */
  DoorTile door(String name) {
    return tileAt(getPoint(name))
        .filter(DoorTile.class::isInstance)
        .map(DoorTile.class::cast)
        .orElseThrow(() -> new IllegalStateException("No door at point " + name));
  }

  /**
   * Records an observation and updates the quest log.
   *
   * @param flag observation flag
   * @return whether the flag was newly set
   */
  boolean observe(String flag) {
    if (!state.set(flag)) return false;
    questLog.sync(state);
    return true;
  }

  /**
   * Records an inspected collectible object and unlocks its achievement once all have been seen.
   *
   * @param prefix flag prefix, see {@link MorphCoState#PLANT_PREFIX}
   * @param id object identifier
   * @param player acting player
   */
  void seen(String prefix, String id, Entity player) {
    if (!observe(prefix + id)) return;
    long count = state.count(prefix);
    if (MorphCoState.PLANT_PREFIX.equals(prefix) && count == MorphCoState.PLANTS) {
      MorphCoAchievements.PLANTS.unlock(player);
    } else if (MorphCoState.POSTER_PREFIX.equals(prefix) && count == MorphCoState.POSTERS) {
      MorphCoAchievements.POSTERS.unlock(player);
    } else if (MorphCoState.BOOK_PREFIX.equals(prefix) && count == MorphCoState.BOOKS) {
      MorphCoAchievements.BOOKWORM.unlock(player);
    }
  }

  /**
   * Completes a milestone, updates the quest log and writes a checkpoint.
   *
   * @param milestone milestone to complete
   * @return whether the milestone was completed by this call
   */
  boolean complete(MorphCoMilestone milestone) {
    if (!MorphCoProgressNet.complete(milestone)) return false;
    questLog.sync(state);
    if (milestone != MorphCoMilestone.PRODUCTION_RESTARTED) save();
    return true;
  }

  /** Writes a checkpoint with the current state. */
  void save() {
    if (Game.isMultiplayerClient()) return;
    MorphCo.saveCheckpoint(
        MorphCoProgressNet.completedMilestones(),
        state.flags(),
        state.assembled(),
        state.leverPulls(),
        state.affixAttempts());
  }

  /**
   * Starts the looping siren for a player who has just joined.
   *
   * <p>A sound played before a client is connected never reaches it, so every player gets an own
   * siren when the level welcomes them.
   *
   * @param playerId joining player
   */
  private void startAlarm(int playerId) {
    if (state.done(MorphCoMilestone.ALARM_OFF) || alarmSounds.containsKey(playerId)) return;
    alarmSounds.put(
        playerId,
        Game.audio()
            .playGlobal(
                SoundSpec.builder("system_recovery_alarm_siren")
                    .looping(true)
                    .volume(0.35f)
                    .targets(playerId)));
  }

  /** Stops the sirens of all players. */
  void stopAlarm() {
    alarmSounds.values().forEach(Game.audio()::stopInstance);
    alarmSounds.clear();
  }

  /**
   * Restarts the production and plays the ending for all players.
   *
   * @param player player who answered the security question
   */
  void finish(Entity player) {
    if (ending || !complete(MorphCoMilestone.PRODUCTION_RESTARTED)) return;
    ending = true;
    MorphCoSave.delete();
    MorphCoTracking.solved(MorphCoTracking.Puzzle.RESTART);
    MorphCoAchievements.RESTART.unlock();
    Game.playClock().end(PlayClock.Outcome.SUCCESS);
    Game.audio().playGlobal(SoundSpec.builder("system_recovery_complete"));
    Game.levelEntities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .toList()
        .forEach(ui -> UIUtils.closeDialog(ui, true));
    int[] players = Game.allPlayers().mapToInt(Entity::id).toArray();
    BlackFadeCutscene.show(
        MorphCoText.endingPages(),
        true,
        false,
        true,
        () ->
            CreditsFeature.showAfterGame(
                MorphCo.CREDITS_ROOM_ID,
                () -> SurveyFeature.show(SurveyFeature.Phase.POST, Game::complete)),
        players);
  }

  /**
   * Returns the control room, which owns the production control system.
   *
   * @return control room controller
   */
  ControlRoom controlRoom() {
    return controlRoom;
  }
}
