package readability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        var path = Path.of(args[0]);
        var lines = Files.lines(path);
        System.out.println("The text is:");
        lines.forEach(System.out::println);
        System.out.println();

        var res = Files.lines(path)
                .map(Main::process)
                .reduce((x, y) -> {
                    x.replaceAll((k, v) -> v + y.get(k));
                    return x;
                }).get();

        System.out.println("Words: " + res.get("Words"));
        System.out.println("Sentences: " + res.get("Sentences"));
        System.out.println("Characters: " + res.get("Characters"));

        var score = ari(res);
        System.out.printf("The score is: %.2f\n", score);
        System.out.println("This text should be understood by " + getAge(score) + "-year-olds.");
    }

    private static Map<String, Integer> process(String text) {
        var unitToCount = new HashMap<String, Integer>();
        unitToCount.put("Characters",
                text.replaceAll("\\s+", "").length());
        var sentences = Arrays.stream(text.split("[.!?]+"))
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toList());
        unitToCount.put("Sentences", sentences.size());
        unitToCount.put("Words", sentences.stream().mapToInt(s -> (int) countWords(s)).sum());
        return unitToCount;
    }

    private static long countWords(String sentence) {
        return Arrays.stream(sentence.split("\\s+"))
                .filter(Predicate.not(String::isBlank))
                .count();
    }

    private static double ari(Map<String, Integer> unitToCount) {
        return 4.71
                * (unitToCount.get("Characters") / (double) unitToCount.get("Words"))
                + 0.5
                * (unitToCount.get("Words") / (double) unitToCount.get("Sentences"))
                - 21.43;
    }

    private static String getAge(double score) {
        var ages = new String[]{"5-6", "6-7", "7-9", "9-10", "10-11", "11-12", "12-13",
        "13-14", "14-15", "15-16", "16-17", "17-18", "18-24", "24+"};
        return ages[(int) Math.ceil(score) - 1];
    }
}
