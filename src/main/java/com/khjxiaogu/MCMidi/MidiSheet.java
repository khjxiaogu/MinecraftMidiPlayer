package com.khjxiaogu.MCMidi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.NoteBlock;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.khjxiaogu.MCMidi.Midi.NoteBlockPlayers;
import com.khjxiaogu.MCMidi.Midi.NotePlayers;
import com.khjxiaogu.MCMidi.Midi.NoteTrack;

public class MidiSheet implements ConfigurationSerializable {
	public List<NoteTrack> tracks = new ArrayList<>();
	private static int MsPerGameTick=50;
	public MidiSheet(File f, int offset, float speed) throws InvalidMidiDataException, IOException {
		Sequence sequence;
		sequence = MidiSystem.getSequence(f);
		float framesPerSecond;
		if (sequence.getDivisionType() == Sequence.PPQ) {
			framesPerSecond = 0F;
		} else {
			framesPerSecond = sequence.getDivisionType();
		}
		int resolution = sequence.getResolution();
		for (Track track : sequence.getTracks()) {
			NoteTrack currentTrack = new NoteTrack();
			double beatsPerMinute = 120;
			double millisPerMidiTick;
			if (framesPerSecond == 0F) {// PPQ mode
				millisPerMidiTick = 60000 / beatsPerMinute / resolution / speed;
			} else {
				millisPerMidiTick = 1000 / resolution / framesPerSecond / speed;
			}
			//int b = 0;
			if (track.size() > 0) {
				for (int i = 0; i < track.size(); i++) {
					

					
					MidiEvent event = track.get(i);
					MidiMessage message = event.getMessage();
					/*if (message instanceof MetaMessage) {
						StringBuilder logsb=new StringBuilder(message.getClass().getSimpleName()).append(":");
						byte[] msg=message.getMessage();
						int status=message.getStatus();
						
						logsb.append(status).append(",");
						logsb.append(((MetaMessage) message).getType()).append(",");
						for(int ki=0;ki<msg.length;ki++) {
							logsb.append(Integer.toHexString(Math.abs(msg[ki]))).append("-");
						}
						System.out.println(logsb.toString());
					}*/
					if (message instanceof ShortMessage) {
						ShortMessage sm = (ShortMessage) message;
						if ((sm.getCommand()&0x90)>0) {// Detect KEY_ON message
							currentTrack.add(sm.getData1() + offset * 12,
									Math.round(event.getTick() * millisPerMidiTick / MsPerGameTick), sm.getData2());
							/*if(b==20) {
								System.out.println(event.getTick());
							}
							b++;*/
						}
					} else if (message instanceof MetaMessage) {
						MetaMessage metaMessage = (MetaMessage) message;
						if (metaMessage.getStatus() == 0xff ) {
							if(metaMessage.getType() == 0x51) {// Detect tempo change
								long microsPerBeat = 0;
								
								byte[] byteData = metaMessage.getData();
								for (int j = 0; j < byteData.length; j++) {
									microsPerBeat *= 0x100;
									microsPerBeat += Byte.toUnsignedInt(byteData[j]);
								}
								//System.out.println(microsPerBeat);
								if (microsPerBeat != 0) {
									beatsPerMinute = 60000000 / microsPerBeat;
								}
								if (framesPerSecond == 0F) {// PPQ mode
									millisPerMidiTick = 60000 / beatsPerMinute / resolution / speed;
								} else {
									millisPerMidiTick = 1000 / resolution / framesPerSecond / speed;
								}
								//b=0;
							}
						}
					}
				}
			}
			if (currentTrack.getSize() != 0) {
				tracks.add(currentTrack);
			}
		}
	}
	@SuppressWarnings("unchecked")
	public MidiSheet(Map<String, Object> map) {
		tracks.addAll((Collection<NoteTrack>) map.get("tracks"));
	}
	public void placeBlock(Location start,Vector direction,Material base) {
		Location cur=start.clone();
		NoteTrack Combined=new NoteTrack();
		for(NoteTrack t:tracks) {
			Combined.addAll(t);
		}
		Combined.sort();
		Combined.placeBlock(cur, direction,24,base);
	}
	public void placeBlock(Location start,Vector direction,final int width,Material base) {
		Location cur=start.clone();
		NoteTrack Combined=new NoteTrack();;
		for(NoteTrack t:tracks) {
			Combined.addAll(t);
		}
		Combined.sort();
		Combined.placeBlock(cur, direction,width,base);
	}
	public boolean Combine() {
		if(tracks.size()==1)
			return false;
		NoteTrack Combined=new NoteTrack();;
		for(NoteTrack t:tracks) {
			Combined.addAll(t);
		}
		Combined.sort();
		tracks.clear();
		tracks.add(Combined);
		return true;
	}
	public NotePlayers playFor(Player p) {
		return new NotePlayers(p, this);
	}

	public NotePlayers playFor(Player p, boolean loop) {
		return new NotePlayers(p, this, loop);
	}
	public NoteBlockPlayers playBlock(NoteBlock nb, boolean loop) {
		return new NoteBlockPlayers(nb, this, loop);
	}
	public String getInfo() {
		StringBuilder sb = new StringBuilder("Tracks Info:\n");
		for (int i = 0; i < tracks.size(); i++) {
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
		Map<String, Object> map = new HashMap<>();
		map.put("tracks", tracks);
		return map;
	}
}
