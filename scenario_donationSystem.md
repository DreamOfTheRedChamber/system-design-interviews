* Say if DoorDash along with other partners across US is sponsoring for 3-day charity event where huge partipation of more than 3 million customers are expected to participare and simply donate money. You were responsible to design an app for this. How would you go about it?

Your app simply accept certain details like customer name, email address, credit/payment method details. You can assume DoorDash already has partned with payment gateway to store the money collected from event, and trasfer them back later.

# Functional requirements
* Participants 
  * Search for a charity. 
  * Donate money
* Donated charity: 
  * 

# NonFunctional requirements
* Accuracy
* Consistency > Availability
* Latency: < 300ms

# API design
* StatusCode = withHoldFund(identity, amount, paymentDetails)
* StatusCode = makePayment(payer, payee, amount, paymentDetails, idempotencyKey)

## High level design


# References
* https://medium.com/partha-pratim-sanyal/system-design-doordash-a-prepared-food-delivery-service-bf44093388e2