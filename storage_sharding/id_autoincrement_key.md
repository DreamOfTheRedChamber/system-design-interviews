- [MySQL auto-increment primary key](#mysql-auto-increment-primary-key)
  - [Cons](#cons)

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
* Not unique in distributed environments. 