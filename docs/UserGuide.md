# ManhwaDex Lite User Guide

ManhwaDex Lite is a single-user desktop and command-line application for maintaining a
personal manhwa or webtoon reading list. It tracks reading status, five rating aspects,
weighted overall scores, chapter progress, tags, and notes. The GUI and CLI use the same
commands and the same saved data.

## 1. System requirements

- Java Development Kit (JDK) 25.
- Windows, macOS, or Linux with a terminal.
- Internet access on the first build so the Gradle Wrapper can obtain Gradle and the test
  dependencies.
- A graphical desktop environment is required only for the Swing GUI. The CLI works without
  one.

Check the installed Java version:

```text
java --version
```

The reported major version must be 25.

## 2. Setup and launch

Run all setup commands from the repository root, the directory containing `build.gradle`.
No separate Gradle installation is needed because the repository includes the Gradle Wrapper.

### Windows

Build and test the application:

```text
.\gradlew.bat clean build
```

Launch the GUI:

```text
java -jar .\build\libs\manhwadexlite.jar
```

Launch the CLI:

```text
java -jar .\build\libs\manhwadexlite.jar --cli
```

### macOS and Linux

Build and test the application:

```text
./gradlew clean build
```

Launch the GUI:

```text
java -jar ./build/libs/manhwadexlite.jar
```

Launch the CLI:

```text
java -jar ./build/libs/manhwadexlite.jar --cli
```

If `./gradlew` is not executable, run `chmod +x gradlew` once or invoke it with
`bash gradlew`.

## 3. Using the interfaces

### GUI

The GUI has a read-only conversation log and an input field. Type a command in the field and
press Enter. The input is appended to the log with a `> ` prefix, followed by the application's
reply. After `bye`, the farewell is displayed and the input field is disabled.

### CLI

Type one command per line and press Enter. The CLI prints replies but does not echo the input.
The process ends after `bye` or end-of-file.

Both interfaces read and write `data/manhwalist.txt` relative to the directory from which the
application is launched. Launch from the same directory each time to use the same list.

## 4. First launch and scoring preferences

If no saved preference profile exists, startup immediately begins onboarding. Enter an
importance from 1 to 5 for each aspect in this order:

1. `plot`
2. `art`
3. `uniqueness`
4. `characters`
5. `pacing`

Example:

```text
Importance of plot (1-5):
5
Importance of art (1-5):
4
Importance of uniqueness (1-5):
3
Importance of characters (1-5):
4
Importance of pacing (1-5):
2
```

Invalid input repeats the current question. Enter `cancel` during onboarding to discard the
unfinished profile. Use `onboard` or `rerank` later to run the same five-question flow and
replace the profile. Existing preferences are replaced only after all five valid answers have
been entered.

### Overall score

Each rated aspect is multiplied by its importance. The weighted values are divided by the sum
of the weights for the aspects that have ratings. The result is rounded to one decimal place.

For example, plot importance 5 with rating 8 and art importance 4 with rating 10 gives:

```text
(5 x 8 + 4 x 10) / (5 + 4) = 8.9
```

Unrated aspects are excluded rather than treated as zero. An entry with no ratings, or any
entry displayed before a profile exists, shows `Score: -`.

## 5. Command rules

- Command words must be lowercase, for example `list`, not `LIST`.
- Status values, aspect names, and sort keys are case-insensitive.
- Entry indices are 1-based.
- Titles are unique without regard to case. `Solo Leveling` and `solo leveling` are duplicates.
- Titles and notes cannot contain `|`.
- Tags are single words, are case-sensitive, and cannot contain whitespace or `|`.
- `find` title matching is case-insensitive.
- During an interactive prompt, `cancel` is case-insensitive.
- Commands entered while a prompt is active are treated as answers to that prompt. Finish or
  cancel the flow before entering another command.

> **Index warning:** Mutating commands use the permanent index shown by a plain `list` command.
> Results from `find`, `filter`, and `sort` are renumbered display views and do not change the
> stored order. Run `list` before `delete`, `status`, `rate`, `chapter`, `tag`, `untag`, or
> `note` if there is any doubt about an entry's permanent index.

## 6. Command reference

### `help`

Shows the formats of all implemented commands.

```text
help
```

### `onboard` and `rerank`

Starts the five-question importance flow. Both commands currently have the same behavior and
replace the profile after all answers are valid.

```text
onboard
rerank
```

Each importance must be an integer from 1 to 5.

### `add`

Starts an interactive flow for a new title.

```text
add <title>
```

Example:

```text
add Solo Leveling
```

The application asks whether the title is `wishlist`, `ongoing`, or `completed`.

- `wishlist` saves the entry immediately without ratings.
- `ongoing` and `completed` ask for a rating from 1 to 10 for plot, art, uniqueness,
  characters, and pacing, then save the entry.
- `cancel` discards the unfinished entry.

### `list`

Shows all entries, or entries with one status.

```text
list
list <wishlist|ongoing|completed>
```

Examples:

```text
list
list ongoing
```

Each row contains a display index, status, title, optional tags, chapter progress, and overall
score. Notes and individual aspect ratings are not shown in this view.

Example row:

```text
1. [ONGOING] Solo Leveling  #action #fantasy  ch. 143/179  Score: 8.7
```

No progress is displayed as `-/-`. A known current chapter without a total is displayed as
`ch. 12`.

### `listall`

Shows the full details of every entry in permanent list order.

```text
listall
```

For each manhwa, the detailed view includes its title, status, date added, chapter progress,
tags, note, and every aspect rating. The score breakdown shows each rating's preference weight
and weighted contribution, followed by the weighted total calculation. Missing values are shown
as `-`.

### `find`

Finds titles containing a keyword or phrase, without regard to case.

