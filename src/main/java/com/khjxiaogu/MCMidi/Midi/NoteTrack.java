package com.khjxiaogu.MCMidi.Midi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import com.khjxiaogu.MCMidi.Messages;

public class NoteTrack implements ConfigurationSerializable{
	List<NoteInfo> notes=new ArrayList<>();
	public NoteTrack() {}
	@SuppressWarnings("unchecked")
	public NoteTrack(Map<String, Object> map) {
		for(Map<?,?> ms:(List<Map<String,Object>>)map.get("notes")) {
			notes.add(new NoteInfo(ms));
		}
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
	public int getSize() {
		return notes.size();
	}
	public String getInfo() {
		if(notes.size()>0)
			return Messages.getString("MCMidi.track_note_count")+notes.size()+Messages.getString("MCMidi.track_length")+notes.get(notes.size()-1).ticks+"ticks"; //$NON-NLS-1$ //$NON-NLS-2$
		else
			return Messages.getString("MCMidi.track_note_count")+0+Messages.getString("MCMidi.track_length")+0; //$NON-NLS-1$ //$NON-NLS-2$
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String,Object> map=new HashMap<>();
		List<Map<String,Object>> lso=new ArrayList<>(notes.size());
		if(notes.size()>0) {
			for(int i=0;i<notes.size();i++) {
				lso.add(notes.get(i).serialize());
			}
			map.put("notes",lso); //$NON-NLS-1$
		}
		return map;
	}
}
