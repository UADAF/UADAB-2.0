FROM gradle:5.4-jdk8

COPY gradle.properties settings.gradle build.gradle.kts /pbbot-build/
COPY src/ /pbbot-build/src

WORKDIR /pbbot-build

USER root
RUN chown -R gradle /pbbot-build
USER gradle

RUN gradle shadowJar --no-daemon --stacktrace

WORKDIR /pbbot/
COPY config.json idlePhrases.txt /pbbot/

USER root
RUN mv /pbbot-build/build/libs/* /pbbot/ && \
    rm -rf /pbbot-build/
