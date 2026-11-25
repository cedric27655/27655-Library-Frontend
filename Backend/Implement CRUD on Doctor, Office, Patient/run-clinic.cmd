@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo JDK not found at "%JAVA_HOME%".
    exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

set "JAR_FILE=%~dp0target\clinic-0.0.1-SNAPSHOT.jar"

if exist "%JAR_FILE%" (
    echo Starting Clinic backend on port 8085...
    "%JAVA_HOME%\bin\java.exe" -Xmx256m -Xms64m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -jar "%JAR_FILE%"
) else (
    echo Building Clinic project...
    set "MAVEN_OPTS=-Xmx256m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
    call "%~dp0mvnw.cmd" package -DskipTests
    if exist "%JAR_FILE%" (
        echo Starting Clinic backend on port 8085...
        "%JAVA_HOME%\bin\java.exe" -Xmx256m -Xms64m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -jar "%JAR_FILE%"
    ) else (
        echo Build failed. Please check the logs.
        exit /b 1
    )
)
