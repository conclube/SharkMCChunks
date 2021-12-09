package se.xfunserver.xplaychunks.utils;

public enum Messages {
    INVALID_SUBCOMMAND("§cFel vid utförning av kommando. Gör /chunk för hjälp"),
    COULD_NOT_FIND_PLAYER("§7Kunde inte hitta spelaren"),
    NO_PERMISSSION("§cDu har inte tillåtelse att utföra detta kommando!"),
    ALREADY_CLAIMED("§cDenna chunk har redan en ägare och är inte till salu"),
    NOT_CLAIMED("§cDenna chunk har ingen ägare"),
    NO_INFORMATION("§7Denna chunk ägs inte av någon och har därför ingen information"),
    CANNOT_AFFORD("§cDu har inte tillräckligt med pengar för att köpa denna chunk.\n§7Chunks kostar %price% PlayMynt"),
    REACHED_MAX_CHUNKS("§cDu har nått gränsen för maximalt antal ägda chunks"),
    CLAIMED_CHUNK("§7Du köpte chunken §a%chunkid% §7för §a%price% PlayMynt"),
    CLAIMED_FREE_CHUNK("§7Du köpte chunken §a%chunkid% §7gratis. §7Notera att det är bara din första chunk som är gratis, chunks kostar i vanliga fall §a%price% PlayMynt"),
    CLAIMED_SERVERCHUNK("§7Du tog över §5%chunkid% §7till servern"),
    FORCE_CLAIMED_CHUNK("§7Du tog över chunken §a%chunkid%"),
    FORCE_CLAIMED_CHUNK_TO_PLAYER("§7Du tog över chunken §a%chunkid% §7till §2%target%"),
    FORCE_UNCLAIMED_CHUNK("§7Tog bort all information om chunken §a%chunkid%"),
    UNCLAIMED_CHUNK("§7Du är nu inte längre ägare över denna chunk. \n§aLade till %price% PlayMynt till ditt konto"),
    UNCLAIMED_FREE_CHUNK("§7Du är nu inte längre ägare över denna chunk.\n§aDu kan nu claima en gratischunk igen"),
    ALREADY_FOR_SALE("§7Denna chunken är redan till salu, du kan använda §a/chunk forsale cancel §7för att avbryta din säljning"),
    PRICE_IS_TOO_HIGH("§7Du kan inte sätta ett pris som är högre än §a10 000 000 PlayMynt"),
    PRICE_IS_TOO_LOW("§7Du kan inte sätta ett pris som är lägre än §a5000 PlayMynt"),
    CANCELED_SELLING("§7Denna chunken är nu längre inte till salu!"),
    NOT_FOR_SALE("§7Denna chunken är inte till salu"),
    NOW_SELLING("§7Du har satt din chunk till salu för §a%price% PlayMynt"),
    HAS_TO_BE_NUMBER("§7Summan du angav var ogiltlig. Försök igen!"),
    NOT_ENOUGH_CHUNKS("§cDu behöver äga minst 2 chunks för att kunna sälja chunks till andra!"),
    CANNOT_AFFORD_SALECHUNK("§cDu har inte tillräckligt med pengar för att köpa denna chunk"),
    BOUGHT_CHUNK_FROM("§7Du köpte chunken §a%chunkid% §7av §a%target% §7för §a%price% PlayMynt"),
    PLAYER_BOUGHT_YOUR_CHUNK("§6En av dina chunks blev precis såld till §a%target% §6för §a%price% PlayMynt"),
    CANNOT_BUY_FROM_YOURSELF("§7Du kan inte köpa din egna chunk!"),
    SELLER_HAS_NOT_ENOUGH_CHUNKS("§cKunde inte köpa chunken eftersom säljaren inte äger nog många chunks"),
    CONFIRM_PURCHASE("§7Är du säker på att du vill köpa chunken §a%chunkid% §7för §a%price% PlayMynt§7? §6Använd §a/chunk confirm §6för att bekräfta ditt köp"),
    NOTHING_TO_CONFIRM("§7Du har ingenting att bekräfta"),
    NOT_FOR_SALE_ANYMORE("§7Chunken du försökte köpa är inte längre till salu!"),
    ADDED_TRUSTED("§7Lade till §2%target% §7till medlemslistan"),
    REMOVED_TRUSTED("§7Tog bort §c%target% §7från medlemslistan"),
    ENTER_CLAIMED("§2§l%chunkowner%"),
    ENTER_SELLING("§2§l%chunkowner% §7| §6Till salu: §a%price% §6§lPlayMynt"),
    ENTER_WILDERNESS("§7§o§lVildmarken"),
    ENTER_SERVER_CLAIMED("§5§lServer"),
    NOT_OWNER("§cDu är inte ägaren över denna chunk"),
    CANNOT_REMOVE_ON_OTHERS("§cDu har inte tillåtelse att ta bort medlemmar från denna chunk"),
    CANNOT_TRUST_ON_OTHERS("§cDu har inte tillåtelse att lägga till medlemmar i denna chunk"),
    CANNOT_TRUST_YOURSELF("§7Du kan inte lägga till dig själv som medlem i chunken"),
    ALREADY_TRUSTED("§7Spelaren är redan tillagd som medlem i den här chunken"),
    IS_NOT_TRUSTED("§7Den spelaren är inte tillagd i denna chunken"),
    TRUSTED_MULTIPLE("§aDu ändrade tillåtelse för flera personer i dina chunks."),
    CLAIMED_DENY("§7Du har inte tillåtelse att göra det där inom den chunken!"),
    WILDERNESS_DENY("§7Du har inte tillåtelse att göra det där i vildmarken!"),
    TNT_ENABLED("§7Du har satt på TnT på dina claims."),
    TNT_DISABLED("§cDu har stängt av TNT på dina claims."),
    ADMIN_OVERRIDE_ENABLED("§7Du har satt på Admin Override, du kommer nu att kunna förstöra på andra plots."),
    ADMIN_OVERRIDE_DISABLED("§cAdmin Override är nu avstängt, du kan inte längre förstöra på andra plots."),
    CLEARED_PLOT_NAME("§7Du tömde namnet på denna plot."),
    NO_NAME_SET("§cInget namn har givits till denna plot, ingen ändring gjordes."),
    NAME_SET("§7Du satte namnet på denna chunk till '%name%'.");

    private String message;

    private Messages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}

