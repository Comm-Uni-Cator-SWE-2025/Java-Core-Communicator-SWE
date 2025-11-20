run-core:
	cd /home2/student/112201013/java/Java-Core-Communicator-SWE/java ; /usr/bin/env /home2/student/112201013/tools/jdk/jdk-24.0.2_linux-x64_bin/jdk-24.0.2/bin/java @/tmp/cp_1k28ue32oc9mdjbgyxgr4il2v.argfile com.swe.controller.Init

compile:
	cd java ; mvn clean compile

test:
	cd java ; mvn test