package entities.utility;

import java.time.LocalDateTime;


public class HUDVariable implements Comparable<HUDVariable>{

  public LocalDateTime modtime;
  public String name;
  public int value;
  public int[] arrayValue;
  public String type;

  public HUDVariable(String name, int value) {
    modtime = LocalDateTime.now();
    this.name = name;
    this.value = value;
    this.type = "base";
  }

  public HUDVariable(String name, int[] value) {
    modtime = LocalDateTime.now();
    this.name = name;
    this.arrayValue = value;
    this.type = "array";
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

  public String getFormattedName(){
    if (this.name.length() > 9) {
      String substr = name.substring(0, 7);
      return substr + "...";
    }
    return name;
  }
}
