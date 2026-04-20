# Guia para completar la entrega

Esta guia describe que falta para cerrar satisfactoriamente el enunciado despues de la migracion inicial a microservicios serverless.

## Estado actual del proyecto

El repositorio ya contiene:

- Monolito Spring Boot en `monolith/TwitterBackend`.
- Frontend React + Vite en `frontend`.
- Integracion Auth0 en frontend y monolito.
- Swagger/OpenAPI en el monolito.
- Fase serverless en `serverless`.
- Tres microservicios Java Lambda:
  - `user-service`: `GET /api/me`.
  - `post-service`: `POST /api/posts`.
  - `stream-service`: `GET /api/posts` y `GET /api/stream`.
- Infraestructura AWS SAM en `serverless/template.yaml`.
- Persistencia serverless propuesta con DynamoDB.

Lo que falta para completar el enunciado no es principalmente codigo. Falta desplegar en AWS, conectar el frontend publico al API Gateway, probar el flujo completo y documentar evidencias.

## Objetivo final

El resultado final debe quedar asi:

```text
React + Vite frontend hosted on S3 or CloudFront
   |
   v
API Gateway HTTP API
   |
   |-- GET  /api/me      -> user-service Lambda
   |-- POST /api/posts   -> post-service Lambda
   |-- GET  /api/posts   -> stream-service Lambda
   `-- GET  /api/stream  -> stream-service Lambda
   |
   v
DynamoDB Posts table
```

Auth0 debe proteger:

- `POST /api/posts` con scope `write:posts`.
- `GET /api/me` con scope `read:profile`.

El feed publico debe seguir disponible en:

- `GET /api/posts`.
- `GET /api/stream`.

## Paso 1: instalar herramientas necesarias

Verificar Java y Maven:

```bash
java -version
mvn -version
```

Instalar AWS SAM CLI en Linux Mint x86_64:

```bash
sudo apt update
sudo apt install -y curl unzip

cd /tmp
curl -Lo aws-sam-cli-linux-x86_64.zip \
  https://github.com/aws/aws-sam-cli/releases/latest/download/aws-sam-cli-linux-x86_64.zip

unzip -q aws-sam-cli-linux-x86_64.zip -d sam-installation
sudo ./sam-installation/install

sam --version
```

Instalar AWS CLI v2:

```bash
cd /tmp
curl -Lo awscliv2.zip https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip
unzip -q awscliv2.zip
sudo ./aws/install

aws --version
```

Si se esta usando AWS Academy o VocLabs, normalmente no se usa `aws configure` permanente. En ese caso se copian las credenciales temporales que entrega el laboratorio y se exportan en la terminal.

Verificar identidad AWS:

```bash
aws sts get-caller-identity
```

## Paso 2: configurar Auth0

En Auth0 se necesitan dos recursos.

### SPA Application

Crear o verificar una aplicacion tipo Single Page Application.

Configurar los valores locales:

```text
Allowed Callback URLs:
http://localhost:5173

Allowed Logout URLs:
http://localhost:5173

Allowed Web Origins:
http://localhost:5173
```

Cuando se despliegue frontend en AWS, agregar tambien el dominio publico:

```text
https://your-cloudfront-domain.cloudfront.net
```

Si no se puede usar CloudFront y solo se tiene S3 website hosting, agregar el endpoint HTTP de S3 como evidencia de laboratorio. Para un flujo publico real con Auth0, CloudFront con HTTPS es la opcion mas correcta.

### Auth0 API

Crear o verificar una API con audience:

```text
https://twitter-like-api
```

Crear scopes:

```text
write:posts
read:profile
read:posts
```

`read:posts` es opcional para el codigo actual porque el feed es publico, pero es bueno tenerlo definido para explicar autorizacion granular.

Guardar estos valores:

```text
AUTH0_ISSUER_URI=https://your-tenant.us.auth0.com/
AUTH0_AUDIENCE=https://twitter-like-api
```

## Paso 3: validar build local

Desde la raiz del repositorio:

```bash
mvn -f serverless/pom.xml clean package
```

Esto debe generar:

```text
serverless/user-service/target/user-service-1.0.0-aws.jar
serverless/post-service/target/post-service-1.0.0-aws.jar
serverless/stream-service/target/stream-service-1.0.0-aws.jar
```

Validar el monolito:

```bash
cd monolith/TwitterBackend
mvn test
```

Si Mockito falla por Byte Buddy dentro de un sandbox, ejecutar el comando en una terminal normal. El fallo conocido dice que no puede auto-adjuntar el agente de Mockito.

Validar frontend:

```bash
cd frontend
npm run lint
npm run build
```

## Paso 4: validar SAM

Desde la raiz del repositorio:

```bash
sam validate --template-file serverless/template.yaml
```

Si este comando falla por sintaxis, corregir `serverless/template.yaml` antes de intentar desplegar.

Si falla por credenciales, revisar:

```bash
aws sts get-caller-identity
```

## Paso 5: desplegar microservicios en AWS

Primero compilar los JARs:

```bash
mvn -f serverless/pom.xml clean package
```

Luego desplegar con SAM:

```bash
sam deploy --guided \
  --template-file serverless/template.yaml \
  --parameter-overrides \
  Auth0Issuer=https://your-tenant.us.auth0.com/ \
  Auth0Audience=https://twitter-like-api \
  AllowedCorsOrigin=http://localhost:5173 \
  Auth0ClaimsNamespace=https://twitter-like-app.example.com
