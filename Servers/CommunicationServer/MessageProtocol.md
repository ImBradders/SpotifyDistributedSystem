# Intro

This is the message protocol that the Communication Server will use.The idea behind the Communication Server is to act as a load balancer between the other online servers - keeping clients always connected whereever possible.
In order to handle this, the Communication Server will allow clients to connect to it where they will be assigned a server to connect to in order for music to be delivered.
The Communication Server will also allow the other servers to connect to it to see what servers are online.

## Format

REQUEST | RESPONSE

Within this document, outlined are the responses that should be received when certain messages are sent to the server from a client. They will be formatted in line with how this section is.

## Client Messages Handled

### Heartbeat

HEARTBEAT | HEARTBEAT

A message to check to ensure that the connection is still alive, as if the client hasn't sent a message for a considerable time.

### Get Server

GETSERVER : [type] | IP : a.b.c.d : PORT : x

A message to ask for a move to a music service or login provider.

### Disconnect

DISCONNECT | DISCONNECT

A message to initiate a disconnect from the server. This will close the connection to the client.

### Unsupported

[Unsupported Message] | MESSAGEUNSUPPORTED

If the client sends a message that is not supported by the server, MESSAGEUNSUPPORTED will be returned.

## Server Messages Handled

### Server Type
SERVERTYPE | TYPESTORED

Allows the remote server to tell the Communication Server what type of server it is.

### Get Server

GETSERVER | IP : a.b.c.d : PORT : x

A message to ask for a bulk storage server or a database server.

### Disconnect

DISCONNECT | DISCONNECT

A message to initiate a disconnect from the server. This will close the connection to the server.

### Unsupported

[Unsupported Message] | MESSAGEUNSUPPORTED

If the server sends a message that is not supported, MESSAGEUNSUPPORTED will be returned.
