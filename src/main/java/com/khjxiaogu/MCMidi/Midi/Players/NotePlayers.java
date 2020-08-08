package com.khjxiaogu.MCMidi.Midi.Players;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.khjxiaogu.MCMidi.MCMidi;
import com.khjxiaogu.MCMidi.Midi.MidiSheet;
import com.khjxiaogu.MCMidi.Midi.NoteTrack;

public class NotePlayers {
	List<TrackPlayer> players = new ArrayList<>();
	BukkitRunnable loopDetect = null;

	public NotePlayers(Player p, MidiSheet mp, boolean loop) {
		for (NoteTrack nc : mp.tracks) {
			players.add(nc.playAll(p));
		}
		if (loop) {
			loopDetect = new BukkitRunnable() {
				@Override
				public void run() {
					for (TrackPlayer np : players) {
						if (!np.isFinished())
							return;
					}
					reset();
				}
			};
			loopDetect.runTaskTimerAsynchronously(MCMidi.plugin, 100, 40);
		}
	}

	public NotePlayers(Player p, MidiSheet mp) {
		this(p, mp, false);
	}

	public void reset() {
		for (TrackPlayer np : players) {
			np.reset();
		}
	}

	public void cancel() {
		if (loopDetect != null) {
			loopDetect.cancel();
		}
		for (TrackPlayer np : players) {
			np.cancel();
		}
	}
}