```

Para el primer despliegue, SAM pedira datos como:

```text
Stack Name: twitter-like-serverless
AWS Region: us-east-1
Confirm changes before deploy: Y
Allow SAM CLI IAM role creation: Y
Disable rollback: N
Save arguments to configuration file: Y
SAM configuration file: samconfig.toml
SAM configuration environment: default
```

Al final debe aparecer un output:

```text
ApiUrl=https://xxxxxxxxxx.execute-api.us-east-1.amazonaws.com
PostsTableName=...
```

Guardar ese `ApiUrl`.

## Paso 6: probar API Gateway

Definir una variable local:

```bash
export API_URL=https://xxxxxxxxxx.execute-api.us-east-1.amazonaws.com
```

Probar endpoints publicos:

```bash
curl "$API_URL/api/posts"
curl "$API_URL/api/stream"
```

Deben responder `200`.

Probar endpoint protegido sin token:

```bash
curl -i -X POST "$API_URL/api/posts" \
  -H "Content-Type: application/json" \
  -d '{"content":"Post without token"}'
```

Debe fallar con `401` o `403`.

Obtener un access token Auth0 con audience `https://twitter-like-api` y scopes `write:posts read:profile`.

Luego probar:

```bash
export ACCESS_TOKEN=your_access_token

curl -i "$API_URL/api/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Crear post:

```bash
curl -i -X POST "$API_URL/api/posts" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Hello from AWS Lambda"}'
```

Validar que el post aparece:

```bash
curl "$API_URL/api/stream"
```

Validar limite de 140 caracteres:

```bash
LONG_CONTENT=$(printf 'a%.0s' {1..141})

curl -i -X POST "$API_URL/api/posts" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"content\":\"$LONG_CONTENT\"}"
```

Debe responder `400`.

## Paso 7: conectar frontend con API Gateway

Actualizar `.env` o variables de build:

```env
VITE_AUTH0_DOMAIN=your-tenant.us.auth0.com
VITE_AUTH0_CLIENT_ID=your_auth0_spa_client_id
VITE_AUTH0_AUDIENCE=https://twitter-like-api
VITE_API_BASE_URL=https://xxxxxxxxxx.execute-api.us-east-1.amazonaws.com
```

Construir frontend:

```bash
cd frontend
npm ci
npm run build
```

Subir `frontend/dist` al bucket S3.

Si se usa AWS CLI:

```bash
aws s3 sync dist/ s3://your-bucket-name --delete
```

## Paso 8: configurar CORS para el dominio final

Si el frontend queda en CloudFront:

```text
AllowedCorsOrigin=https://your-cloudfront-domain.cloudfront.net
```

Si el frontend queda temporalmente en S3 website:

```text
AllowedCorsOrigin=http://your-bucket.s3-website-us-east-1.amazonaws.com
```

Si se cambia el origen, redesplegar:

```bash
sam deploy \
  --template-file serverless/template.yaml \
  --parameter-overrides \
  Auth0Issuer=https://your-tenant.us.auth0.com/ \
  Auth0Audience=https://twitter-like-api \
  AllowedCorsOrigin=https://your-final-frontend-origin \
  Auth0ClaimsNamespace=https://twitter-like-app.example.com
