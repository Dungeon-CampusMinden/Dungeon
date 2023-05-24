package ecs.entities;

import com.badlogic.gdx.Gdx;
import configuration.KeyboardConfig;
import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.IInteraction;
import ecs.components.InteractionComponent;
import ecs.components.PositionComponent;
import ecs.components.quests.KillQuest;
import ecs.components.quests.Quest;
import ecs.components.quests.QuestBuilder;
import ecs.components.quests.QuestComponent;
import graphic.Animation;
import starter.Game;
import tools.Constants;

/**
 * QuestButton, to set up the Quest in the level.
 */
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

    /**
     * InteractionComponent to show the Quest, to accept the Quest or to remove the
     * Quest.
     */
    private void setupInteractionComponent() {
        new InteractionComponent(this, 1, true, new IInteraction() {

            private boolean first = true;
            private boolean enabled = true;

            @Override
            public void onInteraction(Entity entity) {
                held++;
                if (first && Gdx.input.isKeyPressed(KeyboardConfig.INTERACT_WORLD.get())) {
                    quest = QuestBuilder.buildRandomQuest(Game.getHero().get());
                    System.out.println(quest.getName() + ":\n" + quest.getDescription());
                    Game.questMenu
                            .display(quest.getName() + ":\n" + quest.getDescription(), 3);
                    first = !first;
                    held = 0;
                } else if (enabled && held >= HOLD && Gdx.input.isKeyPressed(KeyboardConfig.INTERACT_WORLD.get())) {
                    if (Game.getHero().get().getComponent(QuestComponent.class).isPresent()) {
                        ((QuestComponent) Game.getHero().get().getComponent(QuestComponent.class).get())
                                .addQuest(quest);
                    }
                    animation.setCurrentAnimation(AnimationBuilder.buildAnimation(pathToTriggered));
                    enabled = !enabled;
                    Game.questMenu
                            .display(quest.getName() + ":\nWas added to your Questlog.\nPress M to see your Questlog.",
                                    3);
                } else if (!first && enabled && (held >= HOLD)
                        && Gdx.input.isKeyPressed(KeyboardConfig.INTERACT_WORLD_X.get())) {
                    enabled = !enabled;
                    if (quest.getName().equals("Dungeon Master"))
                        KillQuest.bossQuestExists = false;
                }
            }
        });
    }

    private void setupHitboxComponent() {
        new HitboxComponent(this);
    }

}
