
<!-- MarkdownTOC -->

- Https
	- Structure
	- How does TLS offer security
		- Confidentiality
			- Symmetric encryption
			- Asymmetric encryption
			- PKI
		- Authentication
			- Digital signature
				- Certificate
				- Certificate authority
				- Cons
		- Integrity
	- TLS protocol \(v1.2\)
		- Overall flowchart
		- Components
		- TLS Handshake based on RSA
		- TLS Handshake based on ECDHE
	- TLS protocol \(v1.3\) Optimization

<!-- /MarkdownTOC -->


# Https
## Structure
* Secure Sockets Layer / Transport layer security
* Fifth layer. Netscape 1994. V2/V3
* Versions:
	* SSLv1/v2
	* SSLv3.1 => TLS1.0
	* TLS1.0/1.1、SSLv3/v2 all considered to be unsecure
* Most widely used: TLS 1.2
* SSL/TLS could also be applied to other applications 
	* FTP => FTPS
	* LDAP => LDAPS

![HTTP stack](./images/https_stack.png)

![Security protocols](./images/https_tcpip_securityProtocol.png)

## How does TLS offer security
### Confidentiality
* A cipher suite is negotiated using TLS handshake and PKI is used to shared the secret key.

#### Symmetric encryption
* RC4, DES, 3DES, AES, ChaCha20
* Cons:
	- Does not have a reliable way to transfer cipher key

![Symmetric encryption](./images/https_symmetricCrypto.png)

#### Asymmetric encryption
* DH, DSA, RSA, ECC

![Asymmetric encryption](./images/https_asymmetricCrypto.png)

#### PKI
* First use RSA/ECDHE to solve the problem of exchanging private key
* Generate session key used for symmetric key

![PKI](./images/https_PKI.png)

```
// Speed comparison for symmetric asymetric
aes_128_cbc enc/dec 1000 times : 0.97ms, 13.11MB/s

rsa_1024 enc/dec 1000 times : 138.59ms, 93.80KB/s
rsa_1024/aes ratio = 143.17

rsa_2048 enc/dec 1000 times : 840.35ms, 15.47KB/s
rsa_2048/aes ratio = 868.13
```

### Authentication
* The identification is based on chain of trust and certificate authorities. 

#### Digital signature
*  Reverse the usage of private and public key inside asymmetric encryption
* Private key only encrypts the digest of message

##### Certificate

![digital signature](./images/https_security_digitalsignature.png)

##### Certificate authority
* Certificate authority
* Certificate issuer: DigiCert, VeriSign, Entrust, Let's Encrypt
* Types of certificates: 
	- DV
	- EV
* Root CA needs to have a self-signed certificate / root certificate.

![Certificate authority](./images/https_certificates_CA.png)

##### Cons
* What if CA gets tricked to issue certificate to the wrong person
	- CRL (certificate revocation list)
	- OCSP (online certificate status protocol)
* CA itself get hacked
* RSA asymmetric 

### Integrity
* Whenever a TLS record is sent, a MAC value is generated and appended with each message. 
* Digest algorithm
	- MD5, SHA1-> SHA2 (SHA224, SHA256 and SHA384 could generate 28 bytes, 32 bytes and 48 bytes digests, correspondingly)

## TLS protocol (v1.2)
### Overall flowchart

![Flow chart](./images/https_flowchart.png)

* TLS Cipher suite
	- key exchange algo - signature algo - symmetric encryption algo - digest algo
	- ECDHE-RSA-AES256-GCM-SHA384
* Many softwares such as Nginx/Apache use OpenSSL to implement TLS

### Components
* Record protocol
	- Defines the basic unit for data transfer
* Alert protocol
	- Send an alerting message to the other party
* Change cipher spec protocol 
	- States that all following conversations will be conducted in encrypted version
* Handshake protocol
	- See the next section for details

![TLS components](./images/https_tls12_components.png)

### TLS Handshake based on RSA
* Process
	1. The client generates a symmetric key, encrypts it with the server's public key.
	2. Clients sends it to the server to use as the symmetric key for the established session. 
	3. The server uses its private key to decrypt the sent symmetric key and the key-exchange is complete. 
	4. Client and server could use the negotiated symmetric key to encrypt their session. 
* Weakness:
	- The same public-private key pair is used both to authenticate the server and to encrypt the symmetric session key sent to the server. As a result, if an attacker gains access to the private key and listens in on the exchange, then it could decrypt the entire session. 
	- if an attacker does not currently have access to the private key, they can still record the encrypted session and decrypt it at a later time once they obtain the private key. 

### TLS Handshake based on ECDHE
* Improvement compared with RSA:
	- Allow the client and server to negotiate a shared secret without explicitly communicating it in the handshake: The server's private key is used to sign and verify the handshake, but the established symmetric key never leaves the client or server and cannot be intercepted by a passive attacker even if they have access to the private key. 
	- Diffie-Hellman key exchange can also be used to reduce the risk of compromise of past communication sessions. 
	- TLS False Start: Client could send out request without waiting for server to reply with "Finished"
	- Will generate a new public/private key each time having a handshake. So even when one time's pre-master get deciphered, all previous conversation could stay safe. 
* "Hello" exchange random number, "Key exchange" exchange "pre-master".
* Before "Change Cipher Spec", all plaintext; After all ciphertext.
* Pre-Master must be transmitted in secret. 

0. TCP handshake first

1. Client Hello
	- TLS version 
	- Client random 
	- Cipher suites (e.g. ECDHE_RSA)

```
Handshake Protocol: Client Hello
    Version: TLS 1.2 (0x0303)
    Random: 1cbf803321fd2623408dfe…
    Cipher Suites (17 suites)
        Cipher Suite: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 (0xc02f)
        Cipher Suite: TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 (0xc030)
```

2. Server Hello: 
	- TLS version
	- Server random
	- Cipher suites

```
Handshake Protocol: Server Hello
    Version: TLS 1.2 (0x0303)
    Random: 0e6320f21bae50842e96…
    Cipher Suite: TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 (0xc030)
```

3. Server key exchange: 
	- Server params

```
Handshake Protocol: Server Key Exchange
    EC Diffie-Hellman Server Params
        Curve Type: named_curve (0x03)
        Named Curve: x25519 (0x001d)
        Pubkey: 3b39deaf00217894e...
        Signature Algorithm: rsa_pkcs1_sha512 (0x0601)
        Signature: 37141adac38ea4...
```

4. Client key exchange
	- Client params

```
Handshake Protocol: Client Key Exchange
    EC Diffie-Hellman Client Params
        Pubkey: 8c674d0e08dc27b5eaa…
```

5. Now the Client has Client Params, Server Params, from which they will be able to compute PreMaster

6. Using Client Random, Server Random and PreMaster, client and server will compute Master Secret. 

7. Client sends Change cipher spec.

8. Client sends Finished.

9. Server sends Change cipher spec. 

10. Server sends Finished. 

## TLS protocol (v1.3) Optimization
* OSCP Stapling
* TLS compression
* TLS false start
* Session resumption
* Early termination


