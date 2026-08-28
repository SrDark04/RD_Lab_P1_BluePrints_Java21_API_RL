## Laboratorio #4 – REST API Blueprints (Java 21 / Spring Boot 3.3.x)
# Escuela Colombiana de Ingeniería – Arquitecturas de Software  

---

Resuelto por:
- Roger Mauricio Duran Guacaneme
- Camilo Alfonso Leon Acosta

---

## 📋 Requisitos
- Java 21
- Maven 3.9+

## ▶️ Ejecución del proyecto
```bash
mvn clean install
mvn spring-boot:run
```
Probar con `curl`:
```bash
curl -s http://localhost:8080/api/v1/blueprints | jq
curl -s http://localhost:8080/api/v1/blueprints/john | jq
curl -s http://localhost:8080/api/v1/blueprints/john/house | jq
curl -i -X POST http://localhost:8080/api/v1/blueprints -H 'Content-Type: application/json' -d '{ "author":"john","name":"kitchen","points":[{"x":1,"y":1},{"x":2,"y":2}] }'
curl -i -X PUT  http://localhost:8080/api/v1/blueprints/john/kitchen/points -H 'Content-Type: application/json' -d '{ "x":3,"y":3 }'
```

> Si deseas activar filtros de puntos (reducción de redundancia, *undersampling*, etc.), implementa nuevas clases que implementen `BlueprintsFilter` y cámbialas por `IdentityFilter` con `@Primary` o usando configuración de Spring.
---

Abrir en navegador:  
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)  

---

## 🗂️ Estructura de carpetas (arquitectura)

```
src/main/java/edu/eci/arsw/blueprints
  ├── model/         # Entidades de dominio: Blueprint, Point
  ├── persistence/   # Interfaz + repositorios (InMemory, Postgres)
  │    └── impl/     # Implementaciones concretas
  ├── services/      # Lógica de negocio y orquestación
  ├── filters/       # Filtros de procesamiento (Identity, Redundancy, Undersampling)
  ├── controllers/   # REST Controllers (BlueprintsAPIController)
  └── config/        # Configuración (Swagger/OpenAPI, etc.)
```

> Esta separación sigue el patrón **capas lógicas** (modelo, persistencia, servicios, controladores), facilitando la extensión hacia nuevas tecnologías o fuentes de datos.

---

## 📖 Actividades del laboratorio

### 1. Familiarización con el código base
- Revisa el paquete `model` con las clases `Blueprint` y `Point`.  
- Entiende la capa `persistence` con `InMemoryBlueprintPersistence`.  
- Analiza la capa `services` (`BlueprintsServices`) y el controlador `BlueprintsAPIController`.

### 2. Migración a persistencia en PostgreSQL
- Configura una base de datos PostgreSQL (puedes usar Docker).  

```bash
docker run --rm --name lab5BLuePrints \ -e POSTGRES_PASSWORD=roger \ -p 5432:5432 \ -d dhi.io/postgres:18
```

- Implementa un nuevo repositorio `PostgresBlueprintPersistence` que reemplace la versión en memoria.

  R// La persistencia utiliza `SpringDataBlueprintRepository`, basado en `JpaRepository`, para almacenar las entidades en PostgreSQL
      La entidad `blueprint` se mapea a la tabla `blueprints` y contiene `id`, `author`, `name` y sus puntos.
      Los puntos se almacenan como entidades `BlueprintPoint` en la tabla `blueprint_points`, relacionadas mediante `@OneToMany` y `@ManyToOne`, con `cascade = CascadeType.ALL` y `orphanRemoval = true`.
      `PostgressBluePrintPersistence` convierte el modelo de dominio `Blueprint` en entidades JPA al guardar y realiza la conversión inversa al consultar. También se implemento búsqueda por autor, búsqueda por nombre, consulta de todos los planos, validación de duplicados y adición de puntos.


- Mantén el contrato de la interfaz `BlueprintPersistence`:

  R// `PostgressBluePrintPersistence` conserva las operaciones `saveBlueprint`, `getBlueprint`, `getBlueprintsByAuthor`
      `getAllBlueprints` y `addPoint`, incluyendo sus tipos de retorno y excepciones.

### 3. Buenas prácticas de API REST
- El path base del controlador es `/api/v1/blueprints`, incorporando versionamiento en la API.
- Usa **códigos HTTP** correctos:  
  - `200 OK` (consultas exitosas).  
  - `201 Created` (creación).  
  - `202 Accepted` (actualizaciones).  
  - `400 Bad Request` (datos inválidos).  
  - `404 Not Found` (recurso inexistente).  
- Las respuestas exitosas y los errores utilizan la clase genérica `ApiResponse<T>`:
  ```java
  public record ApiResponse<T>(int code, String message, T data) {}
  ```
  Ejemplo JSON:
  ```json
  {
    "code": 200,
    "message": "execute ok",
    "data": { "author": "john", "name": "house", "points": [...] }
  }
  ```
