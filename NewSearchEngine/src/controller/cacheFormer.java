package controller;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
public class cacheFormer {

	public cacheFormer() {
		// TODO Auto-generated constructor stub
	}
	
	public static void MakeCache(List<String> urlBag)
	{
		CacheManager singletonManager = CacheManager.create();
		Cache memoryOnlyCache = new Cache("URLData", 100, false, true, 86400,86400);
		singletonManager.addCache(memoryOnlyCache);
		Cache test = singletonManager.getCache("URLData");
		String key; 
		for(int i=0;i<urlBag.size();i++)
		{
			key=Integer.toString(i) ;
			test.put(new Element(key,urlBag.get(i)));
		}
	}
	
	
	public static URL_Data getCacheData(int elmNum)
	{
		String elementNum =Integer.toString(elmNum); 
		CacheManager singletonManager = CacheManager.create();
		Cache test = singletonManager.getCache("URLData");
		Element ele = test.get(elementNum);
		String output = (ele == null ? null : ele.getObjectValue().toString());
		singletonManager.shutdown();
		URL_Data urlObj=URL_Data.getRecords(output);
		return urlObj;
	}

}
