package kabbage.killcounter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.Bukkit;

import kabbage.zarena.ZArena;

public class KillCounter
{
	public static KillCounter instance;
	
	private final KCCommands kcCommands = new KCCommands();
	private KCListener kcListener;
	
	private TreeMap<String, Integer> killsMap;
	private HashMap<String, Integer> baseKillsMap;
	
	public void enable()
	{
		instance = this;
		
		kcListener = new KCListener();			
		kcListener.registerEvents(Bukkit.getServer().getPluginManager());
		
		ZArena.getInstance().getCommand("killcounter").setExecutor(kcCommands);
		baseKillsMap = new HashMap<String, Integer>();
		ValueComparator<String, Integer> vc = new ValueComparator<String, Integer>(baseKillsMap);
		killsMap = new TreeMap<String, Integer>(vc);
	}
	
	public void disable()
	{
		
	}
	
	public void addKill(String playerName)
	{
		int kills = baseKillsMap.containsKey(playerName) ? baseKillsMap.get(playerName) + 1 : 1;
		setKills(playerName, kills);
	}
	
	public void setKills(String playerName, int kills)
	{
		baseKillsMap.remove(playerName);
		killsMap.remove(playerName);
		baseKillsMap.put(playerName, kills);
		killsMap.put(playerName, kills);
	}
	
	public Integer getKills(String playerName)
	{
		return killsMap.get(playerName);
	}
	
	public Entry<String, Integer> getEntry(int index)
	{
		Iterator<Entry<String, Integer>> iter = killsMap.entrySet().iterator();
		int i = 0;
		while(++i < index && iter.hasNext()) iter.next();
		return iter.next();
	}
	
	public int indexOf(String playerName)
	{
		if(!killsMap.containsKey(playerName))
			return -1;
		int index = 0;
		Iterator<String> iter = killsMap.keySet().iterator();
		String currentKey;
		do
		{
			index++;
			currentKey = iter.next();
		} while(!currentKey.equals(playerName));
		return index;
	}
	
	public int mapSize()
	{
		return killsMap.size();
	}
	
	class ValueComparator<K, V extends Comparable<V>> implements Comparator<K>
	{
	    Map<K, V> base;
	    public ValueComparator(Map<K, V> base)
	    {
	        this.base = base;
	    }

		@Override
		public int compare(K a, K b)
		{
			if (base.get(a).compareTo(base.get(b)) == 1)
			{
	            return -1;
	        } else {
	            return 1;
	        } // Don't return 0, as that would end up combining the keys, which we don't want
		}
	}
}