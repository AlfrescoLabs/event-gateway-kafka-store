#!/bin/sh

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

start() {
    export HOST_IP=$(ifconfig | grep -E "([0-9]{1,3}\.){3}[0-9]{1,3}" | grep -v 127.0.0.1 | awk '{ print $2 }' | cut -f2 -d: | head -n1)
    docker-compose up -d
}

down() {
    docker-compose down
}

tail() {
    docker-compose logs -f
}

build() {
    docker rmi alfresco/alfresco-event-gateway-app:development
    $MVN_EXEC -f ../pom.xml -Ddocker.tag=development clean package
}

start_gateway() {
    export HOST_IP=$(ifconfig | grep -E "([0-9]{1,3}\.){3}[0-9]{1,3}" | grep -v 127.0.0.1 | awk '{ print $2 }' | cut -f2 -d: | head -n1)
    docker-compose up --build -d alfresco-event-gateway
}

stop_gateway() {
    docker-compose kill alfresco-event-gateway
    yes | docker-compose rm -f alfresco-event-gateway
}

case "$1" in
  build_start)
    down
    build
    start
    tail
    ;;
  start)
    start
    tail
    ;;
  reload_gateway)
    stop_gateway
    build
    start_gateway
    tail
    ;;
  stop)
    down
    ;;
  tail)
    tail
    ;;
  *)
    echo "Usage: $0 {build_start|start|reload_gateway|stop|tail}"
esac
