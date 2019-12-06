FROM openjdk:11.0.5-jre-stretch

ADD ["sources.list", "/etc/apt/"]

RUN apt update && apt install -y nmap

ADD ["target/port-scan-1-jar-with-dependencies.jar", "/"]

CMD java -jar port-scan-1-jar-with-dependencies.jar