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

