# prochaine

A technical study for [Om](https://github.com/omcljs/om) Next.

The word *[prochaine](http://www.larousse.com/en/dictionaries/french-english/prochain/63351?q=prochaine)* means next in French.

This is a simple technical study inspired by
[David Nolen](http://swannodette.github.io/)'s
recent
[EuroClojure](https://twitter.com/swannodette/status/618455025002631168)
talk:
[Om Next](https://www.youtube.com/watch?v=ByNs9TG30E8)

## Running

This demo is intended to be run with [figwheel](https://github.com/bhauman/lein-figwheel) -- an excellent ClojureScript development tool.

To compile the figwheel code and start the browswer REPL do:

    lein figwheel

You will see output like

````
Figwheel: Starting server at http://localhost:3449
Successfully compiled "resources/public/js/compiled/app.js" in 4.699 seconds.
Started Figwheel autobuilder

Launching ClojureScript REPL for build: app
Figwheel Controls:
          (stop-autobuild)                ;; stops Figwheel autobuilder
          (start-autobuild [id ...])      ;; starts autobuilder focused on optional ids
          (switch-to-build id ...)        ;; switches autobuilder to different build
          (reset-autobuild)               ;; stops, cleans, and starts autobuilder
          (build-once [id ...])           ;; builds source one time
          (clean-builds [id ..])          ;; deletes compiled cljs target files
          (fig-status)                    ;; displays current state of system
          (add-dep [org.om/om "0.8.1"]) ;; add a dependency. very experimental
  Switch REPL build focus:
          :cljs/quit                      ;; allows you to switch REPL to another build
    Docs: (doc function-name-here)
    Exit: Control+C or :cljs/quit
 Results: Stored in vars *1, *2, *3, *e holds last exception object
Prompt will show when figwheel connects to your application
To quit, type: :cljs/quit
cljs.user=>
````

You can then interact with the application like this:
````
cljs.user=> (in-ns 'contacts.demo3)
nil
contacts.demo3=> (pprint (om/get-query AddressInfo))
[:address/street :address/city :address/zipcode]

("[:address/street :address/city :address/zipcode]\n")
contacts.demo3=>
````

## Notes

This is not a *complete* study as it does include any support
for serving dynamic data, running tests, creating an uberjar, etc.

## Copyright and license

Copyright © 2015 Tom Marble

Licensed under the [MIT](http://opensource.org/licenses/MIT) [LICENSE](LICENSE)
