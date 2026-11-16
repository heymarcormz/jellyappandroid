# JellyApp QA

Aplicación Android de laboratorio para recorrer un ecommerce ficticio y verificar la
instrumentación de **Firebase Analytics / GA4**, **AppsFlyer** y **Quantum Metric**.

- Kotlin + Jetpack Compose + Material 3
- `applicationId`: `com.marcormz.jellyapp`
- Fondo negro y acento `#F8B133`
- Ocho pantallas: Home, productos, detalle, carrito, checkout, thank you, WebView y Test lab
- Login sin autenticación real
- Formularios para user properties y custom events 
- IDs estables expuestos como `resource-id` para UIAutomator/Appium

## Requisitos

- Android Studio con JDK 17 o superior
- Android SDK 36
- Un dispositivo o emulador con API 24+

Si `java` no está disponible en la terminal de macOS, usa el JBR incluido en Android Studio:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

## Compilar y ejecutar

```bash
./gradlew assembleDebug
./gradlew installDebug
```

La app arranca sin credenciales externas. En ese caso, los SDKs no configurados se
omiten de forma segura y los eventos se siguen mostrando en Logcat con la etiqueta
`JellyAnalytics`.

Para ejecutar las pruebas:

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

## Configuración local

Los secretos no se guardan en el código ni se versionan. Copia las entradas necesarias
de `local.properties.example` a `local.properties`, conservando `sdk.dir`.

### Firebase / GA4

1. Registra una app Android con el paquete `com.marcormz.jellyapp` en Firebase.
2. Descarga `google-services.json`.
3. Colócalo en `app/google-services.json`.
4. Sincroniza Gradle y vuelve a compilar.

El plugin de Google Services solo se aplica cuando el archivo existe. La instrumentación
manual de pantallas está activada; la captura automática de `screen_view` de Activity se
desactiva en el manifest para evitar eventos duplicados.

Para ver eventos en tiempo real:

```bash
adb shell setprop debug.firebase.analytics.app com.marcormz.jellyapp
adb shell setprop log.tag.FA VERBOSE
adb shell setprop log.tag.FA-SVC VERBOSE
adb logcat -s FA FA-SVC JellyAnalytics
```

Después abre **Firebase Console → Analytics → DebugView**:

```text
https://console.firebase.google.com/u/0/project/_/analytics/debugview
```

Al terminar :

```bash
adb shell setprop debug.firebase.analytics.app .none.
```

### AppsFlyer

Añade la Dev Key en `local.properties`:

```properties
APPSFLYER_DEV_KEY=tu_dev_key
```

La app inicializa AppsFlyer y activa logs de depuración en builds debug, pero no
establece ningún Customer User ID al arrancar. El CUID se asigna únicamente al enviar el
formulario de login.

### Quantum Metric

Quantum Metric necesita valores que normalmente proporciona tu contacto o equipo de
Quantum Metric. No se configuran en `google-services.json`, en la pantalla de JellyApp ni
directamente dentro del código.

#### 1. Archivo y ubicación exacta

Edita este archivo de la raíz del proyecto:

```text
/Users/marcoramalf/Documents/OPEN CODE/JELLYAPP/ANDROID/local.properties
```

No está dentro de `app/` ni de `app/src/`. Si no existe, crea una copia de
`local.properties.example` en la raíz y conserva la entrada `sdk.dir` que añade Android
Studio.

#### 2. Valores que debes añadir

Al final de `local.properties`:

```properties
# Quantum Metric
QM_SUBSCRIPTION=valor_real_proporcionado_por_QM
QM_UID=valor_real_proporcionado_por_QM
QM_MAVEN_USERNAME=valor_real_proporcionado_por_QM
QM_MAVEN_PASSWORD=valor_real_proporcionado_por_QM
QM_TEST_MODE=false
```

Significado de cada valor:

| Clave | Valor necesario |
| --- | --- |
| `QM_SUBSCRIPTION` | Nombre de la subscription de Quantum Metric que recibirá las sesiones. |
| `QM_UID` | UID de la aplicación Android facilitado por Quantum Metric para esa subscription. |
| `QM_MAVEN_USERNAME` | Usuario del repositorio Maven privado de Quantum Metric. |
| `QM_MAVEN_PASSWORD` | Contraseña del repositorio Maven privado de Quantum Metric. |
| `QM_TEST_MODE` | Déjalo en `false`. Usa `true` únicamente si Quantum Metric te indica que debes cargar su configuración de pruebas. |

