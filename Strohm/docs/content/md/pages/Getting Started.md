{:title "Getting Started"
 :layout :page
 :page-index 0
 :navbar? true
 :toc true}

# Introduction

This *Getting Started* guide explains how to set up a new Strohm Native project.
To make things more explicit and easier to talk about, let's say the app we're
going to setup is *Yawn: Yet Another Weather Nicety*.

Note: Whenever I mention the *root folder*, I mean the folder that contains your
project. The one in which you do `git init`. For example, it could be
`~/Documents/Yawn`. Unless stated otherwise, all paths are relative to that root
folder. So given that project location, the path `/src` is in fact
`~/Documents/Yawn/src`. Also, my examples assume that you're using a Unix/macOS
like operating system. I'm assuming experienced Windows users will be able to
read the examples and do the corresponding actions on Windows themselves.

Strap in, here we go.

# Prerequisites

When using Strohm, you or your team needs to be able to:

* Develop native iOS apps (using [Swift][swift] typically),
* Develop native Android apps (using [Kotlin][kotlin] typically), and
* Use [ClojureScript][clojurescript] for the common code[^clojurescript].

Please note that that this is only two big platforms that you have to
understand, instead of the three that you need for popular cross-platform
frameworks like [React Native][reactnative].

# Development Environment

A full development environment consists of:

* [Xcode][xcode]
* [Android Studio][androidStudio] (including SDK and emulator)
* Your favorite Clojure(Script) editor (I'm using [VSCode][vscode] +
  [Calva][calva])
* [NodeJS][nodejs]

At the moment I'm using Xcode 12.5, Android Studio 2020.3, NodeJS 14, and Yarn
1.22.10.

# Setup a New Project

Setting up your project breaks down into three parts: setup the common
ClojureScript side, setup the Android project, and setup the iOS project.

But first of course we create the root folder of our project.

```bash
mkdir ~/Documents/Yawn
cd ~/Documents/Yawn
git init
```

# Common Code

The common (ClojureScript) code is built and developed using
[`shadow-cljs`][shadow-cljs]. We're going to setup a pretty straight-forward
`shadow-cljs` project. Hence we need a `package.json`:

```json
{
  "name": "Yawn",
  "version": "1.0.0",
  "description": "Yet Another Weather Nicety is a less than exciting weather app",
  "main": "index.js",
  "devDependencies": {
    "shadow-cljs": "^2.15.5"
  },
  "scripts": {
    "watch": "shadow-cljs watch :app :test",
    "release": "shadow-cljs release :app"
  }
}
```

Install:

```bash
yarn
```

For our ClojureScript code, `shadow-cljs` needs a build config file as well,
named `shadow-cljs.edn`:

```clojure
{:source-paths ["src" "test"]

 :dependencies
 [[dev.strohmnative/strohm-native "0.1.0-SNAPSHOT"]] ;; replace with latest version

 :dev-http {8080  "target/"}

 :builds
 {:app {:output-dir "target/"
        :target :browser
        :asset-path "."
        :modules {:main {:entries [app.main]
                         :init-fn app.main/main!}}}
   :test {:target :node-test
          :output-to "target/node-test.js"
          :ns-regexp "-test$"
          :autorun true
          :compiler-options {:reader-features #{:test}}}}}
```

Create the main source code file `/src/app/main.cljs` and put this in it:

```clojure
(ns app.main
  (:require [strohm-native.flow :refer [create-store]]))

;; Reducer doesn't do anything yet, simply returns state
(defn reducer [state _action] state)

(defn ^:export main! []
  (tap> "Hello world!")
  (create-store reducer :initial-state {:greeting "Hello world!"}))

(defn ^:dev/after-load reload! []
  (tap> "reloaded"))
```

That code is pretty much the minimum you need: create a store with some initial
state, and create the root reducer. The reducer doesn't do anything yet, we'll
get to that later.

We'll also create a HTML file so that we can easily load the compiled JS into a
browser. In `/target/index.html`, put this HTML source:

