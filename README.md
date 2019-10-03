## Steps to reproduce the issue

#### 1. Start the server app from `Server.scala`

This will start an http server on port 9005. This server has an http enpoint which exposes an SSE stream. The stream closes after 10 seconds.
The server keeps printing the number of requests that it encounters.

#### 2. Start the client app from `Client.scala`

This will spawn 1000 concurrent requests to http://localhost:9005. It will print how many streams it has been able to open and how many are ended 
successfully.
