#!/bin/bash
# scripts/check-replication.sh

set -a
source /app/.env
set +a

echo "🔍 Checking MySQL Replication Status"

if [ "$1" = "master" ]; then
    echo "=== MASTER STATUS ==="
    mysql -h mysql_master -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW MASTER STATUS\G"
elif [ "$1" = "slave1" ]; then
    echo "=== SLAVE 1 STATUS ==="
    mysql -h mysql_slave1 -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW SLAVE STATUS\G" | grep -E "Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master|Last_Error"
elif [ "$1" = "slave2" ]; then
    echo "=== SLAVE 2 STATUS ==="
    mysql -h mysql_slave2 -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW SLAVE STATUS\G" | grep -E "Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master|Last_Error"
else
    echo "=== ALL REPLICATION STATUS ==="
    echo "Master:"
    mysql -h mysql_master -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW MASTER STATUS\G" | grep -E "File|Position|Executed_Gtid_Set"
    echo ""
    echo "Slave 1:"
    mysql -h mysql_slave1 -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW SLAVE STATUS\G" | grep -E "Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master"
    echo ""
    echo "Slave 2:"
    mysql -h mysql_slave2 -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW SLAVE STATUS\G" | grep -E "Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master"
fi