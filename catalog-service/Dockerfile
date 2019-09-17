FROM bellsoft/liberica-openjdk-alpine:11 AS catalog-service

RUN apk --no-cache add curl
CMD java ${JAVA_OPTS} -jar catalog-service-*.jar
COPY target/catalog-service-*.jar .