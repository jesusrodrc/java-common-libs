package org.opencb.commons.bioformats.variant.vcf4.io.writers.stats;

import org.opencb.commons.bioformats.variant.utils.stats.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantStatsFileDataWriter implements VariantStatsDataWriter {

    private PrintWriter variantPw;
    private PrintWriter globalPw;
    private PrintWriter samplePw;
    private Map<String, Map<String, PrintWriter>> mapGroupPw;
    private String path;
    private String pathSampleGroup;
    private String pathGroup;


    public VariantStatsFileDataWriter(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        this.path = path;

        mapGroupPw = new LinkedHashMap<>(2);
    }

    @Override
    public boolean open() {

        try {
            variantPw = new PrintWriter(new FileWriter(path + "variants.stats"));
            globalPw = new PrintWriter(new FileWriter(path + "global.stats"));
            samplePw = new PrintWriter(new FileWriter(path + "sample.stats"));

            Path dirPath = Paths.get(path + "sampleGroupStats");
            Path dir = Files.createDirectories(dirPath);
            this.pathSampleGroup = dir.toString() + "/";

            dirPath = Paths.get(path + "groupStats");
            dir = Files.createDirectories(dirPath);
            this.pathGroup = dir.toString() + "/";
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    @Override
    public boolean close() {
        variantPw.close();
        globalPw.close();
        samplePw.close();

        PrintWriter pw;

        for (Map.Entry<String, Map<String, PrintWriter>> entry : mapGroupPw.entrySet()) {
            for (Map.Entry<String, PrintWriter> entryAux : entry.getValue().entrySet()) {
                pw = entryAux.getValue();
                pw.close();
            }
        }

        return true;
    }

    @Override
    public boolean pre() {
        writeVariantStatsHeader();

        return true;
    }

    @Override
    public boolean post() {
        return true;
    }

    @Override
    public boolean writeVariantStats(List<VariantStats> data) {
        for (VariantStats v : data) {
            variantPw.append(String.format("%-5s%-10d%-10s%-5s%-10s%-10s%-10s" +
                    "%-10d%-10d%-10d%-15s%-40s%-10d%-10d%-15d" +
                    "%-10.2f%-10.2f%-10.2f%-10.2f\n",
                    v.getChromosome(),
                    v.getPosition(),
                    (v.isIndel() ? "Y" : "N"),
                    v.getRefAlleles(),
                    Arrays.toString(v.getAltAlleles()),
                    v.getMafAllele(),
                    v.getMgfAllele(),
                    v.getNumAlleles(),
                    v.getMissingAlleles(),
                    v.getMissingGenotypes(),
                    Arrays.toString(v.getAllelesCount()),
                    v.getGenotypes(),
                    v.getTransitionsCount(),
                    v.getTransversionsCount(),
                    v.getMendelinanErrors(),
                    v.getCasesPercentDominant(),
                    v.getControlsPercentDominant(),
                    v.getCasesPercentRecessive(),
                    v.getControlsPercentRecessive()
            ));
        }
        return true;
    }

    @Override
    public boolean writeGlobalStats(VariantGlobalStats variantGlobalStats) {

        globalPw.append("Number of variants = " + variantGlobalStats.getVariantsCount() + "\n");
        globalPw.append("Number of samples = " + variantGlobalStats.getSamplesCount() + "\n");
        globalPw.append("Number of biallelic variants = " + variantGlobalStats.getBiallelicsCount() + "\n");
        globalPw.append("Number of multiallelic variants = " + variantGlobalStats.getMultiallelicsCount() + "\n");
        globalPw.append("Number of SNP = " + variantGlobalStats.getSnpsCount() + "\n");
        globalPw.append("Number of indels = " + variantGlobalStats.getIndelsCount() + "\n");
        globalPw.append("Number of transitions = " + variantGlobalStats.getTransitionsCount() + "\n");
        globalPw.append("Number of transversions = " + variantGlobalStats.getTransversionsCount() + "\n");
        globalPw.append("Ti/TV ratio = " + ((float) variantGlobalStats.getTransitionsCount() / (float) variantGlobalStats.getTransversionsCount()) + "\n");
        globalPw.append("Percentage of PASS = " + (((float) variantGlobalStats.getPassCount() / (float) variantGlobalStats.getVariantsCount()) * 100) + "%\n");
        globalPw.append("Average quality = " + (variantGlobalStats.getAccumQuality() / (float) variantGlobalStats.getVariantsCount()) + "\n");

        return true;
    }

    @Override
    public boolean writeSampleStats(VariantSampleStats variantSampleStats) {
        VariantSingleSampleStats s;
        samplePw.append(String.format("%-10s%-10s%-10s%-10s\n", "Sample", "MissGt", "Mendel Err", "Homoz Count"));
        for (Map.Entry<String, VariantSingleSampleStats> entry : variantSampleStats.getSamplesStats().entrySet()) {
            s = entry.getValue();
            samplePw.append(String.format("%-10s%-10d%-10d%10d\n", s.getId(), s.getMissingGenotypes(), s.getMendelianErrors(), s.getHomozygotesNumber()));

        }
        return true;
    }

    @Override
    public boolean writeSampleGroupStats(VariantSampleGroupStats variantSampleGroupStats) throws IOException {
        PrintWriter pw;
        String filename;
        VariantSampleStats variantSampleStats;
        VariantSingleSampleStats s;

        for (Map.Entry<String, VariantSampleStats> entry : variantSampleGroupStats.getSampleStats().entrySet()) {
            filename = pathSampleGroup + "variant_stats_" + variantSampleGroupStats.getGroup() + "_" + entry.getKey() + ".sample.stats";
            variantSampleStats = entry.getValue();
            pw = new PrintWriter(new FileWriter(filename));

            pw.append(String.format("%-10s%-10s%-10s%-10s\n", "Sample", "MissGt", "Mendel Err", "Homoz Count"));

            for (Map.Entry<String, VariantSingleSampleStats> entrySample : variantSampleStats.getSamplesStats().entrySet()) {
                s = entrySample.getValue();
                pw.append(String.format("%-10s%-10d%-10d%10d\n", s.getId(), s.getMissingGenotypes(), s.getMendelianErrors(), s.getHomozygotesNumber()));

            }
            pw.close();

        }

        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean writeVariantGroupStats(VariantGroupStats groupStats) throws IOException {
        PrintWriter pw;
        String filename;
        List<VariantStats> list;

        Map<String, PrintWriter> auxMap;

        if (groupStats == null)
            return false;

        String group = groupStats.getGroup();
        auxMap = mapGroupPw.get(group);

        if (auxMap == null) {

            auxMap = new LinkedHashMap<>(2);

            for (Map.Entry<String, List<VariantStats>> entry : groupStats.getVariantStats().entrySet()) {
                filename = pathGroup + "variant_stats_" + groupStats.getGroup() + "_" + entry.getKey() + ".stats";
                pw = new PrintWriter(new FileWriter(filename));

                pw.append(String.format("%-5s%-10s%-10s%-5s%-10s%-10s%-10s" +
                        "%-10s%-10s%-10s%-15s%-40s%-10s%-10s%-15s" +
                        "%-10s%-10s%-10s%-10s\n",
                        "Chr", "Pos", "Indel?", "Ref", "Alt", "Maf", "Mgf",
                        "NumAll.", "Miss All.", "Miss Gt", "All. Count", "Gt count", "Trans", "Transv", "Mend Error",
                        "Cases D", "Controls D", "Cases R", "Controls R"));

                auxMap.put(entry.getKey(), pw);
            }

            mapGroupPw.put(group, auxMap);


        }

        auxMap = mapGroupPw.get(group);

        for (Map.Entry<String, PrintWriter> entry : auxMap.entrySet()) {
            pw = entry.getValue();
            list = groupStats.getVariantStats().get(entry.getKey());
            for (VariantStats v : list) {
                pw.append(String.format("%-5s%-10d%-10s%-5s%-10s%-10s%-10s" +
                        "%-10d%-10d%-10d%-15s%-40s%-10d%-10d%-15d" +
                        "%-10.2f%-10.2f%-10.2f%-10.2f\n",
                        v.getChromosome(),
                        v.getPosition(),
                        (v.isIndel() ? "Y" : "N"),
                        v.getRefAlleles(),
                        Arrays.toString(v.getAltAlleles()),
                        v.getMafAllele(),
                        v.getMgfAllele(),
                        v.getNumAlleles(),
                        v.getMissingAlleles(),
                        v.getMissingGenotypes(),
                        Arrays.toString(v.getAllelesCount()),
                        v.getGenotypes(),
                        v.getTransitionsCount(),
                        v.getTransversionsCount(),
                        v.getMendelinanErrors(),
                        v.getCasesPercentDominant(),
                        v.getControlsPercentDominant(),
                        v.getCasesPercentRecessive(),
                        v.getControlsPercentRecessive()
                ));

            }

        }


        return true;
    }

    public void writeVariantStatsHeader() {
        variantPw.append(String.format("%-5s%-10s%-10s%-5s%-10s%-10s%-10s" +
                "%-10s%-10s%-10s%-15s%-40s%-10s%-10s%-15s" +
                "%-10s%-10s%-10s%-10s\n",
                "Chr", "Pos", "Indel?", "Ref", "Alt", "Maf", "Mgf",
                "NumAll.", "Miss All.", "Miss Gt", "All. Count", "Gt count", "Trans", "Transv", "Mend Error",
                "Cases D", "Controls D", "Cases R", "Controls R"));
    }
}
