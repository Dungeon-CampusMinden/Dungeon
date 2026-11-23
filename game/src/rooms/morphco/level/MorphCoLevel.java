package rooms.morphco.level;

import com.badlogic.gdx.utils.TimeUtils;
import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
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
import feature.components.StartupAnswerComponent;
import feature.components.UIComponent;
import feature.credits.CreditsFeature;
import feature.entities.deco.Deco;
import feature.hud.UIUtils;
import feature.hud.dialogs.BlackFadeCutscene;
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
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.save.MorphCoSave;
import rooms.morphco.ui.HiddenHelpComponent;
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

  /** Answer to the startup question in the main menu: play with puzzle explanations. */
  public static final String HELP_ON = "on";

  /** Answer to the startup question in the main menu: play without puzzle explanations. */
  public static final String HELP_OFF = "off";

  private static final String LEVEL_NAME = "morph-co";
  private static final float ENTER_RADIUS = 1.1f;
  private static final float ALARM_VOLUME = 0.35f;
  // Behind the door of the control room the siren sounds about half as loud.
  private static final float ALARM_VOLUME_MUFFLED = 0.12f;
  // The siren changes its volume only half a tile past the entrance of the control room, so a
  // player standing in the doorway does not switch it back and forth.
  private static final float ALARM_MARGIN = 0.5f;

  private final MorphCoState state = new MorphCoState();
  private final MorphCoQuestLog questLog = new MorphCoQuestLog();
  private final MorphCoAmbience ambience = new MorphCoAmbience(state);
  private final Set<Integer> introduced = new HashSet<>();
  private final Set<Integer> playing = new HashSet<>();
  private final Set<Integer> present = new HashSet<>();
  private ControlRoom controlRoom;
  private ShiftLead shiftLead;
  private SnackQuest snackQuest;
  private boolean continued;
  private boolean ending;
  private final Map<Integer, Siren> alarmSounds = new HashMap<>();
  private final Map<Integer, Fade> fadingIn = new HashMap<>();
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
    checkpoint.ifPresent(save -> {
      MorphCoProgressNet.restore(save.milestones());
      state.restore(save.flags(), save.assembled(), save.leverPulls(), save.affixAttempts());
    });

    MorphCoObjects.reset();
    snackQuest = new SnackQuest(this);
    controlRoom = new ControlRoom(this);
    controlRoom.spawn();
    new FlexionLine(this).spawn();
    new AffixSorter(this).spawn();
    new CircumfixStation(this).spawn();
    shiftLead = new ShiftLead(this);
    shiftLead.spawn();
    new Corridor(this).spawn();
    snackQuest.spawn();
    MorphCoObjects.warnMissing();

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
    Conveyors.push();
    List<Entity> players = Game.allPlayers().toList();
    // A rejoining player gets back the same hero, so only presence shows that they left.
    Set<Integer> left = new HashSet<>(present);
    players.forEach(player -> left.remove(player.id()));
    left.forEach(this::leave);
    players.stream().filter(player -> !present.contains(player.id())).forEach(this::enter);
    present.clear();
    players.forEach(player -> present.add(player.id()));
    players.stream().filter(player -> introduced.add(player.id())).forEach(this::welcome);
    fadeInSounds();
    updateAlarmVolume();
    enterF04("f04_enter");
    enterF04("f04_enter_inner");
  }

  // The sounds of a player stop when they leave and start again when they rejoin with their hero.
  private void enter(Entity player) {
    CircumfixStation.returnToPile(player);
    if (!player.isPresent(IntroSeenComponent.class)) return;
    startAlarm(player);
    ambience.welcome(player.id(), soundFade(player.id()));
  }

  private void leave(int playerId) {
    Siren siren = alarmSounds.remove(playerId);
    if (siren != null) Game.audio().stopInstance(siren.instance());
    ambience.leave(playerId);
  }

  private void welcome(Entity player) {
    int id = player.id();
    MorphCoBooks.carry(this, player);
    applyHelpChoice(player, !continued);
    if (continued) {
      startSound(player, false);
      playReady(id);
      return;
    }
    SurveyFeature.show(SurveyFeature.Phase.PRE, () -> intro(player), id);
  }

  /**
   * Hides the puzzle explanations for a player who plays without them. A new game takes the answer
   * the player gave in the main menu and saves it; a continued game keeps the saved choice.
   *
   * @param player joining player
   * @param newGame whether this run started as a new game
   */
  private void applyHelpChoice(Entity player, boolean newGame) {
    String noHelp =
        MorphCoState.NO_HELP_PREFIX
            + player.fetch(PlayerComponent.class).orElseThrow().playerName();
    boolean helpOff =
        player
            .fetch(StartupAnswerComponent.class)
            .filter(answer -> HELP_OFF.equals(answer.answer()))
            .isPresent();
    if (newGame && helpOff && observe(noHelp)) save();
    if (state.has(noHelp)) player.add(new HiddenHelpComponent());
  }

  // Once the intro has faded out, the player hears the room.
  private void intro(Entity player) {
    BlackFadeCutscene.show(
        MorphCoText.introPages(),
        false,
        true,
        true,
        () -> {
          startSound(player, true);
          arrival(player);
        },
        player.id());
  }

  // The player's client plays the music once it sees the IntroSeenComponent; siren and machines
  // come from the server. A continued game starts them at once, after the intro they fade in.
  private void startSound(Entity player, boolean fadeIn) {
    player.add(new IntroSeenComponent());
    if (fadeIn) fadingIn.put(player.id(), new Fade(TimeUtils.millis(), 0f));
    startAlarm(player);
    ambience.welcome(player.id(), soundFade(player.id()));
  }

  // Volume factor of the player's siren and machines; 1 once their fade-in is over.
  private float soundFade(int playerId) {
    Fade fade = fadingIn.get(playerId);
    return fade == null ? 1f : fade.volume();
  }

  // Raises the siren and machines of fading players one step at a time.
  private void fadeInSounds() {
    Map.copyOf(fadingIn)
        .forEach((playerId, fade) -> {
          float volume = MorphCoMusic.fadeIn(fade.start());
          if (volume == fade.volume()) return;
          if (volume < 1f) fadingIn.put(playerId, new Fade(fade.start(), volume));
          else fadingIn.remove(playerId);
          Siren siren = alarmSounds.get(playerId);
          if (siren != null) {
            Game.audio().changeVolume(siren.instance(), alarmVolume(siren.muffled()) * volume);
          }
          ambience.fade(playerId, volume);
        });
  }

  private void arrival(Entity player) {
    int playerId = player.id();
    playReady(playerId);
    DialogFactory.showDialogDialog(
        MorphCoText.arrival(MorphCoText.playerImage(player)),
        () -> {
          observe(MorphCoState.ARRIVAL);
          DialogFactory.showDialogDialog(MorphCoText.controls(), shiftLead::showMood, playerId);
        },
        playerId);
  }

  private void playReady(int playerId) {
    if (!playing.add(playerId)) return;
    Game.playClock().playingParticipants(playing);
    Game.playClock().ready();
  }

  // The first player in F-04 starts the repair branch for the hints. Every player with explanations
  // reads the description of the hall once; a player without explanations only walks in.
  private void enterF04(String point) {
    Point center = getPoint(point);
    Game.allPlayers()
        .filter(player -> distance(player, center) < ENTER_RADIUS)
        .forEach(player -> {
          observe(MorphCoState.F04_ENTERED);
          String name = player.fetch(PlayerComponent.class).orElseThrow().playerName();
          if (!player.isPresent(HiddenHelpComponent.class)
              && state.set(MorphCoState.F04_SEEN_PREFIX + name)) {
            MorphCoProps.note(player, "world.f04.enter");
          }
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
    ambience.completed(milestone);
    if (milestone != MorphCoMilestone.PRODUCTION_RESTARTED) save();
    return true;
  }

  /**
   * Records an assembled circumfix, updates the quest log and writes a checkpoint.
   *
   * @param circumfix assembled circumfix
   * @return whether it was assembled for the first time
   */
  boolean assemble(CircumfixPuzzle.Circumfix circumfix) {
    if (!state.assemble(circumfix)) return false;
    questLog.sync(state);
    save();
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
   * Starts the looping siren for a player who has just joined, muffled if they are already in the
   * control room.
   *
   * <p>A sound played before a client is connected never reaches it, so every player gets their own
   * siren after their intro, and again when they rejoin.
   *
   * @param player joining player
   */
  private void startAlarm(Entity player) {
    if (state.done(MorphCoMilestone.ALARM_OFF) || alarmSounds.containsKey(player.id())) return;
    alarmSounds.put(player.id(), playAlarm(player.id(), muffled(player, false)));
  }

  private Siren playAlarm(int playerId, boolean muffled) {
    long instance =
        Game.audio()
            .playGlobal(
                SoundSpec.builder("system_recovery_alarm_siren")
                    .looping(true)
                    .volume(alarmVolume(muffled) * soundFade(playerId))
                    .targets(playerId));
    return new Siren(instance, muffled);
  }

  private static float alarmVolume(boolean muffled) {
    return muffled ? ALARM_VOLUME_MUFFLED : ALARM_VOLUME;
  }

  /**
   * Plays the siren quieter for players beyond the door of the control room and louder again in the
   * entrance. Each player keeps their one siren, only its volume changes.
   */
  private void updateAlarmVolume() {
    if (alarmSounds.isEmpty()) return;
    for (Entity player : Game.allPlayers().toList()) {
      Siren siren = alarmSounds.get(player.id());
      if (siren == null) continue;
      boolean muffled = muffled(player, siren.muffled());
      if (muffled == siren.muffled()) continue;
      Game.audio().changeVolume(siren.instance(), alarmVolume(muffled) * soundFade(player.id()));
      alarmSounds.put(player.id(), new Siren(siren.instance(), muffled));
    }
  }

  // Whether the player is in the control room, beyond its entrance. Up to ALARM_MARGIN around the
  // entrance the answer stays muffledNow.
  private boolean muffled(Entity player, boolean muffledNow) {
    float entrance = getPoint("control_enter").y();
    float threshold = muffledNow ? entrance - ALARM_MARGIN : entrance + ALARM_MARGIN;
    return player
        .fetch(PositionComponent.class)
        .map(position -> position.position().y() > threshold)
        .orElse(muffledNow);
  }

  /** Stops the sirens of all players. */
  void stopAlarm() {
    alarmSounds.values().forEach(siren -> Game.audio().stopInstance(siren.instance()));
    alarmSounds.clear();
  }

  private record Siren(long instance, boolean muffled) {}

  private record Fade(long start, float volume) {}

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

  /**
   * Returns the side quest around the snack machine, which the phone and the shift lead take part
   * in.
   *
   * @return side quest controller
   */
  SnackQuest snackQuest() {
    return snackQuest;
  }
}
