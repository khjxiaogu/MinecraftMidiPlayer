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
