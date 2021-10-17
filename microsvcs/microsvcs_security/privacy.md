- [Privacy](#privacy)
  - [PII, PHI, PCI](#pii-phi-pci)
  - [Differential Privacy](#differential-privacy)
  - [Decentralized Identity](#decentralized-identity)

# Privacy
## PII, PHI, PCI 
Western culture has a tradition to respect privacy, especially after the Nazis murdered millions of people. Here are some typical sensitive data types: Personally Identifiable Information (PII), Protected Health Information (PHI, regulated by HIPAA), and Credit Card or Payment Card Industry (PCI) Information.

## Differential Privacy 
Redacting sensitive information alone may not be good enough to prevent data associated with other datasets.

Differential privacy helps analysts extract data from the databases containing personal information but still protects individuals’ privacy.

## Decentralized Identity 
To decouple id from a centralized identity provider and its associated sensitive data, we can use decentralized id (DID) instead.

it is essentially in the format of URN: did:example:123456789abcdefghijk
it could be derived from asymmetric keys and its target business domain.
it does not involve your personal info, unlike the traditional way
See DID method for how it is working with blockchains.
it preserves privacy by
use different DIDs for different purposes
selective disclosure / verifiable claims