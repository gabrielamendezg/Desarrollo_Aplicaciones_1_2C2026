# PioTask

Gestor de atención personal para Android: guarda todas las tareas e ideas, pero solo muestra activamente las que necesitan atención ahora.

> "Todo lo que querés hacer, a la vista solo cuando importa."

Trabajo práctico de Desarrollo de Aplicaciones I (UADE). Diseño: [wireframes en Figma](https://www.figma.com/design/Ti47Yc1Tr8jGOTCyL4lfMZ/PioTask-%E2%80%94-Wireframes-TPO).

## Estado del proyecto

La app funciona de punta a punta sin conexión: onboarding, alta, edición y baja de tareas, Rama y Nido, registrar avance, posponer y completar con "Deshacer", categorías y criterios en Ajustes.

| Área | Estado |
|---|---|
| Base del proyecto, Hilt y CI | Hecho |
| Reglas de atención, puntaje y top N (`domain/`) | Hecho, con tests; fórmula pendiente de aprobación |
| Casos de uso de escritura y repositorios (`domain/`) | Hecho, con tests |
| Persistencia con Room y DataStore (`data/`) | Hecho; faltan tests instrumentados de Room |
| Pantallas según el Figma: Onboarding, Rama, Nido, Detalle, Nueva/Editar tarea y Ajustes | Hecho |
| Widget de pantalla de inicio (Glance) | Pendiente |
| Reevaluación diaria (WorkManager) | Pendiente |
| Validación con usuarios y caso de negocio | Pendiente |

El detalle de qué cumple cada requisito de la preentrega está en [`docs/validacion-teoria-y-arquitectura.pdf`](docs/validacion-teoria-y-arquitectura.pdf).

## Requisitos

- Android Studio Quail 3 (2026.1.3) o posterior
- JDK 17 o superior
- Android SDK 37 para compilar; la app corre desde Android 8.0 (API 26)

## Cómo ejecutar

Abrir la carpeta en Android Studio y ejecutar la configuración `app`, o por consola:

```bash
./gradlew build                  # compila, corre los tests y el lint
./gradlew testDebugUnitTest      # solo los tests unitarios
./gradlew installDebug           # instala en el dispositivo o emulador conectado
```

Si `./gradlew` falla con un error de `jlink` al transformar `core-for-system-modules.jar`, está usando un JDK que no lo soporta (por ejemplo GraalVM 24). Usá el JDK que trae Android Studio:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

`local.properties` (con `sdk.dir`) lo genera Android Studio y no se versiona.

## Arquitectura

MVVM con capas de Clean Architecture en un único módulo `:app`. Room es la única fuente de verdad y la UI observa el estado con `StateFlow`. El estado de atención no se guarda: se calcula con la fecha actual cada vez.

```
app/src/main/java/ar/edu/uade/piotask/
 ├── core/            # Resource, UiState, tema (colores, tipografía) y rutas de navegación
 ├── data/            # Room (entidades, DAOs), DataStore, mappers e implementación de repositorios
 ├── domain/          # Kotlin puro: modelos, interfaces de repositorio y casos de uso
 ├── presentation/    # Pantallas Compose, ViewModels y componentes comunes
 └── di/              # Módulos de Hilt
```

Las dependencias apuntan hacia `domain`, que no conoce Android. CI falla si esa capa importa `android` o `androidx`.

**Flujo de datos:** pantalla Compose → ViewModel → caso de uso → interfaz de repositorio (`domain`) ← implementación (`data`) → Room o DataStore. Los datos suben como `Flow`; los eventos bajan como llamadas a casos de uso.

**Repositorios:** `TareaRepository`, `CategoriaRepository`, `ActividadRepository` (solo lectura) y `PreferenciasRepository`. Las escrituras que deben ser atómicas (guardar una tarea y su actividad, o revertirlas) pasan por `TareaRepository` en una sola transacción.

**Avisos con "Deshacer":** viven en un bus compartido (`AvisosBus`) y se muestran en un único snackbar de la app, para que sobrevivan al cambio de pantalla.

## Reglas de atención

Los estados, la fórmula del puntaje, el orden y la posposición están en [docs/reglas-atencion.md](docs/reglas-atencion.md). Los umbrales y los pesos todavía no están validados con usuarios.

## Pruebas

48 tests unitarios en `app/src/test`: reglas de estado, puntaje y orden, casos de uso de escritura (con repositorios en memoria), validaciones y mapeo entre entidades y modelos.

```bash
./gradlew testDebugUnitTest
```

Todavía no hay tests de Room, de ViewModels ni de la UI. Las pantallas se probaron a mano en el emulador.

## Diseño

- **Pájaros, fondo de la rama e íconos:** exportados de los wireframes de Figma a `app/src/main/res/drawable-nodpi` y `drawable`.
- **Tipografía:** IBM Plex Sans e IBM Plex Mono (licencia SIL OFL), en `res/font`.
- **Colores:** definidos en `core/theme/Color.kt` con los valores del Figma.

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
