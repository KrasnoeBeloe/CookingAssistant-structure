package com.example.qpl123;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText ingredientsInput;
    private Button addButton, deleteButton, searchButton;
    private ListView productsListView;
    private ArrayList<String> productsList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация элементов
        ingredientsInput = findViewById(R.id.ingredientsInput);
        addButton = findViewById(R.id.addButton);
        deleteButton = findViewById(R.id.deleteButton);
        searchButton = findViewById(R.id.searchButton);
        productsListView = findViewById(R.id.productsListView);

        // Инициализация списка и адаптера
        productsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice,
                productsList);
        productsListView.setAdapter(adapter);
        productsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Загружаем продукты с сервера при запуске
        loadProductsFromServer();

        // Обработчики кнопок
        addButton.setOnClickListener(v -> addProduct());
        deleteButton.setOnClickListener(v -> deleteSelectedProducts());
        searchButton.setOnClickListener(v -> searchRecipes());
    }

    // Загрузка продуктов с сервера
    private void loadProductsFromServer() {
        new Thread(() -> {
            ArrayList<String> serverProducts = ProductsApi.getUserProducts();

            runOnUiThread(() -> {
                productsList.clear();
                productsList.addAll(serverProducts);
                adapter.notifyDataSetChanged();

                if (productsList.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Список продуктов пуст. Добавьте продукты.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Загружено " + productsList.size() + " продукт(ов)",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    // Добавление продукта
    private void addProduct() {
        String product = ingredientsInput.getText().toString().trim();

        if (!product.isEmpty()) {
            // Показываем продукт локально сразу
            productsList.add(product);
            adapter.notifyDataSetChanged();
            ingredientsInput.setText("");

            // Снимаем выделение с только что добавленного продукта
            productsListView.clearChoices();

            // Отправляем на сервер в фоне
            new Thread(() -> {
                boolean success = ProductsApi.addProduct(product);

                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(MainActivity.this,
                                "Продукт добавлен: " + product,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Если ошибка, удаляем из локального списка
                        productsList.remove(product);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this,
                                "Ошибка при добавлении продукта",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        } else {
            Toast.makeText(this, "Введите название продукта", Toast.LENGTH_SHORT).show();
        }
    }

    // Удаление выбранных продуктов
    private void deleteSelectedProducts() {
        ArrayList<String> toRemove = new ArrayList<>();

        // Собираем выбранные элементы (отмеченные галочкой)
        for (int i = 0; i < productsListView.getCount(); i++) {
            if (productsListView.isItemChecked(i)) {
                toRemove.add(productsList.get(i));
            }
        }

        if (toRemove.isEmpty()) {
            Toast.makeText(this, "Выберите продукты для удаления", Toast.LENGTH_SHORT).show();
            return;
        }

        // Удаляем локально
        productsList.removeAll(toRemove);
        adapter.notifyDataSetChanged();
        productsListView.clearChoices();

        // Удаляем на сервере в фоне
        new Thread(() -> {
            ProductsApi.deleteProducts(toRemove);

            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this,
                        "Удалено: " + toRemove.size() + " продукт(ов)",
                        Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    // Поиск рецептов по ОТМЕЧЕННЫМ продуктам
    private void searchRecipes() {
        // Собираем только отмеченные галочкой продукты
        ArrayList<String> checkedProducts = new ArrayList<>();

        for (int i = 0; i < productsListView.getCount(); i++) {
            if (productsListView.isItemChecked(i)) {
                checkedProducts.add(productsList.get(i));
            }
        }

        if (checkedProducts.isEmpty()) {
            Toast.makeText(this,
                    "Выберите продукты для поиска рецептов\n(отметьте галочкой)",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Переходим на экран рецептов с ОТМЕЧЕННЫМИ продуктами
        Intent intent = new Intent(MainActivity.this, RecipesActivity.class);
        intent.putStringArrayListExtra("selectedProducts", checkedProducts);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран обновляем список и сбрасываем выбор
        loadProductsFromServer();
        productsListView.clearChoices();
    }
}