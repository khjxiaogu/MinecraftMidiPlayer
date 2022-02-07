/**
 * Minecraft midi player
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.MCMidi.Midi.Players;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import com.khjxiaogu.MCMidi.MCMidi;
import com.khjxiaogu.MCMidi.Midi.MidiSheet;
import com.khjxiaogu.MCMidi.Midi.NoteTrack;

public class NoteBlockPlayers {
	List<TrackPlayer> players = new ArrayList<>();
	BukkitRunnable loopDetect = null;
	MidiSheet orig;
	public NoteBlockPlayers(Block p, MidiSheet mp, boolean loop) {
		this.orig=mp;
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
	public boolean isLoop() {
		return loopDetect!=null;
	}
	public MidiSheet getOrig() {
		return orig;
	}

	public NoteBlockPlayers(Block p, MidiSheet mp) {
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
