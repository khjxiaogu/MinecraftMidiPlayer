package com.khjxiaogu.MCMidi;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.*;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.khjxiaogu.MCMidi.Midi.TrackPlayer;
import com.khjxiaogu.MCMidi.Midi.NotePlayers;
import com.khjxiaogu.MCMidi.Midi.NoteTrack;
public class MidiSheet implements ConfigurationSerializable{
	public List<NoteTrack> tracks=new ArrayList<>();
	public MidiSheet(Map<String, Object> map) {
		tracks.addAll((Collection<NoteTrack>) map.get("tracks"));
	}
	public MidiSheet(File f,int offset,float speed) throws InvalidMidiDataException, IOException {
        Sequence sequence;
		sequence = MidiSystem.getSequence(f);
        float framesPerSecond;
        if(sequence.getDivisionType()==Sequence.PPQ) {
        	framesPerSecond=0F;
        }else {
        	framesPerSecond=sequence.getDivisionType();
        }
        int resolution=sequence.getResolution();
        for (Track track :  sequence.getTracks()) {
        	NoteTrack currentTrack=new NoteTrack();
        	float beatsPerMinute=120;
        	if(track.size()>0)
            for (int i=0; i < track.size(); i++) {
            	float millisPerMidiTick;
            	if(framesPerSecond==0F) {//PPQ mode
            		millisPerMidiTick=60000/beatsPerMinute/resolution/speed;
            	}else {
            		millisPerMidiTick=1000/resolution/framesPerSecond/speed;
            	}
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == 144) {//Detect KEY_ON message
                        currentTrack.add(sm.getData1()+offset*12,Math.round(event.getTick()*millisPerMidiTick/50),sm.getData2());
                    }
                } else if(message instanceof MetaMessage){
                	MetaMessage metaMessage=(MetaMessage) message;
                	if(metaMessage.getStatus()==0xff&&metaMessage.getType()==0x51) {//Detect Speed change message
                		long microsPerBeat=0;
                		byte[] byteData=metaMessage.getData();
                		for(int j=0;j<byteData.length;j++) {
                			microsPerBeat*=0x100;
                			microsPerBeat+=byteData[j];
                		}
                		if(microsPerBeat!=0)
                		beatsPerMinute=(60000000/microsPerBeat);
                	}
                }
            }
        	if(currentTrack.getSize()!=0)
        		tracks.add(currentTrack);
        }
	}
	public NotePlayers playFor(Player p) {
		return new NotePlayers(p,this);
	}
	public NotePlayers playFor(Player p,boolean loop) {
		return new NotePlayers(p,this,loop);
	}
	public String getInfo() {
		StringBuilder sb=new StringBuilder("Tracks Info:\n");
		for(int i=0;i<tracks.size();i++) {
			sb.append("Track ");
			sb.append(i);
			sb.append(":");
			sb.append(tracks.get(i).getInfo());
			sb.append("\n");
		}
		return sb.toString();
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String,Object> map=new HashMap<>();
		map.put("tracks",tracks);
		return map;
	}
}




