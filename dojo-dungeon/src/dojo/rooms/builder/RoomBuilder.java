package dojo.rooms.builder;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.IPath;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import dojo.rooms.level_1.L1_R1_Monster_1;
import dojo.rooms.level_1.L1_R2_Fehler_Syntax;
import dojo.rooms.level_1.L1_R3_Fragen_Lambda;
import dojo.rooms.level_2.L2_R1_Monster_2;
import dojo.rooms.level_2.L2_R2_Monster_Implement_1;
import dojo.rooms.level_2.L2_R3_Fehler_Refactoring;
import dojo.rooms.level_3.L3_R1_Fragen_Schriftrollen;
import dojo.rooms.level_3.L3_R2_Fehler_Quader;
import dojo.rooms.level_3.L3_R3_Fragen_RegExes;
import dojo.rooms.level_4.L4_R1_Monster_3;
import dojo.rooms.level_4.L4_R2_Fragen_Pattern;
import dojo.rooms.level_4.L4_R3_Monster_Implement_2;
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

  public Room buildRoom_L1_R1_Monster_1() {
    return new L1_R1_Monster_1(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom_L1_R2_Fehler_Syntax() {
    return new L1_R2_Fehler_Syntax(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom_L1_R3_Fragen_Lambda() {
    return new L1_R3_Fragen_Lambda(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom_L2_R1_Monster_2() {
    return new L2_R1_Monster_2(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom_L2_R2_Monster_Implement_1() {
    return new L2_R2_Monster_Implement_1(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom_L2_R3_Fehler_Refactoring() {
    return new L2_R3_Fehler_Refactoring(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom_L3_R1_Fragen_Schriftrollen() {
    return new L3_R1_Fragen_Schriftrollen(
        levelRoom, gen, nextRoom, levelSize, designLabel, monsterCount, monsterPaths, sortables);
  }

  public Room buildRoom_L3_R2_Fehler_Quader() {
    return new L3_R2_Fehler_Quader(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom_L3_R3_Fragen_RegExes() {
    return new L3_R3_Fragen_RegExes(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom_L4_R1_Monster_3() {
    return new L4_R1_Monster_3(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom_L4_R2_Fragen_Pattern() {
    return new L4_R2_Fragen_Pattern(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  public Room buildRoom_L4_R3_Monster_Implement_2() {
    return new L4_R3_Monster_Implement_2(levelRoom, gen, nextRoom, levelSize, designLabel);
  }
}
