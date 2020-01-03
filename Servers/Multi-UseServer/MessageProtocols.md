# Intro

These are the message protocols that the Multi-UseServer will use.
The idea behind this is that this server can act as either a streaming server or a login server depending upon the needs of the system.

## Format

REQUEST | RESPONSE

Within this document, outlined are the responses that should be received when certain messages are sent to the server from a client. They will be formatted in line with how this section is.

## StreamingServer

### Authorise

AUTHORISE : [token] | AUTHORISED

This message allows a given client to authenticate themselves. If they do this, they will be sent regular suggestions as to what they may wish to listen to next.

### Disconnect

DISCONNECT | DISCONNECT

This message allows the client to disconnect safely from the music streaming provider.

### Enqueue Song

SONG : [search term] | ADDED : [song title]

Adds a song to this user's current queue. If the song could not be found, "ERROR : not found" shall be returned instead.

### Dequeue Song

REMOVE : [search term] | REMOVED : [song title]

Removes a song from the client's current queue. If the song cannot be found within the queue, "ERROR : not found" shall be returned instead.
