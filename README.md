# Core Bringer

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Enhanced Java Code Compilation

Core-Bringer includes an advanced Java code compilation system that provides IDE-like error reporting:

### Error Classification
- **SYNTAX ERROR**: Missing semicolons, brackets, parentheses, etc.
- **TYPE ERROR**: Incompatible types, undefined symbols, etc.
- **METHOD ERROR**: Method not found, incorrect parameters, etc.
- **VARIABLE ERROR**: Undefined variables, duplicate declarations, etc.
- **CLASS ERROR**: Class not found, import issues, etc.
- **RUNTIME EXCEPTION**: NullPointerException, ArrayIndexOutOfBounds, etc.

### Features
- **Line Number Reporting**: Exact line numbers where errors occur
- **Error Categories**: Color-coded error types with 🔴 indicators
- **Stack Trace Parsing**: Runtime error analysis with method call chains
- **External Compiler**: Uses `javac` and `java` for robust compilation
- **JShell Fallback**: Handles code snippets and multi-class scenarios
- **Clean Output**: OnlineGDB-style formatted results

### Example Error Output
```
🔴 SYNTAX ERROR at line 15:
   ';' expected
   
🔴 TYPE ERROR at line 22:
   incompatible types: int cannot be converted to String
   
🔴 NULL POINTER EXCEPTION:
   Exception in thread "main" java.lang.NullPointerException
   → at line 15 in RuntimeErrorTest.java
```

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Main Menu changes (Title background + Atlas buttons)

The main menu now renders a Title Card background and uses the `assets/ui/buttons_atlas.atlas` regions for the Start/Options/Exit buttons.

### Title background
- Add an Image using `backgrounds/TitleCard_bg.png` and add it to the `Stage` before other UI. Call `setFillParent(true)` so it stretches to the viewport.

### Atlas-based buttons
- `assets/ui/buttons_atlas.atlas` defines: `start_btn`, `settings_btn`, `exit_btn`.
- Create `ImageButton`s from the atlas using `new TextureRegionDrawable(atlas.findRegion("..."))`.
- Replace `TextButton` usages with these `ImageButton`s and keep the click listeners.

### Code location
- `core/src/main/java/com/altf4studios/corebringer/screens/MainMenuScreen.java` contains background and button setup.

### Exporting to PDF
- Open this section in a Markdown preview and use "Export as PDF" or your OS "Print to PDF".
