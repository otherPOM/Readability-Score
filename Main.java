package readability;

import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Predicate;

public class Main {
    private static final Scanner scan = new Scanner(System.in);

    public static void main(String[] args) {
        var line = scan.nextLine();
        var sentences = line.split("[.!?]");
        var avg = Arrays.stream(sentences)
                .filter(Predicate.not(String::isBlank))
                .mapToLong(Main::countWords)
                .sum() / (double) sentences.length;
        System.out.println(avg > 10 ? "HARD" : "EASY");
    }

    private static long countWords(String sentence) {
        return Arrays.stream(sentence.split("\\s+"))
                .filter(Predicate.not(String::isBlank))
                .count();
    }
}
