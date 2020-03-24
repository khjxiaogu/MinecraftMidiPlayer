package com.khjxiaogu.MCMidi.Midi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public class NoteTrack implements ConfigurationSerializable{
	List<NoteInfo> notes=new ArrayList<>();
	public NoteTrack() {}
	@SuppressWarnings("unchecked")
	public NoteTrack(Map<String, Object> map) {
		notes.addAll((Collection<NoteInfo>) map.get("notes"));
	}
	public void add(int key,long tick,int vol) {
		NoteInfo ni=NoteInfo.getNote(key, tick,vol);
		if(ni!=null)
		notes.add(ni);
	}
	public TrackPlayer playAll(Player p) {
		TrackPlayer ret=new TrackPlayer(p,this);
		ret.play();
		return ret;
	}
	public String getInfo() {
		return "Notes: "+notes.size()+" Length: "+notes.get(notes.size()-1);
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String,Object> map=new HashMap<>();
		map.put("notes",notes);
		return map;
	}
}
