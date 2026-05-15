# Nodo Cívico — Contrato de API REST
# Entregable 1: Esquema JSON y Documentación de Endpoints
# Base URL: http://10.0.2.2:5000  (emulador) | http://<IP_LOCAL>:5000 (dispositivo físico)

## ─────────────────────────────────────────────
##  AUTENTICACIÓN
## ─────────────────────────────────────────────

### POST /login
# Descripción: Autentica un usuario existente y devuelve un token JWT.
# Request body:
{
  "email": "usuario@ejemplo.com",
  "password": "secreto123"
}
# Response 200 OK:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user_id": 1,
  "name": "Carlos Pérez",
  "email": "usuario@ejemplo.com",
  "zone": "Barrio Bolívar",
  "role": "citizen"
}
# Response 401 Unauthorized:
{
  "error": "Credenciales inválidas"
}

---

### POST /register
# Descripción: Crea una nueva cuenta de ciudadano.
# Request body:
{
  "name": "Carlos Pérez",
  "email": "usuario@ejemplo.com",
  "password": "secreto123",
  "zone": "Barrio Bolívar"
}
# Response 201 Created:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user_id": 2,
  "name": "Carlos Pérez",
  "email": "usuario@ejemplo.com",
  "zone": "Barrio Bolívar",
  "role": "citizen"
}
# Response 409 Conflict:
{
  "error": "El email ya está registrado"
}

## ─────────────────────────────────────────────
##  REPORTES
## ─────────────────────────────────────────────
# Header requerido en todos los endpoints protegidos:
# Authorization: Bearer <token>

### GET /reports
# Descripción: Lista todos los reportes del usuario autenticado.
# Response 200 OK:
[
  {
    "id": 1,
    "title": "Luminaria dañada en la calle 12",
    "description": "La luminaria lleva 3 días apagada causando inseguridad nocturna.",
    "category_id": 1,
    "status_id": 1,
    "priority": "Alta",
    "location": "Calle 12 con Carrera 5, Sector Norte",
    "date": 1746576000000,
    "image_uri": null,
    "created_by": "usuario@ejemplo.com"
  },
  {
    "id": 2,
    "title": "Basura acumulada en el parque central",
    "description": "Hay varios días de basura sin recoger junto al quiosco principal.",
    "category_id": 2,
    "status_id": 2,
    "priority": "Media",
    "location": "Parque Central, Barrio Bolívar",
    "date": 1746489600000,
    "image_uri": "http://server/images/report_2.jpg",
    "created_by": "usuario@ejemplo.com"
  }
]

---

### GET /reports/{id}
# Descripción: Devuelve el detalle de un reporte por su ID.
# Path param: id (integer)
# Response 200 OK:
{
  "id": 1,
  "title": "Luminaria dañada en la calle 12",
  "description": "La luminaria lleva 3 días apagada causando inseguridad nocturna.",
  "category_id": 1,
  "status_id": 1,
  "priority": "Alta",
  "location": "Calle 12 con Carrera 5, Sector Norte",
  "date": 1746576000000,
  "image_uri": null,
  "created_by": "usuario@ejemplo.com"
}
# Response 404 Not Found:
{
  "error": "Reporte no encontrado"
}

---

### POST /reports
# Descripción: Crea un nuevo reporte ciudadano.
# Request body:
{
  "title": "Fuga de agua frente al bloque B",
  "description": "Hay una fuga visible desde las 6 AM, el agua corre por la acera.",
  "category_id": 4,
  "status_id": 1,
  "priority": "Alta",
  "location": "Frente al Bloque B, Conjunto Residencial Los Pinos",
  "date": 1746662400000,
  "image_uri": null,
  "created_by": "usuario@ejemplo.com"
}
# Response 201 Created:
{
  "id": 3,
  "title": "Fuga de agua frente al bloque B",
  "description": "Hay una fuga visible desde las 6 AM, el agua corre por la acera.",
  "category_id": 4,
  "status_id": 1,
  "priority": "Alta",
  "location": "Frente al Bloque B, Conjunto Residencial Los Pinos",
  "date": 1746662400000,
  "image_uri": null,
  "created_by": "usuario@ejemplo.com"
}
# Response 400 Bad Request:
{
  "error": "El campo 'title' es obligatorio"
}

