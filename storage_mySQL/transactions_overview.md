- [Version chain](#version-chain)
- [Redo logs](#redo-logs)
  - [Insert](#insert)
  - [Delete](#delete)
  - [Update](#update)
    - [Not modifying primary key](#not-modifying-primary-key)
    - [Modifying primary key](#modifying-primary-key)
- [Undo logs](#undo-logs)
- [Binlog](#binlog)

# Version chain

![](../.gitbook/assets/mysql_mvcc_versionchain.png)

# Redo logs
* Rollback 

## Insert

![](../.gitbook/assets/mysql_undolog_insert.png)

## Delete

![](../.gitbook/assets/mysql_undolog_delete.png)

## Update

### Not modifying primary key

![](../.gitbook/assets/mysql_undolog_update_noprimarykey.png)

### Modifying primary key

![](../.gitbook/assets/mysql_undolog_update_primarykey.png)

# Undo logs


# Binlog


