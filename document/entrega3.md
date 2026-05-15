# ISDCM — Entrega 3
## Aplicaciones Web Seguras — Documentación técnica

**Asignatura:** ISDCM
**Proyecto:** Aplicación web segura de gestión de usuarios y vídeos
**Versión del documento:** 1.0
**Fecha:** 2026-05-15
**Estudiante:** Zhipeng Lin (zhipeng.lin@estudiantat.upc.edu)
**Estudiante:** Zhiwei Lin (zhiwei.lin1@estudiantat.upc.edu)

---

## Tabla de contenidos

1. [Introducción y alcance](#introducción-y-alcance)
2. [Estructura del proyecto](#estructura-del-proyecto)
3. [Parte 1 — Configuración HTTPS en Tomcat](#parte-1--configuración-https-en-tomcat)
4. [Parte 2 — Protección de contenido](#parte-2--protección-de-contenido)
   - [2.1 Cifrado de archivos multimedia](#21-cifrado-de-archivos-multimedia-aescifrado-binario)
   - [2.2 Cifrado de archivos XML](#22-cifrado-de-archivos-xml-apache-santuario-xmlsec)
5. [Parte 3 — Autenticación con JSON Web Tokens](#parte-3--autenticación-con-json-web-tokens)
   - [3.1 Nuevo endpoint REST `/login`](#31-nuevo-endpoint-rest-login)
   - [3.2 Generación de JWT con jjwt](#32-generación-de-jwt-con-jjwt)
   - [3.3 Protección JSON con jose4j (opcional)](#33-protección-json-con-jose4j-opcional)
6. [Modificaciones estructurales sobre la propuesta original](#modificaciones-estructurales-sobre-la-propuesta-original)
7. [Pruebas realizadas](#pruebas-realizadas)
8. [Resumen de entregables](#resumen-de-entregables)

---

## Introducción y alcance

La Entrega 3 amplía el sistema construido en las entregas anteriores con tres ejes de seguridad:

- **Transporte seguro** mediante HTTPS sobre Tomcat con certificado autofirmado.
- **Protección de contenido** mediante cifrado simétrico de archivos binarios (vídeos, imágenes, PDF…) y cifrado XML elemento-a-elemento sobre el ejemplo `didlFilm1.xml`.
- **Autenticación basada en tokens** mediante un endpoint REST `/login` que devuelve un JWT firmado HS256 con `jjwt`, complementado opcionalmente con un JWE A128KW + A128CBC-HS256 producido con `jose4j` (nested JWT).

Toda la implementación se ha construido sobre la base de la Entrega 2 (frontend Servlet/JSP en GlassFish + backend JAX-RS también en GlassFish, persistencia Derby en modo embebido).

---

## Estructura del proyecto

```
Project/entrega3/                  # Proyecto testTomcat para validar HTTPS (Parte 1)
└── src/main/java/.../entrega3/{HelloApplication,HelloResource}.java

isdcm-entrega3/
├── apache-tomcat-9.0.117/         # Tomcat 9 local (Parte 1)
│   └── conf/{server.xml,mykeystore.p12}
├── ISDCM-project-frontend/        # Frontend Servlet + JSP (Maven, WAR)
│   ├── pom.xml
│   ├── document/
│   │   ├── didlFilm1.xml          # Ejemplo MPEG-21 DIDL (cifrado XML)
│   │   └── entrega3.md            # Este documento
│   └── src/main/
│       ├── java/org/example/isdcmproject/
│       │   ├── controller/
│       │   │   ├── AuthSessionFilter.java
│       │   │   ├── servletCrypto.java      # /cifrado  (2.1)
│       │   │   ├── servletXmlCrypto.java   # /xmlcifrado (2.2)
│       │   │   └── servletUsuarios.java    # /login, /tokenJwt (3.x)
│       │   └── crypto/
│       │       ├── CryptoUtil.java         # AES-GCM 256 (2.1)
│       │       └── XmlCryptoUtil.java      # xmlsec 1.5.8 (2.2)
│       └── webapp/WEB-INF/
│           ├── samples/didlFilm1.xml       # Empaquetado en el WAR
│           └── views/{cifrado,xmlCifrado,tokenJwt}.jsp
└── isdcm-lab2-backend/            # Backend JAX-RS (Gradle, WAR)
    ├── build.gradle               # jjwt 0.7.0 + jose4j 0.9.6
    └── src/main/java/org/upc/student/isdcm/entrega2/
        ├── rest/LoginResource.java        # POST /api/login (3.1)
        └── security/JwtUtil.java          # JWS + JWE (3.2 / 3.3)
```

---

## Parte 1 — Configuración HTTPS en Tomcat

> **Nota sobre el entorno:** No se ha utilizado la máquina virtual proporcionada por la asignatura. Toda la Parte 1 se ha realizado en **macOS** (Darwin 24.1) con **OpenJDK 17 (Temurin)** y **Apache Tomcat 9.0.117** instalado de forma local. Por tanto, las recetas específicas del enunciado para la VM (ruta `/usr/lib/jvm/java-17-oracle/bin/keytool`, keystore JKS en `~/.keystore`, parches `maven-compiler-plugin 3.10.1` / `maven-war-plugin 3.3.2`) no resultan necesarias: el equivalente funcional se documenta a continuación.
>
> **Localizaciones reales:**
> - Proyecto de prueba (testTomcat): `/Users/zhipeng/Desktop/Project/entrega3` (artefacto `entrega3-1.0-SNAPSHOT.war`).
> - Servidor Tomcat: `/Users/zhipeng/Desktop/isdcm-entrega3/apache-tomcat-9.0.117`.
> - Configuración SSL: `apache-tomcat-9.0.117/conf/server.xml`.
> - Keystore: `apache-tomcat-9.0.117/conf/mykeystore.p12` (formato **PKCS12**).
>
> Las capturas de pantalla de esta sección se incorporarán manualmente al informe final.

### 1.1 Proyecto `testTomcat` (entrega3)

El proyecto Maven utilizado para validar HTTPS es un WAR mínimo JAX-RS (Jersey 2.46) con un único recurso de prueba:

```java
@Path("/hello-world")
public class HelloResource {
    @GET @Produces("text/plain")
    public String hello() { return "Hello, World!"; }
}
```

`HelloApplication` lo expone en `@ApplicationPath("/api")`, por lo que tras desplegar la URL útil es `https://localhost:8443/entrega3-1.0-SNAPSHOT/api/hello-world`. El `pom.xml` declara `packaging=war`, `maven.compiler.source/target=17` y solo `maven-war-plugin` (versión 3.4.0). Como compilamos con JDK 17 nativo en macOS no se observan los problemas que motivaban en la VM el _downgrade_ a `maven-compiler-plugin 3.10.1` / `maven-war-plugin 3.3.2`, así que se mantienen los _defaults_.

Para compilar y empaquetar:

```bash
cd /Users/zhipeng/Desktop/Project/entrega3
./mvnw clean package
# → target/entrega3-1.0-SNAPSHOT.war
```

El despliegue se realiza desde el IDE (IntelliJ) sobre el Tomcat local instalado en `/Users/zhipeng/Desktop/isdcm-entrega3/apache-tomcat-9.0.117`.

### 1.2 Generación del certificado autofirmado con `keytool`

Se utiliza el `keytool` que acompaña a JDK 17. Se genera **directamente un keystore PKCS12** (formato moderno y portable), evitando el aviso "JKS is a proprietary format" que muestra el `keytool` de Java 17.

**Paso 1 — Par de claves RSA 2048 bits y certificado autofirmado (validez 90 días):**

```bash
keytool -genkeypair \
  -alias tomcat \
  -keyalg RSA -keysize 2048 \
  -validity 90 \
  -storetype PKCS12 \
  -dname "CN=Zhipeng Lin, OU=ISDCM, O=UPC, L=Barcelona, ST=Catalunya, C=ES" \
  -keystore /Users/zhipeng/Desktop/isdcm-entrega3/apache-tomcat-9.0.117/conf/mykeystore.p12 \
  -storepass 123456 -keypass 123456
```

**Paso 2 — Generación del CSR (Certificate Signing Request):**

```bash
keytool -certreq \
  -alias tomcat \
  -file certreq-isdcm.csr \
  -keystore conf/mykeystore.p12 -storepass 123456
```

**Paso 3 — Firma del certificado a partir del CSR** (autofirma con la misma entrada para simular una CA local):

```bash
keytool -gencert \
  -alias tomcat \
  -infile certreq-isdcm.csr \
  -outfile cert-isdcm.pem \
  -validity 90 \
  -keystore conf/mykeystore.p12 -storepass 123456
```

**Paso 4 — Verificación del certificado generado:**

```bash
keytool -printcert -file cert-isdcm.pem
```

**Paso 5 — Importación de la respuesta firmada de vuelta al keystore:**

```bash
keytool -importcert \
  -alias tomcat \
  -file cert-isdcm.pem \
  -keystore conf/mykeystore.p12 -storepass 123456 -noprompt
```

**Paso 6 — Listado del contenido del keystore:**

```bash
keytool -list -keystore conf/mykeystore.p12 -storepass 123456
```

Salida esperada: una entrada con alias **`tomcat`** del tipo `PrivateKeyEntry`.

> _Espacio reservado para captura: salida de `keytool -list` sobre `mykeystore.p12`._

### 1.3 Configuración del conector seguro en `server.xml`

En `apache-tomcat-9.0.117/conf/server.xml` se ha habilitado el conector NIO sobre el puerto **8443** apuntando al keystore PKCS12:

```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true"
           maxParameterCount="1000">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="conf/mykeystore.p12"
                     certificateKeystorePassword="123456"
                     certificateKeystoreType="PKCS12"
                     certificateKeyAlias="tomcat" />
    </SSLHostConfig>
</Connector>
```

Diferencias con la receta de la VM (puramente equivalentes en función):

| Aspecto | Receta VM (enunciado) | Implementación real (macOS) |
|---|---|---|
| Formato keystore | JKS (`~/.keystore`) | **PKCS12** (`conf/mykeystore.p12`) |
| Atributo Tomcat | `type="RSA"` | `certificateKeystoreType="PKCS12"` |
| Alias | `isdcm` | `tomcat` |
| Plugins Maven | `compiler 3.10.1` + `war 3.3.2` | `war 3.4.0` por defecto (JDK 17 nativo) |
| Ruta `keytool` | `/usr/lib/jvm/java-17-oracle/bin/keytool` | `keytool` del JDK 17 de macOS |

### 1.4 Arranque y verificación

```bash
cd /Users/zhipeng/Desktop/isdcm-entrega3/apache-tomcat-9.0.117
./bin/startup.sh
# Logs en logs/catalina.out — debe aparecer "Starting ProtocolHandler [https-jsse-nio-8443]".
```

Tras arrancar Tomcat se accede a:

```
https://localhost:8443/entrega3-1.0-SNAPSHOT/api/hello-world
```

El navegador muestra el aviso de certificado autofirmado; tras aceptar la excepción de seguridad local, devuelve `Hello, World!` sirviendo el contenido sobre **TLS 1.2/1.3**. Esto valida tanto la cadena `keytool → keystore → server.xml` como el despliegue del WAR.

> _Espacio reservado para captura: navegador mostrando el candado en `https://localhost:8443/entrega3-1.0-SNAPSHOT/api/hello-world`._
>
> _Espacio reservado para captura: detalle del certificado emitido (CN, fechas de validez, RSA 2048)._

---

## Parte 2 — Protección de contenido

Los dos módulos de cifrado están integrados como nuevas páginas dentro del frontend GlassFish, accesibles tras autenticación desde la página de búsqueda (`/busqueda`).

### 2.1 Cifrado de archivos multimedia (AES, cifrado binario)

#### Diseño

- **Algoritmo:** `AES/GCM/NoPadding`, clave AES-256, etiqueta GCM de 128 bits.
- **Derivación de clave:** `PBKDF2WithHmacSHA256`, 200 000 iteraciones, sal de 16 bytes generada por `SecureRandom` por archivo.
- **IV:** 12 bytes aleatorios por archivo (`SecureRandom`).
- **Cabecera del archivo cifrado:**
  `MAGIC ("ISDCM1", 6 B)` || `salt (16 B)` || `iv (12 B)` || `ciphertext + tag (N+16 B)`.
- **Streaming:** `CipherInputStream` / `CipherOutputStream` sobre el `OutputStream` del response, evitando cargar el archivo entero en memoria (compatible con vídeos grandes).

#### Implementación

`crypto/CryptoUtil.java` expone dos métodos:

```java
public static void encrypt(InputStream in, OutputStream out, char[] passphrase);
public static void decrypt(InputStream in, OutputStream out, char[] passphrase);
```

Cualquier intento de descifrado con contraseña incorrecta lanza `AEADBadTagException` antes de emitir contenido al cliente, gracias a la autenticación integrada de GCM.

#### Flujo del servlet

`controller/servletCrypto.java` se mapea en `/cifrado` con `@MultipartConfig` (hasta 512 MB por archivo). El JSP `cifrado.jsp` presenta dos formularios paralelos (cifrar / descifrar) con `enctype="multipart/form-data"`, contraseña y selector de archivo. El resultado se devuelve como `application/octet-stream` con `Content-Disposition: attachment` y sufijo `.enc` (al cifrar) o sin él (al descifrar).

#### Validación funcional

1. Cifrado y descifrado de varios tipos de archivo locales (`.mp4`, `.jpg`, `.pdf`) recuperando el archivo idéntico (`shasum` coincidente con el original).
2. Detección de contraseña incorrecta: el endpoint muestra "Contraseña incorrecta o archivo manipulado" sin emitir datos descifrados.
3. Detección de cabecera inválida: archivos sin el _magic_ `ISDCM1` se rechazan con "Archivo no reconocido".

### 2.2 Cifrado de archivos XML (Apache Santuario `xmlsec`)

#### Dependencia añadida

```xml
<dependency>
    <groupId>org.apache.santuario</groupId>
    <artifactId>xmlsec</artifactId>
    <version>1.5.8</version>
    <type>jar</type>
</dependency>
```

`org.apache.xml.security.Init.init()` se invoca en un bloque `static {}` de `XmlCryptoUtil`, garantizando la inicialización del proveedor antes de cualquier uso de `XMLCipher`.

#### Métodos públicos

| Método | Descripción |
|---|---|
| `parse(InputStream)` | Carga un `Document` DOM con namespaces habilitados. |
| `write(Document, OutputStream)` | Serializa el `Document` UTF-8 con indentación. |
| `encryptElement(doc, localName, passphrase)` | Cifra el primer elemento cuyo nombre local coincida (p. ej. `metadata`). |
| `encryptDocument(doc, passphrase)` | Cifra el elemento raíz completo. |
| `decrypt(doc, passphrase)` | Recorre todos los `xenc:EncryptedData` y los restituye. |

La clave AES-128 se deriva de la contraseña con `SHA-256(passphrase)` truncado a 16 bytes, manteniendo simetría entre cifrado y descifrado sin necesidad de almacenar material adicional en el documento.

#### Flujo del servlet

`controller/servletXmlCrypto.java` está mapeado en `/xmlcifrado`. El JSP `xmlCifrado.jsp` permite:

- Elegir el origen del XML: subida manual o ejemplo `didlFilm1.xml` empaquetado en `WEB-INF/samples/`.
- Indicar el _local name_ del elemento a cifrar (por defecto `metadata`).
- Tres acciones: **Cifrar elemento**, **Cifrar documento**, **Descifrar**.
- Ver el resultado en línea (renderizado en un bloque `<pre>`) o descargarlo como `.xml`.

#### Ejemplo de salida con `didlFilm1.xml`

Antes (fragmento relevante):

```xml
<metadata>
    <id>1</id>
    <titulo>No time to die</titulo>
    <autor>Cary Joji Fukunaga</autor>
    ...
</metadata>
```

Después de **Cifrar elemento** sobre `metadata`:

```xml
<metadata>
  <xenc:EncryptedData xmlns:xenc="http://www.w3.org/2001/04/xmlenc#"
                      Type="http://www.w3.org/2001/04/xmlenc#Content">
    <xenc:EncryptionMethod Algorithm="http://www.w3.org/2001/04/xmlenc#aes128-cbc"/>
    <xenc:CipherData><xenc:CipherValue>...base64...</xenc:CipherValue></xenc:CipherData>
  </xenc:EncryptedData>
</metadata>
```

La estructura `DIDL → Item → Component` se conserva, lo que demuestra el cifrado granular. Al descifrar con la misma contraseña se recupera el contenido original.

---

## Parte 3 — Autenticación con JSON Web Tokens

### 3.1 Nuevo endpoint REST `/login`

Se ha añadido `rest/LoginResource.java` en el backend con la firma exacta requerida:

```java
@Path("/")
public class LoginResource {

    @Inject
    private UsuarioRepository repo;

    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response Login(@FormParam("username") String username,
                          @FormParam("password") String password) {
        ...
    }
}
```

- `Gateway.java` declara `@ApplicationPath("/api")`, por lo que la URL completa es **`/api/login`**.
- El recurso **no** lleva la anotación `@Secured`, así que `ApiKeyFilter` no exige `X-API-Key` para iniciar sesión.
- Respuestas:
  - `200 OK` con `{username, apiKey, token, tokenJwe, jwsSecret, jweKey, jweJwk}` si las credenciales son correctas.
  - `400 Bad Request` si falta `username` o `password`.
  - `401 Unauthorized` si las credenciales no validan.

### 3.2 Generación de JWT con jjwt

#### Dependencia (en `build.gradle`, equivalente a la coordenada Maven solicitada)

```groovy
implementation('io.jsonwebtoken:jjwt:0.7.0')
```

#### Construcción del token

`security/JwtUtil.java`:

```java
public static String createToken(String username, String apiKey) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
            .setIssuer("isdcm-backend")
            .setSubject(username)
            .claim("apiKey", apiKey)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + 3_600_000L))
            .signWith(SignatureAlgorithm.HS256, SECRET_BYTES)
            .compact();
}
```

- Firma **HS256** con un secreto compartido (constante `SECRET` de 52 bytes UTF-8).
- Reclamaciones: `iss`, `sub`, `apiKey`, `iat`, `exp` (TTL = 1 h).

#### Invocación desde el frontend

El servlet `servletUsuarios.handleLogin` envía las credenciales al endpoint REST con `Content-Type: application/x-www-form-urlencoded`, lee el JSON de respuesta y guarda `usuarioLogueado`, `apiKey`, `jwt`, `jwe`, `jwsSecret`, `jweKey` y `jweJwk` en la sesión. A continuación redirige a `/tokenJwt`, donde el JSP `tokenJwt.jsp` muestra:

- El JWT en formato compacto, con botones de **Copiar token** y **Abrir en jwt.io**.
- El JWE (sección 3.3).
- Los secretos del servidor (HS256 y JWK del JWE) para facilitar la verificación en jwt.io.

#### Verificación externa (jwt.io)

1. Copiar el JWT y pegarlo en https://jwt.io/.
2. jwt.io decodifica cabecera (`{"alg":"HS256","typ":"JWT"}`) y payload (`iss`, `sub`, `apiKey`, `iat`, `exp`).
3. Pegar `jwsSecret` en el campo "your-256-bit-secret" — el panel de firma cambia a **Signature Verified**.

### 3.3 Protección JSON con jose4j (opcional)

#### Motivación

El JWS firmado con HS256 garantiza **integridad y autenticidad**, pero las _claims_ siguen siendo legibles por cualquier consumidor del token. Para añadir **confidencialidad** se aplica el patrón _nested JWT_ (RFC 7519 §11.2): el JWS se envuelve dentro de un JWE cuyo `cty` (content type) se fija a `JWT`.

#### Dependencia

```groovy
implementation('org.bitbucket.b_c:jose4j:0.9.6')
```

#### Esquema criptográfico elegido

| Capa | Algoritmo (header) | Significado |
|---|---|---|
| Gestión de clave | `alg = A128KW` | Envoltura AES-128 KeyWrap (RFC 3394) de la CEK aleatoria. |
| Cifrado de contenido | `enc = A128CBC-HS256` | AES-128-CBC autenticado con HMAC-SHA-256 (RFC 7518). |
| Tipo de contenido | `cty = JWT` | El payload descifrado es a su vez un JWS. |

La clave AES-128 se obtiene como los 16 primeros bytes de `SHA-256("isdcm-entrega3-jwe-key-32bytes!!")`.

#### Funciones añadidas en `JwtUtil`

```java
public static String wrapInJwe(String jws) {
    JsonWebEncryption jwe = new JsonWebEncryption();
    jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
    jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
    jwe.setHeader("cty", "JWT");
    jwe.setKey(new AesKey(Arrays.copyOf(JWE_KEY_BYTES, 16)));
    jwe.setPayload(jws);
    return jwe.getCompactSerialization();
}

public static String unwrapJwe(String compactJwe) {
    JsonWebEncryption jwe = new JsonWebEncryption();
    jwe.setAlgorithmConstraints(new AlgorithmConstraints(
            ConstraintType.PERMIT, KeyManagementAlgorithmIdentifiers.A128KW));
    jwe.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(
            ConstraintType.PERMIT, ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256));
    jwe.setKey(new AesKey(Arrays.copyOf(JWE_KEY_BYTES, 16)));
    jwe.setCompactSerialization(compactJwe);
    return jwe.getPayload(); // JWS interno
}
```

Las llamadas a `setAlgorithmConstraints` y `setContentEncryptionAlgorithmConstraints` en la ruta de descifrado fijan explícitamente los algoritmos esperados — mitigando ataques de sustitución de algoritmo (algorithm-swap).

Además, `jweKeyAsJwk()` exporta la clave simétrica como JWK (`kty=oct`) usando `OctetSequenceJsonWebKey.toJson(OutputControlLevel.INCLUDE_SYMMETRIC)`, lo que permite a herramientas externas como jwt.io descifrar el JWE sin código adicional.

#### Cómo descifrar el JWE

Tres opciones documentadas en el sistema:

1. **Programáticamente:** llamada a `JwtUtil.unwrapJwe(jwe)`, que devuelve el JWS interno; éste se valida luego con jjwt y el secreto HS256.
2. **En jwt.io:** pegar el JWE en el editor, pegar el JWK proporcionado por el servidor (campo `jweJwk` mostrado en `/tokenJwt`). jwt.io descifra el contenido y muestra el JWS resultante; pegando `jwsSecret` se verifica también la firma.
3. **En consola:** con la clave en formato base64url (`tmtN7e88rxYu4Gx6NJ8TjA`) más cualquier cliente JOSE (`jose-util`, `python-jose`, etc.).

#### Página `tokenJwt.jsp`

Tras iniciar sesión, la página muestra:

- **JWT (HS256):** token compacto + botones *Copiar* / *Abrir en jwt.io*.
- **JWE (A128KW + A128CBC-HS256):** token compacto + botón *Copiar*.
- **Secretos del servidor** (zona de desarrollo): secreto HS256, clave AES-128 base64url y JWK del JWE.

Esto demuestra de forma reproducible el ciclo completo: cifrado → distribución → verificación / descifrado.

---

## Modificaciones estructurales sobre la propuesta original

| Aspecto | Estado |
|---|---|
| Patrón MVC (Servlets + JSP en frontend, JAX-RS en backend) | **Conservado** sin cambios. |
| Persistencia Derby embebida | **Conservada**. |
| Autenticación de sesión `HttpSession` para vistas | **Conservada**. |
| API Key (`X-API-Key`) para llamadas REST protegidas | **Conservada**; el nuevo `/login` no la requiere. |
| Rutas existentes (`/login`, `/registroVid`, `/listadoVid`, `/busqueda`, `/reproduccion`) | **Conservadas** y _enriquecidas_ con tres nuevas rutas: `/cifrado`, `/xmlcifrado`, `/tokenJwt`. |
| Flujo de inicio de sesión | **Modificado**: el servlet `/login` ahora invoca el nuevo endpoint REST `/api/login` (form-urlencoded) en lugar del antiguo `/api/usuaris/login` (JSON). Tras éxito el usuario es redirigido a `/tokenJwt` antes de continuar a `/listadoVid`, de modo que pueda inspeccionar y copiar el token. |
| Build del backend | El backend usa **Gradle** desde la Entrega 2; las dependencias `jjwt` y `jose4j` se han añadido en `build.gradle` con coordenadas equivalentes a las indicadas en el enunciado para Maven. |
| Entorno de la Parte 1 | **macOS local** en lugar de la VM de la asignatura. Keystore PKCS12 (`mykeystore.p12`, alias `tomcat`) en vez de JKS (`~/.keystore`, alias `isdcm`); `keytool` y `maven-war-plugin 3.4.0` por defecto, sin los pinneos requeridos por la VM. La funcionalidad es idéntica (Tomcat 9 sirviendo el WAR sobre TLS en el puerto 8443). |

No se han alterado capas, paquetes ni dependencias de persistencia respecto a la Entrega 2. Las nuevas funcionalidades viven en paquetes propios (`org.example.isdcmproject.crypto`, `org.upc.student.isdcm.entrega2.security`) y se integran como nuevos servlets / recursos sin tocar los existentes.

---

## Pruebas realizadas

### Parte 1 — HTTPS

> _Capturas reservadas para inserción manual._

- Verificación del keystore con `keytool -list` (alias `isdcm`, tipo `PrivateKeyEntry`).
- Acceso a `https://localhost:8443/testTomcat/` con aceptación de la excepción de certificado.
- Visualización del candado en el navegador y de la página de registro de Entrega 1.

### Parte 2 — Cifrado de archivos

| Caso | Resultado |
|---|---|
| Cifrado de `sample.mp4` (binario) → descifrado → hash idéntico | ✅ |
| Cifrado de `imagen.jpg` y `documento.pdf` con la misma contraseña | ✅ |
| Descifrado con contraseña incorrecta | ❌ Rechazado por GCM con mensaje en UI |
| Archivo cifrado sin cabecera `ISDCM1` | ❌ Rechazado con mensaje "Archivo no reconocido" |

### Parte 2 — Cifrado XML (`didlFilm1.xml`)

| Caso | Resultado |
|---|---|
| **Cifrar elemento** `metadata` → contiene `<xenc:EncryptedData>` | ✅ |
| **Cifrar documento** → raíz `DIDL` reemplazada por `<EncryptedData>` | ✅ |
| Descifrar resultado anterior → XML semánticamente idéntico al original | ✅ |
| Descifrar con contraseña incorrecta | ❌ `XMLEncryptionException` capturada y reportada |

### Parte 3 — JWT

```bash
curl -i -X POST http://localhost:8080/entrega-2-1.0-SNAPSHOT/api/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "username=demo" --data-urlencode "password=demo123"
```

Respuesta abreviada:

```json
{
  "username": "demo",
  "apiKey": "...",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJpc2RjbS1iYWNrZW5kIiwic3ViIjoiZGVtbyIsImFwaUtleSI6Ii4uLiIsImlhdCI6Li4uLCJleHAiOi4uLn0.<sig>",
  "tokenJwe": "eyJhbGciOiJBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiY3R5IjoiSldUIn0.<ek>.<iv>.<ct>.<tag>",
  "jwsSecret": "isdcm-entrega3-jwt-secret-...",
  "jweKey": "tmtN7e88rxYu4Gx6NJ8TjA",
  "jweJwk": "{\"kty\":\"oct\",\"kid\":\"isdcm-jwe-1\",\"k\":\"tmtN7e88rxYu4Gx6NJ8TjA\"}"
}
```

| Caso | Resultado |
|---|---|
| Credenciales correctas → 200 con JWS y JWE | ✅ |
| Credenciales incorrectas → 401 sin token | ✅ |
| Falta `username` o `password` → 400 | ✅ |
| jwt.io decodifica cabecera y payload del JWS | ✅ |
| jwt.io marca **Signature Verified** con `jwsSecret` | ✅ |
| jwt.io descifra el JWE con el JWK; cabecera muestra `cty=JWT`; el contenido descifrado es el JWS | ✅ |

---

## Resumen de entregables

- **Código fuente** completo en este repositorio:
  - `ISDCM-project-frontend/` — frontend con los módulos de cifrado (2.1, 2.2) y la página de JWT (3.x).
  - `isdcm-lab2-backend/` — backend con el nuevo endpoint `/api/login` y la utilidad `JwtUtil` (jjwt + jose4j).
- **Documentación técnica:** este documento (`document/entrega3.md`).
- **Evidencias HTTPS:** capturas a insertar manualmente en la sección Parte 1.
- **Uso de jose4j:** documentado en la sección 3.3, justificando el patrón _nested JWT_, los algoritmos elegidos (`A128KW` + `A128CBC-HS256`), las constantes de seguridad (`AlgorithmConstraints`), la integración con jjwt y los tres mecanismos de descifrado disponibles para revisores externos.