Ejemplo de cómo debe quedar el archivo completo, sin claves reales:

```properties
sdk.dir=/ruta/a/Android/sdk

# AppsFlyer (opcional si solo estás probando Firebase/Quantum Metric)
APPSFLYER_DEV_KEY=

# Quantum Metric
QM_SUBSCRIPTION=tu_subscription
QM_UID=tu_uid_android
QM_MAVEN_USERNAME=tu_usuario_maven
QM_MAVEN_PASSWORD=tu_password_maven
QM_TEST_MODE=false
```

#### 3. Aplicar la configuración

1. Guarda `local.properties`.
2. En Android Studio pulsa **Sync Project with Gradle Files**.
3. Vuelve a compilar e instalar la app:

   ```bash
   ./gradlew installDebug
   ```

Con las cuatro credenciales presentes, Gradle añade automáticamente la dependencia
`com.quantummetric:quantummetric-android:1.0.18`. La app inicializa el SDK nativo y
Quantum Metric empieza a capturar sesiones, pantallas e interacciones.

#### 4. Comprobar que está activo

Filtra Logcat:

```bash
adb logcat -s JellyAnalytics:I JellyStockBackend:W
```

Debe aparecer:

```text
JellyAnalytics: Quantum Metric inicializado.
```

Si aparece:

```text
JellyAnalytics: Quantum Metric omitido: faltan QM_SUBSCRIPTION, QM_UID o credenciales Maven.
```

revisa que los cuatro valores estén escritos en la raíz de `local.properties`, sin comillas
y sin espacios alrededor del signo `=`.

#### 5. Probar el error automático

1. Abre el catálogo y busca **Teclado Quantum** (`JELLY-006`), que tiene stock 0.
2. Pulsa **Probar sin stock**.
3. La app realizará una petición HTTPS real:

   ```text
   POST https://httpbin.org/status/409?item_id=JELLY-006
   ```

4. Logcat debe mostrar `HTTP 409; OUT_OF_STOCK (409)`.
5. En Quantum Metric, abre la sesión Android más reciente y revisa la actividad de red y
   el error visible en pantalla.

No se envía un evento manual de Quantum Metric para esta prueba: el objetivo es comprobar
su captura automática. Los eventos personalizados de la pantalla Test lab se envían a
Firebase/GA4 y AppsFlyer; Quantum Metric utiliza la captura automática y los eventos que
estén configurados con ID en su plataforma.

`local.properties` está excluido por `.gitignore`. Nunca publiques las credenciales Maven
reales. Documentación oficial de integración:

```text
https://community.quantummetric.com/s/article/Android-Integration-Instructions
```

## Identidad de prueba

- Al abrir la app no hay `user_id` de Firebase ni Customer User ID de AppsFlyer.
- Los eventos anteriores al login se registran de forma anónima.
- Al enviar el formulario, el texto exacto introducido en **Usuario** se usa como
  `user_id` de Firebase y Customer User ID de AppsFlyer.
- Si el campo Usuario está vacío, el login se acepta pero el User ID queda `null`.
- La identificación ocurre antes de registrar el evento `login`, por lo que ese evento ya
  incluye el usuario introducido.
- La contraseña nunca se mide ni se registra.
- Al pulsar **Cerrar sesión**, primero se envía `logout` con el User ID actual y después
  se limpia la identidad. La UI vuelve a mostrar “No activo”.
- Usa únicamente usuarios ficticios: el nombre de Usuario se envía como identificador
  analítico a Firebase y AppsFlyer.

## Mapa de eventos

| Acción | Firebase / GA4 | AppsFlyer |
| --- | --- | --- |
| Pantalla visible | `screen_view` | — |
| Botón pulsado | `select_content` | — |
| Ver catálogo | `view_item_list` | `af_list_view` |
| Abrir producto | `select_item` | — |
| Ver detalle | `view_item` | `af_content_view` |
| Añadir | `add_to_cart` | `af_add_to_cart` |
| Reducir / quitar | `remove_from_cart` | `af_remove_from_cart` |
| Ver carrito | `view_cart` | — |
| Iniciar checkout | `begin_checkout` | `af_initiated_checkout` |
| Confirmar envío | `add_shipping_info` | `af_add_shipping_info` |
| Confirmar pago | `add_payment_info` | `af_add_payment_info` |
| Compra | `purchase` | `af_purchase` |
| Login | `login` | `af_login` |
| Logout | `logout` | `af_logout` |
| User property | `setUserProperty` | — |
| Custom event | nombre introducido | nombre introducido |

