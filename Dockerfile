FROM maven:3.6.3-openjdk-15-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:15-alpine
COPY --from=build /home/app/target/PoloniexTrader-0.0.1-SNAPSHOT.jar /usr/local/lib/PoloniexTrader.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/PoloniexTrader.jar"]
