FROM java:8
EXPOSE 8080

VOLUME /tmp
ADD target/mqtt-broker.jar app.jar
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar"]
