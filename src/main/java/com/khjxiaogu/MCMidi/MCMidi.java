package com.khjxiaogu.MCMidi;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.midi.InvalidMidiDataException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MCMidi extends JavaPlugin {
	public static MCMidi plugin;
	public Map<String,MidiPlayer> loaded=new ConcurrentHashMap<>();
	Map<Player,NotePlayers> nps=new ConcurrentHashMap<>();
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length==0) {
			sender.sendMessage(Messages.getString("MCMidi.help0")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help1")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help2")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help3")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help4")); //$NON-NLS-1$
			return true;
		}
		if(args.length>=3) {
			if(args[0].equals("load")) { //$NON-NLS-1$
				try {
					File f=new File(getDataFolder(),args[1]);
					int off=0;
					if(args.length>=4)
						off=Integer.parseInt(args[3]);
					if(f.exists()) {
					MidiPlayer mp=new MidiPlayer(f,off);
					loaded.put(args[2],mp);
					sender.sendMessage(Messages.getString("MCMidi.midi_loaded")); //$NON-NLS-1$
					return true;
					}else {
						sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
						return false;
					}
				} catch (InvalidMidiDataException | IOException e) {
					// TODO Auto-generated catch block
					sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
					e.printStackTrace();
					return false;
				}
			}else if(args[0].equals("play")) { //$NON-NLS-1$
				Player p=Bukkit.getPlayer(args[2]);
				if(p==null) {
					sender.sendMessage(Messages.getString("MCMidi.player_not_exist")); //$NON-NLS-1$
					return true;
				}
				NotePlayers np=nps.get(p);
				if(np!=null)
					np.cancel();
				nps.put(p,loaded.get(args[1]).playFor(p));
				sender.sendMessage(Messages.getString("MCMidi.play_start")); //$NON-NLS-1$
				p.sendMessage(Messages.getString(Messages.getString("MCMidi.play_name_start"))+args[1]);  //$NON-NLS-1$
				return true;
			}
		}else if(args.length==2) {
			if(args[0].equals("stop")) { //$NON-NLS-1$
				Player p=Bukkit.getPlayer(args[1]);
				if(p==null) {
					sender.sendMessage(Messages.getString("MCMidi.player_not_exist")); //$NON-NLS-1$
					return true;
				}
				NotePlayers np=nps.get(p);
				if(np!=null)
					np.cancel();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onEnable() {
		plugin=this;
		this.saveDefaultConfig();//save a dummy config to create plugin folder
	}
}
