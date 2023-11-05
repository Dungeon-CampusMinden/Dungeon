package dsltypeadapters;

import contrib.components.AIComponent;
import contrib.entities.AIFactory;

import core.Entity;
import core.components.DrawComponent;

import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLTypeAdapter;

public class AIComponentAdapter {
    /**
     * Buildermethod for creating a new {@link DrawComponent} from a path, pointing to animations.
     *
     * @return the created {@link DrawComponent}
     */
    @DSLTypeAdapter(name = "ai_component")
    public static AIComponent buildAIComponent(@DSLContextMember(name = "entity") Entity entity) {
        var comp =
                new AIComponent(
                        AIFactory.generateRandomFightAI(),
                        AIFactory.generateRandomIdleAI(),
                        AIFactory.generateRandomTransitionAI(entity));
        return comp;
    }
}