```text
find <keyword>
```

Example:

```text
find leveling
```

### `filter`

Shows entries carrying an exact, case-sensitive tag.

```text
filter <tag>
```

Example:

```text
filter action
```

### `sort`

Shows a sorted view without changing or saving the permanent list order.

```text
sort <score|title|date|chapters|plot|art|uniqueness|characters|pacing>
```

The order is:

- `title`: alphabetical, ignoring case.
- All other keys: descending.
- Unrated entries appear last for an aspect sort. They also sort below rated entries for
  `score` because their internal score is negative.

`date` means the in-memory creation date. The current storage file does not persist this date;
after a restart, loaded entries receive the current date and commonly tie with one another.

### `delete`

Deletes the entry at a permanent 1-based index and saves the list.

```text
delete <index>
```

Example:

```text
delete 2
```

Deletion is immediate and has no confirmation prompt.

### `status`

Changes an entry's reading status.

```text
status <index> <wishlist|ongoing|completed>
```

Examples:

```text
status 1 completed
status 2 wishlist
```

Moving to `wishlist` is immediate and keeps existing ratings. Moving to `ongoing` or
`completed` asks only for rating aspects that are currently missing. If all aspects are already
rated, the status changes immediately. Enter `cancel` during rating prompts to keep the old
status and ratings.

### `rate`

Sets or replaces one aspect rating and saves the entry.

```text
rate <index> <plot|art|uniqueness|characters|pacing> <1-10>
```

Example:

```text
rate 1 art 10
```

Wishlist entries may be rated. Their overall score uses whichever aspects have ratings.

### `chapter`

Shows or updates chapter progress.

```text
chapter <index>
chapter <index> <current>
chapter <index> <current> /of <total>
```

Examples:

```text
chapter 1
chapter 1 143
chapter 1 143 /of 179
chapter 1 143 /of 0
```

The current chapter must be at least 1. The total must be 0 or higher; 0 means unknown. When
the total is greater than 0, the current chapter cannot exceed it. Updating only the current
chapter retains any existing total and must remain within that total.

The application does not currently provide a command to reset recorded progress to zero.

### `tag` and `untag`

Adds or removes an exact, case-sensitive single-word tag.

```text
tag <index> <tag>
untag <index> <tag>
```

Examples:

```text
tag 1 action
untag 1 action
```

Adding the same exact tag again keeps one copy. Removing a tag that is absent reports that no
such tag exists and does not change the entry.

### `note`

Shows, sets, or clears an entry's note.

```text
note <index>
note <index> <text>
note <index> clear
```

Examples:

```text
note 1
note 1 The art carries the story
note 1 clear
```

The exact lowercase text `clear` is reserved for clearing the note. Other capitalization, such
as `Clear`, is saved as note text. Notes may contain spaces but not `|`.

### `stats`

Shows:

- total entries;
- wishlist, ongoing, and completed counts;
- average overall score across rated entries;
- average rating for each aspect that has at least one rating;
- sum of all current chapter values;
- top three rated entries by overall score;
- top three exact tags by use count; and
- top three priority aspects from the preference profile.

```text
stats
```

If no rated entries exist, the output says `No rated entries yet.` If no profile exists, scores
are calculated with equal default weights for statistics, while the priority line asks the user
to run `onboard`.

### `cancel`

Cancels an active onboarding, add, or status-rating flow without saving the pending changes.

```text
cancel
```

When no flow is active, it reports `Nothing to cancel.`

### `bye`

Ends the session.

```text
bye
```

In the GUI, the window stays open to preserve the conversation log, but its input field is
disabled. Close the window normally when finished.

## 7. Data storage and backup

Data is stored as UTF-8 text at:

```text
data/manhwalist.txt
```

The `data` directory and file are created automatically in the launch working directory.
Mutating commands rewrite the full file before returning. To back up the reading list, close
the application and copy `manhwalist.txt`. Avoid editing it manually; malformed lines are
skipped on the next load and a warning is written to standard error.

Deleting `manhwalist.txt` while the application is closed resets the list and causes onboarding
to start at the next launch.

## 8. Testing the installation

### Automated unit tests

Windows:

```text
.\gradlew.bat test
```

macOS/Linux:

```text
./gradlew test
```

A successful run ends with `BUILD SUCCESSFUL`.

### Manual smoke test

Use a disposable working directory if you do not want to alter an existing list. Launch the
fat JAR with `--cli`, then perform this sequence:

1. Complete the five startup importance prompts if they appear.
2. Enter `add Solo Leveling`.
3. Answer `ongoing`.
4. Enter five valid ratings.
5. Run `list`, `sort score`, and `stats`.
6. Run `bye` and confirm the process exits.
7. Launch again from the same directory and run `list` to confirm persistence.

### Current scripted-test limitation

The repository contains `text-ui-test/runtest.bat` and `runtest.sh`, but the current scripts
invoke the GUI-default launcher without `--cli`. Until their JAR invocation is updated to add
`--cli`, use the direct CLI launch and manual smoke test above. Running the scripts unchanged
may open the GUI instead of consuming `input.txt`.

## 9. Troubleshooting

### The GUI does not appear

- Confirm that Java 25 is installed.
- Confirm that the machine has a graphical desktop environment.
- Run the CLI with `--cli` to see startup output in the terminal.

### My previous list is missing

The data path is relative to the working directory. Relaunch from the same directory used in
the earlier session and check for `data/manhwalist.txt` there.

### A command is treated as a rating or importance answer

An interactive flow is still active. Enter the requested value or enter `cancel`, then run the
command again.

### An index changed after sorting or filtering

Sorted and filtered results are display-only views. Run plain `list` and use its permanent
index for commands that modify an entry.
