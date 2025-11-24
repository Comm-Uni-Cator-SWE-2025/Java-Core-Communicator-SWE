cd ~/coding/sem7/se/demo1/Java-front-Communicator-SWE/java/module-ux
(mvn exec:java -Dexec.args="$1 --bypass-auth" > ux{$1}.log 2>&1) &



cd ~/coding/sem7/se/demo1/Java-Core-Communicator-SWE/java

mvn clean install -DskipTests -Dcheckstyle.skip=true
cd module-app

echo "--------------------------------"
echo "Running app on port $1"
echo "Running app on IP $2"
echo "--------------------------------"

mvn exec:java -Dexec.mainClass="com.swe.controller.Init" -Dexec.args="$1 $2" > a{$1}.log 2>&1