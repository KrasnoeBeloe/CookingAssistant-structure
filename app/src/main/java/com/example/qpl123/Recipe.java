package com.example.qpl123;

import java.util.ArrayList;

public class Recipe {
    private String name;
    private ArrayList<Ingredient> ingredients;
    private String instructions;
    private String cookingTime;
    private String difficulty;

    public Recipe() {
        this.name = "";
        this.ingredients = new ArrayList<>();
        this.instructions = "";
        this.cookingTime = "";
        this.difficulty = "";
    }

    public Recipe(String name, ArrayList<Ingredient> ingredients, String instructions, String cookingTime, String difficulty) {
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(String cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return name;
    }

    // Метод для получения списка ингредиентов как строки
    public String getIngredientsAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            sb.append("• ").append(ingredients.get(i).getQuantity()).append(" ").append(ingredients.get(i).getName());
            if (i < ingredients.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}