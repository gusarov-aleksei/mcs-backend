FROM bellsoft/liberica-openjdk-alpine:11 AS cart-service

RUN apk --no-cache add curl
CMD java ${JAVA_OPTS} -jar cart-service-*.jar
COPY target/cart-service-*.jar .
EXPOSE 8090