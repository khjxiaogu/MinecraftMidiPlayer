package com.khjxiaogu.MCMidi.Midi;

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
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.khjxiaogu.MCMidi.Midi.Players.NoteBlockPlayers;
import com.khjxiaogu.MCMidi.Midi.Players.NotePlayers;

// TODO: Auto-generated Javadoc
/**
 * Class MidiSheet.
 *
 * @author khjxiaogu
 * file: MidiSheet.java
 * time: 2020年8月9日
 */
public class MidiSheet implements ConfigurationSerializable {
	
	/** The tracks.<br> 成员 tracks. */
	public List<NoteTrack> tracks = new ArrayList<>();
	public String filename;
	private final static int MsPerGameTick = 50;

	/**
	 * Instantiates a new MidiSheet.<br>
	 * 新建一个MidiSheet类<br>
	 *
	 * @param f the f<br>
	 * @param offset the offset<br>
	 * @param speed the speed<br>
	 * @throws InvalidMidiDataException if an invalid midi data exception occurred.<br>如果invalid midi data exception发生了
	 * @throws IOException Signals that an I/O exception has occurred.<br>发生IO错误
	 */
	public MidiSheet(File f, int offset, float speed) throws InvalidMidiDataException, IOException {
		filename=f.getName();
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
			long lastOffset=0;
			long lastTick=0;
			if (track.size() > 0) {
				for (int i = 0; i < track.size(); i++) {
					MidiEvent event = track.get(i);
					MidiMessage message = event.getMessage();
					if (message instanceof ShortMessage) {
						ShortMessage sm = (ShortMessage) message;
						if ((sm.getCommand() & ShortMessage.NOTE_ON) > 0) {// Detect KEY_ON message
							long delta=event.getTick()-lastTick;
							lastTick=event.getTick();
							lastOffset+=Math.round(delta * millisPerMidiTick / MsPerGameTick);
							currentTrack.add(sm.getData1() + offset * 12,lastOffset, sm.getData2());
						}
					} else if (message instanceof MetaMessage) {
						MetaMessage metaMessage = (MetaMessage) message;
						if (metaMessage.getStatus() == 0xff) {
							if (metaMessage.getType() == 0x51) {// Detect tempo change
								long microsPerBeat = 0;
								byte[] byteData = metaMessage.getData();
								for (int j = 0; j < byteData.length; j++) {
									microsPerBeat *= 0x100;
									microsPerBeat += Byte.toUnsignedInt(byteData[j]);
								}
								if (microsPerBeat != 0) {
									beatsPerMinute = 60000000 / microsPerBeat;
								}
								if (framesPerSecond == 0F) {// PPQ mode
									millisPerMidiTick = 60000 / beatsPerMinute / resolution / speed;
								} else {
									millisPerMidiTick = 1000 / resolution / framesPerSecond / speed;
								}
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

	/**
	 * Instantiates a new MidiSheet with a Map object.<br>
	 * 使用一个Map新建一个MidiSheet类<br>
	 *
	 * @param map the map<br>
	 */
	@SuppressWarnings("unchecked")
	public MidiSheet(Map<String, Object> map) {
		tracks.addAll((Collection<NoteTrack>) map.get("tracks"));
		filename=String.valueOf(map.get("name"));
	}

	/**
	 * Place block.<br>
	 *
	 * @param start the start<br>
	 * @param direction the direction<br>
	 * @param base the base<br>
	 */
	public void placeBlock(Location start, Vector direction, Material base) {
		Location cur = start.clone();
		NoteTrack Combined = new NoteTrack();
		for (NoteTrack t : tracks) {
			Combined.addAll(t);
		}
		Combined.sort();
		Combined.placeBlock(cur, direction, 24, base);
	}

	/**
	 * Place block.<br>
	 *
	 * @param start the start<br>
	 * @param direction the direction<br>
	 * @param width the width<br>
	 * @param base the base<br>
	 */
	public void placeBlock(Location start, Vector direction, final int width, Material base) {
		Location cur = start.clone();
		NoteTrack Combined = new NoteTrack();
		;
		for (NoteTrack t : tracks) {
			Combined.addAll(t);
		}
		Combined.sort();
		Combined.placeBlock(cur, direction, width, base);
	}

	/**
	 * Combine.<br>
	 *
	 * @return true, if <br>如果，返回true。
	 */
	public boolean Combine() {
		if (tracks.size() == 1)
			return false;
		NoteTrack Combined = new NoteTrack();
		;
		for (NoteTrack t : tracks) {
			Combined.addAll(t);
		}
		Combined.sort();
		tracks.clear();
		tracks.add(Combined);
		return true;
	}

	/**
	 * Play for.<br>
	 *
	 * @param p the p<br>
	 * @return return play for <br>返回 note players
	 */
	public NotePlayers playFor(Player p) {
		return new NotePlayers(p, this);
	}

	/**
	 * Play for.<br>
	 *
	 * @param p the p<br>
	 * @param loop the loop<br>
	 * @return return play for <br>返回 note players
	 */
	public NotePlayers playFor(Player p, boolean loop) {
		return new NotePlayers(p, this, loop);
	}

	/**
	 * Play block.<br>
	 *
	 * @param nb the nb<br>
	 * @param loop the loop<br>
	 * @return return play block <br>返回 note block players
	 */
	public NoteBlockPlayers playBlock(Block nb, boolean loop) {
		return new NoteBlockPlayers(nb, this, loop);
	}

	/**
	 * Gets the info.<br>
	 * 获取 info.
	 *
	 * @return info<br>
	 */
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

	/**
	 * Serialize.<br>
	 *
	 * @return return serialize <br>返回 map
	 */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("tracks", tracks);
		map.put("name",filename);
		return map;
	}
}
