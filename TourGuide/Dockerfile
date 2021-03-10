FROM openjdk:8-jdk-alpine
LABEL maintainer="mamoudouba8787@gmail.com"
VOLUME /tmp
EXPOSE 8080
COPY build/libs/tourGuide-1.0.0.jar tourguide.jar
ENTRYPOINT ["java","-jar","tourguide.jar"]