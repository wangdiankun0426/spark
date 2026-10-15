#!/bin/bash
# 星火云服务端自动更新脚本
# 用法：将新包 spark-0.0.1-SNAPSHOT.jar 放到 /root 下，执行 sh deployServer.sh
# 流程：整个部署目录备份为 spark.server.com_日期.序号 → 新包覆盖 → 重启容器 → 跟踪日志

set -e

# 新包路径
SRC_JAR="/root/spark-0.0.1-SNAPSHOT.jar"
# 部署目录与容器名
APP_DIR="/opt/spark/app"
DEPLOY_DIR="${APP_DIR}/spark.server.com"
CONTAINER_NAME="spark-server"
# 日志文件
LOG_FILE="/opt/spark/logs/spark-info.log"

# 校验新包存在
if [ ! -f "${SRC_JAR}" ]; then
    echo "新包不存在: ${SRC_JAR}"
    exit 1
fi

# 计算备份目录后缀 _yymmdd.nn，同一天多次备份序号递增
DATE_STR=$(date +%y%m%d)
SEQ=1
while true; do
    SUFFIX=$(printf "_%s.%02d" "${DATE_STR}" "${SEQ}")
    BACKUP_DIR="${APP_DIR}/spark.server.com${SUFFIX}"
    if [ ! -d "${BACKUP_DIR}" ]; then
        break
    fi
    SEQ=$((SEQ + 1))
done

# 备份整个部署目录，-a保留文件原始时间戳与权限
if [ -d "${DEPLOY_DIR}" ]; then
    cp -a "${DEPLOY_DIR}" "${BACKUP_DIR}"
    echo "旧部署目录已备份: ${BACKUP_DIR}"
else
    echo "部署目录不存在，跳过备份"
fi

# 新包覆盖部署目录内的 jar，配置文件保持线上原样不动
mkdir -p "${DEPLOY_DIR}"
mv "${SRC_JAR}" "${DEPLOY_DIR}/spark-0.0.1-SNAPSHOT.jar"

# 部署目录内文件时间统一刷新为当前时间
for item in "spark-0.0.1-SNAPSHOT.jar" "application-prod.yml" "logback.xml" "lib"; do
    if [ -e "${DEPLOY_DIR}/${item}" ]; then
        touch "${DEPLOY_DIR}/${item}"
    fi
done
echo "新包已部署: ${DEPLOY_DIR}/spark-0.0.1-SNAPSHOT.jar"

# 重启容器
docker restart "${CONTAINER_NAME}"
echo "容器 ${CONTAINER_NAME} 已重启"

# 等待服务启动后跟踪日志，Ctrl+C 退出
sleep 5
echo "开始跟踪日志: ${LOG_FILE}"
tail -F "${LOG_FILE}"
