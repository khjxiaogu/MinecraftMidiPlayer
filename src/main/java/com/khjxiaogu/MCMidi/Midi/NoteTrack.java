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

public class NoteTrack implements ConfigurationSerializable {
	List<NoteInfo> notes = new ArrayList<>();

	public NoteTrack() {
	}

	@SuppressWarnings("unchecked")
	public NoteTrack(Map<String, Object> map) {
		for (Map<?, ?> ms : (List<Map<String, Object>>) map.get("notes")) {
			notes.add(new NoteInfo(ms));
		}
	}

	public void add(int key, long tick, int vol) {
		NoteInfo ni = NoteInfo.getNote(key, tick, vol);
		if (ni != null) {
			notes.add(ni);
		}
	}
	public void addAll(NoteTrack ref) {
		notes.addAll(ref.notes);
	}
	public TrackPlayer playAll(Player p) {
		TrackPlayer ret = new TrackPlayer(p, this);
		ret.play();
		return ret;
	}
	public NoteBlockPlayer playAll(NoteBlock nb) {
		NoteBlockPlayer ret = new NoteBlockPlayer(nb, this);
		ret.play();
		return ret;
	}
	public int getSize() {
		return notes.size();
	}
	public void sort() {
		notes.sort((c1,c2)->{return (int) (c1.ticks-c2.ticks);});
		
	}
	public String getInfo() {
		if (notes.size() > 0)
			return Messages.getString("MCMidi.track_note_count") + notes.size() //$NON-NLS-1$
					+ Messages.getString("MCMidi.track_length") + notes.get(notes.size() - 1).ticks + "ticks"; //$NON-NLS-1$
		else
			return Messages.getString("MCMidi.track_note_count") + 0 + Messages.getString("MCMidi.track_length") + 0; //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void placeBlock(Location start,final Vector direction,final int lineWidth) {
		if (notes.size() == 0) {
			return;
		}
		int turned=0;
		int clw=lineWidth-1;
		int currentWidth=1;
		BlockFace face=BlockFace.SELF;
		Vector forward=direction.clone();
		if(forward.getBlockZ()>0) 
			face=BlockFace.SOUTH;
		else if(forward.getBlockZ()<0) 
			face=BlockFace.NORTH;
		if(forward.getBlockX()>0) 
			face=BlockFace.EAST;
		else if(forward.getBlockX()<0) 
			face=BlockFace.WEST;
		forward.setY(0);
		World world=start.getWorld();
		Vector side=new Vector(direction.getBlockZ(),0,direction.getBlockX());
		Location direct=start.clone();
		long curticks=0;
		for(int i=0;i<notes.size();i++) {
			if(direct.getBlockY()<start.getBlockY()||direct.getBlockY()>255)break;//prevent override
			NoteInfo ni=notes.get(i);
			long deltaTicks=Math.round(ni.ticks/2)-curticks;
			while(deltaTicks>0) {
				if(currentWidth<clw) {
					world.getBlockAt(direct.getBlockX(),direct.getBlockY()-1,direct.getBlockZ()).setType(Material.STONE);
					Block b=world.getBlockAt(direct);
					b.setType(Material.DIODE_BLOCK_OFF);
		
					Diode diode=(Diode) b.getState().getData();
					diode.setFacingDirection(face);
					if(deltaTicks>=4) {
						diode.setDelay(4);
						deltaTicks-=4;
					}else {
						diode.setDelay((int) deltaTicks);
						deltaTicks=0;
					}
					b.setData(diode.getData());
					direct.add(forward);
					currentWidth++;
				}
				if(currentWidth>=clw) {
					turned++;
					currentWidth=0;
					Vector nforward=side.multiply(-1);
					side=forward;
					forward=nforward;
					if(forward.getBlockZ()>0) 
						face=BlockFace.SOUTH;
					else if(forward.getBlockZ()<0) 
						face=BlockFace.NORTH;
					if(forward.getBlockX()>0) 
						face=BlockFace.EAST;
					else if(forward.getBlockX()<0) 
						face=BlockFace.WEST;
					clw=lineWidth-1;
					if(turned==3) {
						clw=lineWidth-4;
					}else if(turned==4) {//create a ladder
						if(direct.getBlockY()>=252)return;
						world.getBlockAt(direct).setType(Material.STONE);
						direct.add(0, 1, 0);
						world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
						direct.add(side);
						world.getBlockAt(direct).setType(Material.STONE);
						direct.add(0, 1, 0);
						world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
						direct.add(side);
						world.getBlockAt(direct).setType(Material.STONE);
						direct.add(0, 1, 0);
						world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
						direct.add(side);
						world.getBlockAt(direct).setType(Material.STONE);
						direct.add(0, 1, 0);
						world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
						turned=0;
					}
					if(deltaTicks!=0) {// compress (place redstone only when needed)
					world.getBlockAt(direct.getBlockX(),direct.getBlockY()-1,direct.getBlockZ()).setType(Material.STONE);
					world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
					direct.add(forward);
					}
					currentWidth++;
				}
			}
			curticks=Math.round(ni.ticks/2);
			Location current=direct.clone().add(forward).add(side).subtract(0, 1, 0);
			Location redstoneline=direct.clone();
			while (i<notes.size() && Math.round(notes.get(i).ticks/2) == curticks) {
				notes.get(i).placeBlock(current);
				redstoneline.add(side);
				world.getBlockAt(redstoneline.getBlockX(),redstoneline.getBlockY()-1,redstoneline.getBlockZ()).setType(Material.STONE);
				world.getBlockAt(redstoneline).setType(Material.REDSTONE_WIRE);
				current.add(side);
				i++;
			}
			world.getBlockAt(direct.getBlockX(),direct.getBlockY()-1,direct.getBlockZ()).setType(Material.STONE);
			world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
			direct.add(forward);
			currentWidth++;
			if((currentWidth<clw-1)&&(notes.size()<=i||Math.round(notes.get(i).ticks/2)-curticks<=4)) {// compress (place redstone only when needed)
				world.getBlockAt(direct.getBlockX(),direct.getBlockY()-1,direct.getBlockZ()).setType(Material.STONE);
				world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
				direct.add(forward);
				currentWidth++;
			}
			//world.getBlockAt(direct).setType(Material.REDSTONE_WIRE);
			//direct.add(forward);
			i--;
		}
	}
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
