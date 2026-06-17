
build:
	mvn clean package
bump:
	mvn versions:set -DgenerateBackupPoms=false
publish-central:
	mvn clean deploy -Pcentral
check-updates:
	mvn -U -ntp net.optionfactory:anarchitect-maven-plugin:LATEST:check-updates
local-journal-webd:
	docker run -ti --rm \
		-p8000:8000 \
		--mount type=bind,source=${PWD}/local/configuration.json,target=/journal-webd-conf/configuration.json,readonly \
		--mount type=bind,source=/var/log/journal/,target=/journal-webd-logs/,readonly \
		optionfactory/debian13-journal-webd:209 

