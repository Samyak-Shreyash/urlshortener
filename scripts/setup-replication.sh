#!/bin/bash

set -a
source .env
set +a

echo "🔧 Setting up MySQL replication..."

# Wait for master to be ready
echo "Waiting for master to be ready..."
until mysql -h mysql_master -u root -p${MYSQL_ROOT_PASSWORD} -e "SELECT 1" &> /dev/null; do
    echo "Master not ready yet... waiting 5 seconds"
    sleep 5
done

echo "Master is ready!"

# Get master GTID position
MASTER_STATUS=$(mysql -h mysql_master -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW MASTER STATUS\G")
MASTER_GTID=$(mysql -h mysql_master -u root -p${MYSQL_ROOT_PASSWORD} -e "SELECT @@GLOBAL.GTID_EXECUTED" -s)

echo "Master GTID Position: $MASTER_GTID"

# Setup slaves
for SLAVE in mysql_slave1 mysql_slave2; do
    echo "Setting up $SLAVE..."
    
    # Wait for slave to be ready
    until mysql -h $SLAVE -u root -p${MYSQL_ROOT_PASSWORD} -e "SELECT 1" &> /dev/null; do
        echo "$SLAVE not ready yet... waiting 5 seconds"
        sleep 5
    done

    # Configure replication
    mysql -h $SLAVE -u root -p${MYSQL_ROOT_PASSWORD} << EOF
STOP SLAVE;
RESET SLAVE ALL;

CHANGE MASTER TO
MASTER_HOST='mysql_master',
MASTER_USER='${MYSQL_REPLICATION_USER}',
MASTER_PASSWORD='${MYSQL_REPLICATION_PASSWORD}',
MASTER_AUTO_POSITION=1,
MASTER_CONNECT_RETRY=10,
MASTER_RETRY_COUNT=1000000,
MASTER_HEARTBEAT_PERIOD=2;

START SLAVE;
EOF

    echo "$SLAVE replication configured"
done

echo "✅ Replication setup completed!"