If you'd like to view the source-code in its original form with subdirectories and package declarations,
send me an email @ prm45@bath.ac.uk or Discord nitro#0001 and I'll send you a copy.
Since subfolders are against the specification, the flattened version is submitted here.

Note: Ensure you're using a version of java that supports "var" syntax i.e. 10+ (Linux.Bath runs Java 11).

Implemented features:

= Server =
Entry Point: ChatServer
- Fast and fault tolerant, capable of reliably supporting a large number of concurrent clients
- Robust commands system for server management
    LIST        : List all connected clients
    BROADCAST   : Send a message to all connected clients
    EXIT        : Shut down the server
- Support for direct messages
- Announces user join/leave

= Client =
Entry Point: ChatClient
- Fully asynchronous with send/receive operating independently
- Responsive and reliable
- Easy to configure and use
- Makes use of all available server features
- Commands
    DM <USER> <MESSAGE> : Send a user a message, privately
    EXIT                : Disconnect and shut down
    NICK <NAME>         : Change your name

= Bot =
Entry Point: ChatBot
- Run with ChatBot and the same CLI options as the ChatClient
- Commands
    ECHO    : Echoes a provided message back to you
    DADJOKE : :^)
    HELP    : Information about available commands

= DoD =
Entry Point: DoDClient
- Select a map from the menu at startup
- Otherwise supports the same config options as ChatClient/ChatBot
- Commands
    JOIN  : Join the game
    <All other DoD commands>