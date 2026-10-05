@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not defined JAVA_HOME (
  echo 请先将 JAVA_HOME 设置为 JDK 25 路径。
  pause
  exit /b 1
)
call gradlew.bat runClient --console=plain
pause
