package org.hsbi.dungeon.intellijplugin;

import com.intellij.openapi.fileTypes.LanguageFileType;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

/** File Type registered to be part of the Dungeon DSl Language. */
public class DngDslFileType extends LanguageFileType {
  /** Singleton instance of the file type, necessary for intellij. */
  @SuppressWarnings("unused")
  public static final DngDslFileType INSTANCE = new DngDslFileType();

  private DngDslFileType() {
    super(DngDslLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Dungeon DSL File";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Dungeon domain specific language file";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "dng";
  }

  @Override
  public Icon getIcon() {
    return Icons.DungeonFileLogo;
  }
}
