import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.*;

public class Bot {
    private final boolean hard;
    private HashSet<String> yellowLetters;

    public Bot(boolean hard) {
        this.hard = hard;
        yellowLetters = new HashSet<>();
    }

    public void run() {
        Scanner reader = new Scanner(System.in);
        //String word = getAdjustedScore(Game.wordList).entrySet().iterator().next().getKey();
        String word = "SALET";
        System.out.println("Guess: " + word);
        System.out.print("Output: ");
        String guess = reader.nextLine();
        ArrayList<String> possibleWords = getBuckets(word, Game.wordList).get(guess);
        ArrayList<String> newPossibleWords;
        int guessNumber = 2;
        while (guessNumber <= 6) {
            try {
                word = getAdjustedScore(possibleWords).entrySet().iterator().next().getKey();
                System.out.println("Guess: " + word);

                try {
                    Robot robot = new Robot();
                    robot.delay(5000);
                    for (char c : word.toCharArray()) {
                        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
                        robot.keyPress(keyCode);
                        robot.keyRelease(keyCode);
                        robot.delay(100);
                    }
                } catch (AWTException e) {
                    e.printStackTrace();
                }

                System.out.print("Output: ");
                guess = reader.nextLine();
                for (int i = 0; i < Game.WORD_LENGTH; i++) {
                    if (guess.charAt(i) == 'Y') {
                        yellowLetters.add(word.substring(i, i + 1));
                    }
                }
                newPossibleWords = getBuckets(word, Game.wordList).get(guess);

                ArrayList<String> commonElements = new ArrayList<>();
                for (String element : newPossibleWords) {
                    if (possibleWords.contains(element)) {
                        commonElements.add(element);
                    }
                }
                possibleWords = commonElements;

                guessNumber++;

                if (possibleWords.size() == 1) {
                    break;
                }
            } catch (NullPointerException e) {
                possibleWords = new ArrayList<>(List.of(word));
            }
        }
        System.out.println("Final: " + possibleWords.get(0));
    }

    public int run(String firstWord, String answer) {
        //String word = getAdjustedScore(Game.wordList).entrySet().iterator().next().getKey();
        String word = firstWord;
        String guess = Game.guess(answer, word);
        ArrayList<String> possibleWords = getBuckets(word, Game.wordList).get(guess);
        ArrayList<String> newPossibleWords;
        int guessNumber = 2;
        //System.out.println("Answer: " + answer);
        while (guessNumber <= 6) {
            try {
                word = getAdjustedScore(possibleWords).entrySet().iterator().next().getKey();
                //System.out.println("Guess " + guessNumber + ": " + word);
                guess = Game.guess(answer, word);
                newPossibleWords = getBuckets(word, Game.wordList).get(guess);

                ArrayList<String> commonElements = new ArrayList<>();
                for (String element : newPossibleWords) {
                    if (possibleWords.contains(element)) {
                        commonElements.add(element);
                    }
                }
                possibleWords = commonElements;

                guessNumber++;

                if (possibleWords.size() == 1) {
                    break;
                }
            } catch (NullPointerException e) {
                possibleWords = new ArrayList<>(List.of(word));
            }
        }
        if (!possibleWords.get(0).equals(answer)) {
            throw new RuntimeException("Wrong answer");
        }
        //System.out.println("Final: " + possibleWords);
        return guessNumber;
    }

    private LinkedHashMap<String, Double> getAdjustedScore(ArrayList<String> possibleWords) {
        HashMap<String, Double> scores = new HashMap<>();
        for (String word : Game.wordList) {
            HashMap<String, ArrayList<String>> buckets = getBuckets(word, possibleWords);
            //double bucketSize = getAverageBucketSize(buckets);
            double bucketSize = getAverageBucketSize(buckets);
            double oddsNextIsAnswer = (double) buckets.size() / Game.wordList.size();
            double adjustedScore = (1 - oddsNextIsAnswer) * bucketSize;
            scores.put(word, adjustedScore);
        }
        return sortByValue(scores);
    }

