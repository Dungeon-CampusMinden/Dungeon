package entities.utility;

import com.badlogic.gdx.graphics.Texture;
import java.time.LocalDateTime;

/**
 * This class holds all important values of variable that should be displayed in the HUD.
 */
public class HUDVariable implements Comparable<HUDVariable> {

  /**
   * Latest modification time of this object.
   */
  public LocalDateTime modtime;
  /**
   * Name of the variable.
   */
  public String name;
  /**
   * Int value of this variable.
   */
  public int value;
  /**
   * Int array value of this variable.
   */
  public int[] arrayValue;
  /**
   * Type of this array. Can either be base (int) or array (int[]).
   */
  public String type;
  /**
   * Texture of the monster that will be used to visualize this variable in the HUD.
   */
  public Texture monsterTexture;

  /**
   * Constructor for base variables (int). Sets the modtime to the current timestamp. Sets the type to "base".
   *
   * @param name Name of the variable.
   * @param value Value of the variable.
   */
  public HUDVariable(String name, int value) {
    modtime = LocalDateTime.now();
    this.name = name;
    this.value = value;
    this.type = "base";
  }

  /**
   * Constructor for array variables (int[]). Sets the modtime to the current timestamp. Sets the type to "array".
   *
   * @param name Name of the variable.
   * @param value Value of the variable.
   */
  public HUDVariable(String name, int[] value) {
    modtime = LocalDateTime.now();
    this.name = name;
    this.arrayValue = value;
    this.type = "array";
  }

  /**
   * Set the texture for the monster that will be used to display the variable in the HUD.
   *
   * @param monsterTexture Texture of the monster
   */
  public void setMonsterTexture(Texture monsterTexture) {
    this.monsterTexture = monsterTexture;
  }

  /**
   * Update the int value of a variable.
   *
   * @param value New int value.
   */
  public void updateVariable(int value) {
    modtime = LocalDateTime.now();
    this.value = value;
  }

  @Override
  public int compareTo(HUDVariable o) {
    if (o.name.equals(this.name)) {
      return 0;
    }
    if (o.modtime.isEqual(this.modtime)) {
      return o.name.compareTo(this.name);
    } else if (o.modtime.isAfter(this.modtime)) {
      return 1;
    } else {
      return -1;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj.getClass() != this.getClass()) {
      return false;
    }
    final HUDVariable other = (HUDVariable) obj;
    return other.name.equals(this.name);
  }

  /**
   * Get the formatted name of the variable. The variable name will be shortened if it is longer than 9 chars.
   * The variable name will be cut at 7 chars and add three dots at the end.
   *
   * @return Returns the formatted name for the HUD.
   */
  public String getFormattedName() {
    if (this.name.length() > 9) {
      String substr = name.substring(0, 7);
      return substr + "...";
    }
    return name;
  }
}
