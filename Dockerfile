FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY src/WordGuessingGame.java src/WordGuessingGameServer.java ./
COPY public ./public

RUN javac WordGuessingGame.java WordGuessingGameServer.java

EXPOSE 8080

CMD ["java", "WordGuessingGameServer"]