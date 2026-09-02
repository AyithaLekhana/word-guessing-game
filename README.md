# Word Guess — Java Word Guessing Game

A word-guessing (hangman-style) game with a Java backend and a web frontend.
Built with plain Java (no frameworks) — the REST API uses the JDK's built-in
`HttpServer`, and the frontend is vanilla HTML/CSS/JS.

## Features

- Random word selection from a word pool
- Guess one letter at a time
- Instant feedback after every guess (correct/incorrect)
- Game state displayed with unguessed letters masked as `*`
- Tracks wrong guesses and remaining attempts
- Web UI with an on-screen + physical keyboard

## Tech stack

- **Backend:** Java (`com.sun.net.httpserver.HttpServer` — no external dependencies)
- **Frontend:** HTML, CSS, JavaScript (`fetch` API)

## Project structure

```
.
├── WordGuessingGame.java        # Core game logic (word, guesses, state)
├── WordGuessingGameServer.java  # REST API + static file server
└── public/
    └── index.html                # Frontend UI
```

## How to run

1. Compile:
   ```bash
   javac WordGuessingGame.java WordGuessingGameServer.java
   ```
2. Run (from the folder containing `public/`):
   ```bash
   java WordGuessingGameServer
   ```
3. Open **http://localhost:8080** in your browser.

## API

| Method | Endpoint      | Description                                  |
|--------|---------------|-----------------------------------------------|
| POST   | `/api/new`    | Starts a new game, returns session + state    |
| POST   | `/api/guess`  | Body: `{"sessionId": "...", "letter": "a"}`   |

## Roadmap

- [ ] Score/streak tracking across rounds
- [ ] Difficulty levels
- [ ] Word bank loaded from an external file
- [ ] Unit tests
- [ ] Live deployment

## Author

Built and actively developed by Lekhana Ayitha as a self-directed project to practice full-stack Java development, REST API design, and frontend engineering.
