package saving;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.ClassNotFoundException;
import java.util.Optional;

public class Saves {

    @SuppressWarnings("unchecked")
    private Optional<GameData>[] saves = (Optional<GameData>[]) new Optional<?>[8];
    private Optional<GameData> autoSave;

    public boolean save() {
        try {
            for (int i = 0; i < saves.length; i++) {
                if (saves[i] != null)
                    GameData.save(saves[i].get(), "saves/" + 1 + i + ".txt");
            }
            GameData.save(autoSave.get(), "saves/autosave.txt");
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void load() {
        for (int i = 0; i < saves.length; i++) {
            try {
                saves[i] = GameData.load("saves" + 1 + i + ".txt");
            } catch (IOException | ClassNotFoundException e) {
                // TODO: handle exception
            }
        }
        try {
            autoSave = GameData.load("saves/autosave.txt");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public Optional<GameData> getAutoSave() {
        return autoSave;
    }

    public Optional<GameData>[] getSaves() {
        return saves;
    }

    public void setAutoSave(Optional<GameData> autoSave) {
        this.autoSave = autoSave;
    }

    public void setSaves(Optional<GameData>[] saves) {
        this.saves = saves;
    }

    public void deleteAutoSave() {
        try {
            Files.deleteIfExists(Paths.get("saves/autosave.txt"));
        } catch (Exception e) {
            //
        }
    }

}
