# Scenario\_RecommendationSystem-\[TODO\]

* [Recommendation system](scenario_recommendationsystem-todo.md#recommendation-system)
  * [Use cases](scenario_recommendationsystem-todo.md#use-cases)
  * [Components](scenario_recommendationsystem-todo.md#components)
  * [Overview](scenario_recommendationsystem-todo.md#overview)
    * [Cold start](scenario_recommendationsystem-todo.md#cold-start)
    * [Offline processing](scenario_recommendationsystem-todo.md#offline-processing)
    * [Online processing](scenario_recommendationsystem-todo.md#online-processing)
  * [Design](scenario_recommendationsystem-todo.md#design)
    * [Version 1](scenario_recommendationsystem-todo.md#version-1)
    * [Version 2](scenario_recommendationsystem-todo.md#version-2)
    * [Version 3](scenario_recommendationsystem-todo.md#version-3)

## Recommendation system

### Use cases

* Long tail
* Utilize the traffic
* Same or close categories of products
  * Collaborative filtering
    * e.g. The number of people viewing A 10, the number of people viewing B 20. The number of people viewing A and B is 5. Then the correlation coefficient between A and B is "A intersect B" / "A union B"

### Components

* Regression
* Sort

### Overview

#### Cold start

* Recommend something popular
  1. Based on the content / based on the user behavior
  2. Compute the correlation coefficient
  3. Sort

#### Offline processing

#### Online processing

### Design

#### Version 1

* No user customization
* Factors
  * Number of pictures
  * The length of online
* Fixed weights 
* Making sure that not all results come from a single saler
* No support for online user testing

#### Version 2

* Overtime users have accumulated their behavior
* Record users' behavior into Hadoop cluster

#### Version 3

