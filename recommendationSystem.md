<!-- MarkdownTOC -->

- [Recommendation system](#recommendation-system)
	- [Use cases](#use-cases)
	- [Components](#components)
	- [Overview](#overview)
		- [Cold start](#cold-start)
		- [Offline processing](#offline-processing)
		- [Online processing](#online-processing)
	- [Design](#design)
		- [Version 1](#version-1)
		- [Version 2](#version-2)
		- [Version 3](#version-3)

<!-- /MarkdownTOC -->

# Recommendation system

## Use cases
* Long tail
* Utilize the traffic
* Same or close categories of products
	- Collaborative filtering
		+ e.g. The number of people viewing A 10, the number of people viewing B 20. The number of people viewing A and B is 5. Then the correlation coefficient between A and B is "A intersect B" / "A union B"

## Components
* Regression
* Sort

## Overview
### Cold start
* Recommend something popular
	1. Based on the content / based on the user behavior
	2. Compute the correlation coefficient
	3. Sort

### Offline processing

### Online processing

## Design
### Version 1
* No user customization
* Factors
	- Number of pictures
	- The length of online
* Fixed weights 
* Making sure that not all results come from a single saler
* No support for online user testing

### Version 2
* Overtime users have accumulated their behavior
* Record users' behavior into Hadoop cluster

### Version 3