```

## Paso 9: actualizar Auth0 con el dominio final

En la SPA de Auth0, agregar el dominio final en:

```text
Allowed Callback URLs
Allowed Logout URLs
Allowed Web Origins
```

Ejemplo con CloudFront:

```text
https://your-cloudfront-domain.cloudfront.net
```

Si se usa S3 website hosting, documentar la limitacion HTTP y adjuntar evidencia. La opcion mas solida es CloudFront porque entrega HTTPS.

## Paso 10: pruebas end-to-end

Desde el frontend publico:

1. Abrir la URL publica.
2. Ver el feed sin iniciar sesion.
3. Iniciar sesion con Auth0.
4. Crear un post menor o igual a 140 caracteres.
5. Ver que el post aparece en el feed.
6. Consultar el panel `/api/me`.
7. Cerrar sesion.
8. Confirmar que no se puede publicar sin sesion.

## Evidencias recomendadas

Guardar capturas de:

- Auth0 SPA Application.
- Auth0 API con audience y scopes.
- `sam deploy` exitoso.
- API Gateway creado.
- JWT Authorizer en API Gateway.
- Lambdas creadas.
- DynamoDB table creada.
- CloudWatch logs de una invocacion.
- `curl` a endpoints publicos.
- `curl` a endpoint protegido sin token.
- `curl` a endpoint protegido con token.
- Frontend S3 o CloudFront cargando.
- Login Auth0 desde frontend publico.
- Post creado desde frontend publico.
- Feed mostrando el post.

## Checklist final contra el enunciado

Antes de entregar, confirmar:

- El monolito Spring Boot sigue funcionando.
- Swagger UI del monolito abre en `/swagger-ui.html`.
- El frontend permite login, logout, crear posts y ver feed.
- Auth0 tiene SPA Application configurada.
- Auth0 tiene API con audience correcto.
- Auth0 tiene scopes `write:posts` y `read:profile`.
- `mvn test` pasa en el monolito.
- `npm run lint` pasa.
- `npm run build` pasa.
- `mvn -f serverless/pom.xml clean package` pasa.
- `sam validate` pasa.
- `sam deploy` crea API Gateway, Lambdas y DynamoDB.
- El frontend publico consume API Gateway, no el backend local.
- `POST /api/posts` esta protegido.
- `GET /api/me` esta protegido.
- `GET /api/posts` o `GET /api/stream` muestra el feed publico.
- Hay evidencias en capturas o en el README.

## Problemas comunes

### `sam: command not found`

AWS SAM CLI no esta instalado o no esta en el `PATH`.

Verificar:

```bash
which sam
sam --version
```

### `aws sts get-caller-identity` falla

No hay credenciales AWS configuradas o las credenciales temporales expiraron.

En VocLabs, copiar nuevamente las credenciales del laboratorio.

### Error de CORS desde el navegador

El valor `AllowedCorsOrigin` del despliegue SAM no coincide con el origen real del frontend.

Redesplegar con el origen correcto.

### Login Auth0 falla despues de desplegar frontend

Faltan URLs en la SPA de Auth0.

Agregar el dominio final en:

```text
Allowed Callback URLs
Allowed Logout URLs
Allowed Web Origins
```

### `POST /api/posts` retorna 403 con token

El access token no trae el scope `write:posts`, o el frontend no esta pidiendo el audience correcto.

Verificar:

```env
VITE_AUTH0_AUDIENCE=https://twitter-like-api
```

### El token parece valido pero API Gateway lo rechaza

Revisar que el issuer y audience de `sam deploy` coincidan exactamente con Auth0:

```text
Auth0Issuer=https://your-tenant.us.auth0.com/
Auth0Audience=https://twitter-like-api
```

El issuer debe incluir `/` al final.

## Recomendacion de entrega

La entrega debe presentar el proyecto como una evolucion:

1. Primero se construyo el monolito Spring Boot con Swagger, JPA, seguridad Auth0 y frontend React.
2. Luego se migro la responsabilidad del monolito a tres microservicios serverless.
3. API Gateway reemplaza a los controllers HTTP de Spring en la fase AWS.
4. DynamoDB reemplaza la persistencia JPA para posts en la fase serverless.
5. Auth0 sigue siendo el proveedor de identidad y autorizacion.
6. El frontend publico debe consumir el API Gateway desplegado.

Esta narrativa encaja directamente con el enunciado: partir de monolito, evolucionar a microservicios serverless en AWS y mantener seguridad con Auth0.
