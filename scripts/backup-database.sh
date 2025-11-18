#!/bin/bash
# scripts/backup-database.sh

set -a
source /app/.env
set +a

BACKUP_DIR="/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/backup_${TIMESTAMP}.sql"

echo "💾 Creating database backup..."

mkdir -p $BACKUP_DIR

# Backup from master
mysqldump -h mysql_master -u root -p${MYSQL_ROOT_PASSWORD} \
    --single-transaction \
    --routines \
    --triggers \
    ${MYSQL_DATABASE} > $BACKUP_FILE

if [ $? -eq 0 ]; then
    echo "✅ Backup created: $BACKUP_FILE"
    echo "📊 Backup size: $(du -h $BACKUP_FILE | cut -f1)"
else
    echo "❌ Backup failed"
    exit 1
fi