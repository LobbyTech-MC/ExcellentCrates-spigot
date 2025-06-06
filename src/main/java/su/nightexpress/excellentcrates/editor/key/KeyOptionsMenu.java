package su.nightexpress.excellentcrates.editor.key;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.Placeholders;
import su.nightexpress.excellentcrates.config.EditorLang;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.item.ItemTypes;
import su.nightexpress.excellentcrates.key.CrateKey;
import su.nightexpress.excellentcrates.util.CrateUtils;
import su.nightexpress.nightcore.ui.UIUtils;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.confirmation.Confirmation;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public class KeyOptionsMenu extends LinkedMenu<CratesPlugin, CrateKey> {

    private static final String SKULL_STACK = "e2e7ac70bf77ba3dd33f4cb78d88ac149ac6036cef2eac8e7a6fd3676fbaf1aa";

    public KeyOptionsMenu(@NotNull CratesPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X5, Lang.EDITOR_TITLE_KEY_LIST.getString());

        this.addItem(MenuItem.buildReturn(this, 40, (viewer, event) -> {
            this.runNextTick(() -> plugin.getEditorManager().openKeyList(viewer.getPlayer()));
        }));

        this.addItem(ItemUtil.getCustomHead(Placeholders.SKULL_DELETE), EditorLang.KEY_EDIT_DELETE, 8, (viewer, event, key) -> {
            Player player = viewer.getPlayer();

            UIUtils.openConfirmation(player, Confirmation.builder()
                .onAccept((viewer1, event1) -> {
                    plugin.getKeyManager().delete(key);
                    plugin.runTask(task -> plugin.getEditorManager().openKeyList(player));
                })
                .onReturn((viewer1, event1) -> {
                    plugin.runTask(task -> plugin.getEditorManager().openKeyOptions(player, key));
                })
                .build());
        });

        this.addItem(Material.NAME_TAG, EditorLang.KEY_EDIT_NAME, 19, (viewer, event, key) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_ENTER_DISPLAY_NAME, input -> {
                key.setName(input.getText());
                key.save();
                return true;
            }));
        });

        this.addItem(Material.TRIPWIRE_HOOK, EditorLang.KEY_EDIT_ITEM, 21, (viewer, event, key) -> {
            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) {
                if (event.isLeftClick()) {
                    Players.addItem(viewer.getPlayer(), key.getItem());
                }
                else if (event.isRightClick()) {
                    Players.addItem(viewer.getPlayer(), key.getRawItem());
                }
                return;
            }

            event.getView().setCursor(null);

            // Remove key tags to avoid infinite recursion in ItemProvider.
            ItemStack clean = CrateUtils.removeCrateTags(new ItemStack(cursor));

            if (!ItemTypes.isCustom(clean)) {
                key.setProvider(ItemTypes.vanilla(clean));
                key.setName(ItemUtil.getNameSerialized(clean));
                this.saveAndFlush(viewer);
            }
            else {
                this.runNextTick(() -> plugin.getEditorManager().openItemTypeMenu(viewer.getPlayer(), clean, provider -> {
                    key.setProvider(provider);
                    key.setName(ItemUtil.getNameSerialized(clean));
                    key.save();
                    this.runNextTick(() -> this.open(viewer.getPlayer(), key));
                }));
            }

            event.getView().setCursor(null);

        }, ItemOptions.builder().setVisibilityPolicy(viewer -> !this.getLink(viewer).isVirtual()).build());

        this.addItem(NightItem.asCustomHead(SKULL_STACK), Lang.EDITOR_BUTTON_KEY_ITEM_STACKABLE, 23, (viewer, event, key) -> {
            key.setItemStackable(!key.isItemStackable());
            this.saveAndFlush(viewer);
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> !this.getLink(viewer).isVirtual()).build());

        this.addItem(Material.ENDER_PEARL, EditorLang.KEY_EDIT_VIRTUAL, 25, (viewer, event, key) -> {
            key.setVirtual(!key.isVirtual());
            this.saveAndFlush(viewer);
        });
    }

    private void saveAndFlush(@NotNull MenuViewer viewer) {
        this.getLink(viewer).save();
        this.runNextTick(() -> this.flush(viewer));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        item.replacement(replacer -> replacer.replace(this.getLink(viewer).replacePlaceholders()));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {

    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);
        if (result.isInventory()) {
            event.setCancelled(false);
        }
    }
}
