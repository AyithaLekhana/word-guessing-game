import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WordGuessingGame {

    private static final String[] WORDS = {
            "apple", "banana", "orange", "grape", "melon",
            "cherry", "mango", "papaya", "lemon", "kiwi"
    };

    public static final int MAX_ATTEMPTS = 6;

    private final String secretWord;
    private final Set<Character> guessedLetters = new HashSet<>();
    private final Set<Character> wrongLetters = new HashSet<>();
    private int remainingAttempts = MAX_ATTEMPTS;

    public WordGuessingGame() {
        this.secretWord = pickRandomWord();
    }

    private String pickRandomWord() {
        Random random = new Random();
        return WORDS[random.nextInt(WORDS.length)].toLowerCase();
    }

    public String getMaskedWord() {
        StringBuilder sb = new StringBuilder();
        for (char c : secretWord.toCharArray()) {
            sb.append(guessedLetters.contains(c) ? Character.toUpperCase(c) : '*');
        }
        return sb.toString();
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