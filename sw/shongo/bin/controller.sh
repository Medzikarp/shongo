cd `dirname $0`/../
VERSION=`cat pom.xml | grep '<shongo.version>' | sed -e 's/.\+>\(.\+\)<.\+/\1/g'`
java -jar controller/target/controller-$VERSION.jar "$@"
