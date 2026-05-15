"""
Nodo Cívico — API REST con Flask
Entregable 1: Servidor base con endpoints de autenticación, reportes y categorías.

Requisitos:
    pip install flask flask-cors

Ejecutar:
    python api_server.py

La API corre en http://localhost:5000
Desde el emulador Android acceder con http://10.0.2.2:5000
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
from datetime import datetime
import hashlib
import secrets
import time

app = Flask(__name__)
CORS(app)

# ─────────────────────────────────────────────────────────────────────────────
# Base de datos simulada en memoria (para Entregable 1)
# En el Entregable 2 se migrará a SQLite con Flask-SQLAlchemy
# ─────────────────────────────────────────────────────────────────────────────

users_db = {}           # email -> user dict
tokens_db = {}          # token -> email
reports_db = {}         # id -> report dict
report_id_counter = [1]

CATEGORIES = [
    {"id": 1, "name": "Alumbrado",          "icon": "ic_lightbulb"},
    {"id": 2, "name": "Aseo",               "icon": "ic_trash"},
    {"id": 3, "name": "Seguridad",          "icon": "ic_shield"},
    {"id": 4, "name": "Servicios públicos", "icon": "ic_water"},
    {"id": 5, "name": "Vías",               "icon": "ic_road"},
    {"id": 6, "name": "Espacios comunes",   "icon": "ic_park"},
    {"id": 7, "name": "Ruido",              "icon": "ic_sound"},
    {"id": 8, "name": "Otro",               "icon": "ic_flag"},
]


# ─────────────────────────────────────────────────────────────────────────────
# Helpers
# ─────────────────────────────────────────────────────────────────────────────

def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode()).hexdigest()


def generate_token() -> str:
    return secrets.token_hex(32)


def get_user_from_token(request) -> dict | None:
    auth = request.headers.get("Authorization", "")
    if not auth.startswith("Bearer "):
        return None
    token = auth[7:]
    email = tokens_db.get(token)
    if not email:
        return None
    return users_db.get(email)


def error(msg: str, code: int = 400):
    return jsonify({"error": msg}), code


def success(data, code: int = 200):
    return jsonify(data), code


# ─────────────────────────────────────────────────────────────────────────────
# Auth endpoints
# ─────────────────────────────────────────────────────────────────────────────

@app.route("/login", methods=["POST"])
def login():
    data = request.get_json(silent=True) or {}
    email    = data.get("email", "").strip().lower()
    password = data.get("password", "")

    if not email or not password:
        return error("Email y contraseña son obligatorios")

    user = users_db.get(email)
    if not user or user["password"] != hash_password(password):
        return error("Credenciales inválidas", 401)

    token = generate_token()
    tokens_db[token] = email

    return success({
        "token":   token,
        "user_id": user["id"],
        "name":    user["name"],
        "email":   user["email"],
        "zone":    user["zone"],
        "role":    user["role"],
    })


@app.route("/register", methods=["POST"])
def register():
    data = request.get_json(silent=True) or {}
    name     = data.get("name", "").strip()
    email    = data.get("email", "").strip().lower()
    password = data.get("password", "")
    zone     = data.get("zone", "").strip()

    if not name or not email or not password:
        return error("Nombre, email y contraseña son obligatorios")
    if len(name) < 2:
        return error("El nombre debe tener al menos 2 caracteres")
    if len(password) < 6:
        return error("La contraseña debe tener al menos 6 caracteres")
    if email in users_db:
        return error("El email ya está registrado", 409)

    user_id = len(users_db) + 1
    user = {
        "id":       user_id,
        "name":     name,
        "email":    email,
        "password": hash_password(password),
        "zone":     zone,
        "role":     "citizen",
    }
    users_db[email] = user

    token = generate_token()
    tokens_db[token] = email

    return jsonify({
        "token":   token,
        "user_id": user_id,
        "name":    name,
        "email":   email,
        "zone":    zone,
        "role":    "citizen",
    }), 201


# ─────────────────────────────────────────────────────────────────────────────
# Reports endpoints
# ─────────────────────────────────────────────────────────────────────────────

@app.route("/reports", methods=["GET"])
def get_reports():
    user = get_user_from_token(request)
    if not user:
        return error("No autorizado", 401)

    user_reports = [r for r in reports_db.values() if r["created_by"] == user["email"]]
    user_reports.sort(key=lambda r: r["date"], reverse=True)
    return success(user_reports)


@app.route("/reports/<int:report_id>", methods=["GET"])
def get_report(report_id):
    user = get_user_from_token(request)
    if not user:
        return error("No autorizado", 401)

    report = reports_db.get(report_id)
    if not report or report["created_by"] != user["email"]:
        return error("Reporte no encontrado", 404)

    return success(report)


@app.route("/reports", methods=["POST"])
def create_report():
    user = get_user_from_token(request)
    if not user:
        return error("No autorizado", 401)

    data = request.get_json(silent=True) or {}
    title       = data.get("title", "").strip()
    description = data.get("description", "").strip()
    location    = data.get("location", "").strip()
    category_id = data.get("category_id", 1)
    status_id   = data.get("status_id", 1)
    priority    = data.get("priority", "Media")
    image_uri   = data.get("image_uri")

    # Validaciones
    if not title or len(title) < 5:
        return error("El título debe tener al menos 5 caracteres")
    if not description or len(description) < 10:
        return error("La descripción debe tener al menos 10 caracteres")
    if not location:
        return error("La ubicación es obligatoria")
    if priority not in ("Baja", "Media", "Alta"):
        return error("La prioridad debe ser Baja, Media o Alta")

    rid = report_id_counter[0]
    report_id_counter[0] += 1

    report = {
        "id":          rid,
        "title":       title,
        "description": description,
        "category_id": category_id,
        "status_id":   status_id,
        "priority":    priority,
        "location":    location,
        "date":        data.get("date", int(time.time() * 1000)),
        "image_uri":   image_uri,
        "created_by":  user["email"],
    }
    reports_db[rid] = report
    return jsonify(report), 201


@app.route("/reports/<int:report_id>", methods=["PUT"])
def update_report(report_id):
    user = get_user_from_token(request)
    if not user:
        return error("No autorizado", 401)

    report = reports_db.get(report_id)
    if not report or report["created_by"] != user["email"]:
        return error("Reporte no encontrado", 404)

    data = request.get_json(silent=True) or {}
    if "title" in data:
        if len(data["title"].strip()) < 5:
            return error("El título debe tener al menos 5 caracteres")
        report["title"] = data["title"].strip()
    if "description" in data:
        if len(data["description"].strip()) < 10:
            return error("La descripción debe tener al menos 10 caracteres")
        report["description"] = data["description"].strip()
    if "location"    in data: report["location"]    = data["location"].strip()
    if "category_id" in data: report["category_id"] = data["category_id"]
    if "status_id"   in data: report["status_id"]   = data["status_id"]
    if "priority"    in data:
        if data["priority"] not in ("Baja", "Media", "Alta"):
            return error("Prioridad inválida")
        report["priority"] = data["priority"]
    if "image_uri"   in data: report["image_uri"]   = data["image_uri"]

    reports_db[report_id] = report
    return success(report)


@app.route("/reports/<int:report_id>", methods=["DELETE"])
def delete_report(report_id):
    user = get_user_from_token(request)
    if not user:
        return error("No autorizado", 401)

    report = reports_db.get(report_id)
    if not report or report["created_by"] != user["email"]:
        return error("Reporte no encontrado", 404)

    del reports_db[report_id]
    return success({"success": True, "data": None, "message": "Reporte eliminado correctamente"})


# ─────────────────────────────────────────────────────────────────────────────
# Categories endpoint
# ─────────────────────────────────────────────────────────────────────────────

@app.route("/categories", methods=["GET"])
def get_categories():
    return success(CATEGORIES)


# ─────────────────────────────────────────────────────────────────────────────
# Health check
# ─────────────────────────────────────────────────────────────────────────────

@app.route("/", methods=["GET"])
def health():
    return jsonify({
        "status": "ok",
        "app": "Nodo Cívico API",
        "version": "1.0.0",
        "timestamp": datetime.utcnow().isoformat()
    })


# ─────────────────────────────────────────────────────────────────────────────
# Datos de prueba (se cargan al iniciar el servidor)
# ─────────────────────────────────────────────────────────────────────────────

def seed_data():
    """Inserta datos de ejemplo para pruebas."""
    # Usuario de prueba
    test_email = "test@nodocivico.com"
    users_db[test_email] = {
        "id": 1,
        "name": "Usuario Test",
        "email": test_email,
        "password": hash_password("test1234"),
        "zone": "Barrio Bolívar",
        "role": "citizen",
    }
    token = "test_token_entregable1"
    tokens_db[token] = test_email

    # Reportes de ejemplo
    sample_reports = [
        ("Luminaria dañada en la calle 12",
         "La luminaria lleva 3 días apagada causando inseguridad nocturna en el sector.",
         1, 1, "Alta", "Calle 12 con Carrera 5, Sector Norte"),
        ("Basura acumulada en el parque central",
         "Hay varios días de basura sin recoger junto al quiosco principal del parque.",
         2, 2, "Media", "Parque Central, Barrio Bolívar"),
        ("Fuga de agua frente al bloque B",
         "Fuga visible desde las 6 AM, el agua corre por la acera afectando el tráfico peatonal.",
         4, 3, "Alta", "Frente al Bloque B, Conjunto Los Pinos"),
    ]
    for title, desc, cat, status, prio, loc in sample_reports:
        rid = report_id_counter[0]
        report_id_counter[0] += 1
        reports_db[rid] = {
            "id": rid, "title": title, "description": desc,
            "category_id": cat, "status_id": status, "priority": prio,
            "location": loc, "date": int(time.time() * 1000),
            "image_uri": None, "created_by": test_email,
        }

    print("\n🌱 Datos de prueba cargados:")
    print(f"   Usuario: {test_email}  /  Contraseña: test1234")
    print(f"   Token de prueba: {token}")
    print(f"   Reportes: {len(reports_db)}\n")


if __name__ == "__main__":
    seed_data()
    print("🚀 Nodo Cívico API corriendo en http://localhost:5000")
    print("   Desde emulador Android: http://10.0.2.2:5000\n")
    app.run(debug=True, host="0.0.0.0", port=5000)
