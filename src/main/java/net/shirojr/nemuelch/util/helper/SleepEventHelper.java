package net.shirojr.nemuelch.util.helper;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import net.shirojr.nemuelch.world.PersistentWorldData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SleepEventHelper {
    /**
     * Get randomly generated Lines for a full SignBlock Text field
     *
     * @param pos   Position which may be printed on the signs
     * @param world used to get access to persistent world data. Entries will only appear once in the world!
     * @return may be <b><i>null</i></b> if all possible Text entries have been used up in the world
     */
    @Nullable
    public static ArrayList<Text> getAvailableLines(BlockPos pos, @NotNull World world, PlayerEntity player) {
        @SuppressWarnings("DataFlowIssue") PersistentWorldData persistentData = PersistentWorldData.getServerState(world.getServer());
        List<SignLines> signLinesList = linesList(pos);
        int possibleTextAmount = signLinesList.size();

        if (persistentData.usedSleepEventEntries.size() >= possibleTextAmount) return null;

        Random random = new Random();
        int index = random.nextInt(0, possibleTextAmount);
        while (persistentData.usedSleepEventEntries.contains(index)) {
            index = new Random().nextInt(0, possibleTextAmount);
        }
        ArrayList<Text> output = new ArrayList<>();
        output.add(signLinesList.get(index).line0);
        output.add(signLinesList.get(index).line1);
        output.add(signLinesList.get(index).line2);
        output.add(signLinesList.get(index).line3);
        persistentData.usedSleepEventEntries.add(new SleepEventDataEntry(player.getName().getString(), index));
        return output;
    }

    public static ArrayList<Text> getEntry(int index) {
        BlockPos pos = new BlockPos(0, 0, 0);
        ArrayList<Text> output = new ArrayList<>();
        output.add(linesList(pos).get(index).line0);
        output.add(linesList(pos).get(index).line1);
        output.add(linesList(pos).get(index).line2);
        output.add(linesList(pos).get(index).line3);
        return output;
    }

    /**
     * Text entries for SignBlocks. Add new lines to the list, to get more Text entries.
     *
     * @param pos may be used to print on the sign.
     */
    private static List<SignLines> linesList(BlockPos pos) {
        Text empty = Text.literal("");

        List<SignLines> lines = new ArrayList<>();

        // Kloster
        lines.add(new SignLines(Text.literal("Hörst du die"), Text.literal("klagenden Stimmen"),
                Text.literal("des Klosters?"), empty));
        // Südgebirge
        lines.add(new SignLines(Text.literal("Vom Südschnee"), Text.literal("umgeben, in der"),
                Text.literal("Höhe findest du"), Text.literal("das Bestreben!")));
        // Westhafen Schmiede
        lines.add(new SignLines(Text.literal("Im nun stillen"), Text.literal("Hafen, man noch"),
                Text.literal("vernimmt den"), Text.literal("Hammersschlag!")));
        // Sägewerk
        lines.add(new SignLines(Text.literal("Die Bäume schrien"), Text.literal("vor langer Zeit;"),
                Text.literal("Das Haus davon"), Text.literal("nicht befreit!")));
        // Große dunkle Brücke
        lines.add(new SignLines(Text.literal("Die dunkle Brücke"), Text.literal("fällt zusammen"),
                Text.literal("fällt zusammen..."), Text.literal("Meine Dame")));
        // Froschhöhle Wasserquelle
        lines.add(new SignLines(Text.literal("Im Fels der Unke,"), Text.literal("Im Ursprung des"),
                Text.literal("Wassers, fällt"), Text.literal("der Groschen")));
        // Spawn Tempel
        lines.add(new SignLines(Text.literal("Wo die Blutvögel"), Text.literal("wachen, ziehe das"),
                Text.literal("Gatter und fall"), Text.literal("in dein Glück")));
        // Burg Mauerturm (nicht Rotfels...)
        lines.add(new SignLines(Text.literal("Im mächtigen Turm"), Text.literal("der einsamen"),
                Text.literal("Burg liegt dein"), Text.literal("Verderben")));
        // Vampir Lager aus dem Trailer
        lines.add(new SignLines(Text.literal("Im Blick die Eule"), Text.literal("und die Unke,"),
                Text.literal("Der Berg steht"), Text.literal("nicht zur Schau")));

        // Caesar Cipher +3 Gruppenaufruf: "Verbünde dich mit Leuten des Caesars. Haltet es geheim!"
        lines.add(new SignLines(Text.literal("Yhueüqgh glfk plw"), Text.literal("Ohxwhq ghv"),
                Text.literal("Fdhvduv. Kdowhw"), Text.literal("hv jhkhlp !")));
        // Caesar Cipher +3 Gruppenaufruf: "Verbuendet euch mit Bruedern des Caesars. Sucht nach ihnen!"
        lines.add(new SignLines(Text.literal("Yhuexhqghw hxfk"), Text.literal("plw Euxhghuq ghv"),
                Text.literal("Fdhvduv. Vxfkw"), Text.literal("qdfk lkqhq !")));
        // Caesar Cipher +3 Gruppenaufruf: "Wartet auf Anweisungen, Leute des Caesars."
        lines.add(new SignLines(Text.literal("Zduwhw dxi"), Text.literal("Dqzhlvxqjhq,"),
                Text.literal("Ohxwh ghv"), Text.literal("Fdhvduv.")));
        // Caesar Cipher +3 Gruppenaufruf: "Brueder Caesars, rekrutiert drei weitere, denen ihr vertraut"
        lines.add(new SignLines(Text.literal("Euxhghu Fdhvduv,"), Text.literal("uhnuxwlhuw guhl"),
                Text.literal("zhlwhuh, ghqhq"), Text.literal("lku yhuwudxw")));

        return lines;
    }

    public static boolean isSleepEventTime() {
        LocalDate time = LocalDate.now();
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LoggerUtil.devLogger("Launched in DevEnv, time is " + time.getDayOfMonth() + "." + time.getMonth());
            return true;
        }

        boolean isOctoberTime = time.getMonth() == Month.OCTOBER &&
                time.getDayOfMonth() != 31;
        boolean isNovemberTime = time.getMonth() == Month.NOVEMBER &&
                (time.getDayOfMonth() >= 3 && time.getDayOfMonth() <= 5);

        if (!isOctoberTime && !isNovemberTime) {
            LoggerUtil.devLogger("Its not time yet! " + time.getDayOfMonth() + "." + time.getMonth());
            return false;
        }
        return true;
    }

    /**
     * Data for handling sleep entries e.g. for command output.
     *
     * @param playerName      player, who activated the entry
     * @param sleepEventIndex saved sleepEvent entry index
     */
    public record SleepEventDataEntry(String playerName, int sleepEventIndex) {
    }

    record SignLines(Text line0, Text line1, Text line2, Text line3) {
    }
}
