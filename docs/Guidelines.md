# Project Guideline: "ManhwaDex Lite" — Personal Manhwa/Webtoon Tracker

## 1. Overview

Build a single-user Java desktop application for tracking a personal manhwa
(Korean webtoon) reading list. Users add titles, classify them as
wishlist / ongoing / completed, rate each title on a fixed set of aspects
(plot, art, uniqueness, characters, pacing), and optionally track reading
progress (current / total chapter). A one-time onboarding step lets the user
rank the importance of each aspect; the app then computes a weighted overall
score per title.

The app is command-driven (text in, text out) with a Swing GUI wrapper around
a pure CLI core, and persists everything to a pipe-delimited text file.

### Constraints
- Pure Java 25, Gradle build with the Shadow plugin for a fat JAR.
- No external dependencies except JUnit 5 (test) and the JDK's bundled Swing (GUI).
- Single user, single machine, local file persistence.
- CLI core must be fully functional and scriptable via `text-ui-test`.
- Timeline: 1–2 weeks. Do NOT over-engineer. If a feature is not in this spec, do not add it.

## 2. Commands (complete spec)

The fixed aspect set is: `plot`, `art`, `uniqueness`, `characters`, `pacing`.

| Command | Format | Behavior |
|---|---|---|
| `onboard` | `onboard` | Starts onboarding flow (§4): prompts for importance 1–5 per aspect. Creates/overwrites the preference profile. |
| `add` | `add <title>` | Starts add flow: asks `wishlist / ongoing / completed`. If ongoing/completed, prompts for a 1–10 rating per aspect. Title is case-insensitive unique. |
| `list` | `list` / `list <status>` | Lists all entries, or only those with the given status. Shows index, status, title, tags, chapters, overall score. |
| `find` | `find <keyword>` | Case-insensitive substring match on title. |
| `filter` | `filter <tag>` | Lists entries carrying the given tag. |
| `sort` | `sort <score\|title\|date\|chapters\|plot\|art\|uniqueness\|characters\|pacing>` | Sorts the displayed list. Score/date/chapters/aspect = descending; title = alphabetical. Unrated entries sort last for score/aspect sorts. |
| `delete` | `delete <index>` | Removes the entry at the 1-based index. |
| `status` | `status <index> <wishlist\|ongoing\|completed>` | Changes status. When moving to ongoing/completed, prompts for any missing aspect ratings (reuses the add-flow state machine, §4). |
| `rate` | `rate <index> <aspect> <1-10>` | Sets/updates one aspect rating. |
| `chapter` | `chapter <index>` / `chapter <index> <n>` / `chapter <index> <n> /of <m>` | Shows progress; sets current chapter; or sets current and total. Validation: `n ≥ 1`, `m ≥ 0`, and `n ≤ m` when `m > 0`. |
| `tag` | `tag <index> <tag>` / `untag <index> <tag>` | Adds / removes a single-word tag (e.g., `action`, `romance`, `regression`, `murim`). |
| `note` | `note <index> <text>` / `note <index>` / `note <index> clear` | Sets, shows, or clears the free-text note. Note may contain spaces but not `\|`. |
| `stats` | `stats` | Prints aggregates (§6). |
| `rerank` | `rerank` | Re-runs the onboarding flow, overwriting the preference profile. |
| `help` | `help` | Lists all commands with formats. |
| `bye` | `bye` | Exits. |
| `cancel` | `cancel` | Cancels any in-progress interactive flow (§4). |

### Validation & error rules
- Unknown command → "Unknown command. Type `help` to see available commands."
- Invalid argument counts/formats → message showing the exact expected format.
- Index out of range → "Entry <index> does not exist."
- Duplicate title → "A title like '<title>' already exists." (case-insensitive).
- Ratings must be integers 1–10; importance must be integers 1–5; chapter values as specified above.
- `|` is forbidden inside title, tag, and note text (reject or strip it).
- All user-facing errors are thrown as a single custom exception type `ManhwaTrackerException`.
- Reject empty inputs politely rather than crashing.

## 3. Domain model

```
Aspect (enum): PLOT, ART, UNIQUENESS, CHARACTERS, PACING
Status (enum): WISHLIST, ONGOING, COMPLETED

Manhwa:
  - String title            (unique, case-insensitive key)
  - Status status
  - LocalDate dateAdded     (today, used by `sort date`)
  - Map<Aspect, Integer> ratings    (1–10; absent = unrated)
  - int currentChapter      (0 = none read)
  - int totalChapter        (0 = unknown)
  - List<String> tags       (single-word)
  - String note             (nullable)
  - double getOverallScore(PreferenceProfile p):
        Σ (weight × rating) / Σ weight  over rated aspects only,
        rounded to 1 decimal. Returns -1 if no ratings (displayed as "—").
  - String getChapterDisplay():  e.g. "ch. 143/179", "ch. 12", "—"

PreferenceProfile:
  - Map<Aspect, Integer> weights  (1–5 importance from onboarding)
  - Persisted; exists after first onboarding.

ManhwaList:
  - add, delete(index), get(index), size, contains(title, case-insensitive)
  - filteredView / sortedView helpers returning new lists (never mutate the original)
```

