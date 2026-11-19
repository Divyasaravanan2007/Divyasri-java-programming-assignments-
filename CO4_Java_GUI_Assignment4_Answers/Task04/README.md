# Task 4 – Event-Handling Forensics (Draw Pad)

Files
- `DrawPad.java` – repaired program: clicks are counted, the label updates, and the dots are stored and redrawn in `paint()`.
- `Prevention/DrawPadMisspelled.java` – the misspelled method with `@Override`, used to show the compiler error.
- `compiler_message.txt` – the compiler output for that file.

Compile and run
```bash
cd Task04
javac DrawPad.java
java DrawPad

cd Prevention
javac DrawPadMisspelled.java      # expected to fail with the message in ../compiler_message.txt
```
Screenshots: `screenshots/T4_1_before_minimise.png` and `screenshots/T4_2_after_minimise_and_restore.png` (generated in a test environment; retake on your own machine if required).