Los eventos ecommerce incluyen `currency = MXN`, `value`, `items` e identificadores de
producto. `purchase` añade `transaction_id`, envío e ingresos. El pedido se guarda antes
de vaciar el carrito para que el payload no se pierda.

Nombres de pantalla enviados a GA4:

`home`, `products`, `product_detail`, `cart`, `checkout`, `thank_you`, `webview`,
`account`.

## IDs para automatización

Los controles usan `Modifier.testTag(...)`, y el árbol raíz activa
`testTagsAsResourceId = true`. Esto permite localizarlos tanto desde Compose Test como
como `resource-id` en UIAutomator/Appium.

Ejemplos:

```text
btn_home_start_shopping
btn_nav_products
btn_product_open_JELLY_001
btn_product_add_JELLY_001
btn_product_add_JELLY_006
stock_error_JELLY_006
btn_detail_add_JELLY_001
btn_cart_quantity_plus_JELLY_001
btn_cart_checkout
btn_checkout_purchase
btn_thankyou_home
btn_webview_reload
btn_login_submit
btn_logout_submit
btn_user_property_send
btn_custom_event_add_parameter
btn_custom_event_send
```

Los IDs de productos usan el SKU con guiones reemplazados por guiones bajos:
`JELLY-001` → `JELLY_001`.

## Productos

Todos los importes se muestran y se envían en pesos mexicanos (`MXN`). Los
valores numéricos se mantienen sin conversión.

| SKU | Producto | Categoría | Precio |
| --- | --- | --- | --- |
| JELLY-001 | Auriculares Nébula | Audio | $79.99 MXN |
| JELLY-002 | Smartwatch Órbita | Wearables | $129.99 MXN |
| JELLY-003 | Mochila Atlas | Accesorios | $59.90 MXN |
| JELLY-004 | Lámpara Lumen | Hogar | $39.95 MXN |
| JELLY-005 | Zapatillas Vértice | Calzado | $89.99 MXN |
| JELLY-006 | Teclado Quantum | Edición limitada | $149.99 MXN · sin stock |

`JELLY-006` tiene `stockUnits = 0`. Su botón permanece habilitado a propósito para
repetir la prueba. Cada pulsación realiza una petición HTTP real:

```text
POST https://httpbin.org/status/409?item_id=JELLY-006
```

El backend de prueba responde siempre con `409 Conflict` y la app muestra
`OUT_OF_STOCK` sin modificar el carrito. El tráfico HTTPS y el error visible permiten
comprobar la captura automática de Quantum Metric; no se envía un evento manual a QM para
esta prueba. Los detalles de cada intento aparecen en Logcat con la etiqueta
`JellyStockBackend`.

La prueba requiere conexión a Internet. Si el endpoint no responde, la app muestra un
error de red distinto y tampoco añade el producto.

## WebView

Pantalla `webview` con URL fija:

```text
https://marcormz.com/jellyapp/
```

Incluye JavaScript, DOM storage, cookies de terceros para las herramientas web,
progreso de carga, reintento si falla la página principal, navegación atrás y depuración
WebView habilitada solo en debug.

## Estructura

```text
app/src/main/java/com/marcormz/jellyapp/
├── analytics/    GA4, AppsFlyer, Quantum Metric e identidad
├── data/         catálogo, carrito, pedido e importes
├── ui/           navegación, ViewModel, componentes y tema
│   └── screens/  las ocho pantallas
├── JellyApplication.kt
└── MainActivity.kt
```

## Limitaciones intencionadas

- No hay backend, autenticación, pago ni persistencia del carrito.
- El login acepta cualquier valor, incluido vacío.
- No se recogen datos personales reales.
- El aspecto visual y los productos son ficticios.
- Las credenciales de Firebase, AppsFlyer y Quantum Metric deben aportarse localmente.
- Las versiones AndroidX/Firebase están fijadas a `compileSdk 36`. Las versiones más
  recientes de Compose y Firebase requieren `compileSdk 37` y AGP 9.1; deben actualizarse
  en conjunto cuando el toolchain local incorpore esa API.
