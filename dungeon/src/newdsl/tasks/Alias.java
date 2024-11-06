package newdsl.tasks;

import java.util.Optional;

public class Alias {
    private Optional<String> id;
    private Optional<String> text;

    public Alias(Optional<String> id, Optional<String> text) {
        this.id = id;
        this.text = text;
    }

    public Optional<String> getId() {
        return id;
    }

    public void setId(Optional<String> id) {
        this.id = id;
    }

    public Optional<String> getText() {
        return text;
    }

    public void setText(Optional<String> text) {
        this.text = text;
    }
}
