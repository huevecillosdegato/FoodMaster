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

## Flujo actual (Fase 1 — MVP)

Navegación inferior con tres pestañas: **Inventario · Escanear · Compra**.

**Escanear**
1. Vista previa a pantalla completa; la detección es continua y automática.
2. Al leer un código: caché local → Open Food Facts → se cachea el resultado.
3. Ficha del producto (nombre, marca, imagen, macros/100) o aviso de
   "no encontrado" / error.
4. **Añadir al inventario**: cantidad + unidad + ubicación + caducidad
   (opcional) + umbral de stock bajo (opcional).

**Inventario**
- Lista de lo que tienes en casa (cantidad, ubicación, caducidad).
- **Consumir** una cantidad (la resta se hace en unidad base) o eliminar.
- Al bajar del umbral, el producto entra **automáticamente** en la lista de la
  compra (regla idempotente: no duplica líneas).

**Compra**
- Lista con líneas manuales y auto-generadas.
- Marca lo comprado y pulsa **"Comprado → añadir al inventario"**: los ítems
  marcados vuelven al inventario, cerrando el bucle
  escanear → guardar → consumir → reponer → comprar.

## Bucle funcional

```
Escanear ──► Producto (OFF/caché) ──► Añadir al inventario
                                            │
                              Consumir ◄────┘
                                 │
                    stock ≤ umbral ──► Lista de la compra (auto)
                                            │
                         Comprado ─────► Inventario
```

## Fase 2 — Nutrición y recetas

Navegación ampliada a cinco pestañas: **Inventario · Escanear · Compra ·
Recetas · Estadísticas**.

- **Recetas**: crea recetas (nombre, raciones, ingredientes desde el catálogo de
  productos escaneados, pasos). Muestra macros **por ración**.
- **Preparar** una receta: calcula y guarda un `Meal` con sus macros
  (snapshot del momento) y **consume cada ingrediente del inventario**, lo que a
  su vez dispara la reposición automática de la compra. Cierra el bucle
  inventario ↔ nutrición.
- **Estadísticas**: resumen de macros de hoy y de los últimos 7 días a partir de
  las comidas preparadas.
- **Alertas de caducidad**: `WorkManager` diario que notifica los productos que
  caducan en ≤ 3 días (permiso `POST_NOTIFICATIONS` en Android 13+).

Cálculo de macros: `Σ (macros/100 × gramos-o-ml / 100)`. Los ingredientes por
pieza se omiten hasta disponer de peso por unidad (mejora de Fase 3+).

## Siguientes pasos (según el diseño técnico)

Fase 3: OCR de facturas (ML Kit Text Recognition), parsers por tienda, matching
de productos, historial de precios y **coste por comida**. Pendientes menores:
alta manual de productos no encontrados y peso por pieza para macros de
ingredientes contados. Ver el documento de diseño para la hoja de ruta completa.
