- [Functional requirements](#functional-requirements)
  - [Core](#core)
  - [Optional](#optional)
- [Non-Functional requirements](#non-functional-requirements)
  - [High performance](#high-performance)
  - [Reliable storage](#reliable-storage)
  - [High availability](#high-availability)
  - [Data security](#data-security)
  - [No duplication](#no-duplication)
  - [Real world practices](#real-world-practices)


# Functional requirements
## Core
* File upload
* File download
* File continue uploading/downloading on interruption

## Optional
* File sharing
* Speed control to prioritize paid users

# Non-Functional requirements
## High performance
* 10^8 TB storage space, 10^4 average QPS, 80 Gb/s

## Reliable storage
* Files don't get lost. Reliability reaches 99.9999%, 1 lost file among 1 million files. 

## High availability
* 99.99% availability per year. Less than 53 minutes downtime per year. 

## Data security
* Files are encrypted when stored on the web server. People other than the user itself and shared ones could not read the file. 

## No duplication
* If the same file is uploaded by two different users, the later does not need to upload again from the beginning. 

## Real world practices

* [https://www.infoq.com/presentations/dropbox-infrastructure/](https://www.infoq.com/presentations/dropbox-infrastructure/)
  * [How do we scale Dropbox slides](https://github.com/DreamOfTheRedChamber/system-design-interviews/tree/b195bcc302b505e825a1fbccd26956fa29231553/files/QConSF2016-PreslavLe-ScalingDropbox.pdf)
  * [Dropbox security whitepaper](https://www.dropbox.com/static/business/resources/Security_Whitepaper.pdf)
  * [S3 design](https://www.youtube.com/watch?v=UmWtcgC96X8)
