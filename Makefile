all:
	MAVEN_OPTS="-Xms128M -Xmx256M -Xss2M -XX:MaxMetaspaceSize=128M" mvn clean compile exec:java
