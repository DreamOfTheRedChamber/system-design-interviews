- [Def](#def)
- [Three states](#three-states)
- [CLOSED =\> OPEN](#closed--open)
- [HALF-OPEN =\> CLOSED](#half-open--closed)
- [Diagrams](#diagrams)
- [How to avoid the constantly switching between OPEN and CLOSED state ??](#how-to-avoid-the-constantly-switching-between-open-and-closed-state-)

# Def
* Implementing a circuit breaker pattern helps prevent cascading failures and provides a way to gracefully handle service failures.

# Three states
* OPEN: In this state, the circuit breaker blocks requests from reaching the actual microservice. The proxy starts & manages the timeout period during the Open state, giving the system time to recover from the fault. The circuit breaker will remain in the Open state until the timeout period ends. 
* HALF-OPEN: In this state, the circuit breaker will allow a limited number of requests to reach service. 
  * If those requests are successful, the circuit breaker will switch the state to Closed and allow normal operations. 
  * If not, it will again block the requests for the defined timeout period.
* CLOSED: In this state, circuit breaker silently observe the health of service and shift to half-open state if needed. 

# CLOSED => OPEN 
* Choosing the timeout period

# HALF-OPEN => CLOSED 
* Allowing limited num of requests to come through. This criteria is important to avoid the service switching between OPEN and CLOSED constantly. 

# Diagrams
* Please see https://www.pollydocs.org/strategies/circuit-breaker

# How to avoid the constantly switching between OPEN and CLOSED state ??