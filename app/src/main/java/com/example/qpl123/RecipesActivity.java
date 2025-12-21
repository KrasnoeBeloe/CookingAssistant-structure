package com.example.qpl123;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class RecipesActivity extends AppCompatActivity {

    ListView listView;
    TextView filterInfoTextView;
    Button backButton, refreshButton;
    ArrayAdapter<Recipe> adapter;
    ArrayList<Recipe> allRecipes = new ArrayList<>();
    ArrayList<Recipe> filteredRecipes = new ArrayList<>();
    ArrayList<String> selectedProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        // Инициализация элементов UI
        listView = findViewById(R.id.recipesListView);
        filterInfoTextView = findViewById(R.id.filterInfoTextView);
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);

        // Получаем ОТМЕЧЕННЫЕ продукты из MainActivity
        selectedProducts = getIntent().getStringArrayListExtra("selectedProducts");
        if (selectedProducts == null) {
            selectedProducts = new ArrayList<>();
        }

        // Обновляем информацию о фильтрации
        updateFilterInfo();

        // Создаем адаптер для отфильтрованных рецептов
        adapter = new ArrayAdapter<Recipe>(
                this,
                android.R.layout.simple_list_item_1,
                filteredRecipes
        ) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                android.widget.TextView textView = (android.widget.TextView) view.findViewById(android.R.id.text1);

                Recipe recipe = getItem(position);
                if (recipe != null) {
                    int matchPercent = calculateMatchPercent(recipe);
                    int totalIngredients = recipe.getIngredients().size();

                    // Форматируем строку для отображения
                    String displayText = recipe.getName() +
                            "\nИнгредиентов: " + totalIngredients +
                            " | Совпадение: " + matchPercent + "%";

                    textView.setText(displayText);

                    // Меняем цвет текста в зависимости от процента совпадения
                    if (matchPercent >= 80) {
                        textView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        textView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                    } else if (matchPercent >= 50) {
                        textView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                        textView.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                    } else {
                        textView.setTextColor(getResources().getColor(android.R.color.black));
                        textView.setBackgroundColor(getResources().getColor(android.R.color.white));
                    }

                    // Добавляем отступы для лучшего вида
                    textView.setPadding(16, 16, 16, 16);
                }
                return view;
            }
        };

        listView.setAdapter(adapter);

        // Обработка клика по рецепту
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                try {
                    if (position < 0 || position >= filteredRecipes.size()) {
                        Toast.makeText(RecipesActivity.this, "Неверный выбор", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Recipe selectedRecipe = filteredRecipes.get(position);
                    if (selectedRecipe == null || selectedRecipe.getName() == null) {
                        Toast.makeText(RecipesActivity.this, "Ошибка: рецепт не найден", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Открываем экран с деталями рецепта
                    Intent intent = new Intent(RecipesActivity.this, RecipeDetailActivity.class);
                    intent.putExtra("recipe_name", selectedRecipe.getName());
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(RecipesActivity.this,
                            "Ошибка при открытии рецепта: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        // Обработка кнопки "Назад к продуктам"
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Обработка кнопки "Обновить"
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRecipes();
            }
        });

        // Загружаем рецепты
        loadRecipes();
    }

    // Обновление информации о фильтрации
    private void updateFilterInfo() {
        if (selectedProducts.isEmpty()) {
            filterInfoTextView.setText("Фильтрация не активна (не выбраны продукты)");
        } else {
            String productsText = "Выбрано продуктов: " + selectedProducts.size();
            filterInfoTextView.setText(productsText + " | Фильтр: >50% совпадения");
        }
    }

    private void loadRecipes() {
        Toast.makeText(this, "Загрузка рецептов...", Toast.LENGTH_SHORT).show();
        refreshButton.setEnabled(false);

        new Thread(() -> {
            try {
                // Получаем все рецепты через API
                allRecipes = RecipesApi.getRecipes();

                runOnUiThread(() -> {
                    refreshButton.setEnabled(true);

                    if (allRecipes == null || allRecipes.isEmpty()) {
                        Toast.makeText(RecipesActivity.this,
                                "Не удалось загрузить рецепты",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Фильтруем рецепты по выбранным продуктам
                    filterRecipesByProducts();

                    if (filteredRecipes.isEmpty()) {
                        if (selectedProducts.isEmpty()) {
                            Toast.makeText(RecipesActivity.this,
                                    "На сервере нет рецептов",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RecipesActivity.this,
                                    "Рецептов с выбранными продуктами не найдено\nПопробуйте выбрать другие продукты",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(RecipesActivity.this,
                                "Найдено " + filteredRecipes.size() + " рецептов (из " + allRecipes.size() + ")",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    refreshButton.setEnabled(true);
                    Toast.makeText(RecipesActivity.this,
                            "Ошибка загрузки: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // Фильтрация рецептов по выбранным продуктам (>50% совпадение)
    private void filterRecipesByProducts() {
        filteredRecipes.clear();

        if (allRecipes == null || allRecipes.isEmpty()) {
            adapter.notifyDataSetChanged();
            return;
        }

        if (selectedProducts.isEmpty()) {
            // Если нет выбранных продуктов, показываем все
            filteredRecipes.addAll(allRecipes);
            Toast.makeText(this, "Показаны все рецепты (фильтр не активен)", Toast.LENGTH_SHORT).show();
        } else {
            // Фильтруем рецепты: >50% ингредиентов есть у пользователя
            for (Recipe recipe : allRecipes) {
                int matchPercent = calculateMatchPercent(recipe);
                if (matchPercent > 50) {
                    filteredRecipes.add(recipe);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // Улучшенный расчет процента совпадения ингредиентов
    private int calculateMatchPercent(Recipe recipe) {
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return 0;
        }

        if (selectedProducts == null || selectedProducts.isEmpty()) {
            return 0;
        }

        int matchingCount = 0;

        // Для каждого ингредиента в рецепте проверяем, есть ли он в выбранных продуктах
        for (Ingredient ingredient : recipe.getIngredients()) {
            String ingredientName = normalizeString(ingredient.getName());

            for (String product : selectedProducts) {
                String productName = normalizeString(product);

                // Проверяем разные варианты совпадения
                if (isIngredientsMatch(ingredientName, productName)) {
                    matchingCount++;
                    break; // Нашли совпадение, переходим к следующему ингредиенту
                }
            }
        }

        // Рассчитываем процент совпадения
        double percent = ((double) matchingCount / recipe.getIngredients().size()) * 100;
        return (int) Math.round(percent);
    }

    // Нормализация строки для сравнения
    private String normalizeString(String str) {
        if (str == null) return "";
        return str.toLowerCase().trim()
                .replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", "") // Удаляем спецсимволы
                .replaceAll("ё", "е"); // Заменяем ё на е
    }

    // Проверка совпадения ингредиентов
    private boolean isIngredientsMatch(String ingredient, String product) {
        if (ingredient.isEmpty() || product.isEmpty()) {
            return false;
        }

        // Разделяем на слова для более точного сравнения
        String[] ingredientWords = ingredient.split("\\s+");
        String[] productWords = product.split("\\s+");

        // Проверяем каждое слово ингредиента
        for (String ingWord : ingredientWords) {
            for (String prodWord : productWords) {
                if (ingWord.length() > 2 && prodWord.length() > 2) {
                    if (ingWord.equals(prodWord) ||
                            ingWord.contains(prodWord) ||
                            prodWord.contains(ingWord)) {
                        return true;
                    }
                }
            }
        }

        // Также проверяем полное совпадение строк
        return ingredient.equals(product) ||
                ingredient.contains(product) ||
                product.contains(ingredient);
    }
}