Weighted-score example: weights {plot:5, art:4}, ratings {plot:8, art:10} →
(5×8 + 4×10) / (5+4) = 80/9 ≈ 8.9/10.

## 4. Conversation state machine (multi-turn flows)

The GUI and CLI both feed one line at a time into the controller — the controller
`getResponse(String)` returns the reply for each line, so both frontends are
identical from the controller's point of view. Interactive flows (onboarding,
add, status-change prompts) require the controller to remember what it is waiting
for between lines.

- `ConversationState (enum)`: `IDLE`, `AWAITING_STATUS`, `AWAITING_RATINGS`,
  `AWAITING_IMPORTANCE`.
- The controller holds: current state, the pending `Manhwa` under construction,
  the aspect index currently being prompted, and (for `rerank`) a pending
  `PreferenceProfile`.
- `getResponse(input)`: if state is `IDLE`, parse `input` as a command; otherwise
  treat `input` as the answer to the currently pending question.
- Prompts must show context, e.g.
  `Rate plot (1-10) for "Solo Leveling":` and `Importance of uniqueness (1-5):`.
- `cancel` from any non-IDLE state aborts the flow, discards the pending object,
  and reports what was discarded.

## 5. Storage format (pipe-delimited)

One file `data/manhwalist.txt`; directory auto-created on startup.

```
PREF | plot=5 | art=4 | uniqueness=5 | characters=3 | pacing=2
MANHWA | Solo Leveling | ONGOING | action,fantasy | plot=9;art=10;uniqueness=8;characters=7;pacing=8 | 143/179 | The art carries the story
MANHWA | Tower of God | WISHLIST | action |  | 0/0 | Heard good things
```

- `PREF` line stores the profile (exactly one line, present after onboarding).
- `MANHWA` fields: `type | title | status | tags(comma-separated) | ratings(semicolon-separated key=value) | current/total chapters | note | dateAdded`.
- Ratings, chapters (`0/0`), and note may be empty / default.
- On load: parse line by line; a corrupt line is skipped with a warning printed to
  stderr — never crash the app. A missing `PREF` line is handled gracefully (no
  profile until the user runs `onboard`).
- Save: full rewrite of the file after every mutating command (simple and reliable).

## 6. `stats` output (exact)

- Total entries; counts per status (wishlist/ongoing/completed).
- Average overall score of rated entries.
- Average rating per aspect (over entries that rated it).
- Total chapters read (sum of all `currentChapter` values).
- Top 3 entries by overall score.
- Top 3 most-used tags.
- Your top 3 priority aspects (from the preference profile).

## 7. Architecture (self-contained description)

Package layout — package `manhwa`:

```
src/main/java/manhwa/
  ManhwaTracker.java        controller: getResponse(String), conversation state, wiring
  CliMain.java              CLI entry point: read line -> getResponse -> print
  Ui.java                   interface: showWelcome, showMessage, readLine
  CliUi.java                System.out / Scanner implementation of Ui
  SwingMain.java            Swing entry point: launches MainWindow
  MainWindow.java           Swing chat window (JTextArea log + JTextField input)
  SwingUi.java              adapter implementing Ui via MainWindow callbacks
  ManhwaTrackerException.java
  Aspect.java, Status.java, ConversationState.java
  Manhwa.java, PreferenceProfile.java, ManhwaList.java
  Parser.java               static parseCommand(String) -> Command (switch on first word)
  Storage.java              createDirAndFile, saveData, loadData
  commands/
    Command.java            abstract base class
    AddCommand, DeleteCommand, ListCommand, FindCommand, FilterCommand, SortCommand,
    RateCommand, StatusCommand, ChapterCommand, TagCommand, UntagCommand, NoteCommand,
    StatsCommand, OnboardCommand, HelpCommand, ByeCommand, CancelCommand
```

### 7.1 Command pattern

Every user action is an object. Each command subclass declares its own
`COMMAND_WORD` constant and implements `execute`.

```java
public abstract class Command {
    private final boolean isExit;

    protected Command() { this(false); }
    protected Command(boolean isExit) { this.isExit = isExit; }
    public boolean isExit() { return isExit; }

    /** Executes this command against the app's data. Returns the message to show the user. */
    public abstract String execute(ManhwaList list, PreferenceProfile profile,
            Storage storage, ManhwaTracker controller) throws ManhwaTrackerException;
}
```

Commands that start a multi-turn flow (add, onboard, status-change) do not finish
their work in `execute`; instead they stash a partially-built object on the
controller, set `controller.setState(...)`, and return the first prompt message.

### 7.2 Parser — validation only, no logic

The Parser's only job is to turn a command string into a `Command` object or
throw `ManhwaTrackerException` with a helpful message. It NEVER touches data.

