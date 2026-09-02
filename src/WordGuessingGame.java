import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WordGuessingGame {

    /** Pairs a secret word with a clue describing it. */
    private static class WordClue {
        final String word;
        final String clue;

        WordClue(String word, String clue) {
            this.word = word;
            this.clue = clue;
        }
    }

    private static final WordClue[] WORD_BANK = {
            new WordClue("apple",  "A common fruit that's often red or green, and keeps the doctor away."),
            new WordClue("banana", "A long, curved yellow fruit that monkeys love."),
            new WordClue("orange", "A round citrus fruit that shares its name with a color."),
            new WordClue("grape",  "A small, round fruit that grows in bunches and can be made into wine."),
            new WordClue("melon",  "A large, round fruit with a sweet, watery interior."),
            new WordClue("cherry", "A small, red stone fruit often seen on top of desserts."),
            new WordClue("mango",  "A sweet tropical fruit sometimes called the king of fruits."),
            new WordClue("papaya", "An orange-fleshed tropical fruit with black seeds inside."),
            new WordClue("lemon",  "A sour, bright yellow citrus fruit."),
            new WordClue("kiwi",   "A small fruit with fuzzy brown skin and bright green flesh inside.")
    };

    public static final int MAX_ATTEMPTS = 6;

    private final String secretWord;
    private final String clue;
    private final Set<Character> guessedLetters = new HashSet<>();
    private final Set<Character> wrongLetters = new HashSet<>();
    private int remainingAttempts = MAX_ATTEMPTS;

    public WordGuessingGame() {
        WordClue chosen = pickRandomEntry();
        this.secretWord = chosen.word.toLowerCase();
        this.clue = chosen.clue;
    }

    private WordClue pickRandomEntry() {
        Random random = new Random();
        return WORD_BANK[random.nextInt(WORD_BANK.length)];
    }

    public String getMaskedWord() {
        StringBuilder sb = new StringBuilder();
        for (char c : secretWord.toCharArray()) {
            sb.append(guessedLetters.contains(c) ? Character.toUpperCase(c) : '*');
        }
        return sb.toString();
    }

    public String getClue() {
        return clue;
    }

    private boolean isWordFullyGuessed() {
        for (char c : secretWord.toCharArray()) {
            if (!guessedLetters.contains(c)) return false;
        }
        return true;
    }

    public static class GuessResult {
        public boolean valid;
        public boolean correct;
        public String message;

        GuessResult(boolean valid, boolean correct, String message) {
            this.valid = valid;
            this.correct = correct;
            this.message = message;
        }
    }

    public GuessResult guessLetter(char letter) {
        letter = Character.toLowerCase(letter);

        if (!Character.isLetter(letter)) {
            return new GuessResult(false, false, "Please enter a single letter.");
        }
        if (guessedLetters.contains(letter) || wrongLetters.contains(letter)) {
            return new GuessResult(false, false, "You already guessed '" + letter + "'.");
        }
        if (isGameOver()) {
            return new GuessResult(false, false, "Game is already over.");
        }

        if (secretWord.indexOf(letter) >= 0) {
            guessedLetters.add(letter);
            return new GuessResult(true, true, "Correct! '" + letter + "' is in the word.");
        } else {
            wrongLetters.add(letter);
            remainingAttempts--;
            return new GuessResult(true, false, "Wrong! '" + letter + "' is not in the word.");
        }
    }

    public boolean isGameOver() {
        return remainingAttempts <= 0 || isWordFullyGuessed();
    }

    public boolean isWon() {
        return isWordFullyGuessed();
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public Set<Character> getWrongLetters() {
        return wrongLetters;
    }

    public String getSecretWord() {
        return secretWord;
    }
}