```html
<!DOCTYPE html>
<html>
  <head>
    <script src="main.js"></script>
  </head>
  <body>
    Yawn!
  </body>
</html>
```

This puts all the basics in place. We can now run `shadow-cljs`:

```bash
$ yarn shadow-cljs watch :app
shadow-cljs - config: [path]/Documents/Yawn/shadow-cljs.edn
shadow-cljs - HTTP server available at http://localhost:8080
shadow-cljs - server version: 2.15.5 running at http://localhost:9630
shadow-cljs - nREPL server started on port 56827
shadow-cljs - watching build :app
[:app] Configuring build.
[:app] Compiling ...
[:app] Build completed. (119 files, 0 compiled, 0 warnings, 2,30s)
```

When you now browse to `http://localhost:9630` you should see the `shadow-cljs`
dashboard. There should be one active HTTP server on `http://localhost:8080`.
When you click that link, a new browser tab opens that says "Yawn!". Going back
to the `shadow-cljs` dashboard, in the "Inspect Stream" tab you should see the
text "Hello world!" that was tapped in `main.cljs`.

## Hot Reloading

`shadow-cljs` comes with hot reloading out of the box. So for the final touch in
this section, add the following function to your `main.cljs`:

```clojure
(defn ^:dev/after-load reload! []
  (tap> "reloaded"))
```

Now, while having the tab with `http://localhost:8080` open, save the file. You
should now see "reloaded" appear in the `shadow-cljs` dashboard's Tap History
(Inspect Stream). In other words: `shadow-cljs` has hot reloaded the code and it
has called the hook `reload!` afterwards.

<div style="text-align: center; font-size: 3em;" title="Tada!">🎉</div>

# Android

## Create Project

Using Android Studio, create a new Android project in the folder `/android`.
Make sure to run the empty project, because this is a good time to fix potential
issues around Java versions and the like.

## Add Dependency

In your app's `build.gradle` file, add the Strohm Native for Android dependency:

```gradle
ext {
    strohmNativeVersion = '0.1.0-SNAPSHOT' // replace with latest version
}

dependencies {
    [...]
    debugImplementation "dev.strohmnative:strohm-native-android-debug:$strohmNativeVersion"
    releaseImplementation "dev.strohmnative:strohm-native-android:$strohmNativeVersion"
}
```

Pick the latest released version of course.

## Setup Strohm Native

A good way of setting up Strohm Native for Android, is to put it in your own
`Application` subclass. For this to work, you create the `YawnApplication` class:

```kotlin
package dev.strohmnative.examples.yawn

import android.app.Application
import dev.strohmnative.StrohmNative

class YawnApplication: Application() {
    lateinit var strohmNative: StrohmNative

    override fun onCreate() {
        super.onCreate()
        strohmNative = StrohmNative.getInstance(applicationContext)
    }
}
```

Then you need to tell Android to use an instance of this class by configuring
your application in `AndroidManifest.xml`. Add the attribute `android:name` like
so:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="dev.strohmnative.examples.yawn">

    <application
        android:name="dev.strohmnative.examples.yawn.YawnApplication"
        ... >
    </application>
</manifest>
```

## Network Security Config

During development, the app needs to be able to connect to `localhost`, namely
to communicate with shadow-cljs and the REPL. By default, this is not allowed,
so you have to configure this. In `res/xml`, add the file
`network_security_config.xml` with the following contents:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">localhost</domain>
    </domain-config>
</network-security-config>
```

Then refer to this file from your `AndroidManifest.xml` by adding the
`android:networkSecurityConfig` attribute:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="dev.strohmnative.examples.yawn">

    <application
        android:name="dev.strohmnative.examples.yawn.YawnApplication"
        android:networkSecurityConfig="@xml/network_security_config"
        ... >
    </application>
