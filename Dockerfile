FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY build/libs/msa_board-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8002
ENTRYPOINT [ "java","-jar","app.jar" ]