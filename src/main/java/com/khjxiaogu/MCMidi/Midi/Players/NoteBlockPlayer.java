package com.khjxiaogu.MCMidi.Midi.Players;

import org.bukkit.block.NoteBlock;

import com.khjxiaogu.MCMidi.Midi.NoteInfo;
import com.khjxiaogu.MCMidi.Midi.NoteTrack;

public class NoteBlockPlayer extends TrackPlayer {
	NoteBlock nb;

	public NoteBlockPlayer(NoteBlock b, NoteTrack nc) {
		super(null, nc);
		nb = b;
	}

	@Override
	public void play(NoteInfo note) {
		note.play(nb);
	}

	@Override
	public boolean canPlay() {
		return nb.isPlaced();
	}

}
