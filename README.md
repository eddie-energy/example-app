# Example App
Repository for the EDDIE Example App

## Setup

### Backend Environments

The example app needs some configuration to work properly. This is best done inside the [.env](./docker/.env) file:

```dotenv
POSTGRES_USERNAME=user
POSTGRES_PASSWORD=user

KAFKA_GROUP_ID=kafka-client-group
KAFKA_PASSWORD=my-kafka-password
KAFKA_USERNAME=my-kafka-user
MQTT_CLIENT_ID=mqtt-client-id
MQTT_PASSWORD=my-mqtt-password
MQTT_USERNAME=my-mqtt-user

BACKEND_BASE_URL=http://localhost:8080

KEYCLOAK_URL=https://eddie-demo.projekte.fh-hagenberg.at/iam/
KEYCLOAK_REALM=EDDIE
KEYCLOAK_CLIENT=example-app-client

TRUST_STORE_LOCATION=certs/kafka/client.truststore.jks
TRUST_STORE_PASSWORD=REPLACE_ME
```

You may also run a local image of the example app. To do this, a few more properties need to be set inside the [docker-compose](./docker/docker-compose.yml) - an example configuration could look something like this:

```yaml
backend:
  image: ghcr.io/eddie-energy/example-app:latest
  environment:
    POSTGRES_USER: admin
    POSTGRES_PASSWORD: admin
    POSTGRES_DB: example-app
    KAFKA_GROUP_ID: kafka-client-group-floauzi
    KAFKA_PASSWORD: my-kafka-password
    KAFKA_USERNAME: kafka-username
    MQTT_CLIENT_ID: example-app-client-auzi
    MQTT_PASSWORD: mqtt-password
    MQTT_USERNAME: mqtt-username
    EXAMPLE_APP_CORS_ALLOWED_ORIGINS: '*'
    POSTGRES_URL: jdbc:postgresql://db:5432/example-app
    SPRING_KAFKA_CONSUMER_SSL_TRUST_STORE_LOCATION: "file:/certs/client.truststore.jks"
    SPRING_KAFKA_CONSUMER_SSL_TRUST_STORE_PASSWORD: truststore-password
    EXAMPLE_APP_BACKEND_BASE_URL: "http://localhost:8080"
  env_file:
    - .env 
  ports:
    - "8080:8080"
  depends_on:
    - db
  volumes:
    - C:/path/to/truststore/kafka/jka:/certs
```

For convenient local development, you might consider putting following snippet into an application-local.yml file:

```yaml
example-app:
  cors: # für local dev
    allowed-origins: '*'
```

### Database

Use the database configured in the [docker-compose](./docker/docker-compose.yml):
- inside a terminal, navigate to the docker folder
- run ``docker compose up -d``

