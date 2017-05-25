# RXA TCP BENCHMARK

The project consists of creating a network benchmark tool.
It has been implemented in java socket, a server part that deals with the
Requests from the customer.

We will generate two graphs at first:
- A graph measuring the bit rate in nbr bytes / second as a function of the number of bytes
Sent via connection.
- A graph measuring the bit rate in nbr bytes / second as a function of the number of threads in the
Client connected to the server.


# Structure


```
RXA_TCP_BENCH
|____client
| |____src
| | |____client
| | | |____ConnectionMode.java
| | | |____Mode.java
| | | |____Client.java
| | | |____ThreadMode.java
| | | |____ThreadModeClient.java
| | |____utils
| | | |____ByteFormat.java
|____server
| |____src
| | |____utils
| | | |____ByteFormat.java
| | |____serveur
| | | |____ServeurThread.java
| | | |____Serveur.java
|____Readme.md
|____ReportRXA.pdf


```

