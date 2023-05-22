package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.IInteraction;
import ecs.components.InteractionComponent;
import ecs.components.PositionComponent;
import ecs.components.quests.Quest;
import ecs.components.quests.QuestBuilder;
import ecs.components.quests.QuestComponent;
import graphic.Animation;
import starter.Game;
import tools.Constants;

public class QuestButton extends Entity {

    private final String pathToIdle = "QuestButton/idle";
    private final String pathToTriggered = "QuestButton/triggered";
    private AnimationComponent animation;
    private Quest quest;
    private static final int HOLD = Constants.FRAME_RATE;
    private int held = 0;

    public QuestButton() {
        super();
        new PositionComponent(this);
        setupAnimationComponent();
        setupHitboxComponent();
        setupInteractionComponent();
    }

    private void setupAnimationComponent() {
        Animation idle = AnimationBuilder.buildAnimation(pathToIdle);
        animation = new AnimationComponent(this, idle);
    }

    private void setupInteractionComponent() {
        new InteractionComponent(this, 1, true, new IInteraction() {

            private boolean first = true;
            private boolean enabled = true;

            @Override
            public void onInteraction(Entity entity) {
                held++;
                if (first) {
                    quest = QuestBuilder.buildRandomQuest(Game.getHero().get());
                    System.out.println(quest.getName() + ":\n" + quest.getDescription());
                    first = !first;
                } else if (enabled && held >= HOLD) {
                    if (Game.getHero().get().getComponent(QuestComponent.class).isPresent()) {
                        ((QuestComponent) Game.getHero().get().getComponent(QuestComponent.class).get())
                                .addQuest(quest);
                    }
                    animation.setCurrentAnimation(AnimationBuilder.buildAnimation(pathToTriggered));
                    enabled = !enabled;
                    System.out.println(quest.getName() + "Was added to your Questlog. Press M to see your Questlog.");
                }
            }

        });
    }

    private void setupHitboxComponent() {
        new HitboxComponent(this);
    }

}
