package contrib.crafting;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import contrib.crafting.ingredient.CraftingIngredient;
import contrib.crafting.ingredient.CraftingItemIngredient;
import contrib.crafting.result.CraftingItemResult;
import contrib.crafting.result.CraftingResult;

import starter.Main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class Crafting {

    private static final HashSet<Recipe> recipes = new HashSet<>();
    private static final Logger logger = Logger.getLogger(Crafting.class.getName());

    /**
     * Get a recipe based on the provided items.
     *
     * <p>If there are multiple recipes that can be made with the provided items, recipes where
     * items must be in a specific order will be prioritized.
     *
     * @param inputs Ingredients used.
     * @return The recipe that can be crafted with the provided ingredients. If none can be crafted,
     *     the returned optional will be empty.
     */
    public static Optional<Recipe> getRecipeByIngredients(CraftingIngredient[] inputs) {

        List<Recipe> possibleRecipes = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.canCraft(inputs)) {
                possibleRecipes.add(recipe);
            }
        }

        if (possibleRecipes.isEmpty()) {
            return Optional.empty();
        }
        if (possibleRecipes.size() == 1) {
            return Optional.of(possibleRecipes.get(0));
        }
        possibleRecipes.sort(Comparator.comparing(c -> !c.isOrdered()));

        return Optional.of(possibleRecipes.get(0));
    }

    public static void addRecipe(Recipe recipe) {
        if (recipe.getIngredients().length == 0) {
            throw new InvalidRecipeException("Recipes with no ingredients are not allowed!");
        }
        recipes.add(recipe);
    }

    public static void removeRecipe(Recipe recipe) {
        recipes.remove(recipe);
    }

    public static void clearRecipes() {
        recipes.clear();
    }

    /**
     * Load recipes from the recipes folder.
     *
     * <p>If the program is compiled to a jar file, recipes will be loaded from within the jar file.
     */
    public static void loadRecipes() {
        if (Objects.requireNonNull(Crafting.class.getResource("/recipes"))
                .toString()
                .startsWith("jar:")) {
            loadFromJar();
        } else {
            loadFromFile();
        }
    }

    /** Load recipes if the program was started from a jar file. */
    private static void loadFromJar() {
        try {
            String path =
                    new File(Main.class.getResource("").getPath())
                            .getParent()
                            .replaceAll("(!|file:\\\\)", "");
            JarFile jar = new JarFile(path);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("recipes") && entry.getName().endsWith(".recipe")) {
                    logger.info("Load recipe: " + entry.getName());
                    Crafting.recipes.add(
                            parseRecipe(Main.class.getResourceAsStream("/" + entry.getName())));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Load recipes if the program was started from a folder. */
    private static void loadFromFile() {
        File folder = new File(Main.class.getResource("/recipes").getPath());
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.getName().endsWith(".recipe")) {
                logger.info("Load recipe: " + file.getName());
                Crafting.recipes.add(
                        parseRecipe(Main.class.getResourceAsStream("/recipes/" + file.getName())));
            }
        }
    }

    private static Recipe parseRecipe(InputStream stream) {

        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            reader.lines().forEach(builder::append);

            JsonReader jsonReader = new JsonReader();
            JsonValue root = jsonReader.parse(builder.toString());

            boolean orderedRecipe = root.getBoolean("ordered");
            JsonValue ingredients = root.get("ingredients"); // Array of items
            JsonValue results = root.get("results"); // Array of results

            if (ingredients.size <= 0) {
                throw new InvalidRecipeException("Recipes with no ingredients are not allowed!");
            }

            // Load Ingredients
            CraftingIngredient[] ingredientsArray = new CraftingIngredient[ingredients.size];
            for (int i = 0; i < ingredients.size; i++) {
                JsonValue ingredient = ingredients.get(i);
                String type = ingredient.getString("type");
                switch (type) {
                    case "item":
                        CraftingIngredient ci = new CraftingItemIngredient();
                        ci.parse(ingredients.get(i).get("item"));
                        ingredientsArray[i] = ci;
                        break;
                    default:
                        throw new RuntimeException("Unknown ingredient type: " + type);
                }
            }

            // Load Results
            CraftingResult[] resultsArray = new CraftingResult[results.size];
            for (int i = 0; i < results.size; i++) {
                JsonValue result = results.get(i);
                String type = result.getString("type");
                switch (type) {
                    case "item":
                        CraftingResult cr = new CraftingItemResult();
                        cr.parse(results.get(i).get("item"));
                        resultsArray[i] = cr;
                        break;
                    default:
                        throw new RuntimeException("Unknown result type: " + type);
                }
            }
            Recipe recipe = new Recipe(orderedRecipe, ingredientsArray, resultsArray);
            recipes.add(recipe);

            reader.close();

            return recipe;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
