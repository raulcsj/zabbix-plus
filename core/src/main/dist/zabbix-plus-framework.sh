#!/bin/bash

# Resolve APP_HOME to the directory where the script is located
APP_HOME="$(cd "$(dirname "$0")/.." && pwd)"

# Add default JVM options if needed
DEFAULT_JVM_OPTS="-Dfile.encoding=UTF-8"
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="$DEFAULT_JVM_OPTS"
else
    JAVA_OPTS="$DEFAULT_JVM_OPTS $JAVA_OPTS"
fi

# Construct the classpath
# Add all JARs from the lib directory
CLASSPATH="$APP_HOME/lib/*"

# Main class to execute
MAIN_CLASS="io.zabbixplus.framework.core.CoreApplication"

# Change to APP_HOME to resolve relative paths for config/ui if needed by the app
cd "$APP_HOME"

echo "Starting Zabbix Plus Framework..."
echo "APP_HOME: $APP_HOME"
echo "CLASSPATH: $CLASSPATH"
echo "JAVA_OPTS: $JAVA_OPTS"
echo "MAIN_CLASS: $MAIN_CLASS"
echo "Arguments: $@"

# Execute the Java application
java $JAVA_OPTS -cp "$CLASSPATH" "$MAIN_CLASS" "$@"
