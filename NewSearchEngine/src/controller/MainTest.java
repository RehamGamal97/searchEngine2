package controller;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class MainTest {

	public MainTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		MakeCache();
		GetCacheData();
//		CacheManager singletonManager = CacheManager.create();
//		Cache memoryOnlyCache = new Cache("URLData", 100, false, true, 86400,86400);
//		singletonManager.addCache(memoryOnlyCache);
//		Cache test = singletonManager.getCache("URLData");
//
//		//4. Put few elements in cache
//		test.put(new Element("1","Jan"));
//		test.put(new Element("2","Feb"));
//		test.put(new Element("3","Mar"));
//
//		//5. Get element from cache
//		Element ele = test.get("1");
//
//		//6. Print out the element
//		String output = (ele == null ? null : ele.getObjectValue().toString());
//		System.out.println(output);
//
//		//7. Is key in cache?
//		System.out.println(test.isKeyInCache("1"));
//		System.out.println(test.isKeyInCache("5"));
//
//		//8. shut down the cache manager
//		singletonManager.shutdown();
	}
	static void MakeCache()
	{
		CacheManager singletonManager = CacheManager.create();
		Cache memoryOnlyCache = new Cache("URLData", 100, false, true, 86400,86400);
		singletonManager.addCache(memoryOnlyCache);
		Cache test = singletonManager.getCache("URLData");
		test.put(new Element("0","Jan"));
		test.put(new Element("2","Feb"));
		test.put(new Element("3","Mar"));
	}
	
	static void GetCacheData()
	{
		CacheManager singletonManager = CacheManager.create();
		Cache test = singletonManager.getCache("URLData");
		System.out.println("test");

				//5. Get element from cache
				Element ele = test.get("0");

				//6. Print out the element
				String output = (ele == null ? null : ele.getObjectValue().toString());
				System.out.println(output);

				//7. Is key in cache?
				System.out.println(test.isKeyInCache("1"));
				System.out.println(test.isKeyInCache("5"));

				//8. shut down the cache manager
				singletonManager.shutdown();
	}

}
