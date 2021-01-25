package readability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    private static final Scanner scan = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        var path = Path.of(args[0]);
        var lines = Files.lines(path);
        System.out.println("The text is:");
        lines.forEach(System.out::println);
        System.out.println();

        var unitToCount = Files.lines(path)
                .map(Main::process)
                .reduce((x, y) -> {
                    x.replaceAll((k, v) -> v + y.get(k));
                    return x;
                }).get();

        unitToCount.forEach((k, v) -> System.out.println(k + ": " + v));

        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");
        var choice = scan.nextLine().strip();
        System.out.println();
        printScore(choice, unitToCount);
    }

    private static double printScore(String alg, Map<String, Integer> unitToCount) {
        switch (alg) {
            case "ari":
                var ari = ari(unitToCount);
                System.out.printf("Automated Readability Index: %.2f (about %d-year-olds)." + System.lineSeparator(), ari, getAge(ari));
                return ari;
            case "fk":
                var fk = fk(unitToCount);
                System.out.printf("Flesch–Kincaid readability tests: %.2f (about %d-year-olds)." + System.lineSeparator(), fk, getAge(fk));
                return fk;
            case "smog":
                var smog = smog(unitToCount);
                System.out.printf("Simple Measure of Gobbledygook: %.2f (about %d-year-olds)." + System.lineSeparator(), smog, getAge(smog));
                return smog;
            case "cl":
                var cl = cl(unitToCount);
                System.out.printf("Coleman–Liau index: %.2f (about %d-year-olds)." + System.lineSeparator(), cl, getAge(cl - 1));
                return cl;
            default:
                var alg1 = printScore("ari", unitToCount);
                var age1 = getAge(alg1 - 1);
                var alg2 = printScore("fk", unitToCount);
                var age2 = getAge(alg2 - 1);
                var alg3 = printScore("smog", unitToCount);
                var age3 = getAge(alg3 - 1);
                var alg4 = printScore("cl", unitToCount);
                var age4 = getAge(alg4 - 1);
                System.out.println();
                System.out.printf("This text should be understood in average by %.2f-year-olds." + System.lineSeparator(),
                        (age1 + age2 + age3 + age4) / 4d);
                return 0;
        }
    }

    private static Map<String, Integer> process(String text) {
        var unitToCount = new LinkedHashMap<String, Integer>();

        var sentences = Arrays.stream(text.split("[.!?]+"))
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toList());
        var words = sentences.stream()
                .flatMap(s -> Arrays.stream(s.split("\\s+"))
                        .filter(Predicate.not(String::isBlank)))
                .map(word -> word.replaceAll("\\W", ""))
                .collect(Collectors.toList());


        unitToCount.put("Words", words.size());
        unitToCount.put("Sentences", sentences.size());
        unitToCount.put("Characters",
                text.replaceAll("\\s+", "").length());
        unitToCount.put("Syllables", words.stream().mapToInt(Main::countSyllables).sum());
        unitToCount.put("Polysyllables", (int) words.stream().filter(Main::isPolysyllable).count());

        return unitToCount;
    }


    private static int countSyllables(String word) {
        var count = 0;
        var onVowel = false;
        for (int i = 0; i < word.length(); i++) {
            if (isVowel(word.charAt(i))) {
                if (i == word.length() - 1 && word.charAt(i) == 'e') {
                    break;
                }
                count = onVowel ? count : count + 1;
                onVowel = true;
            } else {
                onVowel = false;
            }
        }
        return count == 0 ? 1 : count;
    }

    private static boolean isVowel(char c) {
        var vowels = "aeiouy";
        return vowels.contains(c + "");
    }

    private static boolean isPolysyllable(String word) {
        return countSyllables(word) > 2;
    }

    private static double ari(Map<String, Integer> unitToCount) {
        return 4.71 * (unitToCount.get("Characters") / (double) unitToCount.get("Words"))
                + 0.5 * (unitToCount.get("Words") / (double) unitToCount.get("Sentences"))
                - 21.43;
    }

    private static double fk(Map<String, Integer> unitToCount) {
        return 0.39 * (unitToCount.get("Words") / (double) unitToCount.get("Sentences"))
                + 11.8 * (unitToCount.get("Syllables") / (double) unitToCount.get("Words"))
                - 15.59;
    }

    private static double smog(Map<String, Integer> unitToCount) {
        return 1.0430 * Math.sqrt(unitToCount.get("Polysyllables") * (30 / (double) unitToCount.get("Sentences")))
                + 3.1291;
    }

    private static double cl(Map<String, Integer> unitToCount) {
        return 0.0588 * unitToCount.get("Characters") / (double) unitToCount.get("Words") * 100
                - 0.296 * unitToCount.get("Sentences") / (double) unitToCount.get("Words") * 100
                - 15.8;
    }

    private static int getAge(double score) {
        var ages = new int[]{6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 24, 24};
        return ages[(int) Math.ceil(score) - 1];
    }
}
