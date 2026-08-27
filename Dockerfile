# 基础镜像
FROM openjdk:17

# author
MAINTAINER wangdiankun

# 设置时区为上海
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 创建目录
RUN mkdir -p /opt/spark/app/spark.server.com

# 指定路径
WORKDIR /opt/spark/app/spark.server.com

# 暴露端口
EXPOSE 8000

# 设置 JVM 参数
ENV JAVA_OPTS="\
    -server \
    -Duser.timezone=GMT+08 \
    -Xms1024m \
    -Xmx1024m \
    -XX:MaxMetaspaceSize=128m \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseZGC \
    -Djava.awt.headless=true \
    -Dsun.net.client.defaultConnectTimeout=60000 \
    -Dsun.net.client.defaultReadTimeout=60000 \
    -Djmagick.systemclassloader=no \
    -Dnetworkaddress.cache.ttl=300 \
    -Dsun.net.inetaddr.ttl=300 \
    -XX:+HeapDumpOnOutOfMemoryError"

# 启动服务
ENTRYPOINT java ${JAVA_OPTS} \
    -Dspring.config.location=file:./application-prod.yml \
    -jar ./spark-0.0.1-SNAPSHOT.jar