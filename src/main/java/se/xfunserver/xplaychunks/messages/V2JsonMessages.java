package se.xfunserver.xplaychunks.messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class V2JsonMessages {

    // Global localization
    public String errEnterValidNum = "&cSkriv en giltig siffra.";
    public String noPlayer = "&cDen spelare har inte anslutit sig till servern tidigare.";

    // Command localization
    public String ingameOnly = "Endast in-game spelare kan använda det kommandot.";
    public String consoleOnly = "&cEndast konsolen har tillgång till det kommandot.";
    public String invalidCommand = "&cOgiltigt kommando. Se: &4/chunk hjälp&c.";
    public String commandNoPermission =
            "&cDu har inte tillåtelse att utföra detta kommando!";

    // Claim localization
    public String claimNoPerm = "&cDu har inte tillåtelse att köpa chunks, kontakta en admin.";
    public String claimWorldDisabled = "&cDu kan inte köpa chunks i denna värld.";
    public String claimLocationBlock = "&cDu kan inte köpa chunks här.";
    public String claimAlreadyOwned = "&cDenna chunk har redan en ägare och är inte till salu.";

    /* LOADING */

    private static transient Gson gson;

    public static V2JsonMessages load(File file) throws Exception {
        // Create empty one
        V2JsonMessages messages = new V2JsonMessages();

        // Load from a file if it exists
        if (file.exists()) {
            try {
                messages =
                        getGson()
                                .fromJson(
                                        String.join(
                                                "",
                                                Files.readAllLines(
                                                        file.toPath(), StandardCharsets.UTF_8)),
                                        V2JsonMessages.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getLogger().info("Skapar en ny fil: messages.yml");
        }

        // Write it so new messages are written
        Files.write(
                file.toPath(),
                Collections.singletonList(getGson().toJson(messages)),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE);

        return messages;
    }

    private static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setLenient()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
        }

        return gson;
    }
}
