# FoodMaster

Gestor de comida inteligente para Android. Este repositorio arranca con el
**Feature 1 (escaneo de código de barras)** del diseño técnico, en su versión
mínima: una pantalla de inicio con un botón que abre la cámara y **detecta el
código de barras automáticamente** (sin pulsar ningún disparador), mostrando el
número escaneado.

## Stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **CameraX** para la vista previa y el análisis de imagen
- **ML Kit Barcode Scanning** (on-device, sin red) para decodificar EAN-13/8 y UPC-A/E
- **Accompanist Permissions** para el permiso de cámara en Compose
- Versiones fijadas en `gradle/libs.versions.toml` (version catalog)

## Estructura

```
app/
  src/main/java/com/foodmaster/app/
    MainActivity.kt              punto de entrada + pantalla de inicio (botón)
    ui/Theme.kt                  tema Material 3 (con dynamic color)
    scanner/
      ScannerScreen.kt           permiso de cámara + overlay + mensaje con el nº
      CameraPreview.kt           vista previa CameraX + ImageAnalysis
      BarcodeAnalyzer.kt         analizador ML Kit con debounce
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
4. Al leer un código, aparece un diálogo con el **número** y opción de escanear otro.

## Siguientes pasos (según el diseño técnico)

Enriquecer el producto vía Open Food Facts, guardar en Room (offline-first) y
enlazar con inventario y lista de la compra. Ver el documento de diseño para la
hoja de ruta completa por fases.
