#!/bin/bash
# scripts/wait-for-db.sh

set -e

host="$1"
shift
port="$1"
shift
user="$1"
shift
password="$1"
shift
cmd="$@"

echo "⏳ Waiting for MySQL at $host:$port..."

until mysql -h "$host" -P "$port" -u "$user" -p"$password" -e "SELECT 1;" &> /dev/null; do
  echo "📡 MySQL is unavailable - sleeping"
  sleep 2
done

echo "✅ MySQL is up - executing command"
exec $cmd