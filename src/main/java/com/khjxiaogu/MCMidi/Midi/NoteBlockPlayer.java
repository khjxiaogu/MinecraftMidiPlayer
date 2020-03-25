package com.khjxiaogu.MCMidi.Midi;

import org.bukkit.block.NoteBlock;

public class NoteBlockPlayer extends TrackPlayer {
	NoteBlock nb;
	public NoteBlockPlayer(NoteBlock b, NoteTrack nc) {
		super(null, nc);
		nb=b;
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
