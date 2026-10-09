@echo off
cd /d "%~dp0"
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
call gradlew.bat runClient --console=plain
pause