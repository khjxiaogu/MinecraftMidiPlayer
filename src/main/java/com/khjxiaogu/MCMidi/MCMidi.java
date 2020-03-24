package com.khjxiaogu.MCMidi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.midi.InvalidMidiDataException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.khjxiaogu.MCMidi.Midi.NoteInfo;
import com.khjxiaogu.MCMidi.Midi.NotePlayers;
import com.khjxiaogu.MCMidi.Midi.NoteTrack;

public class MCMidi extends JavaPlugin {
	public static MCMidi plugin;
	public Map<String, MidiSheet> loaded = new ConcurrentHashMap<>();
	Map<Player, NotePlayers> nps = new ConcurrentHashMap<>();
	FileConfiguration midifile = new YamlConfiguration();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(Messages.getString("MCMidi.help0")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help1")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help2")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help3")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help4")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help5")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help6")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help7")); //$NON-NLS-1$
			return true;
		}
		if (args.length >= 2) {
			if (args[0].equals("load")) { //$NON-NLS-1$
				try {
					File f = new File(getDataFolder(), args[1]);
					int off = 0;
					if (args.length >= 3) {
						off = Integer.parseInt(args[2]);
					}
					float factor = 1;
					if (args.length >= 4) {
						factor = Float.parseFloat(args[3]);
					}
					if (f.exists()) {
						MidiSheet mp = new MidiSheet(f, off, factor);
						loaded.put(args[1], mp);
						sender.sendMessage(Messages.getString("MCMidi.midi_loaded") + args[1]); //$NON-NLS-1$
						return true;
					} else {
						sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
						return false;
					}
				} catch (InvalidMidiDataException | IOException e) {
					// TODO Auto-generated catch block
					sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
					e.printStackTrace();
					return false;
				}
			} else if (args[0].equals("play")) { //$NON-NLS-1$
				Player p;
				if (args.length >= 3) {
					p = Bukkit.getPlayer(args[2]);
					if (p == null) {
						sender.sendMessage(Messages.getString("MCMidi.player_not_exist")); //$NON-NLS-1$
						return true;
					}
				} else {
					if (sender instanceof Player) {
						p = (Player) sender;
					} else {
						sender.sendMessage(Messages.getString("MCMidi.must_be_player"));//$NON-NLS-1$
						return false;
					}
				}
				NotePlayers np = nps.get(p);
				if (np != null) {
					np.cancel();
				}
				MidiSheet mp = loaded.get(args[1]);
				if (mp == null) {
					sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
					return false;
				}
				nps.put(p, mp.playFor(p));
				sender.sendMessage(Messages.getString("MCMidi.play_start")); //$NON-NLS-1$
				p.sendMessage(Messages.getString("MCMidi.play_name_start") + args[1]); //$NON-NLS-1$
				return true;
			} else if (args[0].equals("loop")) { //$NON-NLS-1$
				Player p;
				if (args.length >= 3) {
					p = Bukkit.getPlayer(args[2]);
					if (p == null) {
						sender.sendMessage(Messages.getString("MCMidi.player_not_exist")); //$NON-NLS-1$
						return true;
					}
				} else {
					if (sender instanceof Player) {
						p = (Player) sender;
					} else {
						sender.sendMessage(Messages.getString("MCMidi.must_be_player"));//$NON-NLS-1$
						return false;
					}
				}
				NotePlayers np = nps.get(p);
				if (np != null) {
					np.cancel();
				}
				MidiSheet mp = loaded.get(args[1]);
				if (mp == null) {
					sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
					return false;
				}
				nps.put(p, mp.playFor(p, true));
				sender.sendMessage(Messages.getString("MCMidi.play_start")); //$NON-NLS-1$
				p.sendMessage(Messages.getString("MCMidi.play_name_start") + args[1]); //$NON-NLS-1$
				return true;
			} else if (args[0].equals("info")) { //$NON-NLS-1$
				MidiSheet mp = loaded.get(args[1]);
				if (mp == null) {
					sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
					return false;
				}
				sender.sendMessage(mp.getInfo());
				return true;
			}
		}
		if (args.length >= 1) {
			if (args[0].equals("stop")) { //$NON-NLS-1$
				Player p;
				if (args.length >= 2) {
					p = Bukkit.getPlayer(args[1]);
					if (p == null) {
						sender.sendMessage(Messages.getString("MCMidi.player_not_exist")); //$NON-NLS-1$
						return true;
					}
				} else {
					if (sender instanceof Player) {
						p = (Player) sender;
					} else {
						sender.sendMessage(Messages.getString("MCMidi.must_be_player"));//$NON-NLS-1$
						return false;
					}
				}
				NotePlayers np = nps.get(p);
				if (np != null) {
					np.cancel();
				}
				return true;
			} else if (args[0].equals("list")) { //$NON-NLS-1$
				sender.sendMessage(Messages.getString("MCMidi.list_of_file"));//$NON-NLS-1$
				loaded.keySet().forEach((s) -> {
					sender.sendMessage(s);
				});
				return true;
			}
		}
		return false;
	}

	public void filterList(String input, List<String> list) {
		list.removeIf((s) -> {
			return !s.startsWith(input);
		});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			list.add("load");
			list.add("play");
			list.add("loop");
			list.add("stop");
			list.add("info");
			list.add("list");
			filterList(args[0], list);
		} else if (args.length == 2) {
			if (args[0].equals("load")) {
				list.addAll(Arrays.asList(getDataFolder().list((d, n) -> {
					return !n.endsWith(".cfg");
				})));
			} else if (args[0].equals("play") || args[0].equals("loop")) {
				list.addAll(loaded.keySet());
			} else if (args[0].equals("stop"))
				return null;
			else if (args[0].equals("info")) {
				list.addAll(loaded.keySet());
			}
			filterList(args[1], list);
		} else if (args.length == 3) {
			if (args[0].equals("play") || args[0].equals("loop"))
				return null;
			else if (args[0].equals("load")) {
				list.add("0");
			}
			filterList(args[2], list);
		} else if (args.length == 4) {
			if (args[0].equals("load")) {
				list.add("1");
			}
			filterList(args[3], list);
		}

		return list;
	}

	@Override
	public void onLoad() {
		saveDefaultConfig();

		ConfigurationSerialization.registerClass(NoteInfo.class);
		ConfigurationSerialization.registerClass(NoteTrack.class);
		ConfigurationSerialization.registerClass(MidiSheet.class);
	}

	@Override
	public void onEnable() {
		MCMidi.plugin = this;
		NoteInfo.initNotes();
		File cfg = new File(MCMidi.plugin.getDataFolder(), "data.yml");
		if (cfg.exists()) {
			try {
				midifile.load(cfg);
				ConfigurationSection cs = midifile.getConfigurationSection("midi");//$NON-NLS-1$
				if (cs != null) {
					for (String s : cs.getKeys(false)) {
						try {
							ConfigurationSection cur = cs.getConfigurationSection(s);
							loaded.put((String) cur.get("name"), (MidiSheet) cur.get("midi"));
						} catch (Throwable t) {
							getLogger().info("midi " + s + " load failure");//$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			} catch (IOException | InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDisable() {
		MCMidi.plugin = this;
		ConfigurationSection cs = midifile.createSection("midi");//$NON-NLS-1$
		int i = 0;
		loaded.forEach((n, m) -> {
			ConfigurationSection cur = cs.createSection(Integer.toString(i));
			cur.set("name", n);
			cur.set("midi", m);
		});
		try {
			midifile.save(new File(MCMidi.plugin.getDataFolder(), "data.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