```java
public class Parser {
    public static Command parseCommand(String input) throws ManhwaTrackerException {
        String word = getFirstWord(input); // split(" ")[0]
        switch (word) {
        case AddCommand.COMMAND_WORD:      return parseAddCommand(input);
        case ListCommand.COMMAND_WORD:     return parseListCommand(input);
        case OnboardCommand.COMMAND_WORD:  return parseOnboardCommand(input);
        // ... one case per command word ...
        default:
            throw new ManhwaTrackerException(
                "Unknown command. Type `help` to see available commands.");
        }
    }
}
```

### 7.3 Controller and state machine

```java
public class ManhwaTracker {
    private ConversationState state = ConversationState.IDLE;
    private Manhwa pendingManhwa;         // built during `add` / `status` flows
    private PreferenceProfile pendingProfile; // built during `onboard` / `rerank`
    private int nextAspectIndex;          // which aspect we are prompting for

    public String getResponse(String input) {
        if (state == ConversationState.IDLE) {
            try {
                Command c = Parser.parseCommand(input);
                return c.execute(manhwaList, profile, storage, this);
            } catch (ManhwaTrackerException e) {
                return e.getMessage();
            }
        }
        return handleConversationInput(input); // answers the pending question
    }
}
```

Rules:
- The controller is the ONLY class that knows about `ConversationState`.
- GUI and CLI share 100% of the core. The only UI-specific classes are
  `CliUi`, `MainWindow`, and `SwingUi` — swapping `CliUi` for `SwingUi`
  changes nothing else.

### 7.4 Ui abstraction

```java
public interface Ui {
    void showWelcome();
    void showMessage(String message);
    String readLine();
}
```

- `CliUi`: prints to `System.out`, reads from `System.in` via `Scanner`.
- `SwingUi`: the Swing window receives text and pushes it into the same
  `getResponse` pipeline, appending replies to a scrollable log area.
- No UI class ever contains business logic.

### 7.5 Storage

```java
public class Storage {
    private final String filePath; // "data/manhwalist.txt"

    public void createDirAndFile() throws IOException;
    public void saveData(ManhwaList list, PreferenceProfile profile); // full rewrite
    public LoadResult loadData() throws IOException; // skips corrupt lines with a warning
}
```

- `Manhwa.toFileString()` / `Manhwa.fromFileString(String)` own the line format;
  `Storage` owns reading/writing the whole file. Keep the two concerns separate.
- After any mutating command, the controller calls `storage.saveData(...)`.

### 7.6 General quality rules
- Constants for all command words and aspect names; no magic numbers for weights/ratings.
- Javadoc on all public classes and methods; `assert` on non-null invariants.
- SLAP: short methods, one responsibility per class, no god classes.

## 8. Testing requirements

JUnit 5 (Gradle `test` task, JUnit Platform):
- `ParserTest`: valid + invalid inputs for every command; error-message content.
- `ManhwaTest`: weighted-score math, unrated → -1, rounding, duplicate-title check, chapter validation.
- `PreferenceProfileTest`: weight normalization (Σw ≠ 0, all aspects present).
- `StorageTest`: round-trip save→load equality; corrupt line skipped; missing PREF line.
- `ManhwaListTest`: index bounds, filtered/sorted views.
- `text-ui-test`: `input.txt` + `EXPECTED.TXT` + `runtest.bat`/`runtest.sh` for the
  CLI core, covering: onboard → add (ongoing) → rate → list → sort score → stats → bye.

## 9. Build setup

- Gradle with `application` + `com.github.johnrengelman.shadow` plugins (v9.7.1).
- `mainClass = manhwa.CliMain`; fat JAR named `manhwadex`.
- `run { standardInput = System.in }` so the CLI works under Gradle.
- No other production dependencies (Swing ships with the JDK).

## 10. Milestones (1–2 weeks)

1. **Milestone 1 (days 1–3):** Model classes, `ManhwaList`, `Storage`, `Parser` for
   `add`/`list`/`delete`/`find`/`bye`, `CliUi`, `ManhwaTrackerException`. No GUI,
   no ratings. Storage round-trip + parser tests green.
2. **Milestone 2 (days 4–6):** `onboard` + conversation state machine; aspect
   ratings, `rate`, `status`, `chapter`, weighted score, `sort`, `filter`,
   `tag`/`untag`.
3. **Milestone 3 (days 7–9):** `note`, `stats`, `rerank`, `help`, `cancel`;
   storage format finalized; full test suite + `text-ui-test` passing.
4. **Milestone 4 (days 10–12):** Swing GUI (`MainWindow` + `SwingUi`) wrapping the
   same controller; polish error messages; README with command reference.
5. **Done:** all commands work identically in CLI and GUI; tests pass; fat JAR runs.

## 11. Definition of done

- Every command in §2 works in both CLI and GUI modes.
- Ratings, status changes, chapters, tags, notes, and the profile survive an app restart.
- Corrupt storage lines never crash the app.
- `./gradlew test` and the text-UI test pass.
- README documents every command and the onboarding flow.