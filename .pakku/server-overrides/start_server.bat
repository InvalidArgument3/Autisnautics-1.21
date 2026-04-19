@echo off

:START
echo Starting Minecraft server...
java -Xmx12288M -Xms6144M -jar minecraft_server.jar nogui

echo Server stopped/crashed. Restarting in 5 seconds...
timeout /t 5 /nobreak >nul
goto START
