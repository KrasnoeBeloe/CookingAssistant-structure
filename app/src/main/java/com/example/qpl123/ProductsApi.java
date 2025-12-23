package com.example.qpl123;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ProductsApi {

    private static final String SERVER_URL = "http://10.0.2.2:8080";

    // Метод для получения продуктов пользователя с сервера
    public static ArrayList<String> getUserProducts() {
        ArrayList<String> products = new ArrayList<>();
        try {
            URL url = new URL(SERVER_URL + "/products");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8")
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                // Обрабатываем JSON-ответ
                String response = sb.toString().trim();
                if (response.startsWith("[") && response.endsWith("]")) {
                    // Это JSON-массив
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        products.add(jsonArray.getString(i));
                    }
                } else if (response.startsWith("{") && response.endsWith("}")) {
                    // Это JSON-объект (может быть сообщение об ошибке)
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("error")) {
                        System.err.println("Ошибка сервера: " + jsonObject.getString("error"));
                    }
                }
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    // Метод для добавления продукта на сервер
    public static boolean addProduct(String product) {
        try {
            URL url = new URL(SERVER_URL + "/products");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // Отправляем данные
            OutputStream os = conn.getOutputStream();
            os.write(product.getBytes("UTF-8"));
            os.flush();
            os.close();

            // Получаем ответ
            int responseCode = conn.getResponseCode();

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

                // Проверяем ответ
                String response = sb.toString();
                return response.contains("success") || response.contains("добавлен");
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Метод для удаления продукта с сервера
    public static boolean deleteProduct(String product) {
        try {
            URL url = new URL(SERVER_URL + "/products");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // Отправляем данные
            OutputStream os = conn.getOutputStream();
            os.write(product.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            return responseCode == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Метод для удаления нескольких продуктов
    public static void deleteProducts(ArrayList<String> products) {
        for (String product : products) {
            deleteProduct(product);
        }
    }
}