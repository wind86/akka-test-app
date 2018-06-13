FROM cogniteev/oracle-java

RUN apt-get update && apt-get install -y git maven

WORKDIR /app
RUN git clone https://github.com/wind86/akka-test-app.git && cd akka-test-app && mvn clean package