(ns risp.repl
  (:require
   [toml.core :as toml]
   [clojure.data.json :as json]
   [clojure.java.shell :as shell]
   [babashka.fs :as fs]
   [clojure.string :as str]))

(def *deps (atom {}))
(def *nodes (atom {}))

(defn compile-unit [{:keys [cargo-target-dir rust-src-dir crate-name deps-dirs deps crate-type]}
                    unit-path]
  (let [edition "2021"
        unit-filestr (apply str (interpose "_" unit-path))
        unit-filepath (fs/path rust-src-dir (str unit-filestr ".rs"))
        debug-dir (fs/path cargo-target-dir "debug")
        out-dir (fs/path debug-dir "deps")]
    (when-not (fs/exists? out-dir)
      (fs/create-dirs out-dir))
    (apply vector
           "rustc" "-v"
           "--crate-name" (if crate-name
                            (str crate-name "__" unit-filestr)
                            unit-filestr)
           "--edition" edition
           "--out-dir" (str out-dir)
           "--error-format=human"
           ;; "--cap-lints" "allow"
           "--crate-type" (or crate-type "rlib,cdylib")
           "--emit=dep-info,link,metadata"
          ;; "--emit=metadata"
           "-C" "prefer-dynamic=yes"
           "-C" "embed-bitcode=no"
           "-C" "split-debuginfo=unpacked"
           "-C" "debuginfo=2"
           "-C" (str "incremental=" (fs/path debug-dir "incremental"))
           (str unit-filepath)
           (eduction
            cat
            [(eduction cat
                       (for [ddir deps-dirs]
                         (vector "-L" (str "dependency=" ddir))))
             (eduction cat
                       (for [[name' filename] deps]
                         (vector "--extern" (str name' "=" filename))))]))))

(comment
  (reset! *deps {})
  (reset! *nodes {[:root "build-root-widget"]
                  {:deps {"druid" {"widget" {:root ["Flex" "Label" "TextBox"]
                                             "Flex" {:root ["column"]}
                                             "Label" {:root ["new"]}
                                             "TextBox" {:root "new"}}}}}
                  [:root "main"]
                  {}})
  (defn shell [& args]
    (doto (apply clojure.java.shell/sh args)
      (-> :out println)
      (-> :err println)))

  (def workspace-dir (str (System/getProperty "user.dir") "/dev"))
  (def deps-dir (fs/path workspace-dir "deps/target/debug/deps"))
  (def target-dir (fs/path workspace-dir "target" "dynamic"))

  (spit "/Volumes/House/prg/rustlisp/dev/deps/target/tmp/libloading/Cargo.toml" "\n" :append true)

  (defn compile-thirdparty-dyn []
    (let [workspace-dir (str (System/getProperty "user.dir") "/dev")
          cargo-target-dir (or (System/getenv "CARGO_TARGET_DIR")
                               (fs/path workspace-dir "target/dynamic"))
          deps-debug-dir (fs/path workspace-dir "deps" "target" "debug")
          ctx {:rust-src-dir (fs/path workspace-dir "out-rs" "_deps")
               :cargo-target-dir cargo-target-dir
               :crate-type "rlib,dylib"
               :deps-dirs [(fs/path deps-debug-dir "deps")]
               :crate-name "_deps"
               :deps {"_deps" (fs/path deps-debug-dir "lib_deps.dylib")}}]
      (doseq [depname [["druid"] ["libloading"]]]
        (doto (apply shell/sh (compile-unit ctx depname))
          (-> :err println)
          (-> :out println)))))

  (compile-thirdparty-dyn)

  (println
   (:err
    (shell/sh
     "cargo"
     "build" "--lib" "-v"
     :env (assoc (into {} (System/getenv))
                 "RUSTFLAGS"
                 (str/join
                  " "
                  ["-C", "link-arg=-undefined","-C", "link-arg=dynamic_lookup", ;; allow undefined symbols
                   "-C", "link-args=-rdynamic", ;; export symbols in an executable
                   "-C", "prefer-dynamic=yes",
                   "--emit=dep-info,link,metadata"]))
     :dir "dev/deps")))

  (let [workspace-dir (str (System/getProperty "user.dir") "/dev")
        cargo-target-dir (or (System/getenv "CARGO_TARGET_DIR")
                             (fs/path workspace-dir "target" "dynamic"))
        deps-debug-dir (fs/path cargo-target-dir "debug")
        deps-dir (fs/path deps-debug-dir "deps")
        ctx {:rust-src-dir (fs/path workspace-dir "out-rs")
             :cargo-target-dir cargo-target-dir
             ;; :deps-dir (fs/path workspace-dir "deps/target/debug/deps")
             :deps-dirs [deps-dir (fs/path workspace-dir "deps" "target" "debug" "deps")]
             :crate-name "hello"
             ;; :deps {"_deps" (fs/path deps-dir "lib_deps.dylib")}
             :deps {"druid" (fs/path deps-dir "lib_deps__druid.dylib")
                    "libloading" (fs/path deps-dir "lib_deps__libloading.dylib")}}
        x [;;
           #_[(assoc ctx :crate-type "dylib")
            ["__globals"]]
           [(-> ctx (update :deps assoc "__globals"
                            (fs/path cargo-target-dir "debug" "deps" "libhello____globals.dylib")))
            ["buildRootWidget"]]
           [(-> ctx (update :deps assoc "__globals"
                            (fs/path cargo-target-dir "debug" "deps" "libhello____globals.dylib")))
            ["main"]]]]
    (doseq [x x]
      (println "COMPILING")
      (doto (apply shell/sh (apply compile-unit x))
        (-> :err println)
        (-> :out println)))
    ;; cmd
    ;;
    )
  (def cargo-target "/Volumes/House/cargotarget")

  (shell
   "cargo"
   "b"
   :dir "dev/deps"
   :env (merge (into {} (System/getenv))
               {"CARGO_TARGET_DIR" "target"
                "RUSTFLAGS" "-C prefer-dynamic=yes"}))

  (shell
   "rustc" "
--crate-name druid
--edition=2021
/Users/luis/.cargo/git/checkouts/druid-f71533f3d81c0bc8/2f5beb8/druid/src/lib.rs
--error-format=human
--cap-lints allow
--crate-type dylib
--emit=dep-info,link
-C prefer-dynamic=yes
-C embed-bitcode=no
-C split-debuginfo=unpacked
-C debuginfo=2
-C metadata=35d717bdac0f918d
--out-dir /Volumes/House/prg/rustlisp/dev/target/deps
-C incremental=/Volumes/House/prg/rustlisp/dev/target/incremental
-L dependency=/Volumes/House/prg/carputil/target/debug/deps
--extern druid=/Volumes/House/prg/carputil/target/debug/deps/libdruid-2dd71c10e7bc1da8.rlib

-L dependency=/Volumes/House/prg/rustlisp/dev/target/debug/deps
"
   {:env {"CARGO_TARGET_DIR" cargo-target}})

;; direct dependencies need to compile with lib.crate-type=["rlib", "dylib"] (maybe)
  ;; and must have -C prefer-dynamic=yes eg with RUSTFLAGS
  ;;
  )
