# TinyURL 

## Comments
* Comment1
Design tiny URL 问了很多细节，最后居然问到了怎么配置memcache, 估计是不揭穿lz的画皮不甘心，不过相信他们是为了找我的亮点吧
长到短，要查长的是否已经存在，存在直接返回已有值（这个就是我说的查重）；如果不存在则生成一个唯一的短id存到数据库
短到长，查短id是否存在，不存在就报错，存在则返回。
其实都需要index, 然后根据需要load进cache，但是这些普通数据库都已经实现了，不需要我们操心。
当然你可以把index都load到memcache/redis加快点访问速度，不过这里都是没有必要的。

## Naive thought
* shortURL insert(longURL)
* longURL lookup(shortURL)
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

## How to generate shortURL from long URL
### Traditional hash function
#### Types
* Crypto hash function: MD5 and SHA-1
	- Secure but slow
* Fast hash function: Murmur and Jenkins
	- Performance
	- Have 32-, 64-, and 128-bit variants available

#### Pros
* No need to write additional hash function, easy to implement
* Are randomly distributed
* Support URL clean

#### Cons

| Problem                             | Possible solution                                                                                                                                              | 
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| Not short enough (At least 4 bytes) | Only retrieve first 4-5 digit of the hash result                                                                                                               | 
| Collision cannot be avoided         | Use long_url + timestamp as hash argument, if conflict happens, try again (timestamp changes) -> multiple attempts, highly possible conflicts when data is big | 
| Slow                                |                                                                                                                                                                | 

### Base10 / Base62
#### Base is important

| Encoding           | Base10     | Base62      | 
|--------------------|------------|-------------| 
| Year               | 36,500,000 | 36,500,000  | 
| Usable characters  | [0-9]      | [0-9a-zA-Z] | 
| Encoding length    | 8          | 5           | 

#### Pros:
* Shorter URL
* No collision
* Simple computation

#### Cons:
* No support for URL clean

#### Long to short
```java
    public String longToShort( String url ) 
    {
        if ( url2id.containsKey( url ) ) 
        {
            return "http://tiny.url/" + idToShortKey( url2id.get( url ) );
        }
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

#### Short to long
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