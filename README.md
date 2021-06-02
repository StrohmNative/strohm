# Strohm Native

Strohm Native is my attempt at improving the way cross-platform mobile apps are
developed. If you want to learn more about my motivation, please read my blog
post [ReactNative, Flutter, Cordova, etc: the state of the art is not good
enough.][blogpost]

This project is under active development. It is in its early stages. Even though
it is usable at the moment, I would not yet recommend doing that for commercial
app development. I keep a more detailed [status overview below](#status). As you
can see there, one notable item that is not yet done is the documentation. This
includes everything below, which should be considered my personal notes that are
to be expanded into proper documentation.

## Prerequisites

When using Strohm, you or your team needs to be able to:

* Develop native iOS apps (using Swift typically),
* Develop native Android apps (using Kotlin typically), and
* Use Clojure for the common code.

Please note that that this is only two big platforms that you have to
understand, instead of the three that you need for popular cross-platform
frameworks like ReactNative.

Clojure is just the language that I happen to use most at the moment. In
principle, any language that compiles to JavaScript can be used instead. The
common part of Strohm of course has to be rewritten in that language in that
case, but it is really not a lot of code.

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
Also a REPL is started that allows you to start developing in your IDE of
choice. Instead of running `yarn watch`, some Clojure IDEs support starting
everything from inside the IDE. I use the [Calva][calva] VSCode plugin for that.

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
| Load web view                   | [X] |   [X]   |        |
| Call native -> CLJS             | [X] |   [X]   |        |
| Call CLJS -> native             | [X] |   [X]   |        |
| Dispatch from native            | [X] |   [X]   |        |
| Subscribe + receive updates     | [X] |   [X]   |        |
| Simulator, debug                | [X] |   [X]   |        |
| Simulator, release              | [X] |   [X]   |        |
| Device, debug                   | [X] |   [X]   |        |
| Device, release                 | [X] |   [X]   |        |
| Simple example project          | [X] |   [X]   |        |
| View binding                    | [X] |   [X]   |        |
| Complex store structure         | [X] |   [X]   |        |
| Support middleware              |     |         |  […]   |
| Support easy state persistence  | [ ] |   [ ]   |  [ ]   |
| Test using NPM dependencies     | [ ] |   [ ]   |        |
| Developer Experience            | [ ] |   [ ]   |  [ ]   |
| Setup CI incl example app tests | [ ] |   [ ]   |        |
| Setup CD of native libs         | [ ] |   [ ]   |        |
| Setup CD of CLJS lib            |     |         |  [ ]   |
| Documentation                   |     |         |  [ ]   |
| Starter project?                |     |         |  [ ]   |
| Complex example project         | [ ] |   [ ]   |        |
| Branding, marketing, web site   |     |         |  [ ]   |

Note: […] means that it is in progress.

[shadow-cljs]: https://shadow-cljs.github.io/docs/UsersGuide.html
[blogpost]: https://unfolded.dev/posts-output/2021-05-08-mobile-cross-platform/
[calva]: https://calva.io
