- [MySQL auto-increment primary key](#mysql-auto-increment-primary-key)
  - [Cons](#cons)
  - [Not continously increasing](#not-continously-increasing)

# MySQL auto-increment primary key
* Different ways to define automatic incremental primary key

```sql
-- MySQL
create table ‘test’ (
  ‘id’  int(16) NOT NULL AUTO_INCREMENT,
  ‘name’  char(10) DEFAULT NULL,
  PRIMARY KEY(‘id’) 
) ENGINE = InnoDB;

-- Oracle create sequence
create sequence test_seq increment by 1 start with 1;
insert into test(id, name) values(test_seq.nextval, ' An example ');
```

## Cons

## Not continously increasing

* However, auto-increment primary key is not a continuously increasing sequence. 
* For example, two transactions T1 and T2 are getting primary key 25 and 26. However, T1 transaction gets rolled back and then 

![](../.gitbook/assets/uniqueIdGenerator_primaryKey_notContinuous.png)

![](../.gitbook/assets/uniqueIdGenerator_primaryKey_notContinuous2.png)

![](../.gitbook/assets/uniqueIdGenerator_primaryKey_notContinuous3.png)
