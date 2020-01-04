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

### Get Song

GET : [song title] | SENDING

This will request the file from the server. It will then send multiple messages until the entire file has been transferred. Once the whole file is transferred, the message "EOF : EOF : EOF" will be sent to say that the end of the file has been reached.
