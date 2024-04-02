- [RPC](#rpc)
  - [Initial design](#initial-design)
- [Restful](#restful)
- [Interface impl](#interface-impl)
  - [shortURL insert( longURL )](#shorturl-insert-longurl-)
  - [longURL lookup( shortURL )](#longurl-lookup-shorturl-)


# RPC

## Initial design

```java
public void generateTinyUrl(TinyUrlRequest request) throw ServiceException;

class TinyUrlRequest
{
    String originalUrl;
    Date expiredDate;
}

public string getOriginalUrl(String tinyUrl) throw ServiceException;
```

* If we need to provide service \(API\) to other vendors, we need api key and rate limiting

```java
public void generateTinyUrl(String APIKey, TinyUrlRequest request) throw ServiceException;
public string getOriginalUrl(String APIKey, String tinyUrl) throw ServiceException;
```

# Restful

* Post /tinyurl
* Get /tinyurl?url=xxxxx
* Put data into post body

```json
data:
{
    original_url: xxxx,
    expired_date: xxxx, 
    ip: xxxx
}
```

# Interface impl

```java
class TinyURL
{
    map<longURL, shortURL> longToShortMap;
    map<shortURL, longURL> shortToLongMap;

    shortURL insert( longURL )
    {
        if longToShortMap not containsKey longURL
            generate shortURL;
            put<longURL, shortURL> into longToShortMap;
            put<shortURL, longURL> into shortToLongMap;
        return longToShortMap.get(longURL);
    }

    longURL lookup( shortURL )
    {
        return shortToLongMap.get( shortURL );
    }
}
```

## shortURL insert\( longURL \)
* Long to short with Base62

```java
    public String longToShort( String url ) 
    {
        GLOBAL_ID++;
        url2id.put( url, GLOBAL_ID );
        id2url.put( GLOBAL_ID, url );
        return "http://tiny.url/" + idToShortKey( GLOBAL_ID );
    }

    private String idToShortKey( int id )
    {        
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String short_url = "";
        while ( id > 0 ) 
        {
            short_url = chars.charAt( id % 62 ) + short_url;
            id = id / 62;
        }

        while ( short_url.length() < 6 ) 
        {
            short_url = "0" + short_url;
        }

        return short_url;
    }
```

## longURL lookup\( shortURL \)
* Short to long with Base62

```java
    public String shortToLong( String url ) 
    {
        String short_key = getShortKey( url );
        int id = shortKeytoID( short_key );
        return id2url.get( id );
    }

    private String getShortKey( String url ) 
    {
        return url.substring( "http://tiny.url/".length() );
    }

    private int shortKeytoID( String short_key ) 
    {
        int id = 0;
        for ( int i = 0; i < short_key.length(); ++i ) 
        {
            id = id * 62 + toBase62( short_key.charAt( i ) );
        }

        return id;
    }
```
