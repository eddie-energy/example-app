FROM eclipse-temurin:21-jre-alpine

COPY build/libs/example-app-0.0.1.jar /opt/example-app-backend.jar
COPY docker/entrypoint.sh /opt/entrypoint.sh

RUN chmod +x /opt/entrypoint.sh

WORKDIR /opt

CMD ["/opt/entrypoint.sh"]

EXPOSE 8080