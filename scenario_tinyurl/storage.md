- [Traditional SQL](#traditional-sql)
  - [Schema design](#schema-design)
  - [HBase for storing the short-url =\> long-url mapping](#hbase-for-storing-the-short-url--long-url-mapping)
  - [HDFS for storing the pre-generated short-url =\> long-url mapping](#hdfs-for-storing-the-pre-generated-short-url--long-url-mapping)


# Traditional SQL
## Schema design

```SQL
Create table tiny_url (
    Id bigserial primary key,
    short_url text not null, 
    original_url text not null,
    expired_datetime timestamp without timezone)
```

## HBase for storing the short-url => long-url mapping

## HDFS for storing the pre-generated short-url => long-url mapping