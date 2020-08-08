package com.khjxiaogu.MCMidi.api;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.khjxiaogu.MCMidi.MCMidi;
import com.khjxiaogu.MCMidi.Midi.MidiSheet;
import com.khjxiaogu.MCMidi.Midi.Players.NotePlayers;

public class MidiAPI {
	public static boolean generateStucture(String name, Location l, int width, Material baseBlock) {
		MidiSheet mp = MCMidi.plugin.loaded.get(name);
		if (mp == null) {
			return false;
		}
		Vector v = new Vector(Math.round(l.getDirection().getX()), 0, Math.round(l.getDirection().getZ()));
		if (v.getBlockX() != 0 && v.getBlockZ() != 0) {
			v.setZ(0);
		}
		if (v.getBlockX() == 0 && v.getBlockZ() == 0) {
			v.setX(1);
		}
		mp.placeBlock(l, v, width, baseBlock);
		return true;
	}

	public static boolean playFor(String name, Player p, boolean loop) {
		NotePlayers np = MCMidi.plugin.nps.get(p);
		if (np != null) {
			np.cancel();
		}
		MidiSheet mp = MCMidi.plugin.loaded.get(name);
		if (mp == null) {
			return false;
		}
		MCMidi.plugin.nps.put(p, mp.playFor(p, loop));
		return true;
	}

	public static void load(String name, File f, int octaveOffset, float factor)
			throws InvalidMidiDataException, IOException {
		MidiSheet mp = new MidiSheet(f, octaveOffset, factor);
		MCMidi.plugin.loaded.put(name, mp);
	}

	public static void stop(Player p) {
		NotePlayers np = MCMidi.plugin.nps.get(p);
		if (np != null) {
			np.cancel();
		}
	}
}
