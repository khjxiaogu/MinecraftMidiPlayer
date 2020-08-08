package com.khjxiaogu.MCMidi.Midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.NoteBlock;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.material.Diode;
import org.bukkit.util.Vector;

import com.khjxiaogu.MCMidi.Messages;
import com.khjxiaogu.MCMidi.Midi.Players.NoteBlockPlayer;
import com.khjxiaogu.MCMidi.Midi.Players.TrackPlayer;

// TODO: Auto-generated Javadoc
/**
 * Class NoteTrack.
 *
 * @author khjxiaogu
 * file: NoteTrack.java
 * time: 2020年8月9日
 */
public class NoteTrack implements ConfigurationSerializable {
	
	/** The notes.<br> 成员 notes. */
	public final List<NoteInfo> notes = new ArrayList<>();

	/**
	 * Gets the notes.<br>
	 * 获取 notes.
	 *
	 * @return notes<br>
	 */
	public List<NoteInfo> getNotes() {
		return notes;
	}

	/**
	 * Instantiates a new NoteTrack.<br>
	 * 新建一个NoteTrack类<br>
	 */
	public NoteTrack() {
	}

	/**
	 * Instantiates a new NoteTrack with a Map object.<br>
	 * 使用一个Map新建一个NoteTrack类<br>
	 *
	 * @param map the map<br>
	 */
	@SuppressWarnings("unchecked")
	public NoteTrack(Map<String, Object> map) {
		for (Map<String, Object> ms : (List<Map<String, Object>>) map.get("notes")) {
			notes.add(NoteInfo.valueOf(ms));
		}
	}

	/**
	 * Adds the.<br>
	 *
	 * @param key the key<br>
	 * @param tick the tick<br>
	 * @param vol the vol<br>
	 */
	public void add(int key, long tick, int vol) {
		NoteInfo ni = NoteInfo.getNote(key, tick, vol);
		if (ni != null) {
			notes.add(ni);
		}
	}

	/**
	 * Adds the all.<br>
	 *
	 * @param ref the ref<br>
	 */
	public void addAll(NoteTrack ref) {
		notes.addAll(ref.notes);
	}

	/**
	 * Play all.<br>
	 *
	 * @param p the p<br>
	 * @return return play all <br>返回 track player
	 */
	public TrackPlayer playAll(Player p) {
		TrackPlayer ret = new TrackPlayer(p, this);
		ret.play();
		return ret;
	}

	/**
	 * Play all.<br>
	 *
	 * @param nb the nb<br>
	 * @return return play all <br>返回 note block player
	 */
	public NoteBlockPlayer playAll(NoteBlock nb) {
		NoteBlockPlayer ret = new NoteBlockPlayer(nb, this);
		ret.play();
		return ret;
	}

	/**
	 * Gets the size.<br>
	 * 获取 size.
	 *
	 * @return size<br>
	 */
	public int getSize() {
		return notes.size();
	}

	/**
	 * Sort.<br>
	 */
	public void sort() {
		notes.sort((c1, c2) -> {
			return (int) (c1.ticks - c2.ticks);
		});

	}

