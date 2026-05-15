## Aplicación Android para reportes ciudadanos

---

## Estructura del proyecto

```
NodoCivico/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/nodocivico/app/
│       │   ├── NodoCivicoApp.kt              ← Application class
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   ├── NodoCivicoDatabase.kt ← Room Database
│       │   │   │   ├── dao/Daos.kt           ← 7 DAOs
│       │   │   │   └── entity/Entities.kt    ← 7 entidades Room
│       │   │   ├── remote/
│       │   │   │   ├── ApiService.kt         ← Retrofit interface + DTOs
│       │   │   │   └── RetrofitClient.kt     ← Singleton Retrofit
│       │   │   └── repository/
│       │   │       ├── ReportRepository.kt   ← Offline-first + sync
│       │   │       └── AuthRepository.kt     ← Login / Register
│       │   ├── domain/model/Models.kt        ← Modelos de dominio
│       │   ├── ui/
│       │   │   ├── MainActivity.kt           ← NavController + BottomNav
│       │   │   ├── auth/                     ← Splash, Login, Register
│       │   │   ├── home/                     ← Home, Sync, Map (stubs)
│       │   │   ├── reports/                  ← List, Detail, Create, Edit
│       │   │   ├── profile/                  ← Profile, Settings
│       │   ├── adapters/ReportAdapter.kt     ← RecyclerView adapter
│       │   ├── receivers/Receivers.kt        ← 3 BroadcastReceivers
│       │   ├── viewmodel/                    ← AuthVM, ReportVM, HomeVM
│       │   └── utils/                        ← Extensions, Network, Prefs
│       └── res/
│           ├── layout/                       ← 12 layouts XML
│           ├── navigation/nav_graph.xml      ← Navigation Component
│           ├── menu/bottom_nav_menu.xml
│           ├── drawable/                     ← 20 vector drawables
│           └── values/                       ← colors, strings, themes, arrays
├── api_server.py                             ← API Flask lista para correr
├── API_CONTRACT.md                           ← Contrato JSON de endpoints
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---



---

## Cómo correr la API Flask

```bash
# 1. Instalar dependencias
pip install flask flask-cors

# 2. Correr el servidor
python api_server.py

# La API queda disponible en:
#   http://localhost:5000         (desde el PC)
#   http://10.0.2.2:5000          (desde el emulador Android)
```

**Usuario de prueba precargado:**
- Email: `test@nodocivico.com`
- Contraseña: `test1234`

---

## Tecnologías implementadas

| Tecnología              | Estado       | Archivo principal              |
|-------------------------|-------------|-------------------------------|
| Kotlin                  | ✅ Completo  | Todos los .kt                  |
| MVVM Architecture       | ✅ Completo  | viewmodel/                     |
| Navigation Component    | ✅ Completo  | nav_graph.xml                  |
| Room Database           | ✅ Completo  | NodoCivicoDatabase.kt          |
| Retrofit                | ✅ Completo  | ApiService.kt, RetrofitClient  |
| ViewBinding             | ✅ Completo  | Todos los Fragments            |
| Material Design 3       | ✅ Completo  | themes.xml, layouts            |
| LiveData / ViewModel    | ✅ Completo  | viewmodel/                     |
| Repository Pattern      | ✅ Completo  | repository/                    |
| BroadcastReceivers (3)  | ✅ Completo  | receivers/Receivers.kt         |
| DataStore Preferences   | ✅ Completo  | utils/UserPreferences.kt       |
| RecyclerView            | ✅ Completo  | ReportAdapter.kt               |
| Flask API               | ✅ Completo  | api_server.py                  |
| BottomNavigationView    | ✅ Completo  | activity_main.xml              |
| SafeArgs Navigation     | ✅ Completo  | nav_graph.xml + NavDirections  |

---

## Fragments incluidos 

| Fragment                  | Estado       | Descripción                     |
|---------------------------|-------------|--------------------------------|
| SplashFragment            | ✅ Funcional | Redirige a Login o Home         |
| LoginFragment             | ✅ Funcional | Autenticación con validaciones  |
| RegisterFragment          | ✅ Funcional | Registro de nuevo ciudadano     |
| HomeFragment              | ✅ Funcional | Dashboard con stats y reportes  |
| ReportListFragment        | ✅ Funcional | Lista con filtros y FAB         |
| ReportDetailFragment      | ✅ Funcional | Detalle + editar/eliminar       |
| CreateReportFragment      | ✅ Funcional | Formulario validado             |
| EditReportFragment        | ✅ Stub      | Completo en Entregable 2        |
| CalendarRemindersFragment | ✅ Stub      | Completo en Entregable 3        |
| SyncStatusFragment        | ✅ Funcional | Estado de sync + receivers      |
| ProfileFragment           | ✅ Funcional | Datos del usuario + logout      |
| SettingsFragment          | ✅ Funcional | Tema, notificaciones            |
| MapZoneFragment           | ✅ Stub      | Completo en Entregable 3        |

---

## Endpoints de la API

| Método | Ruta              | Descripción           | Auth |
|--------|-------------------|-----------------------|------|
| POST   | /login            | Autenticar usuario    | No   |
| POST   | /register         | Registrar usuario     | No   |
| GET    | /reports          | Listar reportes       | Sí   |
| GET    | /reports/{id}     | Detalle de reporte    | Sí   |
| POST   | /reports          | Crear reporte         | Sí   |
| PUT    | /reports/{id}     | Actualizar reporte    | Sí   |
| DELETE | /reports/{id}     | Eliminar reporte      | Sí   |
| GET    | /categories       | Listar categorías     | No   |

---


