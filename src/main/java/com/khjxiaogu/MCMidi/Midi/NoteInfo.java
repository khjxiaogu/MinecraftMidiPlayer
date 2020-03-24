package com.khjxiaogu.MCMidi.Midi;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import com.khjxiaogu.MCMidi.MCMidi;


public class NoteInfo implements ConfigurationSerializable{
	@FunctionalInterface 
	interface Initializer{
		public void init(NoteInfo n,int key);
	}
	long ticks;
	Note n;
	org.bukkit.Instrument ins;
	int volume=64;
	private final static org.bukkit.Instrument[] inss=new org.bukkit.Instrument[10];
	private final static Note[] notes=new Note[25];
	private static Initializer init;
	private final int key;
	public static void initNotes() {
		if(!MCMidi.plugin.getConfig().getBoolean("universal",false)){
			inss[9]=org.bukkit.Instrument.BASS_DRUM;
			inss[8]=org.bukkit.Instrument.BASS_DRUM;
			inss[7]=org.bukkit.Instrument.BASS_DRUM;
			inss[6]=org.bukkit.Instrument.STICKS;
			inss[5]=org.bukkit.Instrument.PIANO;
			inss[4]=org.bukkit.Instrument.SNARE_DRUM;
			inss[3]=org.bukkit.Instrument.BASS_GUITAR;
			inss[2]=org.bukkit.Instrument.BASS_GUITAR;
			inss[1]=org.bukkit.Instrument.BASS_GUITAR;
			inss[0]=org.bukkit.Instrument.BASS_GUITAR;
			init=(t,k)->{t.n=notes[k%12];t.ins=inss[k/12];};
		}else {
			inss[9]=org.bukkit.Instrument.BELL;
			inss[8]=org.bukkit.Instrument.BELL;
			inss[7]=org.bukkit.Instrument.SNARE_DRUM;
			inss[6]=org.bukkit.Instrument.SNARE_DRUM;
			inss[5]=org.bukkit.Instrument.PIANO;
			inss[4]=org.bukkit.Instrument.PIANO;
			inss[3]=org.bukkit.Instrument.BASS_GUITAR;
			inss[2]=org.bukkit.Instrument.BASS_GUITAR;
			inss[1]=org.bukkit.Instrument.BASS_DRUM;
			inss[0]=org.bukkit.Instrument.BASS_DRUM;
			init=(t,k)->{t.n=notes[k%24];t.ins=inss[k/12];};
		}
		notes[0]=Note.sharp(0,Tone.F);
		notes[1]=Note.natural(0,Tone.G);
		notes[2]=Note.sharp(0,Tone.G);
		notes[3]=Note.natural(0,Tone.A);
		notes[4]=Note.sharp(0,Tone.A);
		notes[5]=Note.natural(0,Tone.B);
		notes[6]=Note.natural(0,Tone.C);
		notes[7]=Note.sharp(0,Tone.C);
		notes[8]=Note.natural(0,Tone.D);
		notes[9]=Note.sharp(0,Tone.D);
		notes[10]=Note.natural(0,Tone.E);
		notes[11]=Note.natural(0,Tone.F);
		notes[12]=Note.sharp(1,Tone.F);
		notes[13]=Note.natural(1,Tone.G);
		notes[14]=Note.sharp(1,Tone.G);
		notes[15]=Note.natural(1,Tone.A);
		notes[16]=Note.sharp(1,Tone.A);
		notes[17]=Note.natural(1,Tone.B);
		notes[18]=Note.natural(1,Tone.C);
		notes[19]=Note.sharp(1,Tone.C);
		notes[20]=Note.natural(1,Tone.D);
		notes[21]=Note.sharp(1,Tone.D);
		notes[22]=Note.natural(1,Tone.E);
		notes[23]=Note.natural(1,Tone.F);
		notes[24]=Note.sharp(2,Tone.F);
	} 
	public NoteInfo(long ticks) {
		this.ticks=ticks;
		n=null;
		key=0;
	}
	public NoteInfo(int key,long tick,int vol) {
		volume=vol;
		ticks=tick;
		init.init(this, key);
		this.key=key;
	}

	public NoteInfo(Map<String,Object> map) {
		this((int)map.get("key"),(long)map.get("time"),(int)map.get("volume"));
	}
	public static NoteInfo getNote(int key,long tick,int vol) {
		return new NoteInfo(key,tick,vol);
	}
	public void play(Player p) {
		if(n!=null)
			p.playNote(p.getLocation(),ins, n);
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String,Object> map=new HashMap<>();
		map.put("key",key);
		map.put("time",ticks);
		map.put("volume", volume);
		return map;
	}
}