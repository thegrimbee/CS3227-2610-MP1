- Wrote docs/UserGuide.md covering setup, GUI/CLI use, every command, validation rules, scoring, persistence, testing, troubleshooting, and known limitations.
- Wrote docs/DeveloperGuide.md covering architecture, conversation state machine, command pattern, domain model, storage format, error handling, testing strategy, development process, extension guidance, limitations, and acknowledgements.
Documented implementation-specific details such as:- command-word case sensitivity;
- permanent versus sorted/filtered display indices;
- the actual seven-field storage format;
- dateAdded not being persisted;
- GUI manual testing;
- the missing --cli argument in the current text-UI scripts.