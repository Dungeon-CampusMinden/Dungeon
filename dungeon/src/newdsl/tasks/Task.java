package newdsl.tasks;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import core.Entity;
import newdsl.ast.ASTNodes;
import newdsl.foreigncode.LaTeXHandler;
import newdsl.graph.petrinet.Place;
import newdsl.interpreter.Environment;
import task.game.components.TaskComponent;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public abstract class Task<T extends Answer> implements Gradable<T> {

    public Task(ASTNodes.TaskType type, String id, Environment env) {
        this.type = type;
        this.id = id;
        this.env = env;
    }

    public static final String DEFAULT_EXPLANATION = "No task description provided";
    public static final String DEFAULT_CUSTOM_PASS_CODE = "";
    public static final String DEFAULT_CUSTOM_POINTS_CODE = "";
    public static final String DEFAULT_CUSTOM_SOLUTION_CODE = "";

    private ASTNodes.TaskType type;

    private Set<Set<Entity>> entitySets = new HashSet<>();

    private Entity managementEntity;
    private static final List<Task> SOLVED_TASK_IN_ORDER = new ArrayList<>();
    private final Set<Place> observer = new HashSet<>();
    private TaskState state = TaskState.INACTIVE;
    private String id;
    private String title;
    private String scenarioText;
    private List<T> answers;
    private float achievedPoints;
    private float maxPoints;
    private String explanation = DEFAULT_EXPLANATION;
    private String customPointsCode = DEFAULT_CUSTOM_POINTS_CODE;
    private String customPassCode = DEFAULT_CUSTOM_PASS_CODE;
    private String customSolution = DEFAULT_CUSTOM_SOLUTION_CODE;
    private Environment env;

    private Function<Task, Set<Object>> answerPickingFunction;

    public Task() {
    }

    public TaskState getState() {
        return state;
    }

    public boolean setState(TaskState state) {
        if (this.state == state) return false;
        this.state = state;
        observer.forEach(place -> place.notify(this, state));
        if (state == TaskState.FINISHED_CORRECT || state == TaskState.FINISHED_WRONG)
            SOLVED_TASK_IN_ORDER.add(this);
        else if (state == TaskState.ACTIVE && managementEntity != null) {
            managementEntity.fetch(TaskComponent.class).ifPresent(tc -> tc.activate(managementEntity));
        }

        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public BufferedImage getTitleAsBufferedImage() {
        return LaTeXHandler.createLatexBufferedImage(title);
    }

    public Image getTitleAsImage() {
        return LaTeXHandler.createLatexImage(title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<T> getAnswers() {
        return answers;
    }

    public void setAnswers(List<T> answers) {
        this.answers = answers;
    }

    public float getAchievedPoints() {
        return achievedPoints;
    }

    public void setAchievedPoints(float achievedPoints) {
        this.achievedPoints = achievedPoints;
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void registerPlace(Place place) {
        observer.add(place);
    }

    public boolean state(final TaskState state) {
        if (this.state == state) return false;
        this.state = state;
        observer.forEach(place -> place.notify(this, state));
        if (state == TaskState.FINISHED_CORRECT || state == TaskState.FINISHED_WRONG)
            SOLVED_TASK_IN_ORDER.add(this);
        else if (state == TaskState.ACTIVE && managementEntity != null) {
            managementEntity.fetch(TaskComponent.class).ifPresent(tc -> tc.activate(managementEntity));
        }

        return true;
    }

    public Set<Set<Entity>> getEntitySets() {
        return entitySets;
    }

    public void setEntitySets(Set<Set<Entity>> entitySets) {
        this.entitySets = entitySets;
    }

    public Optional<Entity> getManagementEntity() {
        return Optional.ofNullable(managementEntity);
    }

    public void setManagementEntity(Entity managementEntity) {
        this.managementEntity = managementEntity;
    }

    public Set<Place> getObserver() {
        return observer;
    }

    public ASTNodes.TaskType getType() {
        return type;
    }

    public void setType(ASTNodes.TaskType type) {
        this.type = type;
    }

    public float getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(float maxPoints) {
        this.maxPoints = maxPoints;
    }

    public abstract String correctAnswersAsString();

    public String getExplanation() {
        return explanation;
    }

    public String getScenarioText() {
        return scenarioText;
    }

    public void setScenarioText(String scenarioText) {
        this.scenarioText = scenarioText;
    }

    public Function<Task, Set<Object>> getAnswerPickingFunction() {
        return answerPickingFunction;
    }

    public void setAnswerPickingFunction(Function<Task, Set<Object>> answerPickingFunction) {
        this.answerPickingFunction = answerPickingFunction;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getCustomPointsCode() {
        return customPointsCode;
    }

    public void setCustomPointsCode(String customPointsCode) {
        this.customPointsCode = customPointsCode;
    }

    public String getCustomPassCode() {
        return customPassCode;
    }

    public void setCustomPassCode(String customPassCode) {
        this.customPassCode = customPassCode;
    }

    public String getCustomSolution() {
        return customSolution;
    }

    public void setCustomSolution(String customSolution) {
        this.customSolution = customSolution;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.getId(), this.getType());
    }
}
