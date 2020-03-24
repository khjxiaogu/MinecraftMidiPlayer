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
		if(index>=nc.notes.size())return;
		
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
	private static org.bukkit.Instrument[] inss=new org.bukkit.Instrument[10];
	private static Note[] notes=new Note[25];
	static {
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
	}
	public NoteInfo(int key,long tick,int vol) {
		volume=vol;
		ticks=tick;
		setMinecraftOctave(key/12);
		setMinecraftNote(key%12);
	}
	private void setMinecraftNote(int note) {
		n=notes[note+6];
	}
	private void setMinecraftOctave(int octave) {
		ins=inss[octave];

	}
	public NoteInfo(int octave,int note,long tick,int vol) {
		volume=vol;
		ticks=tick;
		setMinecraftOctave(octave);
		setMinecraftNote(note);
	}
	public static NoteInfo getNote(int key,long tick,int vol) {
		return new NoteInfo(key,tick,vol);
	}
	public void play(Player p) {
		if(n!=null)
		p.playNote(p.getLocation()/*.add(p.getLocation().getDirection().multiply((127-volume)*5))*/,ins, n);
	}
}
