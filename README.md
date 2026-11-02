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
