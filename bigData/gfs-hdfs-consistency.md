- [Consistency](#consistency)
  - [Normal write](#normal-write)
  - [Record appends](#record-appends)
    - [Guarantees at least once](#guarantees-at-least-once)

# Consistency

![](../.gitbook/assets/gfs_data_consistency.png)

## Normal write
* Only guarantees lose consistency but not defined. 

## Record appends

![](../.gitbook/assets/gfs_recordappend.png)

### Guarantees at least once

![](../.gitbook/assets/gfs_write_atleastonce.png)