FROM bellsoft/liberica-openjdk-alpine:11 AS order-service

RUN apk --no-cache add curl
CMD java "${JAVA_OPTS}" -cp ./order-service-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.example.order.ApplicationKt
COPY target/order-service-*-with-dependencies.jar .
EXPOSE 8094