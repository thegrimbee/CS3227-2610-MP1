For each of these, I had a starting prompt as follows:
```
You are implementing ONE feature of an existing Java project: ManhwaDex Lite, a
command-line manhwa/reading-list tracker. You have access to the project's current
source code and to `guidelines.md` (the full project specification). Read the
relevant sections of `guidelines.md` (domain model, conversation state machine,
storage format, architecture, testing) and follow them exactly.

RULES:
- Implement the feature described in the block below. Do not refactor unrelated code.
- Create or modify ONLY the files listed in the block. If you believe another file
  must change, say so in your summary instead of changing it.
- Follow the existing code style: package `manhwa`, Javadoc on all public
  classes/methods, `assert` on non-null invariants, short single-responsibility
  methods, constants for command words and aspect names, no magic numbers.
- Error handling: throw `ManhwaTrackerException` with the exact message text given
  in the block. Case-insensitivity applies wherever stated.
- Include JUnit 5 tests for this feature in the same change, under
  `src/test/java/manhwa/`. Existing tests must still pass.
- Persistence: any command that mutates data must call `storage.saveData(...)`
  before returning.
- When done, reply with: (1) every file created/modified with a one-line reason;
  (2) the exact commands to run to verify (build, tests, manual CLI smoke test);
  (3) any deviations from `guidelines.md` and why.
```

Then, I followed the prompt with a feature description, which included the files to create/modify, the exact behavior to implement, and the expected tests. I implemented each feature in order, following the rules above, and summarized the results below. For example:
```
FEATURE: UI abstraction (guidelines.md §7.4).
- `Ui` interface: `void showWelcome()`, `void showMessage(String)`, `String readLine()`.
- `CliUi implements Ui`: constructor `CliUi(Scanner scanner, PrintStream out)` so
  tests can inject a StringReader-backed Scanner and a ByteArrayOutputStream.
  showWelcome prints "Welcome to ManhwaDex Lite! Type `help` to see available commands."
  readLine() delegates to scanner.nextLine(). No business logic in this class.
- Do not create the GUI yet.

FILES: src/main/java/manhwa/Ui.java, src/main/java/manhwa/CliUi.java
TESTS: CliUiTest — welcome text is printed, showMessage output appears, readLine
       returns the injected input line.
```

Chronological summary
1. Core enums and exception
You requested:
- ManhwaTrackerException
- Aspect
- Status
- Tests for parsing, case-insensitivity, and exact errors
Implemented the three production types plus AspectTest and StatusTest. Six tests passed.
2. Core model classes
You requested:
- Manhwa
- PreferenceProfile
- Ratings, chapters, tags, notes, scoring, and serialization
- Corresponding tests
Implemented both models and their tests. The weighted-score example produced 8.9, validation messages were covered, and serialization round trips passed.
One specification conflict was noted: the feature explicitly required seven MANHWA fields, so dateAdded was not serialized despite the broader guideline mentioning it.
3. Collection and sorting
You requested:
- SortKey
- ManhwaList
- Duplicate detection, indexing, searching, filtering, and all sorts
- ManhwaListTest
Implemented the collection and sort behavior. Views return new lists and preserve original order. All 36 tests existing at that point passed.
4. UI abstraction
You requested:
- Ui
- Injectable CliUi
- CliUiTest
Implemented the UI interface and console adapter using injected Scanner and PrintStream. Tests covered welcome output, arbitrary messages, and input reading. All 39 tests passed.
5. CliMain integration
You then asked for CliMain to use CliUi.
CliMain was changed to:
- Construct CliUi
- Use showWelcome()
- Read through readLine()
- Print through showMessage()
- Preserve blank input, EOF, unknown-command, and bye behavior
You subsequently asked to move UI logic such as the goodbye message out of CliMain. The goodbye and unknown-command presentation strings were moved into CliUi, while command-flow decisions remained in CliMain to avoid placing business logic in the UI class.
All 39 tests still passed.
6. File persistence
You requested:
- Storage
- A small load-result type
- StorageTest
Implemented:
- Directory and file creation
- Full-rewrite saving
- Optional PREF serialization
- Per-line corruption handling with System.err warnings
- Missing-file and missing-profile behavior
- LoadResult
All 44 tests passed.
7. Command architecture and runnable CLI
You requested:
- Abstract Command
- ByeCommand
- Minimal Parser
- Minimal ManhwaTracker
- Fully initialized CliMain
- Parser and controller tests
Implemented:
- Exit-aware command hierarchy
- bye parsing and execution
- Blank-input handling
- Unknown-command propagation
- Controller exit state
- Startup storage loading
- CLI loop through CliUi
- Startup IOException handling
build.gradle already specified manhwa.CliMain, so it did not require modification.
All 49 tests passed. The CLI smoke test confirmed blank, unknown, and bye responses.
8. List, delete, and find commands
You requested:
- ListCommand
- DeleteCommand
- FindCommand
- Parser wiring
- Shared entry formatting
- Parser, delete, and find tests
Implemented:
- Full and status-filtered listing
- Shared numbered entry formatting
- Tag, chapter, and score display
- Null/unrated score display as —
- Case-insensitive keyword search
- Persistent deletion
- Required success and error messages
- Parser validation for all three formats