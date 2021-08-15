package com.khjxiaogu.MCMidi.Midi.Players;

import org.bukkit.block.Block;

import com.khjxiaogu.MCMidi.Midi.NoteInfo;
import com.khjxiaogu.MCMidi.Midi.NoteTrack;

public class NoteBlockPlayer extends TrackPlayer {
	Block nb;

	public NoteBlockPlayer(Block b, NoteTrack nc) {
		super(null, nc);
		nb = b;
	}

	@Override
	public void play(NoteInfo note) {
		note.play(nb);
	}

	@Override
	public boolean canPlay() {
		try {
			//nb.getBlock();
			return true;
		}catch(Exception ex){
			return false;
		}
		
	}

}
