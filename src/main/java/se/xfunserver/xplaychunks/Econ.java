package se.xfunserver.xplaychunks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public final class Econ {

    private Economy economy;
    private xPlayChunks chunksCore;

    boolean setupEconomy(xPlayChunks chunksCore) {
        this.chunksCore = chunksCore;

        // Check if vault is present
        if (chunksCore.getServer().getPluginManager().getPlugin("Vault") == null) return false;

        // Get the vault service if it is present
        RegisteredServiceProvider<Economy> rsp =
                chunksCore.getServer().getServicesManager().getRegistration(Economy.class);

        // Check if the service is valid
        if (rsp == null) return false;

        // Update current economy handler
        economy = rsp.getProvider();

        // Success
        return true;
    }

    public double getMoney(UUID player) {
        Player ply = this.getPlayer(player);

        // If the player has joined the server before, return their balance,
        if (ply != null)
            return economy.getBalance(ply);

        return -1.0D;
    }

    @SuppressWarnings("UnusedReturnValue")
     public EconomyResponse addMoney(UUID player, double amount) {
        Player ply = this.getPlayer(player);
        if (ply != null) {
            // Remove the money from the player's balance.
            return economy.depositPlayer(ply, Math.abs(amount));
        }
        return null;
    }


    @SuppressWarnings("UnusedReturnValue")
    public EconomyResponse takeMoney(UUID player, double amount) {
        Player ply = this.getPlayer(player);
        if (ply != null) {
            // Remove the money from the player's balance.
            return economy.withdrawPlayer(ply, Math.abs(amount));
        }
        return null;
    }

    /**
     * Take money from the player.
     *
     * @param player Player purchasing.
     * @param cost The cost of the purchase.
     * @return Whether the transaction was successful.
     */
    public boolean buy(UUID player, double cost) {
        if (this.getMoney(player) >= cost) {
            EconomyResponse response = this.takeMoney(player, cost);
            // Return whether the transaction was completed successfully.
            return response != null && response.type == EconomyResponse.ResponseType.SUCCESS;
        }

        return false;
    }

    public String format(double amount) {
        return economy.format(amount);
    }

    private Player getPlayer(UUID uuid) {
        if (chunksCore == null)
            return null;

        return chunksCore.getServer().getPlayer(uuid);
    }
}
