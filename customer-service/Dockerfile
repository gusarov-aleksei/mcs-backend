FROM bellsoft/liberica-openjdk-alpine:11 AS customer-service

RUN apk --no-cache add curl
CMD java ${JAVA_OPTS} -jar customer-service-*-with-dependencies.jar
COPY target/customer-service-*-with-dependencies.jar .
EXPOSE 8093