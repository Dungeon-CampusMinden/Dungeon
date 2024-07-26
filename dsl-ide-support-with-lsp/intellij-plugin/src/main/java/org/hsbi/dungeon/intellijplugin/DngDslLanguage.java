package org.hsbi.dungeon.intellijplugin;

import com.intellij.lang.Language;

/** Dungeon Language registered for intellij. */
public class DngDslLanguage extends Language {

  /** Singleton instance of the language, necessary for intellij. */
  public static final DngDslLanguage INSTANCE = new DngDslLanguage();

  private DngDslLanguage() {
    super("DungeonDsl");
  }
}
