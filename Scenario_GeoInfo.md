- [Location-based service](#location-based-service)
  - [Data modeling](#data-modeling)
    - [Operations to support](#operations-to-support)
    - [SQL](#sql)
      - [Most native way](#most-native-way)
      - [Geo location support in MySQL](#geo-location-support-in-mysql)
      - [Geo location support in PostgreSQL](#geo-location-support-in-postgresql)
    - [SQUAD](#squad)
    - [Graph database](#graph-database)
  - [References](#references)

# Location-based service

## Data modeling
* Different ways to represent geolocation
  - https://www.red-gate.com/simple-talk/sql/t-sql-programming/simple-sql-handling-location-datatypes/

### Operations to support
* Update location
* Given a location, find the nearest k points

### SQL
#### Most native way

```
# Create schema
Create Location
{
    locationId text,
    latitude double,
    longtitude double,
},

# Add location
Insert into location (locationId, latitude, longtitude) values ("id1", 48.88, 2.31)

# Search nearby k locations within r radius
Select locationId from Location where 48.88 - radius < latitude < 48.88 + radis and 2.31 + radius < longtitude < 2.31 + radius

```

#### Geo location support in MySQL

* Spatial reference system: https://mysqlserverteam.com/spatial-reference-systems-in-mysql-8-0/

#### Geo location support in PostgreSQL
1. PostgreSQL supports KNN search on top using distance operator <->

```
select id, name, pos
    from pubnames
order by pos <-> point(51.516,-0.12)
    limit 3;

     id     |          name          |           pos           
------------+------------------------+-------------------------
   21593238 | All Bar One            | (51.5163499,-0.1192746)
   26848690 | The Shakespeare's Head | (51.5167871,-0.1194731)
  371049718 | The Newton Arms        | (51.5163032,-0.1209811)
(3 rows)

# evaluated on 30k rows in total
Time: 18.679 ms 
```

2. The above query takes about 20 minutes, using KNN specific index (called GiST / SP-GiST) to speed up

```
> create index on pubnames using gist(pos);

> select id, name, pos
    from pubnames
order by pos <-> point(51.516,-0.12) limit 3;
     
     id     |          name          |           pos           
------------+------------------------+-------------------------
   21593238 | All Bar One            | (51.5163499,-0.1192746)
   26848690 | The Shakespeare's Head | (51.5167871,-0.1194731)
  371049718 | The Newton Arms        | (51.5163032,-0.1209811)
(3 rows)

# evaluated on 30k rows in total
Time: 0.849 ms
```

* https://tapoueh.org/blog/2013/08/the-most-popular-pub-names/

### SQUAD


### Graph database


![Schema design](./images/location_mySQL.jpg)

![Report design](./images/location_redis.jpg)

![Storage design](./images/location_storage.jpg)


## References
* [R tree / KD - tree](https://blog.mapbox.com/a-dive-into-spatial-search-algorithms-ebd0c5e39d2a)
* [Geohash vs S2 vs H3](https://dev.to/phm200/the-problem-of-nearness-part-2-a-solution-with-s2-23gm)
* [Redis data model based basic algorithm for finding available drivers](https://www.youtube.com/watch?v=cSFWlF96Sds)