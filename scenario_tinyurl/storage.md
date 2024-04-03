- [Traditional SQL](#traditional-sql)
  - [Schema design](#schema-design)


# Traditional SQL
## Schema design

```SQL
Create table tiny_url (
    Id bigserial primary key,
    short_url text not null, 
    original_url text not null,
    expired_datetime timestamp without timezone)
```
