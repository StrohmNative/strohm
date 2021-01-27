# Strohm Native

## Why

* Cross platform business logic
  * Write only once for both Android and iOS
* Native UI, as thin as possible
  * Best possible look and feel
  * Best possible accessibility
  * Best possible performance
  * Best possible conformity to user expectations
  * As little platform-specific knowledge needed as possible
* Zero Warnings
* Easy to tap into platform-specific features

## Note

It is important to stress that the goal of this project **is not** to make
cross-platform development as easy as possible and to remove the need for
knowing how Android and iOS app development works. Your team will still have to
program Swift and Kotlin besides Clojure, just a lot less of it while still
keeping the advantages listed above. So you still need to know about code
signing, app permissions, `adb`, etc.

## Development Environment

A full development environment consists of:

* Xcode
* Android Studio (including SDK and emulator)
* Your favorite Clojure(Script) editor (I'm using VSCode + Calva)
* NodeJS

At the moment I'm using Xcode 12.3, Android Studio 4.1.2, NodeJS 14, and Yarn
1.22.10.

## Developing Apps

### Preparation after Checkout

In the app's root folder, run:

```bash
yarn
```

This installs the required dependencies for Strohm and the build tooling.

### Common Code

The common code is written in ClojureScript. As a build tool,
[`shadow-cljs`][shadow-cljs] is used. (It's much more than just a build tool,
I'm using that term for lack of a better one.) Start the `shadow-cljs` watcher
in your app's root folder (the one with the `shadow-cljs.edn` file):

```bash
yarn watch
```

This will compile the ClojureScript source code and enable hot code reloading.

### Android

Open the root's subfolder `android` in Android Studio.

For Android, you need to configure reverse port forwarding. While your device
(emulator or physical) is running, run the following commands:

```bash
adb reverse tcp:8080 tcp:8080
adb reverse tcp:9630 tcp:9630
```

If you have multiple devices connected (e.g. both an emulator and a physical
device), you need to tell `adb` which one you want to use. For example, for the
emulator (run `adb devices` to get a list of connected device identifiersd):

```bash
adb -s emulator-5554 reverse tcp:8080 tcp:8080
adb -s emulator-5554 reverse tcp:9630 tcp:9630
```

You can now run your project by hitting the run-button in Android Studio.
`shadow-cljs` takes care of starting a REPL into your running project that you
can connect to from your Clojure editor.

### iOS

Open the Xcode project in the `ios` subfolder. (Tip: use `xed .` on the command
line to open it in Xcode.) Run the app as you would normally do. `shadow-cljs`
takes care of starting a REPL into your running project that you can connect to
from your Clojure editor.

## Status

This project is work in progress. You probably should not use this yet.

|                                 | iOS | Android | Common |
| :------------------------------ | :-: | :-----: | :----: |
| Load web view                   | [X] | [X]     |        |
| Call native -> CLJS             | [X] | [X]     |        |
| Call CLJS -> native             | [X] | [X]     |        |
| Dispatch from native            | [X] | [X]     |        |
| Subscribe + receive updates     | [X] | [X]     |        |
| Simulator, debug                | [X] | [X]     |        |
| Simulator, release              | [X] | [X]     |        |
| Device, debug                   | [X] | [X]     |        |
| Device, release                 | [X] | [X]     |        |
| View binding                    | [ ] | [ ]     |        |
| Complex store structure         | [ ] | [ ]     |        |
| Test using NPM dependencies     | [ ] | [ ]     |        |
| Setup CI incl example app tests | [ ] | [ ]     |        |
| Setup CD of native libs         | [ ] | [ ]     |        |
| Setup CD of CLJS lib            |     |         | [ ]    |
| Documentation                   |     |         | [ ]    |
| Starter project                 |     |         | [ ]    |
| Complex example project         | [ ] | [ ]     |        |

[shadow-cljs]: https://shadow-cljs.github.io/docs/UsersGuide.html
