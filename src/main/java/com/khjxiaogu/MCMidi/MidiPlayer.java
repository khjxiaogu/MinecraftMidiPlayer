package com.khjxiaogu.MCMidi;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.*;

import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
public class MidiPlayer {
	List<NoteTrack> tracks=new ArrayList<>();
	public MidiPlayer(File f,int offset) throws InvalidMidiDataException, IOException {
        Sequence sequence;
		sequence = MidiSystem.getSequence(f);
        int Tracknum=0;
        float fps;
        if(sequence.getDivisionType()==Sequence.PPQ) {
        	fps=0F;
        }else {
        	fps=sequence.getDivisionType();
        }
        int res=sequence.getResolution();
        System.out.println(res);
        for (Track track :  sequence.getTracks()) {
        	NoteTrack curt;
        	tracks.add(curt=new NoteTrack());
        	float bpmcr=120;
        	
            for (int i=0; i < track.size(); i++) {
            	//System.out.println(i);
            	float crspt;
            	if(fps==0.0) {
            		crspt=60000 / (bpmcr * res);
            	}else {
            		crspt=1000/res/fps;
            	}
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == 144) {
                        int key = sm.getData1();
                        curt.add(key+offset*12,(long)(event.getTick()*crspt/50),sm.getData2());
                    }
                } else if(message instanceof MetaMessage){
                	MetaMessage mm=(MetaMessage) message;
            
                	if(mm.getStatus()==0xff&&mm.getType()==0x51) {
                		long msps=0;
                		byte[] bs=mm.getData();
                		for(int j=0;j<bs.length;j++) {
                			msps*=0x100;
                			msps+=bs[j];
                		}
                		if(msps!=0)
                		bpmcr=(msps/ 500000);
                	}
                }
            }

            System.out.println();
            Tracknum++;
        }
	}
	public NotePlayers playFor(Player p) {
		return new NotePlayers(p,this);
	}
}
class NoteTrack{
	List<NoteInfo> notes=new ArrayList<>();
	public NoteTrack() {}
	public void add(int key,long tick,int vol) {
		NoteInfo ni=NoteInfo.getNote(key, tick,vol);
		if(ni!=null)
		notes.add(ni);
	}
	public NotePlayer playAll(Player p) {
		NotePlayer ret=new NotePlayer(p,this);
		ret.play();
		return ret;
	}
}
class NotePlayers{
	List<NotePlayer> players=new ArrayList<>();
	public NotePlayers(Player p,MidiPlayer mp) {
		for(NoteTrack nc:mp.tracks) {
			players.add(nc.playAll(p));
		}
	}
	public void cancel() {
		for(NotePlayer np:players) {
			np.cancel();
		}
	}
}
class NotePlayer{
	Player p;
	NoteTrack nc;
	int index=-1;
	long curticks=0;
	boolean canceled=false;
	public NotePlayer(Player p,NoteTrack nc) {
		this.p=p;
		this.nc=nc;
	}
	public void cancel() {
		canceled=true;
	}
	public void play() {
		if(canceled)return;
		if(nc.notes.size()==0)return;
		if(index>=nc.notes.size())return;
		if(index>0)
		nc.notes.get(index).play(p);
		index++;
		while(index<nc.notes.size()&&nc.notes.get(index).ticks==curticks) {
			nc.notes.get(index).play(p);
			index++;
		}
		
		
		long wait=nc.notes.get(index).ticks-curticks;
		curticks=nc.notes.get(index).ticks;
		new BukkitRunnable() {
			@Override
			public void run() {
				if(p.isValid()&&p.isOnline())
				play();
			}
		}.runTaskLater(MCMidi.plugin,wait);
	}
}
class NoteInfo{
	long ticks;
	Note n;
	org.bukkit.Instrument ins;
	int volume=64;
	public NoteInfo(long ticks) {
		this.ticks=ticks;
		n=null;
	}
	public NoteInfo(int key,long tick,int vol) {
		int octave=key%12-4;
		volume=vol;
		ins=org.bukkit.Instrument.PIANO;
		if(octave>1) {
			//ins=org.bukkit.Instrument.STICKS;
			octave=octave%2;
		}
		if(octave<-2) {
			ins=org.bukkit.Instrument.BASS_DRUM;
			octave=-octave%2;
		}
		if(octave<0) {
			ins=org.bukkit.Instrument.BASS_GUITAR;
			octave=-octave%2;
		}
		ticks=tick;
		int note=key%12;
		switch(note) {
		case 0:n=Note.natural(octave,Tone.C);
		case 1:n=Note.sharp(octave,Tone.C);
		case 2:n=Note.natural(octave,Tone.D);
		case 3:n=Note.sharp(octave,Tone.D);
		case 4:n=Note.natural(octave,Tone.E);
		case 5:n=Note.natural(octave,Tone.F);
		case 6:n=Note.sharp(octave,Tone.F);
		case 7:n=Note.natural(octave,Tone.G);
		case 8:n=Note.sharp(octave,Tone.G);
		case 9:n=Note.natural(octave,Tone.A);
		case 10:n=Note.sharp(octave,Tone.A);
		case 11:n=Note.natural(octave,Tone.B);
		}
	}
	public NoteInfo(int octave,int note,long tick,int vol) {
		ins=org.bukkit.Instrument.PIANO;
		if(octave>1) {
			//ins=org.bukkit.Instrument.STICKS;
			octave=octave%2;
		}
		if(octave<-2) {
			ins=org.bukkit.Instrument.BASS_DRUM;
			octave=-octave%2;
		}
		if(octave<0) {
			ins=org.bukkit.Instrument.BASS_GUITAR;
			octave=-octave%2;
		}
		volume=vol;
		ticks=tick;
		switch(note) {
		case 0:n=Note.natural(octave,Tone.C);
		case 1:n=Note.sharp(octave,Tone.C);
		case 2:n=Note.natural(octave,Tone.D);
		case 3:n=Note.sharp(octave,Tone.D);
		case 4:n=Note.natural(octave,Tone.E);
		case 5:n=Note.natural(octave,Tone.F);
		case 6:n=Note.sharp(octave,Tone.F);
		case 7:n=Note.natural(octave,Tone.G);
		case 8:n=Note.sharp(octave,Tone.G);
		case 9:n=Note.natural(octave,Tone.A);
		case 10:n=Note.sharp(octave,Tone.A);
		case 11:n=Note.natural(octave,Tone.B);
		}
	}
	public static NoteInfo getNote(int key,long tick,int vol) {
		//int octave=key%12-4;
		//if(octave>1||octave<0)return null;
		return new NoteInfo(key,tick,vol);
	}
	public void play(Player p) {
		if(n!=null)
		p.playNote(p.getLocation().add(p.getLocation().getDirection().multiply((127-volume)*5)),ins, n);
	}
}
