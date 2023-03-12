FROM maven:3.8.5-openjdk-17-slim as builder

# COPY docker/settings.xml /root/.m2/settings.xml

RUN mkdir /app
COPY src /app/src
COPY pom.xml /app
WORKDIR /app
RUN mvn package

FROM registry.access.redhat.com/ubi8/ubi-minimal:8.6

# JAVA_VERSION has to align with WGET_JDK_URL
ARG JAVA_VERSION=17.0.2
# find most recet version at https://jdk.java.net/17/
ARG WGET_JDK_URL='https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz'
ARG RUN_JAVA_VERSION=1.3.8
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'
# Install java and the run-java script
# Also set up permissions for user `1001`


RUN microdnf install curl ca-certificates wget tar gzip nmap \
    && microdnf update \
    && microdnf clean all \
    && wget ${WGET_JDK_URL} \
    && tar xvf openjdk-${JAVA_VERSION}_linux-x64_bin.tar.gz \
    && mv jdk-${JAVA_VERSION} /opt/jdk-17 \
    && update-alternatives --install /usr/bin/java java /opt/jdk-17/bin/java 100 \
    && update-alternatives --install /usr/bin/javac javac /opt/jdk-17/bin/javac 100 \
    && mkdir /deployments \
    && chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments \
    && curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o /deployments/run-java.sh \
    && chown 1001 /deployments/run-java.sh \
    && chmod 540 /deployments/run-java.sh \
    && echo "securerandom.source=file:/dev/urandom" >> /opt/jdk-17/conf/security/java.security \
    # For integration tests fixing "Permission denied" while writing cache
    && mkdir /tmp/vertx-cache \
    && chmod -R 775 /tmp/vertx-cache

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --from=builder --chown=1001 /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=builder --chown=1001 /app/target/quarkus-app/*.jar /deployments/
COPY --from=builder --chown=1001 /app/target/quarkus-app/app/ /deployments/app/
COPY --from=builder --chown=1001 /app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 1001

CMD [ "/deployments/run-java.sh" ]
