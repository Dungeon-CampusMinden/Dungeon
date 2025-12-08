package mushRoom;

import core.Game;
import core.sound.SoundSpec;

/** Enum that lists available in-game sounds and convenient playback helpers. */
public enum Sounds {
  /** Sound for picking up a key item. */
  KEY_ITEM_PICKUP_SOUND("uisp_African4", 1.0f),

  /** General mushroom pickup sound. */
  MUSHROOM_PICKUP_SOUND("uisp_Abstract1", 0.8f),

  /** Sound for breaking stone. */
  BREAK_STONE_SOUND("uisp_African2", 0.7f),

  /** Sound for breaking a tree. */
  BREAK_TREE_SOUND("multiple_cracks_2", 0.8f),

  /** Sound when opening the inventory. */
  OPEN_INVENTORY_SOUND("uisp_African2", 0.5f),

  /** Sound for activating a pressure plate. */
  PRESSURE_PLATE_ACTIVATE_SOUND("uisp_WoodBlock3", 0.6f),

  /** Sound for deactivating a pressure plate. */
  PRESSURE_PLATE_DEACTIVATE_SOUND("uisp_WoodBlock2", 0.6f),

  /** Sound for opening a door. */
  DOOR_OPEN_SOUND("qubodup_DoorOpen", 0.5f),

  /** Sound for closing a door. */
  DOOR_CLOSE_SOUND("qubodup_DoorClose", 0.5f),

  /** NPC talk sound variant A. */
  NPC_TALK("ad_giant1", 0.2f),

  /** NPC talk sound variant B. */
  NPC_TALK_ALT("ad_giant2", 0.2f),

  /** NPC death sound. */
  NPC_DIE("ad_giant3", 0.5f),

  /** NPC success/celebration sound. */
  NPC_SUCCESS("ad_giant1", 0.5f),

  /** Sound for flipping a book page. */
  FLIP_BOOK_PAGE("flipping_through_book", 1.0f),

  /** Sound for activating the magic lens. */
  MAGIC_LENS_ACTIVATED("ad_spell", 0.2f),

  /** Ambient creaking noise for trees. */
  TREE_AMBIENT_CREAK("tree_creak", 0.2f),

  /** General animal ambient noise. */
  ANIMAL_AMBIENT("animal_noise", 0.2f),

  /** Wind ambient variant 1. */
  WIND_AMBIENT_1("wind_1", 0.5f),

  /** Wind ambient variant 2. */
  WIND_AMBIENT_2("wind_2", 0.5f),

  /** Wind ambient variant 3. */
  WIND_AMBIENT_3("wind_3", 0.5f),
  ;

  private final String soundName;
  private final float volume;

  /**
   * Create a Sounds enum element.
   *
   * @param soundName the identifier of the sound resource
   * @param volume the default playback volume for this sound
   */
  Sounds(String soundName, float volume) {
    this.soundName = soundName;
    this.volume = volume;
  }

  /**
   * Play a random sound from the provided list.
   *
   * @param sounds the candidate sounds to choose from
   */
  public static void random(Sounds... sounds) {
    int index = (int) (Math.random() * sounds.length);
    sounds[index].play();
  }

  /**
   * Play a random sound from the provided list with a pitch.
   *
   * @param pitch the playback pitch to apply
   * @param sounds the candidate sounds to choose from
   */
  public static void random(float pitch, Sounds... sounds) {
    int index = (int) (Math.random() * sounds.length);
    sounds[index].play(pitch);
  }

  /**
   * Play this sound with its default volume.
   *
   * @return an Optional holding the play handle if playback started
   */
  public long play() {
    return Game.audio().playGlobal(new SoundSpec.Builder(soundName).volume(volume));
  }

  /**
   * Play this sound with a custom pitch.
   *
   * @param pitch the playback pitch to apply
   * @return an Optional holding the play handle if playback started
   */
  public long play(float pitch) {
    return Game.audio().playGlobal(new SoundSpec.Builder(soundName).volume(volume).pitch(pitch));
  }
}
