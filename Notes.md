Notes
=====

To Do
-----

* Is `app.main/init` needed?
* Different signature in iOS/Android: `getInstance` vs `default` of Strohm
* Put `network_security_config` in debug folder (and add empty one in release)

Creating new example project
----------------------------

* `Package.json`:

  ```json
  {
    "name": "{{ app name }}",
    "version": "1.0.0",
    "description": "{{ app description }}",
    "main": "index.js",
    "devDependencies": {
      "shadow-cljs": "^2.11.14"
    },
    "scripts": {
      "watch": "shadow-cljs watch :app :test",
      "release": "shadow-cljs release :app"
    }
  }
  ```

* `shadow-cljs.edn`:

  ```clojure
  ;; shadow-cljs configuration
  {:deps true

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

* `deps.edn`:

  ```clojure
  {:paths ["dev"
           "src"
           "test"]
   :deps {thheller/shadow-cljs {:mvn/version "2.20.16"}
          strohm/native {:local/root "../../Strohm"}}}
  ```

* Create directories:

  ```bash
  mkdir -p src/app
  mkdir ios
  mkdir android
  ```

* `src/app/main.cljs`:

  ```clojure
  (ns app.main
    (:require [strohm-native.flow :refer [create-store]]))

  (defn reducer
    [state _action]
    state)

  (defn ^:export main! []
    (create-store reducer :initial-state {})
    (js/console.debug "[main] started"))

  (defn ^:export init []
    (js/console.debug "[main] init done"))

  (defn ^:dev/after-load reload! []
    (js/console.debug "[main] reloaded"))
  ```

* Android project:
  * Create new project in Android Studio like normal, in the `android` folder
  * Add Strohm dependency: add to top-level `settings.gradle`:

    ```gradle
    include ':strohm'
    project(':strohm').projectDir = new File('../../../Strohm/android/strohm')
    ```

  * Add dependency in `app/build.gradle`:

    ```gradle
    dependencies {
      // ...
      implementation project(path: ':strohm')
    }
    ```

  * Create subclass of `Application` and register it in `AndroidManifest.xml` by
    adding the property `android:name="<MyApplicationSubclass>"` to the
    `<application>` tag. The put this in the `Application` subclass:

    ```kotlin
    lateinit var strohm: Strohm

    override fun onCreate() {
        super.onCreate()
        strohm = Strohm.getInstance(applicationContext)
    }
    ```

  * Add the network security config
    `app/src/main/res/xml/network_security_config.xml` needed for development:

    ```xml
    <network-security-config>
        <domain-config cleartextTrafficPermitted="true">
            <domain includeSubdomains="false">localhost</domain>
        </domain-config>
    </network-security-config>
    ```

  * Reference the network security in `app/src/main/AndroidManifest.xml` as a
    attribute of the `application` tag:

    ```xml
    <application
        ...
        android:networkSecurityConfig="@xml/network_security_config">
    ```

* iOS project:
  * Create new project in Xcode the normal way in the `ios` folder
  * This yields a folder structure with `/ios/ProjectName/ProjectName`; move the
    folders `ProjectName` and `ProjectName.xcodeproj` one level up to simplify
    the structure
  * From Finder, drag the folder containing Strohm into the project navigator,
    into the project, next to `Products`.
  * In the app's target settings, add Strohm to the section "Frameworks,
    Libraries and Embedded Content"
  * In the app's top-level scene, the one annotated with `@main` and conforming
    to the `App` protocol, store the default instance of `Strohm`:

    ```swift
    @main
    struct MyApp: App {
      let strohm = Strohm.default
      ...
    }
    ```

  * Add permission to use local networking in `Info.plist`:

    ```xml
    <key>NSAppTransportSecurity</key>
    <dict>
        <key>NSAllowsLocalNetworking</key>
        <true/>
    </dict>
    ```

  * Add `main.js` to Xcode project
