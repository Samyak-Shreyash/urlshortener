#!/bin/bash

set -a
source .env
set +a

echo "📊 Monitoring MySQL Replication Status"

while true; do
    clear
    echo "=== MySQL Replication Status - $(date) ==="
    echo ""
    
    # Master status
    echo "🔴 MASTER STATUS:"
    mysql -h mysql_master -u root -p${MYSQL_ROOT_PASSWORD} -e "
        SHOW MASTER STATUS\G
    " | grep -E "(File|Position|Executed_Gtid_Set)"
    
    echo ""
    
    # Slave status
    for SLAVE in mysql_slave1 mysql_slave2; do
        echo "🟢 $SLAVE STATUS:"
        SLAVE_STATUS=$(mysql -h $SLAVE -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW SLAVE STATUS\G")
        
        echo "$SLAVE_STATUS" | grep -E "(Slave_IO_State|Master_Host|Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master|Last_Error)" | while read line; do
            echo "  $line"
        done
        
        # Check for replication lag
        LAG=$(echo "$SLAVE_STATUS" | grep "Seconds_Behind_Master" | awk '{print $2}')
        if [ "$LAG" == "0" ]; then
            echo "  ✅ Replication is real-time (0 seconds behind)"
        elif [ "$LAG" == "NULL" ]; then
            echo "  ⚠️  Replication not running"
        else
            echo "  ⚠️  Replication lag: $LAG seconds"
        fi
        
        echo ""
    done
    
    # Check for errors
    echo "🔍 ERROR CHECK:"
    for SLAVE in mysql_slave1 mysql_slave2; do
        ERROR=$(mysql -h $SLAVE -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW SLAVE STATUS\G" | grep "Last_Error" | awk '{print $2}')
        if [ "$ERROR" ]; then
            echo "  ❌ $SLAVE Error: $ERROR"
        else
            echo "  ✅ $SLAVE No errors"
        fi
    done
    
    sleep 5
done