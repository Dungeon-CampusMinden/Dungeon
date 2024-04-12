package dojo.rooms.builder;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.IPath;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import dojo.rooms.level_1.Room1;
import dojo.rooms.level_1.Room11;
import dojo.rooms.level_1.Room2;
import dojo.rooms.level_2.Room4;
import dojo.rooms.level_2.Room5;
import dojo.rooms.level_2.Room6;
import dojo.rooms.level_3.Room3;
import dojo.rooms.level_3.Room7;
import dojo.rooms.level_3.Room8;
import dojo.rooms.level_4.Room10;
import dojo.rooms.level_4.Room12;
import dojo.rooms.level_4.Room9;
import java.util.ArrayList;
import java.util.HashMap;

public class RoomBuilder {
  private LevelRoom levelRoom;
  private RoomGenerator gen;
  private Room nextRoom;
  private LevelSize levelSize;
  private DesignLabel designLabel;

  private int monsterCount;
  private IPath[] monsterPaths;

  private HashMap<String, ArrayList<String>> sortables;

  public RoomBuilder() {}

  public RoomBuilder levelRoom(LevelRoom levelRoom) {
    this.levelRoom = levelRoom;
    return this;
  }

  public RoomBuilder roomGenerator(RoomGenerator gen) {
    this.gen = gen;
    return this;
  }

  public RoomBuilder nextRoom(Room nextRoom) {
    this.nextRoom = nextRoom;
    return this;
  }

  public RoomBuilder levelSize(LevelSize levelSize) {
    this.levelSize = levelSize;
    return this;
  }

  public RoomBuilder designLabel(DesignLabel designLabel) {
    this.designLabel = designLabel;
    return this;
  }

  public RoomBuilder monsterCount(int monsterCount) {
    this.monsterCount = monsterCount;
    return this;
  }

  public RoomBuilder monsterPaths(IPath[] monsterPaths) {
    this.monsterPaths = monsterPaths;
    return this;
  }

  public RoomBuilder sortables(HashMap<String, ArrayList<String>> sortables) {
    this.sortables = sortables;
    return this;
  }

  public Room buildRoom1() {
    return new Room1(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom2() {
    return new Room2(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom3() {
    return new Room3(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom4() {
    return new Room4(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom5() {
    return new Room5(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom6() {
    return new Room6(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom7() {
    return new Room7(
        levelRoom, gen, nextRoom, levelSize, designLabel, monsterCount, monsterPaths, sortables);
  }

  public Room buildRoom8() {
    return new Room8(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom9() {
    return new Room9(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom10() {
    return new Room10(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom11() {
    return new Room11(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom12() {
    return new Room12(levelRoom, gen, nextRoom, levelSize, designLabel);
  }
}
