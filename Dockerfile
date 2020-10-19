FROM 289208114389.dkr.ecr.us-east-1.amazonaws.com/maven:3.6.3-openjdk-8-slim AS build

WORKDIR /build

COPY settings.xml .
COPY pom.xml .
COPY src src

RUN mvn package -s settings.xml --batch-mode

FROM 289208114389.dkr.ecr.us-east-1.amazonaws.com/picpay/java:jre-8-alpine-base

COPY --from=build /build/target/*.jar /app.jar

COPY docker-entrypoint.sh /
RUN apk add --no-cache curl && \
    chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