    private LinkedHashMap<String, Double> sortByValue(HashMap<String, Double> unsorted) {
        ArrayList<Map.Entry<String, Double>> entryList = new ArrayList<>(unsorted.entrySet());
        entryList.sort(Map.Entry.comparingByValue());
        LinkedHashMap<String, Double> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : entryList) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        return sorted;
    }

    private HashMap<String, ArrayList<String>> getBuckets(String guess, ArrayList<String> possibleWords) {
        HashMap<String, ArrayList<String>> buckets = new HashMap<>();
        for (String word : possibleWords) {
            if (!word.equals(guess)) {
                String bucket = Game.guess(word, guess);
                if (!buckets.containsKey(bucket)) {
                    buckets.put(bucket, new ArrayList<>());
                }
                buckets.get(bucket).add(word);
            }
        }

        if (hard) {
            System.out.println(sortByLetters(buckets));
            return sortByLetters(buckets);
        }

        return buckets;
    }

    private int[] getBucketSizes(HashMap<String, ArrayList<String>> buckets) {
        int[] output = new int[buckets.size()];
        int i = 0;
        for (ArrayList<String> bucket : buckets.values()) {
            output[i] = bucket.size();
            i++;
        }
        return output;
    }

    private double getMedianBucketSize(HashMap<String, ArrayList<String>> buckets) {
        int[] bucketSizes = getBucketSizes(buckets);
        Arrays.sort(bucketSizes);
        double median;
        if (bucketSizes.length % 2 == 0) {
            median = ((double) bucketSizes[bucketSizes.length / 2] + (double) bucketSizes[bucketSizes.length / 2 - 1]) / 2;
        } else {
            median = bucketSizes[bucketSizes.length / 2];
        }
        return median;
    }

    private double getAverageBucketSize(HashMap<String, ArrayList<String>> buckets) {
        int[] bucketSizes = getBucketSizes(buckets);
        int sum = 0;
        for (int i : bucketSizes) {
            sum += i;
        }
        return (double) sum / bucketSizes.length;
    }

    private HashMap<String, ArrayList<String>> sortByLetters(HashMap<String, ArrayList<String>> buckets) {
        for (Map.Entry<String, ArrayList<String>> entry : buckets.entrySet()) {
            wordLoop:
            for (String word : entry.getValue()) {
                for (String letter : yellowLetters) {
                    if (!word.contains(letter)) {
                        entry.getValue().remove(word);
                        continue wordLoop;
                    }
                }
            }
        }
        return buckets;
    }

    public static HashMap<String, Double> test(int start, int end) throws FileNotFoundException {
        Bot bot = new Bot(false);
        Scanner answerReader = new Scanner(new File("words.txt"));
        for (int i = 0; i < start; i++) {
            answerReader.nextLine();
        }
        Scanner reader;
        HashMap<String, Double> words = new HashMap<>();
        for (int i = start; i < end; i++) {
            String word = answerReader.nextLine();
            reader = new Scanner(new File("words.txt"));
            int sum = 0;
            int count = 0;
            while (reader.hasNextLine()) {
                String answer = reader.nextLine();
                sum += bot.run(word, answer);
                count++;
                System.out.print("\033[H\033[2J");
                System.out.flush();
                System.out.print(count + " ");
            }
            double average = (double) sum / count;
            System.out.println("\n" + word + ": " + average);
            words.put(word, average);
            reader.close();
        }
        return words;
    }

    public static void main(String[] args) throws FileNotFoundException {
        /*ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < Integer.parseInt(args[0]); i++) {
            long startTime = System.nanoTime();
            test(1636, 1637);
            long endTime = System.nanoTime();
            times.add((endTime - startTime) / 1000000000);
        }
        System.out.println(times);*/
        /*Runner runner = new Runner();
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 47; i++) {
            threads.add(new Thread(runner));
        }
        for (Thread thread : threads) {
            thread.start();
        }*/
        //HashMap<String, Double> words = test(0, 2);
        //System.out.println(words);
        Bot bot = new Bot(false);
        Scanner reader = new Scanner(System.in);
        String yn = "y";
        while (yn.equalsIgnoreCase("y")) {
            bot.run();
            System.out.print("Play again? (y/n): ");
            yn = reader.nextLine();
        }
    }
}
