# Intro

This is the message protocol that the StorageServer will use.
This server will be serving music to the streaming servers

## Format

REQUEST | RESPONSE

Within this document, outlined are the responses that should be received when certain messages are sent to the server from a client. They will be formatted in line with how this section is.

## Messages Handled

### Search

SEARCH : [keyword] | TITLE : [song name]

This will search the list of songs for a given keyword. If there is something matching that keyword, it will return the full title of the song. If nothing is found, "ERROR : no songs matching this keyword" will be sent back instead.

### A Song has been played

SONGPLAYED : [song title]

This simply tells the storage server that a given song has been played. It can then add it to the list of recent requests.

### Get a Song

SONG [title] or [keyword] | SONG followed by multiple messages containing the song itself.

Retrieves a song from the storage server to be sent to the streaming server.

### Retrieve List of Songs

SONGLIST | SONGS : [a song title] + more

Gets the full song list and sends this to the client.
SONGS is followed by the first returned song and then further messages in the same format will be sent until the end of the list of songs is complete.

### Get Recommendation

RECOMMEND | [song title]

Gets a recommendation which can be sent to the user.

### Add User

ADD : [username] : [password] | ADDED or ERROR

This will add a user to the system.
Possible Errors:

- Username already exists
- Unable to create account

### Login

LOGIN : [username] : [password] | AUTH or ERROR

Logs a user into the system.
Possible errors:

- Username does not exist
- Password incorrect
- Unable to log in

### Disconnect

DISCONNECT | DISCONNECT

This message allows servers to safely disconnect from this server.
