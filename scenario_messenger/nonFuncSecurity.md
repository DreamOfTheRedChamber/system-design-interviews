- [Security](#security)
  - [Transmission security](#transmission-security)
  - [Storage security](#storage-security)
  - [Content security](#content-security)
- [[Nice-have nonfunctional features] Perf for multi-media](#nice-have-nonfunctional-features-perf-for-multi-media)
  - [Upload](#upload)
  - [Send](#send)
  - [Network stability](#network-stability)

# Security
## Transmission security
* Entrance security:
  * Router's DNS hijacked: DNS location is set to a location with virus.
  * Operator's local DNS hijacked:
    * Operator might send DNS requests to other operators to reduce the resource consumption
    * Operator might modify the TTL for DNS
  * Ways to prevent DNS from being hijacked
    * HttpDNS protocol: Prevent domain name from being hijacked by operators. It uses HTTP protocol instead of UDP to directly interact with DNS servers.
    * Combine HttpDNS with localDNS.
* TLS transmission layer security:
  * Cut off network
    * Failover to multiple connection IP address returned by HttpDNS service
    * Change from UDP based QUIC protocol to TCP protocol
  * Intercept/Man in the middle/Forfeit: Use TLS protocol
    * Insymetric encryption and key exchange algorithm are used to guarantee message encryption key not being corrupted or leaked.
    * Symmetric encryption is used to guarantee that the msg could not be decrypted after being intercepted.
    * Digital signature and CA certificate could be used to verify the valid status of public key.

## Storage security
* Account credentials: Hashing algorithm with salt.
* Message security: End to end encryption

## Content security
* Link to external phishing website
* Crawler

# [Nice-have nonfunctional features] Perf for multi-media
## Upload
* Picture/Video/Voice:
  * Picture/Video media: Have a dedicated channel for video and picture media. After media (video/picture) is uploaded to the storage, a unique ID will be generated and used along with messages.
  * Voice media：There is no miniature for preview. Voice media will be transmitted in the same channel as message team.
* Divide and send:
  * Size of the divide: Divide too big, not enough parrallelism; Divide too small, too many TCP connections and increased cost for merge.
  * Typical size of pieces: WiFi 2M; 4G 1M; 3G/2G 256K.
  * Since the size of media is big, it will be beneficial to divide and retransmit.
* Dedupe the media
  * Compute the hash for media before uploading

## Send
* Prerequisites for supporting watch while send
  1. Format and key frame info is at the top of file.
  2. Storage support range queries.
     * Ali OSS / Tencent COS, support range queries
     * Utilize load balance layer range query. (Nginx HTTP Slice)
* CDN
  * Encryption with HLS.
* Compression
  * Image compression
    * Adaptive resolution
    * WebP: WebP is roughly 30% smaller than PNG/JPEG. Cons is not easy to be integrated on iOS platform / JPEG
    * JPEG: Two types of JPEG ???
  * Video:
    * H.265 is 50% less than H.264. But encoding/decoding much more time consuming.

## Network stability

* Use public allowed ports when possible: 80, 8080, 443, 14000
* Http Tunnel: Use Http protocol to encapsulate other incompatible protocols
* Multi IP addresses: Rely on HttpDNS to return multiple IP addresses
* Connection fast
  * Reduce the latency among multi network operators
  * Race among multiple endpoints: After multiple IP addresses returned by HttpDNS, benchmark against different endpoints.
* Separating upload and download tunnel:
  * In case of broadcasting, there will be lots of msgs being sent in the downward channel.
  * Could use short connection in upload channel, long connection in download channel.
