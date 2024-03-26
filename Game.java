import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public final class Game {
    public static final int WORD_LENGTH = 5;

    private Game() {}

    public static final ArrayList<String> wordList = new ArrayList<>();
    static {
        try (Scanner words = new Scanner(new File("WordleWords.txt"))) {
            while (words.hasNext()) {
                wordList.add(words.next());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static int numTimes(String word, char letter) {
        int numTimes = 0;
        for (char c : word.toCharArray()) {
            if (c == letter) {
                numTimes++;
            }
        }
        return numTimes;
    }

    public static String guess(String word, String guess) {
        char[] output = new char[]{' ', ' ', ' ', ' ', ' '};
        char[] wordGuesses = new char[]{' ', ' ', ' ', ' ', ' '};

        for (int i = 0; i < WORD_LENGTH; i++) {
            char guessChar = guess.charAt(i);
            if (!word.contains(String.valueOf(guessChar))) {
                output[i] = 'B';
                wordGuesses[i] = guessChar;
            } else if (word.charAt(i) == guessChar) {
                output[i] = 'G';
                wordGuesses[i] = guessChar;
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            char guessChar = guess.charAt(i);
            if (output[i] == ' ') {
                if (numTimes(String.valueOf(wordGuesses), guessChar) < numTimes(word, guessChar)) {
                    output[i] = 'Y';
                } else {
                    output[i] = 'B';
                }
                wordGuesses[i] = guessChar;
            }
        }

        return String.valueOf(output);
    }

    public static void main(String[] args) {
        System.out.println(guess("WOMEN", "TRACE"));
    }
}
