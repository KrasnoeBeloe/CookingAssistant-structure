package com.example.qpl123;


import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipesActivity extends AppCompatActivity {

    private ListView recipesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        recipesListView = findViewById(R.id.recipesListView);

        String ingredients = getIntent().getStringExtra("ingredients");
        List<String> userIngredients = Arrays.asList(ingredients.split(",\\s*"));

        ArrayList<String> suitableRecipes = new ArrayList<>();

        // Простейшие рецепты
        if (userIngredients.contains("Яйцо") && userIngredients.contains("Молоко")) {
            suitableRecipes.add("Яичница");
        }
        if (userIngredients.contains("Хлеб") && userIngredients.contains("Молоко")) {
            suitableRecipes.add("Французские тосты");
        }
        if (userIngredients.contains("Молоко") && userIngredients.contains("Сахар")) {
            suitableRecipes.add("Молочный коктейль");
        }

        if (suitableRecipes.isEmpty()) {
            suitableRecipes.add("Нет подходящих рецептов");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, suitableRecipes);
        recipesListView.setAdapter(adapter);
    }
}
