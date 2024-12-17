FROM 289208114389.dkr.ecr.us-east-1.amazonaws.com/moonlight-images/java8-maven-debian:3.9.9-36e51a68d53c46e4d9ba19e680610a8c1495036b AS build

WORKDIR /build

COPY settings.xml .
COPY pom.xml .
COPY src src

RUN mvn package --batch-mode

FROM 289208114389.dkr.ecr.us-east-1.amazonaws.com/picpay/java:jre-8-alpine-base

COPY --from=build /build/target/*.jar /app.jar

COPY docker-entrypoint.sh /
RUN apk add --no-cache curl && \
    chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
