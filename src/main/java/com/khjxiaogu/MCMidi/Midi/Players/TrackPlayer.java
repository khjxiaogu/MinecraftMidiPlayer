package com.khjxiaogu.MCMidi.Midi.Players;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.khjxiaogu.MCMidi.MCMidi;
import com.khjxiaogu.MCMidi.Midi.NoteInfo;
import com.khjxiaogu.MCMidi.Midi.NoteTrack;

public class TrackPlayer {
	private Player p;
	private NoteTrack nc;
	private int index = -1;
	private long curticks = 0;
	private boolean canceled = false;
	private Boolean finished = false;

	public boolean isFinished() {
		synchronized (finished) {
			return finished;
		}
	}

	public TrackPlayer(Player p, NoteTrack nc) {
		this.p = p;
		this.nc = nc;
	}

	public void reset() {
		index = -1;
		curticks = 0;
		canceled = false;
		synchronized (finished) {
			finished = false;
		}
		play();
	}

	public void cancel() {
		canceled = true;
	}

	public void play(NoteInfo note) {
		note.play(p);
	}

	public boolean canPlay() {
		return p.isValid() && p.isOnline();
	}

	public void play() {
		List<NoteInfo> notes=nc.getNotes();
		if (canceled)
			return;
		if (notes.size() == 0) {
			synchronized (finished) {
				finished = true;
			}
			return;
		}
		if (index >= notes.size()) {
			synchronized (finished) {
				finished = true;
			}
			return;
		}
		if (index > 0) {
			play(nc.getNotes().get(index));
		}
		index++;
		while (index < notes.size() && notes.get(index).ticks == curticks) {
			play(notes.get(index));
			index++;
		}
		if (index >= notes.size()) {
			synchronized (finished) {
				finished = true;
			}
			return;
		}

		long wait = notes.get(index).ticks - curticks;
		curticks = notes.get(index).ticks;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (canPlay()) {
					play();
				}
			}
		}.runTaskLater(MCMidi.plugin, wait);
	}
}