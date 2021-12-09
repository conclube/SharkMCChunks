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
    public String claimTooMany = "&cDu har nått gränsen för maximalt antal ägda chunks";
    public String claimSuccess = "&7Du köpte chunken &a%chunkid% &7för &a%price% PlayMynt";
    public String claimNoCost = "Gratis";
    public String claimFreeOne = "Du köpte chunken &a%chunkid% &7gratis. &7Notera att det är bara din första chunk som är gratis, chunks kostar i vanliga fall &a%price% PlayMynt";
    public String claimNotEnoughMoney = "Du har inte tillräckligt med pengar för att köpa denna chunk. &7Chunks kostar %price% PlayMynt";

    // Unclaim localization
    public String unclaimNoPerm = "&cDu har inte tillåtelse att unclaima chunks.";
    public String unclaimNotOwned = "&cDenna chunk har ingen ägare och tillhör vildmarken.";
    public String unclaimNotOwner = "&cDu är inte ägaren över denna chunk";
    public String unclaimSuccess = "&7Du är nu inte längre ägare över denna chunk. \n&aLade till %price% PlayMynt till ditt konto";

    // Access localization
    public String accessTitle = "&8&m   &8[ &3Main Mark &8]&8&m   ";
    public String accessNoPerm = "&cDu har inte tillåtelse att ge tillåtelse till chunks.";
    public String accessHas = "&a%player% har nu tillgång till dina chunks";
    public String accessNoLongerHas = "&a%player% har inte längre tillgång till dina chunks.";
    public String accessToggleMultiple =
            "&aDom följande spelarnas'' tillgång har blivit bytt.";
    public String accessOneself = "&cDu har redan tillgång till dina egna chunks.";
    public String accessNoOthers = "&cInga andra spelare har tillgång till dina chunks.";

    // Name localization
    public String nameClear = "&aDitt namn på dina chunks har tömts.";
    public String nameNotSet = "&cDu har inte satt ett namn på dina chunks än.";
    public String nameSet = "&aDitt namn har satts till: %name%";

    // AdminOverride localization
    public String adminOverrideEnable = "&7Du har satt på Admin Override, du kommer nu att kunna förstöra på andra plots";
    public String adminOverrideDisabled = "&cAdmin Override är nu avstängt, du kan inte längre förstöra på andra plots";

    // Protection localization
    public String denyWilderness = "&7Du har inte tillåtelse att göra det där i vildmarken!";
    public String denyClaimed = "&7Du har inte tillåtelse att göra det där inom den chunken!";


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
