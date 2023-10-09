package net.shirojr.nemuelch.util.helper;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
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
     * @param world used to get access to persistent world data. Entries will only appear once!
     * @return may be <b><i>null</i></b> if all possible Text entries have been used up in the world
     */
    @Nullable
    public static ArrayList<Text> getLines(BlockPos pos, @NotNull World world) {
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
        persistentData.usedSleepEventEntries.add(index);
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
        Text empty = new LiteralText("");

        List<SignLines> lines = new ArrayList<>();
        lines.add(new SignLines(nailLines(), new LiteralText("We are always"), new LiteralText("watching you!"), nailLines()));
        lines.add(new SignLines(nailLines(), new LiteralText("...on't urn around!"), empty, nailLines()));
        lines.add(new SignLines(nailLines(), empty, new LiteralText("...ou hear that?"), nailLines()));
        lines.add(new SignLines(nailLines(), new LiteralText("YoU aare"), new LiteralText("DEaD"), nailLines()));
        lines.add(new SignLines(nailLines(), new LiteralText(pos.getX() + " " + pos.getY() + " " + pos.getZ()), new LiteralText("RUN !"), nailLines()));
        return lines;
    }

    public static Text nailLines() {
        return new LiteralText("--x--X---X--x--");
    }

    public static boolean isSleepEventTime() {
        LocalDate time = LocalDate.now();
        if (time.getMonth() != Month.OCTOBER || time.getDayOfMonth() != 31) {
            NeMuelch.devLogger("Its not time yet! " + time.getDayOfMonth() + "." + time.getMonth());
            return false;
        }
        return true;
    }

    record SignLines(Text line0, Text line1, Text line2, Text line3) {
    }
}
