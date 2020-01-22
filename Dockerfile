FROM navikt/java:8

COPY app/target/app.jar /app/app.jar
COPY init-scripts /init-scripts

ENV JAVA_OPTS="-Xmx1024m \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=nais"