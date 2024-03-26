- [Storage](#storage)
  - [Crawled webpage schema](#crawled-webpage-schema)
    - [DB selection](#db-selection)

# Storage

## Crawled webpage schema

```
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
                                               Storage                                               
│  ┌───────────────────────────────┐                                                                │
   │Webpage crawl history          │                                                                 
│  │                               │                                                                │
   │Url: string                    │                                                                 
│  │Domain: string (sharding key)  │                                                                │
   │Expected frequency: date       │                                                                 
│  │Last crawl timestamp: date     │                                                                │
   │Content signature: string      │                                                                 
│  │(calculate similarity)         │                                                                │
   │                               │                                                                 
│  │                               │                                                                │
   └───────────────────────────────┘                                                                 
│                                                                                                   │
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─
```

### DB selection

* Wide-column preferred because snapshot of the same page could be stored - support 3-dimensional query

```
(row, column family, timestamp)
```
