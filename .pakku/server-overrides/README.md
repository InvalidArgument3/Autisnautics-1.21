# Running the Server Pack

To run the Server Pack:

1. Install the ServerPack in your server of choice
    * Doing it manually or utilizing a third party website should work fine
2. Start the Server Pack
    * You can start it by running the "start_server.bat" file
    * On Linux/macOS, run `./start_server.sh`
    * Or just open the batch file if on a windows machine.
3. The startup script will launch the server and auto-restart on crash/stop.

## Update Behavior

Server runtime preferences like Java path should be configured locally on the server host.
They are intentionally not overwritten by pack updates.