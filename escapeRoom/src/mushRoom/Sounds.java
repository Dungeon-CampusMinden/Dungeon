package mushRoom;

import core.Game;

public enum Sounds {

  KEY_ITEM_PICKUP_SOUND("uisp_African4", 1.0f),
  MUSHROOM_PICKUP_SOUND("uisp_Abstract1", 0.5f),
  BREAK_STONE_SOUND("uisp_African2", 0.7f),
  BREAK_TREE_SOUND("multiple_cracks_2", 0.8f),
  OPEN_INVENTORY_SOUND("uisp_African2", 0.5f),
  PRESSURE_PLATE_ACTIVATE_SOUND("uisp_WoodBlock3", 0.6f),
  PRESSURE_PLATE_DEACTIVATE_SOUND("uisp_WoodBlock2", 0.6f),
  DOOR_OPEN_SOUND("qubodup_DoorOpen", 0.5f),
  DOOR_CLOSE_SOUND("qubodup_DoorClose", 0.5f),
  DIALOG_SOUND("uisp_Coffee1", 0.5f),
  NPC_DIE("uisp_Coffee1", 0.5f),
  NPC_SUCCESS("uisp_Coffee1", 0.5f),

  TREE_AMBIENT_CREAK("tree_creak", 0.2f),
  ANIMAL_AMBIENT("animal_noise", 0.2f),
  WIND_AMBIENT_1("wind_1", 0.5f),
  WIND_AMBIENT_2("wind_2", 0.5f),
  WIND_AMBIENT_3("wind_3", 0.5f),
  ;

  private final String soundName;
  private final float volume;

  Sounds(String soundName, float volume) {
    this.soundName = soundName;
    this.volume = volume;
  }

  public void play() {
    Game.soundPlayer().play(soundName, volume);
  }

}
