FROM maven:3.8.1-openjdk-11
WORKDIR /debts
COPY . .
RUN mvn clean install -DskipTests
ENTRYPOINT ["java","-jar","/debts/target/debt-0.0.1-SNAPSHOT.jar"]