---

### PUT /reports/{id}
# Descripción: Actualiza un reporte existente.
# Path param: id (integer)
# Request body (campos a actualizar):
{
  "title": "Fuga de agua frente al bloque B",
  "description": "Actualizado: la fuga ya fue atendida por el fontanero.",
  "category_id": 4,
  "status_id": 3,
  "priority": "Media",
  "location": "Frente al Bloque B, Conjunto Residencial Los Pinos",
  "date": 1746662400000,
  "image_uri": null,
  "created_by": "usuario@ejemplo.com"
}
# Response 200 OK:
{
  "id": 3,
  "title": "Fuga de agua frente al bloque B",
  "description": "Actualizado: la fuga ya fue atendida por el fontanero.",
  "category_id": 4,
  "status_id": 3,
  "priority": "Media",
  "location": "Frente al Bloque B, Conjunto Residencial Los Pinos",
  "date": 1746662400000,
  "image_uri": null,
  "created_by": "usuario@ejemplo.com"
}

---

### DELETE /reports/{id}
# Descripción: Elimina un reporte por su ID.
# Response 200 OK:
{
  "success": true,
  "data": null,
  "message": "Reporte eliminado correctamente"
}
# Response 404 Not Found:
{
  "success": false,
  "data": null,
  "message": "Reporte no encontrado"
}

## ─────────────────────────────────────────────
##  CATEGORÍAS
## ─────────────────────────────────────────────

### GET /categories
# Descripción: Lista todas las categorías disponibles (sin autenticación).
# Response 200 OK:
[
  { "id": 1, "name": "Alumbrado",          "icon": "ic_lightbulb" },
  { "id": 2, "name": "Aseo",               "icon": "ic_trash"     },
  { "id": 3, "name": "Seguridad",          "icon": "ic_shield"    },
  { "id": 4, "name": "Servicios públicos", "icon": "ic_water"     },
  { "id": 5, "name": "Vías",              "icon": "ic_road"      },
  { "id": 6, "name": "Espacios comunes",   "icon": "ic_park"      },
  { "id": 7, "name": "Ruido",              "icon": "ic_sound"     },
  { "id": 8, "name": "Otro",               "icon": "ic_flag"      }
]

## ─────────────────────────────────────────────
##  MODELOS DE DATOS (referencia)
## ─────────────────────────────────────────────

# User:
# id          INTEGER  PRIMARY KEY AUTOINCREMENT
# name        TEXT     NOT NULL
# email       TEXT     NOT NULL UNIQUE
# password    TEXT     NOT NULL (hash bcrypt)
# zone        TEXT     DEFAULT ''
# role        TEXT     DEFAULT 'citizen'

# Report:
# id          INTEGER  PRIMARY KEY AUTOINCREMENT
# title       TEXT     NOT NULL  (min 5 chars)
# description TEXT     NOT NULL  (min 10 chars)
# category_id INTEGER  NOT NULL  FK -> categories.id
# status_id   INTEGER  DEFAULT 1 FK -> report_status.id
# priority    TEXT     CHECK IN ('Baja','Media','Alta')
# location    TEXT     NOT NULL
# date        INTEGER  (Unix timestamp ms)
# image_uri   TEXT     NULLABLE
# created_by  TEXT     (email del creador)

# Category:
# id          INTEGER  PRIMARY KEY
# name        TEXT     NOT NULL
# icon        TEXT     (nombre del drawable)

# ReportStatus:
# id          INTEGER  PRIMARY KEY
# name        TEXT     NOT NULL
# -- Valores: 1=Abierto, 2=En proceso, 3=Cerrado, 4=Rechazado

## ─────────────────────────────────────────────
##  CÓDIGOS DE ERROR ESTÁNDAR
## ─────────────────────────────────────────────
# 200  OK             — Operación exitosa
# 201  Created        — Recurso creado
# 400  Bad Request    — Validación fallida
# 401  Unauthorized   — Token inválido o ausente
# 404  Not Found      — Recurso no existe
# 409  Conflict       — Duplicado (ej: email ya existe)
# 500  Server Error   — Error interno del servidor
