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
 [[dev.strohmnative/strohmnative "0.1.0-SNAPSHOT"]] ;; replace with latest version

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
(ns app.main)

(defn ^:export main! []
  (tap> "Hello world!"))
```

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

Create the folder `/android` that will contain the Android project sources:

```bash
mkdir android
```

Using Android Studio, create a new Android project in that folder.

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

# iOS

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
