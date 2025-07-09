package com.naufalverse.iclan;

import io.papermc.lib.PaperLib;
import org.bukkit.plugin.java.JavaPlugin;

public class IClanPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    PaperLib.suggestPaper(this);

    saveDefaultConfig();
  }
}
