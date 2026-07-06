# FoodMaster

Gestor de comida inteligente para Android. Este repositorio implementa el
**Feature 1 (escaneo de código de barras)** del diseño técnico: una pantalla de
inicio con un botón que abre la cámara y **detecta el código de barras
automáticamente** (sin pulsar ningún disparador), consulta el producto en
**Open Food Facts**, lo **cachea en Room** (offline-first) y muestra una ficha
con nombre, marca, imagen y macros por 100 g/ml. Si el código no existe o falla
la red, se informa al usuario para dar de alta el producto manualmente más
adelante.

## Stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **CameraX** para la vista previa y el análisis de imagen
- **ML Kit Barcode Scanning** (on-device, sin red) para decodificar EAN-13/8 y UPC-A/E
- **Retrofit + OkHttp + kotlinx.serialization** contra Open Food Facts
- **Room** como fuente de verdad / caché (offline-first)
- **Coil** para imágenes de producto
- **Accompanist Permissions** para el permiso de cámara en Compose
- DI manual ligera (`AppContainer`), migrable a Hilt más adelante
- Versiones fijadas en `gradle/libs.versions.toml` (version catalog)

## Estructura

```
app/src/main/java/com/foodmaster/app/
  MainActivity.kt                 punto de entrada + pantalla de inicio (botón)
  FoodMasterApplication.kt        crea el AppContainer
  ui/Theme.kt                     tema Material 3 (con dynamic color)
  di/AppContainer.kt              wiring de Room, Retrofit y repositorios
  domain/
    model/Product.kt              modelos de dominio (Product, Macros…)
    repository/ProductRepository  interfaz de repositorio
  data/
    remote/                       OpenFoodFactsApi + DTOs
    local/                        Room: entidad, DAO, base de datos
    mapper/                       DTO ↔ entidad ↔ dominio
    repository/                   ProductRepositoryImpl (cache-first)
  scanner/
    ScannerViewModel.kt           estado por fases (Scanning/Loading/Found…)
    ScannerScreen.kt              permiso + overlay + ficha de producto
    CameraPreview.kt              vista previa CameraX + ImageAnalysis
    BarcodeAnalyzer.kt            analizador ML Kit con debounce
```

## Cómo compilar

Requiere el **Android SDK** (compileSdk 35) y JDK 17+. Ábrelo en Android Studio,
o desde la línea de comandos con el SDK configurado en `local.properties`:

```bash
./gradlew assembleDebug
```

Instala en un dispositivo o emulador con cámara:

```bash
./gradlew installDebug
```

> El escaneo usa la cámara física; el emulador puede simularla con una imagen
> virtual, pero se recomienda un dispositivo real para probar la detección.

## Flujo actual

1. Pantalla de inicio → botón **"Escanear código de barras"**.
2. Se solicita el permiso de cámara (con enlace a ajustes si se deniega).
3. Vista previa a pantalla completa; la detección es continua y automática.
4. Al leer un código: caché local → Open Food Facts → se cachea el resultado.
5. Se muestra la **ficha del producto** (nombre, marca, imagen, macros/100) o un
   aviso de "no encontrado" / error, con opción de escanear otro.

## Siguientes pasos (según el diseño técnico)

Alta manual de productos no encontrados, y el bucle de inventario:
guardar el producto escaneado en el inventario (cantidad + ubicación +
caducidad) y generar la lista de la compra por stock bajo. Ver el documento de
diseño para la hoja de ruta completa por fases.
