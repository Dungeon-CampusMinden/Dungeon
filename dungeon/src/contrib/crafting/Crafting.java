package contrib.crafting;

import contrib.item.Item;
import contrib.item.ItemRegistry;
import contrib.item.MissingItemRegistrationException;
import core.Game;
import core.utils.JsonHandler;
import core.utils.logging.DungeonLogger;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Handles the crafting system.
 *
 * <p>It contains a list of recipes and methods to {@link #addRecipe(Recipe) add} recipes. It also
 * provides the method {@link #recipeByIngredients(CraftingIngredient[])} to get a recipe based on
 * the provided ingredients.
 *
 * <p>It will load the recipes from the files via {@link #loadRecipes()}. Recipes have to be in the
 * 'assets/recipes' directory. This will autmaticly happen an program start. Call this in your
 * {@link core.game.PreRunConfiguration#userOnSetup() onSetup callback}.
 */
public final class Crafting {
  private static final HashSet<Recipe> RECIPES = new HashSet<>();
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Crafting.class);

  /**
   * Get a recipe based on the provided items.
   *
   * <p>If there are multiple recipes that can be made with the provided items, recipes where items
   * must be in a specific order will be prioritized.
   *
   * @param inputs Ingredients used.
   * @return The recipe that can be crafted with the provided ingredients. If none can be crafted,
   *     the returned optional will be empty.
   */
  public static Optional<Recipe> recipeByIngredients(final CraftingIngredient[] inputs) {
    List<Recipe> possibleRecipes = new ArrayList<>();
    for (Recipe recipe : RECIPES) {
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
    possibleRecipes.sort(Comparator.comparing(c -> !c.ordered()));

    return Optional.of(possibleRecipes.get(0));
  }

  /**
   * Add A recipe to the list of recipes programmatically.
   *
   * @param recipe The recipe to add.
   */
  public static void addRecipe(final Recipe recipe) {
    if (recipe.ingredients().length == 0) {
      throw new InvalidRecipeException("Recipes with no ingredients are not allowed!");
    }
    RECIPES.add(recipe);
  }

  /** Remove all recipes. */
  public static void clearRecipes() {
    RECIPES.clear();
  }

  /**
   * Load recipes from the recipes' folder.
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
          new File(Objects.requireNonNull(Game.class.getResource("")).getPath())
              .getParent()
              // for windows
              .replaceAll("(!|file:\\\\)", "")
              // for unix/macos
              .replaceAll("(!|file:)", "");
      JarFile jar = new JarFile(path);
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.getName().startsWith("recipes") && entry.getName().endsWith(".recipe")) {
          LOGGER.info("Load recipe: " + entry.getName());
          Recipe r =
              parseRecipe(Game.class.getResourceAsStream("/" + entry.getName()), entry.getName());
          if (r != null) Crafting.RECIPES.add(r);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Load recipes if the program was started from a folder. */
  private static void loadFromFile() {
    File folder =
        new File(Objects.requireNonNull(Crafting.class.getResource("/recipes")).getPath());
    File[] files = folder.listFiles();
    if (files == null) {
      return;
    }
    for (File file : files) {
      if (file.getName().endsWith(".recipe")) {
        LOGGER.info("Load recipe: " + file.getName());
        Recipe r =
            parseRecipe(
                Crafting.class.getResourceAsStream("/recipes/" + file.getName()), file.getName());
        if (r != null) Crafting.RECIPES.add(r);
      }
    }
  }

  /**
   * Parse a recipe from a file.
   *
   * @param stream The stream to read from.
   * @param name The name of the file. Used for error logging only.
   * @return The parsed recipe.
   */
  @SuppressWarnings("unchecked")
  private static Recipe parseRecipe(final InputStream stream, final String name) {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      String jsonText = reader.lines().collect(Collectors.joining());
      Map<String, Object> root = JsonHandler.readJson(jsonText);

      boolean orderedRecipe = Boolean.TRUE.equals(root.get("ordered"));

      Object ingredientsObj = root.get("ingredients");
      if (!(ingredientsObj instanceof List) || ((List<?>) ingredientsObj).isEmpty()) {
        throw new InvalidRecipeException("Recipes with no ingredients are not allowed!");
      }
      List<Map<String, Object>> ingredientsList = new ArrayList<>();
      for (Object ingObj : (List<?>) ingredientsObj) {
        if (!(ingObj instanceof Map))
          throw new IllegalArgumentException(
              "Ingredient entry in 'ingredients' array must be an object. File: " + name);
        ingredientsList.add((Map<String, Object>) ingObj);
      }

      Object resultsObj = root.get("results");
      if (!(resultsObj instanceof List) || ((List<?>) resultsObj).isEmpty()) {
        throw new InvalidRecipeException("Recipes with no results are not allowed! File: " + name);
      }
      List<Map<String, Object>> resultsList = new ArrayList<>();
      for (Object resObj : (List<?>) resultsObj) {
        if (!(resObj instanceof Map))
          throw new IllegalArgumentException(
              "Result entry in 'results' array must be an object. File: " + name);
        resultsList.add((Map<String, Object>) resObj);
      }

      // Load Ingredients
      CraftingIngredient[] ingredientsArray = new CraftingIngredient[ingredientsList.size()];
      for (int i = 0; i < ingredientsList.size(); i++) {
        Map<String, Object> ingredientMap = ingredientsList.get(i);
        Map<String, Object> itemMap = (Map<String, Object>) ingredientMap.get("item");
        if (itemMap == null)
          throw new IllegalArgumentException("Ingredient missing 'item' object. File: " + name);

        String id = (String) itemMap.get("id");
        if (id == null)
          throw new IllegalArgumentException(
              "Ingredient 'item' missing 'id' string. File: " + name);

        String type = (String) ingredientMap.get("type");
        if (type == null)
          throw new IllegalArgumentException("Ingredient missing 'type' string. File: " + name);

        if (type.equals("item")) {
          if (itemMap.containsKey("param")) {
            Object paramObj = itemMap.get("param");
            if (!(paramObj instanceof List))
              throw new IllegalArgumentException(
                  "'param' must be an array of strings. File: " + name);
            List<Object> paramList = (List<Object>) paramObj;
            ItemRegistry.lookup(id)
                .orElseThrow(
                    () ->
                        new MissingItemRegistrationException(
                            "Unknown item id: " + id + ". File: " + name));
            Map<String, String> paramData = parseParamData(paramList, name);
            Item item =
                ItemRegistry.create(id, paramData)
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Item '"
                                    + id
                                    + "' uses 'param' but no factory is registered. File: "
                                    + name));
            ingredientsArray[i] = item;
          } else {
            Class<? extends Item> itemClass =
                ItemRegistry.lookup(id)
                    .orElseThrow(
                        () ->
                            new MissingItemRegistrationException(
                                "Unknown item id: " + id + ". File: " + name));
            ingredientsArray[i] = itemClass.getDeclaredConstructor().newInstance();
          }
          if (itemMap.containsKey("count")) {
            Object countObj = itemMap.get("count");
            int count;
            try {
              count = Integer.parseInt(String.valueOf(countObj));
            } catch (ClassCastException ex) {
              throw new IllegalArgumentException("'count' must be an integer. File: " + name, ex);
            }
            ingredientsArray[i].setAmount(count);
          }
        } else {
          throw new RuntimeException("Unknown ingredient type: " + type + ". File: " + name);
        }
      }

      // Load Results
      CraftingResult[] resultsArray = new CraftingResult[resultsList.size()];
      for (int i = 0; i < resultsList.size(); i++) {
        Map<String, Object> resultMap = resultsList.get(i);
        Map<String, Object> itemMap = (Map<String, Object>) resultMap.get("item");
        if (itemMap == null)
          throw new IllegalArgumentException("Result missing 'item' object. File: " + name);

        String id = (String) itemMap.get("id");
        if (id == null)
          throw new IllegalArgumentException("Result 'item' missing 'id' string. File: " + name);

        String type = (String) resultMap.get("type");
        if (type == null)
          throw new IllegalArgumentException("Result missing 'type' string. File: " + name);

        if (type.equals("item")) {
          if (itemMap.containsKey("param")) {
            Object paramObj = itemMap.get("param");
            if (!(paramObj instanceof List))
              throw new IllegalArgumentException(
                  "'param' must be an array of strings. File: " + name);
            List<Object> paramList = (List<Object>) paramObj;

            Class<? extends Item> itemClass =
                ItemRegistry.lookup(id)
                    .orElseThrow(
                        () ->
                            new MissingItemRegistrationException(
                                "Unknown item id: " + id + ". File: " + name));
            Map<String, String> paramData = parseParamData(paramList, name);
            Item item =
                ItemRegistry.create(id, paramData)
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Item '"
                                    + id
                                    + "' uses 'param' but no factory is registered. File: "
                                    + name));
            resultsArray[i] = item;
          } else {
            Class<? extends Item> itemClass =
                ItemRegistry.lookup(id)
                    .orElseThrow(
                        () ->
                            new MissingItemRegistrationException(
                                "Unknown item id: " + id + ". File: " + name));
            resultsArray[i] = itemClass.getDeclaredConstructor().newInstance();
          }
          if (itemMap.containsKey("count")) {
            Object countObj = itemMap.get("count");
            int count;
            try {
              count = Integer.parseInt(String.valueOf(countObj));
            } catch (ClassCastException ex) {
              throw new IllegalArgumentException("'count' must be an integer. File: " + name, ex);
            }
            resultsArray[i].setAmount(count);
          }
        } else {
          throw new RuntimeException("Unknown result type: " + type + ". File: " + name);
        }
      }
      // RECIPES.add(recipe); // Recipe is added by the calling methods loadFromJar/loadFromFile

      return new Recipe(orderedRecipe, ingredientsArray, resultsArray);
    } catch (MissingItemRegistrationException ex) {
      throw ex;
    } catch (IOException | ReflectiveOperationException ex) {
      LOGGER.error("Error parsing recipe ({}): {}", name, ex.getMessage(), ex);
    } catch (IllegalArgumentException | InvalidRecipeException | ClassCastException ex) {
      // Catching more specific exceptions from JsonHandler or casting issues
      LOGGER.warn("Warning parsing recipe ({}): {}", name, ex.getMessage(), ex);
    } catch (Exception ex) { // Catch any other unexpected errors
      LOGGER.error("Unexpected error parsing recipe ({}): {}", name, ex.getMessage(), ex);
    }
    return null;
  }

  private static Map<String, String> parseParamData(List<Object> paramList, String recipeName) {
    if (paramList.isEmpty()) {
      throw new IllegalArgumentException("'param' must not be empty. File: " + recipeName);
    }
    Map<String, String> data = new LinkedHashMap<>();
    for (int i = 0; i < paramList.size(); i++) {
      Object pObj = paramList.get(i);
      if (!(pObj instanceof String)) {
        throw new IllegalArgumentException(
            "Parameter in 'param' list must be a string. Found: "
                + (pObj == null ? "null" : pObj.getClass().getName())
                + " in recipe "
                + recipeName);
      }
      String paramString = ((String) pObj).trim();
      if (paramString.isEmpty()) {
        throw new IllegalArgumentException(
            "Parameter in 'param' list must not be blank. Recipe: " + recipeName);
      }
      data.put("param" + i, paramString);
    }
    return data;
  }
}