</manifest>
```

Please note that you can limit this to your Debug configuration if desired.

## Running

Your app needs to be able to contact the `shadow-cljs` server and vice versa, so
we need to configure ADB reverse port forwarding. Since we'll be doing that many
times, let's add a script to our `package.json`:

```json
{
  ...
  "scripts": {
    ...
    "adb-reverse": "adb reverse tcp:8080 tcp:8080 && adb reverse tcp:9630 tcp:9630"
  }
}
```

Now you can run your app:

1. Make sure shadow-cljs is running the `:app` build, either by running `yarn
   watch` or by jacking-in from your favorite Clojure editor.
2. Make sure the Android simulator is running or your device is connected, and
   run `yarn adb-reverse` to activate reverse port forwarding.
3. Run the app from Android Studio.

If everything is well, you should see a log message in Android Studio's "Run"
tab where shadow-cljs says that it is ready.

# iOS

## Create Project

Create a new project in Xcode, using SwiftUI and Swift.

## Add Dependency

Then add Stohm Native as a Swift package dependency in the project settings, by
pointing Xcode to `https://github.com/StrohmNative/strohm-native`.

## Setup Strohm Native

Now open the Swift file containing your `App` instance, `YawnApp` in this
example. It is the file that contains the `@main` annotation. Add an import of
`StrohmNative` and add a reference to `StrohmNative.default`, like this:

```swift
import SwiftUI
import StrohmNative

@main
struct YawnApp: App {
    let strohmNative = StrohmNative.default

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

## Allow Local Networking

Just like on Android, the app needs to be able to use local networking during
development. In your app's info properties, add the section "App Transport
Security Settings", and inside of it the setting "Allows Local Networking". Set
the value to `YES`.

![Info plist](/img/info_plist.png "Info property list")

Note that you can limit this to your Debug configuration if desired, by setting
the value here to a custom variable, e.g. `$(ALLOW_LOCAL_NETWORKING)`. In your
build settings you can then set the value of that variable to `YES` and `NO`
depending on the build configuration.

## Add Compiled JavaScript

In order for Xcode to include the compiled JavaScript in your project, you need
to add it manually. Just drag-and-drop the file `target/main.js` into your
project and make sure the checkbox indicating the file needs to be included in
the target is checked. Also, don't allow Xcode to copy the file, just add a
reference.

## Running on Simulator

You can now run your app on a simulator. Just like on Android, shadow-cljs will
report to the JavaScript console that it is ready. Unfortunately, you cannot see
this as you can only open the console (using Safari's develop menu) _after_ this
line has been logged, so it has been lost. Rest assured though: when you don't
see any errors in Xcode's debug area, everything should be fine.

In shadow-cljs' [Inspect Stream](http://localhost:9630/inspect) you should now
see the text "Hello world!" that was tapped in the clojure code.

## Running on Device

When running on device, a bit more setup is needed, because your app needs to
know the hostname of your development machine, so that it can connect to
shadow-cljs. The following simple "Run Script Phase" takes care of that:

```bash
hostname -s > ${CODESIGNING_FOLDER_PATH}/devhost.txt
```

![Devhost run script phase](/img/devhost.png "Dev host run script phase")

This simply captures your IP address in a file, which is then used during
development to connect to that shadow-cljs running on that IP address.

[^clojurescript]: Clojure(Script) is just the language that I happen to use most at the moment. In principle, any language that compiles to JavaScript can be used instead. The common part of Strohm of course has to be rewritten in that language in that case, but it is really not a lot of code. I'm not planning to port it to another language any time soon.

[xcode]: https://developer.apple.com/xcode/
[androidStudio]: https://developer.android.com/studio/
[vscode]: https://code.visualstudio.com
[calva]: https://calva.io
[nodejs]: https://nodejs.org/en/
[kotlin]: https://kotlinlang.org
[swift]: https://swift.org
[clojurescript]: https://clojurescript.org
[reactnative]: https://reactnative.dev
[shadow-cljs]: https://shadow-cljs.github.io/docs/UsersGuide.html
