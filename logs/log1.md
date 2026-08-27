Chat Summary
This conversation went through four phases:

1. Project Analysis + App Idea Brainstorm
You asked me to analyze your Kyrie project (a CS2103T Duke-style Java task manager) and suggest personal utility apps at the same feature level or higher, with variety. After reading the codebase (Kyrie.java, Parser.java, Storage.java, Task.java, TaskList.java, DateTime.java, build.gradle), I identified the feature profile: text-command CRUD app, inheritance model (Todo/Deadline/Event), search, tagging, date/time parsing, file persistence, JavaFX GUI, JUnit tests, Gradle/shadowJar packaging.

After clarifying questions (you chose: future course project, Java desktop/CLI, "slightly more" advanced, top 5 + a TCG one), I delivered:

5 utility apps: Coin (budget tracker), Streak (habit tracker), Flash (flashcards), Shelf (media library), Pantry (meal planner)
Bonus: CardVault (TCG collection manager)
2. First Guideline Draft
You picked a Shelf-like app specifically for manga/manhwa with onboarding (rank aspect importance: plot, art, uniqueness, etc.), an add flow with wishlist/ongoing/completed status and aspect ratings, plus my suggested commands. You asked for a Codex-ready guideline following SE principles like Kyrie.

After clarifying questions (Swing over JavaFX — no dependencies vs. JavaFX's platform-classifier pain; 1–10 ratings; fixed aspects; all six extra commands: stats, filter/sort, rate-updates, tags, notes, re-ranking; pipe-delimited storage; 1–2 weeks), I produced a full guideline for "MangaDex Lite": commands table, domain model, conversation state machine, storage format, architecture mirroring Kyrie, testing, build setup, and milestones.

3. Guideline Revision
You noted Codex wouldn't have access to Kyrie, so all Kyrie references had to be removed and the architecture described self-contained within the guideline. You also wanted the focus shifted to manhwa specifically.

After clarifying questions (rename classes to Manhwa/ManhwaList; app name ManhwaDex Lite; add chapter tracking; include code sketches in the architecture section), I rewrote the guideline with:

Zero Kyrie/Duke references — full self-contained architecture with code sketches (Command pattern, Parser, controller/state machine, Ui abstraction, Storage)
Manhwa-focused naming and examples (Solo Leveling, Tower of God)
A new chapter command with currentChapter/totalChapter tracking, including chapter stats and sorting
Final State
The deliverable is a complete, self-contained, Codex-ready guideline for ManhwaDex Lite — a 1–2 week, single-user Java 25 + Swing + JUnit 5 command-driven manhwa tracker with onboarding-driven weighted aspect ratings, chapter tracking, tags/notes, stats, and pipe-delimited persistence, built on the same architecture patterns as your Kyrie project (Command pattern, Parser→Command dispatch, tolerant Storage, Ui abstraction, CLI-first with a GUI wrapper).