# Payment system

## Payment process
1. Retrieve users that needs charging from SQS and corresponding info: subscription ID, charge fees. 
2. Retrieve user payment method from MySQL.
3. Prepare payment request based on obtained info.
4. Check whether the request is pending (Such payment request is sent, but not finished due to server failure or third party payment problems), if yes, ignore (avoid paying twice).
5. Send request to Third Party Payment, retry or failure process ( send payment failure back to SQS )
6. If OK response from Third Party, update pending list, send confirmation back to SQS. 