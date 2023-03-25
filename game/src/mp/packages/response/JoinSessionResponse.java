package mp.packages.response;

import level.elements.ILevel;

public class JoinSessionResponse {

    private ILevel level;

    public JoinSessionResponse(ILevel level) {
        this.level = level;
    }

    public ILevel getLevel() {
        return level;
    }
}
