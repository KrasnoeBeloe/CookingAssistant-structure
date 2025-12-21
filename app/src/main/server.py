from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import sqlite3
import os

# Создаем базу данных SQLite
DB_FILE = "cooking_assistant.db"


def init_database():
    """Инициализация базы данных"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()

    # Удаляем старую таблицу рецептов и создаем новую с доп. полями
    cursor.execute("DROP TABLE IF EXISTS recipes")

    # Новая таблица рецептов с инструкциями и граммовками
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS recipes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            ingredients TEXT NOT NULL,  -- JSON с названием и количеством
            instructions TEXT,
            cooking_time TEXT,
            difficulty TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')

    # Таблица пользовательских продуктов
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS user_products (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            product TEXT NOT NULL UNIQUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')

    # Вставляем тестовые рецепты с деталями
    sample_recipes = [
        ("Яичница",
         json.dumps([
             {"name": "яйца", "quantity": "2 шт."},
             {"name": "соль", "quantity": "щепотка"},
             {"name": "масло растительное", "quantity": "1 ст.л."}
         ], ensure_ascii=False),
         "1. Сковороду разогреть, добавить масло.\n2. Разбить яйца на сковороду.\n3. Посолить по вкусу.\n4. Жарить 5-7 минут до готовности.",
         "10 мин",
         "легко"),

        ("Макароны",
         json.dumps([
             {"name": "макароны", "quantity": "200 г"},
             {"name": "соль", "quantity": "1 ч.л."},
             {"name": "вода", "quantity": "2 л"}
         ], ensure_ascii=False),
         "1. Довести воду до кипения, посолить.\n2. Добавить макароны, перемешать.\n3. Варить 8-10 минут до готовности.\n4. Откинуть на дуршлаг.",
         "15 мин",
         "легко"),

        ("Бутерброд с сыром",
         json.dumps([
             {"name": "хлеб", "quantity": "2 ломтика"},
             {"name": "сыр", "quantity": "50 г"},
             {"name": "масло сливочное", "quantity": "10 г"}
         ], ensure_ascii=False),
         "1. Хлеб поджарить на сковороде или в тостере.\n2. Намазать маслом.\n3. Положить ломтик сыра.\n4. Подавать теплым.",
         "5 мин",
         "легко"),

        ("Омлет",
         json.dumps([
             {"name": "яйца", "quantity": "3 шт."},
             {"name": "молоко", "quantity": "100 мл"},
             {"name": "соль", "quantity": "щепотка"},
             {"name": "масло сливочное", "quantity": "20 г"}
         ], ensure_ascii=False),
         "1. Яйца взбить с молоком и солью.\n2. Разогреть сковороду с маслом.\n3. Вылить яичную смесь.\n4. Жарить на среднем огне 5-7 минут.\n5. Сложить пополам и подавать.",
         "10 мин",
         "легко"),

        ("Салат овощной",
         json.dumps([
             {"name": "помидоры", "quantity": "2 шт."},
             {"name": "огурцы", "quantity": "2 шт."},
             {"name": "лук репчатый", "quantity": "1 шт."},
             {"name": "масло оливковое", "quantity": "2 ст.л."},
             {"name": "соль", "quantity": "по вкусу"}
         ], ensure_ascii=False),
         "1. Овощи помыть и обсушить.\n2. Помидоры и огурцы нарезать кубиками.\n3. Лук мелко нарезать.\n4. Смешать все овощи в миске.\n5. Заправить маслом, посолить и перемешать.",
         "15 мин",
         "легко"),

        ("Рис отварной",
         json.dumps([
             {"name": "рис", "quantity": "1 стакан"},
             {"name": "вода", "quantity": "2 стакана"},
             {"name": "соль", "quantity": "1 ч.л."}
         ], ensure_ascii=False),
         "1. Рис промыть холодной водой.\n2. В кастрюлю налить воду, довести до кипения.\n3. Добавить рис и соль.\n4. Варить 15-20 минут на медленном огне.\n5. Дать постоять под крышкой 5 минут.",
         "25 мин",
         "средне")
    ]

    for recipe in sample_recipes:
        cursor.execute(
            "INSERT INTO recipes (name, ingredients, instructions, cooking_time, difficulty) VALUES (?, ?, ?, ?, ?)",
            recipe
        )

    conn.commit()
    conn.close()


init_database()


class RequestHandler(BaseHTTPRequestHandler):
    def _set_cors_headers(self):
        """Установка CORS заголовков"""
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')

    def do_OPTIONS(self):
        """Обработка preflight запросов"""
        self.send_response(200)
        self._set_cors_headers()
        self.end_headers()

    def _send_json_response(self, data, status_code=200):
        """Универсальный метод для отправки JSON ответа"""
        json_data = json.dumps(data, ensure_ascii=False)
        json_bytes = json_data.encode('utf-8')

        self.send_response(status_code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(json_bytes)))  # ВАЖНО: добавляем Content-Length
        self._set_cors_headers()
        self.end_headers()

        self.wfile.write(json_bytes)

    def do_GET(self):
        """Обработка GET запросов"""
        conn = sqlite3.connect(DB_FILE)
        cursor = conn.cursor()

        try:
            if self.path == "/recipes":
                cursor.execute("SELECT name, ingredients, instructions, cooking_time, difficulty FROM recipes")
                rows = cursor.fetchall()
                recipes_list = []

                for row in rows:
                    recipes_list.append({
                        "name": row[0],
                        "ingredients": json.loads(row[1]),
                        "instructions": row[2],
                        "cooking_time": row[3],
                        "difficulty": row[4]
                    })

                self._send_json_response(recipes_list)

            elif self.path.startswith("/recipe/"):
                # Получение конкретного рецепта по имени
                import urllib.parse
                recipe_name = urllib.parse.unquote(self.path.split("/")[2])
                cursor.execute(
                    "SELECT name, ingredients, instructions, cooking_time, difficulty FROM recipes WHERE name = ?",
                    (recipe_name,)
                )
                row = cursor.fetchone()

                if row:
                    recipe_data = {
                        "name": row[0],
                        "ingredients": json.loads(row[1]),
                        "instructions": row[2],
                        "cooking_time": row[3],
                        "difficulty": row[4]
                    }
                    self._send_json_response(recipe_data)
                else:
                    self._send_json_response({"error": "Рецепт не найден"}, 404)

            elif self.path == "/products":
                cursor.execute("SELECT product FROM user_products ORDER BY created_at")
                rows = cursor.fetchall()
                products_list = [row[0] for row in rows]
                self._send_json_response(products_list)

            else:
                self._send_json_response({"error": "Endpoint не найден"}, 404)

        except Exception as e:
            import traceback
            print(f"Ошибка в do_GET: {e}")
            print(traceback.format_exc())
            self._send_json_response({"error": str(e)}, 500)

        finally:
            conn.close()

    def do_POST(self):
        """Обработка POST запросов"""
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)

        conn = sqlite3.connect(DB_FILE)
        cursor = conn.cursor()

        try:
            if self.path == "/products":
                product = post_data.decode('utf-8').strip()

                if product:
                    cursor.execute(
                        "INSERT OR IGNORE INTO user_products (product) VALUES (?)",
                        (product,)
                    )
                    conn.commit()

                    self._send_json_response({
                        "status": "success",
                        "message": f"Продукт '{product}' добавлен"
                    })
                else:
                    raise ValueError("Пустое название продукта")

            else:
                self._send_json_response({"error": "Endpoint не найден"}, 404)

        except Exception as e:
            self._send_json_response({"error": str(e)}, 400)

        finally:
            conn.close()

    def do_DELETE(self):
        """Обработка DELETE запросов"""
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)

        conn = sqlite3.connect(DB_FILE)
        cursor = conn.cursor()

        try:
            if self.path == "/products":
                product = post_data.decode('utf-8').strip()

                cursor.execute(
                    "DELETE FROM user_products WHERE product = ?",
                    (product,)
                )
                conn.commit()

                deleted_count = cursor.rowcount

                self._send_json_response({
                    "status": "success",
                    "message": f"Удалено {deleted_count} продукт(ов)",
                    "deleted": deleted_count
                })

            else:
                self._send_json_response({"error": "Endpoint не найден"}, 404)

        except Exception as e:
            self._send_json_response({"error": str(e)}, 500)

        finally:
            conn.close()


server = HTTPServer(("0.0.0.0", 8080), RequestHandler)
print("=" * 50)
print("Сервер CookingAssistant запущен!")
print("Адрес: http://0.0.0.0:8080")
print("\nДоступные endpoint'ы:")
print("  GET    /recipes          - получить все рецепты")
print("  GET    /recipe/{name}    - получить конкретный рецепт")
print("  GET    /products         - получить продукты пользователя")
print("  POST   /products         - добавить продукт")
print("  DELETE /products         - удалить продукт")
print("=" * 50)
server.serve_forever()