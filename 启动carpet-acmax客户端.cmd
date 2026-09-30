@echo off
cd /d "D:\mc\开发\carpet-acmax-26.1.2"
set "JAVA_HOME=C:\Users\29800\AppData\Roaming\.hmcl\java\windows-x86_64\mojang-java-runtime-epsilon"
echo 正在启动 Minecraft 开发客户端，首次启动需要编译，请稍候...
call gradlew.bat runClient
pause
