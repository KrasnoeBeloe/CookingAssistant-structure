package com.example.qpl123;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RecipesApi {

    private static final String SERVER_URL = "http://10.0.2.2:8080";

    // Метод для получения всех рецептов
    public static ArrayList<Recipe> getRecipes() {
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            URL url = new URL(SERVER_URL + "/recipes");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000); // 10 секунд
            conn.setReadTimeout(10000);

            System.out.println("Запрос рецептов: " + url);

            int responseCode = conn.getResponseCode();
            System.out.println("Код ответа: " + responseCode);

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8")
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                String jsonString = sb.toString();
                System.out.println("Получено символов: " + jsonString.length());

                if (jsonString.length() > 0) {
                    JSONArray jsonArray = new JSONArray(jsonString);
                    System.out.println("Найдено рецептов: " + jsonArray.length());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Recipe recipe = parseRecipeFromJson(obj);
                        if (recipe != null) {
                            recipes.add(recipe);
                        }
                    }
                }
            } else {
                System.err.println("Ошибка сервера: " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("Ошибка в getRecipes: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Возвращаем рецептов: " + recipes.size());
        return recipes;
    }

    // Метод для получения конкретного рецепта по имени
    public static Recipe getRecipeDetails(String recipeName) {
        try {
            // Кодируем название рецепта для URL
            String encodedName = java.net.URLEncoder.encode(recipeName, "UTF-8");
            URL url = new URL(SERVER_URL + "/recipe/" + encodedName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            System.out.println("Запрос деталей рецепта: " + url);

            int responseCode = conn.getResponseCode();
            System.out.println("Код ответа: " + responseCode);

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8")
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                String jsonString = sb.toString();
                System.out.println("Получено деталей рецепта: " + jsonString.length() + " символов");

                if (jsonString.length() > 0) {
                    JSONObject obj = new JSONObject(jsonString);
                    return parseRecipeFromJson(obj);
                }
            } else {
                System.err.println("Ошибка сервера для рецепта '" + recipeName + "': " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("Ошибка в getRecipeDetails для '" + recipeName + "': " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Метод для парсинга рецепта из JSON
    private static Recipe parseRecipeFromJson(JSONObject obj) {
        try {
            Recipe recipe = new Recipe();

            if (obj.has("name")) {
                recipe.setName(obj.getString("name"));
            }

            // Парсинг ингредиентов
            if (obj.has("ingredients")) {
                JSONArray ingredientsArray = obj.getJSONArray("ingredients");
                ArrayList<Ingredient> ingredients = new ArrayList<>();

                for (int j = 0; j < ingredientsArray.length(); j++) {
                    JSONObject ingObj = ingredientsArray.getJSONObject(j);
                    Ingredient ingredient = new Ingredient();

                    if (ingObj.has("name")) {
                        ingredient.setName(ingObj.getString("name"));
                    }

                    if (ingObj.has("quantity")) {
                        ingredient.setQuantity(ingObj.getString("quantity"));
                    }

                    ingredients.add(ingredient);
                }

                recipe.setIngredients(ingredients);
            }

            // Дополнительные поля
            if (obj.has("instructions")) {
                recipe.setInstructions(obj.getString("instructions"));
            }

            if (obj.has("cooking_time")) {
                recipe.setCookingTime(obj.getString("cooking_time"));
            }

            if (obj.has("difficulty")) {
                recipe.setDifficulty(obj.getString("difficulty"));
            }

            return recipe;

        } catch (Exception e) {
            System.err.println("Ошибка парсинга рецепта: " + e.getMessage());
            return null;
        }
    }
}