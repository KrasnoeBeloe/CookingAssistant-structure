package com.example.qpl123;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView recipeNameTextView;
    private TextView recipeDetailsTextView;
    private TextView ingredientsTextView;
    private TextView instructionsTextView;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Инициализация элементов UI
        recipeNameTextView = findViewById(R.id.recipeNameTextView);
        recipeDetailsTextView = findViewById(R.id.recipeDetailsTextView);
        ingredientsTextView = findViewById(R.id.ingredientsTextView);
        instructionsTextView = findViewById(R.id.instructionsTextView);
        backButton = findViewById(R.id.backButton);

        // Получаем название рецепта из интента
        String recipeName = getIntent().getStringExtra("recipe_name");
        if (recipeName == null || recipeName.isEmpty()) {
            Toast.makeText(this, "Ошибка: рецепт не выбран", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Устанавливаем обработчик кнопки "Назад"
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Загружаем детали рецепта
        loadRecipeDetails(recipeName);
    }

    private void loadRecipeDetails(String recipeName) {
        // Показываем сообщение о загрузке
        recipeNameTextView.setText("Загрузка: " + recipeName);
        recipeDetailsTextView.setText("");
        ingredientsTextView.setText("Загрузка ингредиентов...");
        instructionsTextView.setText("Загрузка инструкции...");

        new Thread(() -> {
            try {
                Recipe recipe = RecipesApi.getRecipeDetails(recipeName);

                runOnUiThread(() -> {
                    if (recipe != null && recipe.getName() != null) {
                        // Устанавливаем название
                        recipeNameTextView.setText(recipe.getName());

                        // Устанавливаем детали рецепта
                        StringBuilder details = new StringBuilder();
                        if (recipe.getCookingTime() != null && !recipe.getCookingTime().isEmpty()) {
                            details.append("Время приготовления: ").append(recipe.getCookingTime());
                        }
                        if (recipe.getDifficulty() != null && !recipe.getDifficulty().isEmpty()) {
                            if (details.length() > 0) details.append("\n");
                            details.append("Сложность: ").append(recipe.getDifficulty());
                        }
                        if (details.length() > 0) {
                            recipeDetailsTextView.setText(details.toString());
                        }

                        // Устанавливаем ингредиенты с граммовками
                        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                            StringBuilder ingredientsText = new StringBuilder();
                            for (Ingredient ingredient : recipe.getIngredients()) {
                                ingredientsText.append("• ")
                                        .append(ingredient.getQuantity() != null ? ingredient.getQuantity() : "")
                                        .append(" ")
                                        .append(ingredient.getName() != null ? ingredient.getName() : "")
                                        .append("\n");
                            }
                            ingredientsTextView.setText(ingredientsText.toString());
                        } else {
                            ingredientsTextView.setText("Ингредиенты не указаны");
                        }

                        // Устанавливаем инструкции
                        if (recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
                            instructionsTextView.setText(recipe.getInstructions());
                        } else {
                            instructionsTextView.setText("Инструкция отсутствует");
                        }

                        Toast.makeText(RecipeDetailActivity.this,
                                "Рецепт загружен",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Обработка ошибки загрузки
                        recipeNameTextView.setText("Рецепт не найден");
                        ingredientsTextView.setText("Не удалось загрузить рецепт. Проверьте подключение к серверу.");
                        instructionsTextView.setText("");

                        Toast.makeText(RecipeDetailActivity.this,
                                "Ошибка загрузки рецепта",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    recipeNameTextView.setText("Ошибка");
                    ingredientsTextView.setText("Произошла ошибка при загрузке: " + e.getMessage());
                    instructionsTextView.setText("");

                    Toast.makeText(RecipeDetailActivity.this,
                            "Критическая ошибка: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}