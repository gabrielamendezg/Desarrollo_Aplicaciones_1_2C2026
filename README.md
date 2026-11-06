# PioTask

Gestor de atención personal para Android: guarda todas las tareas e ideas, pero solo muestra activamente las que necesitan atención ahora.

Trabajo práctico de Desarrollo de Aplicaciones I (UADE).

## Requisitos

- Android Studio Quail 3 (2026.1.3) o posterior
- JDK 17 o superior
- Android SDK 37 para compilar; la app corre desde Android 8.0 (API 26)

## Cómo ejecutar

Abrir la carpeta en Android Studio y ejecutar la configuración `app`, o por consola:

```bash
./gradlew build
```

## Arquitectura

MVVM con capas de Clean Architecture en un único módulo `:app`. Room es la única fuente de verdad y la UI observa el estado con `StateFlow`.

```
app/src/main/java/ar/edu/uade/piotask/
 ├── core/            # Resource, UiState, tema y rutas de navegación
 ├── data/            # Room, DataStore, mappers e implementación de repositorios
 ├── domain/          # Kotlin puro: modelos, interfaces de repositorio y casos de uso
 ├── presentation/    # Pantallas Compose, ViewModels y widget Glance
 ├── worker/          # Reevaluación diaria con WorkManager
 └── di/              # Módulos de Hilt
```

Las dependencias apuntan hacia `domain`, que no conoce Android. CI falla si esa capa importa `android` o `androidx`.

## Estado del proyecto

La app ya funciona de punta a punta para crear tareas, verlas en la Rama o el Nido, registrar avance, posponer y completar con deshacer, además de editar y eliminar. Faltan el widget y la reevaluación diaria.

| Área | Estado |
|---|---|
| Base del proyecto, Hilt y CI | Hecho |
| Reglas de atención, puntaje y top N (`domain/`) | Hecho, con tests; fórmula pendiente de aprobación |
| Casos de uso de escritura y repositorios (`domain/`) | Hecho, con tests |
| Persistencia con Room y DataStore (`data/`) | Hecho; faltan tests instrumentados de Room |
| Pantallas Compose según el Figma: Onboarding, Rama, Nido, Detalle, Nueva/Editar tarea y Ajustes (`presentation/`) | Hecho |
| Widget y reevaluación diaria | Pendiente |
| Validación con usuarios y caso de negocio | Pendiente |

## Reglas de atención

Los estados, la fórmula del puntaje, el orden y la posposición están en [docs/reglas-atencion.md](docs/reglas-atencion.md). Los umbrales y los pesos todavía no están validados con usuarios.

Para correr los tests del dominio:

```bash
./gradlew testDebugUnitTest
```

## Forma de trabajo

- **Ramas:** `main` estable, `develop` de integración y `feature/...`, `chore/...`, `test/...` o `docs/...` para cada cambio.
- **Commits:** pequeños y con [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/) (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`, `ci:`).
- **Pull requests:** uno por funcionalidad, hacia `develop`, indicando el RF que cubre y con revisión de otro integrante.
- **Seguimiento:** un issue por RF.

## Integrantes

- Gabriela Elizabeth Mendez Gutierrez
- Felipe Forte
- Florencia Paucar Barrenechea Crisler
- Matias Young Christiansen