	/**
	 * Gets the info.<br>
	 * 获取 info.
	 *
	 * @return info<br>
	 */
	public String getInfo() {
		if (notes.size() > 0)
			return Messages.getString("MCMidi.track_note_count") + notes.size() //$NON-NLS-1$
					+ Messages.getString("MCMidi.track_length") + notes.get(notes.size() - 1).ticks + "ticks"; //$NON-NLS-1$
		else
			return Messages.getString("MCMidi.track_note_count") + 0 + Messages.getString("MCMidi.track_length") + 0; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Place block.<br>
	 *
	 * @param start the start<br>
	 * @param direction the direction<br>
	 * @param lineWidth the line width<br>
	 * @param base the base<br>
	 */
	public void placeBlock(Location start, final Vector direction, final int lineWidth, Material base) {
		if (notes.size() == 0) {
			return;
		}
		int turned = 0;
		int clw = lineWidth - 1;
		int currentWidth = 1;
		BlockFace face = BlockFace.SELF;
		Vector forward = direction.clone();
		if (forward.getBlockZ() > 0)
			face = BlockFace.SOUTH;
		else if (forward.getBlockZ() < 0)
			face = BlockFace.NORTH;
		if (forward.getBlockX() > 0)
			face = BlockFace.EAST;
		else if (forward.getBlockX() < 0)
			face = BlockFace.WEST;
		forward.setY(0);
		World world = start.getWorld();
		Vector side = new Vector(direction.getBlockZ(), 0, direction.getBlockX());
		Location direct = start.clone();
		long curticks = 0;
		for (int i = 0; i < notes.size(); i++) {
			if (direct.getBlockY() < start.getBlockY() || direct.getBlockY() > 255)
				break;// prevent override
			NoteInfo ni = notes.get(i);
			long deltaTicks = Math.round(ni.ticks / 2) - curticks;
			while (deltaTicks > 0) {
				if (currentWidth < clw) {
					world.getBlockAt(direct.getBlockX(), direct.getBlockY() - 1, direct.getBlockZ()).setType(base);
					Block b = world.getBlockAt(direct);
					b.setType(Material.DIODE_BLOCK_OFF);

					Diode diode = (Diode) b.getState().getData();
					diode.setFacingDirection(face);
					if (deltaTicks >= 4) {
						diode.setDelay(4);
						deltaTicks -= 4;
					} else {
						diode.setDelay((int) deltaTicks);
						deltaTicks = 0;
					}
					b.setData(diode.getData());
					direct.add(forward);
					currentWidth++;
				}
				if (currentWidth >= clw) {
					turned++;
					currentWidth = 0;
					Vector nforward = side.multiply(-1);
					side = forward;
					forward = nforward;
					if (forward.getBlockZ() > 0)
						face = BlockFace.SOUTH;
					else if (forward.getBlockZ() < 0)
						face = BlockFace.NORTH;
					if (forward.getBlockX() > 0)
						face = BlockFace.EAST;
					else if (forward.getBlockX() < 0)
						face = BlockFace.WEST;
					clw = lineWidth - 1;
					if (turned == 3) {
						clw = lineWidth - 4;
					} else if (turned == 4) {// create a ladder
						if (direct.getBlockY() >= 252)
							return;
						world.getBlockAt(direct).setType(base);
						direct.add(0, 1, 0);
						world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
						direct.add(side);
						world.getBlockAt(direct).setType(base);
						direct.add(0, 1, 0);
						world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
						direct.add(side);
						world.getBlockAt(direct).setType(base);
						direct.add(0, 1, 0);
						world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
						direct.add(side);
						world.getBlockAt(direct).setType(base);
						direct.add(0, 1, 0);
						world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
						turned = 0;
					}
					if (deltaTicks != 0) {// compress (place redstone only when needed)
						world.getBlockAt(direct.getBlockX(), direct.getBlockY() - 1, direct.getBlockZ()).setType(base);
						world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
						direct.add(forward);
					}
					currentWidth++;
				}
			}
			curticks = Math.round(ni.ticks / 2);
			Location current = direct.clone().add(forward).add(side).subtract(0, 1, 0);
			Location redstoneline = direct.clone();
			while (i < notes.size() && Math.round(notes.get(i).ticks / 2) == curticks) {
				notes.get(i).placeBlock(current, base);
				redstoneline.add(side);
				world.getBlockAt(redstoneline.getBlockX(), redstoneline.getBlockY() - 1, redstoneline.getBlockZ())
						.setType(base);
				world.getBlockAt(redstoneline).setType(Material.REDSTONE_WIRE);
				current.add(side);
				i++;
			}
			world.getBlockAt(direct.getBlockX(), direct.getBlockY() - 1, direct.getBlockZ()).setType(base);
			world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
			direct.add(forward);
			currentWidth++;
			if ((currentWidth < clw - 1) && (notes.size() <= i || Math.round(notes.get(i).ticks / 2) - curticks <= 4)) {// compress
																														// (place
																														// redstone
																														// only
																														// when
																														// needed)
				world.getBlockAt(direct.getBlockX(), direct.getBlockY() - 1, direct.getBlockZ()).setType(base);
				world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
				direct.add(forward);
				currentWidth++;
			}
			// world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
			// direct.add(forward);
			i--;
		}
	}

	/**
	 * Serialize.<br>
	 *
	 * @return return serialize <br>返回 map
	 */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		List<Map<String, Object>> lso = new ArrayList<>(notes.size());
		if (notes.size() > 0) {
			for (int i = 0; i < notes.size(); i++) {
				lso.add(notes.get(i).serialize());
			}
			map.put("notes", lso); //$NON-NLS-1$
		}
		return map;
	}
}