- Las solicitudes con campos obligatorios ausentes, JSON inválido o blueprints duplicados devuelven `400 Bad Request`.

  - Se creo una nueva clase:

  - Se modifico el controlador para que todas las respuestas sean de tipo `ApiResponse<T>`, incluyendo los errores.

  - Se cambiaron los path de los endpoints para que sean más RESTful, por ejemplo:  
    - `GET /api/v1/blueprints` → todos los planos.  
    - `GET /api/v1/blueprints/{author}` → planos de un autor.  
    - `GET /api/v1/blueprints/{author}/{name}` → plano específico.  
    - `POST /api/v1/blueprints` → crear un nuevo plano.  
    - `PUT /api/v1/blueprints/{author}/{name}/points` → agregar un punto a un plano existente.

### 4. OpenAPI / Swagger
- Configura springdoc-openapi en el proyecto.
- Expón documentación automática en /swagger-ui.html.
- Anota endpoints con @Operation y @ApiResponse.

  R// 
    - Se configuró `springdoc-openapi-starter-webmvc-ui` para generar la documentación OpenAPI automáticamente.
    - `OpenApiConfig` define la información general de la API: título, versión `v1` y descripción.
    - El controlador `BlueprintsAPIController` está documentado con `@Tag`, `@Operation` y `@ApiResponse`, incluyendo las respuestas `200`, `201`, `202`, `400` y `404`.
    - La documentación interactiva está disponible en `/swagger-ui.html` y el documento OpenAPI en `/v3/api-docs`.
    - Anota endpoints con `@Operation` y `@ApiResponse`.

### 5. Filtros de *Blueprints*
- Implementa filtros:
    - ** RedundancyFilter: elimina puntos duplicados consecutivos.
    - ** UndersamplingFilter: conserva 1 de cada 2 puntos.
- Activa los filtros mediante perfiles de Spring (redundancy, undersampling).

  R//
    - Se implementó `RedundancyFilter`, que elimina puntos duplicados consecutivos.
    - Se implementó `UndersamplingFilter`, que conserva los puntos de índices pares, es decir, uno de cada dos.
    - `IdentityFilter` funciona como filtro predeterminado cuando no se activa ningún perfil específico.
    - Los filtros se activan mediante perfiles de Spring:
      ```bash
      mvn spring-boot:run -Dspring-boot.run.profiles=redundancy
      mvn spring-boot:run -Dspring-boot.run.profiles=undersampling
      ```
    - Los perfiles son excluyentes: al activar `redundancy` o `undersampling`, `IdentityFilter` se desactiva para evitar ambigüedad entre beans.
    - El filtro seleccionado se aplica al consultar un blueprint específico; la información persistida permanece sin modificaciones.

---

## ✅ Entregables

1. Repositorio en GitHub con:  
   - Código fuente actualizado.  
   - Configuración PostgreSQL (`application.yml` o script SQL).  
   - Swagger/OpenAPI habilitado.  
   - Clase `ApiResponse<T>` implementada.  

2. Documentación:  
   - Informe de laboratorio con instrucciones claras.  
   - Evidencia de consultas en Swagger UI y evidencia de mensajes en la base de datos.  
   - Breve explicación de buenas prácticas aplicadas.  

---

## 📊 Criterios de evaluación

| Criterio | Peso |
|----------|------|
| Diseño de API (versionamiento, DTOs, ApiResponse) | 25% |
| Migración a PostgreSQL (repositorio y persistencia correcta) | 25% |
| Uso correcto de códigos HTTP y control de errores | 20% |
| Documentación con OpenAPI/Swagger + README | 15% |
| Pruebas básicas (unitarias o de integración) | 15% |

**Bonus**:  

- Imagen de contenedor (`spring-boot:build-image`).  
- Métricas con Actuator.

  **Respuesta / implementación del bonus:**

  ### Imagen de contenedor

  Para este bonus se puede usar el `Dockerfile` del proyecto. Este archivo primero compila la aplicación con Maven y después la ejecuta usando Java 21. La imagen se construye con el siguiente comando:

  ```bash
  docker build -t blueprints-api .
  ```

  Luego se puede ejecutar el contenedor así:

  ```bash
  docker run --rm -p 8080:8080 blueprints-api
  ```

  Otra opción es generar la imagen directamente con el plugin de Spring Boot:

  ```bash
  mvn spring-boot:build-image -Dspring-boot.build-image.imageName=blueprints-api:1.0
  docker run --rm -p 8080:8080 blueprints-api:1.0
  ```

  Para ejecutar estos comandos es necesario tener Docker iniciado y Maven 3.9 o una versión superior.

  ### Métricas con Actuator

  Para agregar métricas a la aplicación se debe incluir la dependencia `spring-boot-starter-actuator` en el archivo `pom.xml`:

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  ```

  Después se configuran los endpoints de monitoreo en `application.properties`:

  ```properties
  management.endpoints.web.exposure.include=health,info,metrics
  management.endpoint.health.show-details=always
  management.info.env.enabled=true

  info.app.name=Blueprints API
  info.app.version=1.0.0
  info.app.description=REST API for blueprints
  ```

  Con la aplicación iniciada, se pueden consultar los endpoints usando `curl`:

  ```bash
  curl http://localhost:8080/actuator/health
  curl http://localhost:8080/actuator/info
  curl http://localhost:8080/actuator/metrics
  ```

  El endpoint `/actuator/health` sirve para comprobar si la aplicación está funcionando. Cuando el servicio está disponible, la respuesta debe mostrar el estado `UP`.