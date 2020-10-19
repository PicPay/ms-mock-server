FROM 289208114389.dkr.ecr.us-east-1.amazonaws.com/picpay/java:jre-8-alpine-base

COPY target/*.jar /app.jar

COPY docker-entrypoint.sh /
RUN apk add --no-cache curl && \
    chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]