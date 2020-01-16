# Intro

These are the message protocols that the Multi-UseServer will use.
The idea behind this is that this server can act as either a streaming server or a login server depending upon the needs of the system.

## Format

REQUEST | RESPONSE

Within this document, outlined are the responses that should be received when certain messages are sent to the server from a client. They will be formatted in line with how this section is.

## StreamingServer

### Disconnect

DISCONNECT | DISCONNECT

This message allows the client to disconnect safely from the music streaming provider.

### Enqueue Song

SONG : [search term] | ADDED : [song title]

Adds a song to this user's current queue. If the song could not be found, "ERROR : not found" shall be returned instead.

### Retrieve List of Songs

SONGLIST | SONGS : [a song title] + more

Gets the full song list and sends this to the client. 
SONGS is followed by the first returned song and then further messages in the same format will be sent until the end of the list of songs is complete.
Can also return "ERROR:No songs" if there are no songs in the system.

### Get Recommendation

RECOMMENDATION | The recommended song

Sends a recommendation for what song the user could play next.

## Login Server

### Disconnect

DISCONNECT | DISCONNECT

This message allows the client to disconnect safely from the login server.

### Create Account

CREATE : [username] : [password] | ADDED or ERROR

Creates an account for the username and password provided.
Possible errors include:

- Not enough params.
- Username already exists
- Unable to create user

### Login

LOGIN : [username] : [password] | AUTH or ERROR

Logs a user into their account providing them with an auth token which they can use later.
Possible errors include:

- Not enough params
- Username does not exist
- Password incorrect
- Unable to log in
