package dojo.rooms.builder;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.IPath;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import dojo.rooms.rooms.boss.RefactoringRoom;
import dojo.rooms.rooms.boss.Fragen_Lambda;
import dojo.rooms.rooms.boss.Fragen_RegExes;
import dojo.rooms.rooms.boss.MyImpRoom;
import dojo.rooms.rooms.riddle.QuaderRoom;
import dojo.rooms.rooms.riddle.SyntaxRoom;
import dojo.rooms.rooms.riddle.Fragen_Pattern;
import dojo.rooms.rooms.riddle.MyMonsterRoom;
import dojo.rooms.rooms.search.Fragen_Schriftrollen;
import dojo.rooms.rooms.search.Key;
import dojo.rooms.rooms.search.Monster_Kill;
import dojo.rooms.rooms.search.Saphire;
import java.util.ArrayList;
import java.util.HashMap;

/** The builder for the rooms. */
public class RoomBuilder {
  private LevelRoom levelRoom;
  private RoomGenerator gen;
  private Room nextRoom;
  private LevelSize levelSize;
  private DesignLabel designLabel;

  private int monsterCount;
  private IPath[] monsterPaths;

  private HashMap<String, ArrayList<String>> sortables;

  /**
   * Set the level room.
   *
   * @param levelRoom the level room
   * @return this builder object
   */
  public RoomBuilder levelRoom(LevelRoom levelRoom) {
    this.levelRoom = levelRoom;
    return this;
  }

  /**
   * Set the room generator.
   *
   * @param gen the room generator
   * @return this builder object
   */
  public RoomBuilder roomGenerator(RoomGenerator gen) {
    this.gen = gen;
    return this;
  }

  /**
   * Set the next room.
   *
   * @param nextRoom the next room.
   * @return this builder object
   */
  public RoomBuilder nextRoom(Room nextRoom) {
    this.nextRoom = nextRoom;
    return this;
  }

  /**
   * Set the level size.
   *
   * @param levelSize the size of the level
   * @return this builder object
   */
  public RoomBuilder levelSize(LevelSize levelSize) {
    this.levelSize = levelSize;
    return this;
  }

  /**
   * Set the design label.
   *
   * @param designLabel the design label
   * @return this builder object
   */
  public RoomBuilder designLabel(DesignLabel designLabel) {
    this.designLabel = designLabel;
    return this;
  }

  /**
   * Set the monster count.
   *
   * @param monsterCount the number of monsters
   * @return this builder object
   */
  public RoomBuilder monsterCount(int monsterCount) {
    this.monsterCount = monsterCount;
    return this;
  }

  /**
   * Set the monster paths.
   *
   * @param monsterPaths the monster paths
   * @return this builder object
   */
  public RoomBuilder monsterPaths(IPath[] monsterPaths) {
    this.monsterPaths = monsterPaths;
    return this;
  }

  /**
   * Set the sortables.
   *
   * @param sortables the sortables
   * @return this builder object
   */
  public RoomBuilder sortables(HashMap<String, ArrayList<String>> sortables) {
    this.sortables = sortables;
    return this;
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Key() {
    return new Key(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Fehler_Syntax() {
    return new SyntaxRoom(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Fragen_Lambda() {
    return new Fragen_Lambda(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Saphire() {
    return new Saphire(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Implement_MyMonster() {
    return new MyMonsterRoom(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Fehler_Refactoring() {
    return new RefactoringRoom(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Fragen_Schriftrollen() {
    return new Fragen_Schriftrollen(
        levelRoom, gen, nextRoom, levelSize, designLabel, monsterCount, monsterPaths, sortables);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Fehler_Quader() {
    return new QuaderRoom(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Fragen_RegExes() {
    return new Fragen_RegExes(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Monster_Kill() {
    return new Monster_Kill(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_Fragen_Pattern() {
    return new Fragen_Pattern(levelRoom, gen, nextRoom, levelSize, designLabel);
  }

  /**
   * Generate the room.
   *
   * @return the newly generated room instance
   */
  public Room buildRoom_MyImpRoom() {
    return new MyImpRoom(levelRoom, gen, nextRoom, levelSize, designLabel);
  }
}
