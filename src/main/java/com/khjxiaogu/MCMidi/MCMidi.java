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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.khjxiaogu.MCMidi.Midi.MidiSheet;
import com.khjxiaogu.MCMidi.Midi.NoteInfo;
import com.khjxiaogu.MCMidi.Midi.NoteTrack;
import com.khjxiaogu.MCMidi.Midi.Players.NoteBlockPlayers;
import com.khjxiaogu.MCMidi.Midi.Players.NotePlayers;
import com.khjxiaogu.MCMidi.api.MidiAPI;

public class MCMidi extends JavaPlugin {
	public static MCMidi plugin;
	public Map<String, MidiSheet> loaded = new ConcurrentHashMap<>();
	public Map<Player, NotePlayers> nps = new ConcurrentHashMap<>();
	public Map<Location, NoteBlockPlayers> nbs = new ConcurrentHashMap<>();
	FileConfiguration midifile = new YamlConfiguration();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(Messages.getString("MCMidi.help0")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help1")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help1_1")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help1_2")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help1_3")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help1_4")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help2")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help2_1")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help3")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help4")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help5")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help6")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help7")); //$NON-NLS-1$
			sender.sendMessage(Messages.getString("MCMidi.help7_1"));
			sender.sendMessage(Messages.getString("MCMidi.help7_2"));
			sender.sendMessage(Messages.getString("MCMidi.help8"));
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
			} else if (args[0].equals("combine")) { //$NON-NLS-1$
				MidiSheet mp = loaded.get(args[1]);
				if (mp == null) {
					sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
					return false;
				}
				if (mp.Combine())
					sender.sendMessage(Messages.getString("MCMidi.combined"));
				else
					sender.sendMessage(Messages.getString("MCMidi.already_combined"));
				return true;
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
			} else if (args[0].equals("loopblock")) { //$NON-NLS-1$
				World w;
				if (sender instanceof Player) {
					w = ((Player) sender).getWorld();
				} else if (sender instanceof BlockCommandSender) {
					w = ((BlockCommandSender) sender).getBlock().getWorld();

				} else {
					sender.sendMessage(Messages.getString("MCMidi.must_be_player"));//$NON-NLS-1$
					return false;
				}
				Location l = new Location(w, Integer.parseInt(args[2]), Integer.parseInt(args[3]),
						Integer.parseInt(args[4]));
				NoteBlockPlayers np = nbs.get(l);
				if (np != null) {
					np.cancel();
				}
				MidiSheet mp = loaded.get(args[1]);
				if (mp == null) {
					sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
					return false;
				}
				Block ob = l.getBlock();
				if (ob == null || ob.getType() != Material.NOTE_BLOCK)
					return false;
				nbs.put(l, mp.playBlock((NoteBlock) ob.getState(), true));
				sender.sendMessage(Messages.getString("MCMidi.play_start")); //$NON-NLS-1$
				return true;
			} else if (args[0].equals("playblock")) { //$NON-NLS-1$
				World w;
				if (sender instanceof Player) {
					w = ((Player) sender).getWorld();
				} else if (sender instanceof BlockCommandSender) {
					w = ((BlockCommandSender) sender).getBlock().getWorld();

				} else {
					sender.sendMessage(Messages.getString("MCMidi.must_be_player"));//$NON-NLS-1$
					return false;
				}
				Location l = new Location(w, Integer.parseInt(args[2]), Integer.parseInt(args[3]),
						Integer.parseInt(args[4]));
				NoteBlockPlayers np = nbs.get(l);
				if (np != null) {
					np.cancel();
				}
				MidiSheet mp = loaded.get(args[1]);
				if (mp == null) {
					sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
					return false;
				}
				Block ob = l.getBlock();
				if (ob == null || ob.getType() != Material.NOTE_BLOCK)
					return false;
				nbs.put(l, mp.playBlock((NoteBlock) ob.getState(), false));
				sender.sendMessage(Messages.getString("MCMidi.play_start")); //$NON-NLS-1$
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
			} else if (args[0].equals("generate")) { //$NON-NLS-1$
				if (!(sender instanceof Player)) {
					sender.sendMessage(Messages.getString("MCMidi.must_be_player"));//$NON-NLS-1$
					return false;
				}
				Player p = (Player) sender;
				int width = 24;
				Material type = Material.STONE;
				if (args.length >= 3)
					type = Material.getMaterial(Integer.valueOf(args[2]));
				if (args.length >= 4)
					width = Integer.parseInt(args[3]);
				if (!MidiAPI.generateStucture(args[1], p.getLocation(), width, type)) {
					sender.sendMessage(Messages.getString("MCMidi.invalid_midi")); //$NON-NLS-1$
					return false;
				}
				sender.sendMessage(Messages.getString("MCMidi.generate_finish"));//$NON-NLS-1$
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
			list.add("load");//$NON-NLS-1$
			list.add("play");//$NON-NLS-1$
			list.add("loop");//$NON-NLS-1$
			list.add("stop");//$NON-NLS-1$
			list.add("info");//$NON-NLS-1$
			list.add("list");//$NON-NLS-1$
			list.add("generate");//$NON-NLS-1$
			filterList(args[0], list);
		} else if (args.length == 2) {
			if (args[0].equals("load")) {//$NON-NLS-1$
				list.addAll(Arrays.asList(getDataFolder().list((d, n) -> {
					return !n.endsWith(".yml");//$NON-NLS-1$
				})));
			} else if (args[0].equals("play") || args[0].equals("loop") || args[0].equals("generate") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					|| args[0].equals("loopblock") || args[0].equals("playblock")) {
				list.addAll(loaded.keySet());
			} else if (args[0].equals("stop"))//$NON-NLS-1$
				return null;
			else if (args[0].equals("info")) {//$NON-NLS-1$
				list.addAll(loaded.keySet());
			}
			filterList(args[1], list);
		} else if (args.length == 3) {
			if (args[0].equals("play") || args[0].equals("loop"))//$NON-NLS-1$ //$NON-NLS-2$
				return null;
			else if (args[0].equals("load")) {//$NON-NLS-1$
				list.add("0"); //$NON-NLS-1$
			}
			filterList(args[2], list);
		} else if (args.length == 4) {
			if (args[0].equals("load")) {//$NON-NLS-1$
				list.add("1"); //$NON-NLS-1$
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
		File cfg = new File(MCMidi.plugin.getDataFolder(), "data.yml");//$NON-NLS-1$
		if (cfg.exists()) {
			try {
				midifile.load(cfg);
				ConfigurationSection cs = midifile.getConfigurationSection("midi");//$NON-NLS-1$
				if (cs != null) {
					for (String s : cs.getKeys(false)) {
						try {
							ConfigurationSection cur = cs.getConfigurationSection(s);
							getLogger().info("loading" + cur.getString("name"));
							loaded.put(cur.getString("name"), (MidiSheet) cur.get("midi"));//$NON-NLS-1$ //$NON-NLS-2$
						} catch (Throwable t) {
							getLogger().info("midi " + s + " load failure");//$NON-NLS-1$ //$NON-NLS-2$
							t.printStackTrace();
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
		for (Map.Entry<String, MidiSheet> entry : loaded.entrySet()) {
			ConfigurationSection cur = cs.createSection(Integer.toString(i));
			cur.set("name", entry.getKey());//$NON-NLS-1$
			cur.set("midi", entry.getValue());//$NON-NLS-1$
			i++;
		}
		try {
			midifile.save(new File(MCMidi.plugin.getDataFolder(), "data.yml"));//$NON-NLS-1$
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
