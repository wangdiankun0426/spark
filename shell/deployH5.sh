#!/bin/bash
# 星火云移动端H5自动更新脚本
# 用法：将新包 h5.zip 放到 /root 下，执行 sh deployH5.sh
# 流程：整个部署目录备份为 spark.h5.com_日期.序号 → 解压新包覆盖 → 清理临时文件
# H5为静态资源，nginx挂载目录直接生效，无需重启容器

set -e

# 新包路径
SRC_ZIP="/root/h5.zip"
# 部署目录，与 docker-compose 中 spark-nginx 挂载路径一致
APP_DIR="/opt/spark/app"
DEPLOY_DIR="${APP_DIR}/spark.h5.com"
# 解压临时目录
TMP_DIR="/root/h5_deploy_tmp"

# 校验新包存在
if [ ! -f "${SRC_ZIP}" ]; then
    echo "新包不存在: ${SRC_ZIP}"
    exit 1
fi

# 计算备份目录后缀 _yymmdd.nn，同一天多次备份序号递增
DATE_STR=$(date +%y%m%d)
SEQ=1
while true; do
    SUFFIX=$(printf "_%s.%02d" "${DATE_STR}" "${SEQ}")
    BACKUP_DIR="${APP_DIR}/spark.h5.com${SUFFIX}"
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

# 解压新包到临时目录
rm -rf "${TMP_DIR}"
mkdir -p "${TMP_DIR}"
unzip -q "${SRC_ZIP}" -d "${TMP_DIR}"

# 兼容 zip 内多裹一层目录的情况，内容以实际解压结果为准
CONTENT_DIR="${TMP_DIR}"
SUB_COUNT=$(ls -A "${TMP_DIR}" | wc -l)
FIRST_ITEM=$(ls -A "${TMP_DIR}" | head -1)
if [ "${SUB_COUNT}" -eq 1 ] && [ -d "${TMP_DIR}/${FIRST_ITEM}" ]; then
    CONTENT_DIR="${TMP_DIR}/${FIRST_ITEM}"
fi

# 清空部署目录后拷入新包内容
mkdir -p "${DEPLOY_DIR}"
rm -rf "${DEPLOY_DIR:?}"/*
cp -a "${CONTENT_DIR}/." "${DEPLOY_DIR}/"
echo "新包已部署: ${DEPLOY_DIR}"

# 部署目录内文件时间统一刷新为当前时间
touch "${DEPLOY_DIR}"/* 2>/dev/null || true

# 清理临时目录与已使用的新包
rm -rf "${TMP_DIR}"
rm -f "${SRC_ZIP}"
echo "部署完成，静态资源即时生效，无需重启nginx"
