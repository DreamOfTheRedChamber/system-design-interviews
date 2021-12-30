# Reverse proxy

* Steps: 
  1. Client's requests first reach reverse proxy. 
  2. The reverse proxy forwards requests to internal servers and gets the response. 
  3. The reverse proxy forwards the response to clients.
* Pros: 
  * Integrated together with reverse proxy. No additional deployment. 
* Cons:
  * Reverse proxy operates on the HTTP layer so not high performance. It is usually used on a small scale when there are fewer than 100 servers. 
* There is a flow chart [Caption in Chinese to be translated](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/images/loadBalancing-ReverseProxy.png)
