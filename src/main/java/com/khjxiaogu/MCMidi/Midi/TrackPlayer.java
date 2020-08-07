package com.khjxiaogu.MCMidi.Midi;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.khjxiaogu.MCMidi.MCMidi;

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
		if (canceled)
			return;
		if (nc.notes.size() == 0) {
			synchronized (finished) {
				finished = true;
			}
			return;
		}
		if (index >= nc.notes.size()) {
			synchronized (finished) {
				finished = true;
			}
			return;
		}
		if (index > 0) {
			play(nc.notes.get(index));
		}
		index++;
		while (index < nc.notes.size() && nc.notes.get(index).ticks == curticks) {
			play(nc.notes.get(index));
			index++;
		}
		if (index >= nc.notes.size()) {
			synchronized (finished) {
				finished = true;
			}
			return;
		}

		long wait = nc.notes.get(index).ticks - curticks;
		curticks = nc.notes.get(index).ticks;
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