package su.nightexpress.excellentcrates.menu;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.Placeholders;
import su.nightexpress.excellentcrates.config.Config;
import su.nightexpress.excellentcrates.menu.impl.CratesMenu;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;

import java.io.File;
import java.util.*;

@Deprecated
public class MenuManager extends AbstractManager<CratesPlugin> {

    private final Map<String, CratesMenu> menuMap;

    public MenuManager(@NotNull CratesPlugin plugin) {
        super(plugin);
        this.menuMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        this.loadMenus();
    }

    @Override
    protected void onShutdown() {
        this.getMenus().forEach(CratesMenu::clear);
        this.menuMap.clear();
    }

    public void loadMenus() {
        File dirMenus = new File(plugin.getDataFolder().getAbsolutePath(), Config.DIR_MENUS);
        if (!dirMenus.exists() && dirMenus.mkdirs()) {
            new CratesMenu(this.plugin, FileConfig.loadOrExtract(this.plugin, Config.DIR_MENUS, Placeholders.DEFAULT + ".yml"));
        }

        for (FileConfig config : FileConfig.loadAll(plugin.getDataFolder() + Config.DIR_MENUS, false)) {
            CratesMenu menu = new CratesMenu(plugin, config);
            String id = FileConfig.getName(config.getFile());

            this.menuMap.put(id, menu);
        }

        this.plugin.info("Loaded " + this.menuMap.size() + " crate menus.");
    }

    @NotNull
    public Map<String, CratesMenu> getMenuMap() {
        return this.menuMap;
    }

    @Nullable
    public CratesMenu getMenuById(@NotNull String id) {
        return this.menuMap.get(id.toLowerCase());
    }

    @NotNull
    public Collection<CratesMenu> getMenus() {
        return this.menuMap.values();
    }

    @NotNull
    public List<String> getMenuIds() {
        return new ArrayList<>(this.menuMap.keySet());
    }
}
