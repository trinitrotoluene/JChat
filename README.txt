To <insert classmate code-reviewing this>, if you'd like the source code of this in its original (structured) form,
send me an email @ prm45@bath.ac.uk or Discord nitro#0001 and I'll send it to you.

Since project structure is against the specification, the flattened version is enclosed here for your pleasure.

This project requires Java 11+ to compile due to a dependency on java.net.http

Linux.Bath's openjdk installation supports Java 11, so if you're having difficulty compiling, check your version or do it there.

Implemented features:

= Server =
Entry Point: ChatServer.java
- Fast and fault tolerant, capable of reliably supporting a large number of concurrent clients
- Robust commands system for server management
    LIST        : List all connected clients
    BROADCAST   : Send a message to all connected clients
    EXIT        : Shut down the server
    KICK <name> : Disconnects a client from the server
- Support for direct messages
- Support for username changes
- Announces user join/leave

= Client =
Entry Point: ChatClient.java
- Fully asynchronous with send/receive operating independently
- Responsive and reliable
- Easy to configure and use
- Makes use of all available server features
- Commands
    DM <USER> <MESSAGE> : Send a user a message, privately
    NICK <NAME>         : Change your name if it's available
    EXIT                : Disconnect and shut down

= Bot =
Entry Point: ChatBot.java
- Run with ChatBot and the same CLI options as the ChatClient
- Commands
    ECHO    : Echoes a provided message back to you
    DADJOKE : :^)

= DoD =
Entry Point: DoDClient.java
- Run with the --map argument to select a map from the ./maps folder
- Otherwise supports the same config options as ChatClient/ChatBot
- Commands
    JOIN  : Join the game
    LEAVE : Leave the game
    <All other DoD commands>