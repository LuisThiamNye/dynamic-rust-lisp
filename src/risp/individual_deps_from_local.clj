(ns risp.individual-deps-from-local)

(defn compile-thirdparty-dyn-sh [{:keys [target-dir manifest-path]} dir]
  (shell/sh "cargo"
            "build" "--lib" "-v"
            "--manifest-path" (str manifest-path)
            :env (assoc (into {} (System/getenv))
                        "RUSTFLAGS" "-C prefer-dynamic=yes --emit=metadata"
                        "CARGO_TARGET_DIR" target-dir)
            :dir dir))

(defn compile-thirdparty-dyn [{:keys [workspace-dir target-dir crate-types]} package-name]
  {:pre [(some? workspace-dir) (some? target-dir)]}
  (let [manifest-path (-> (shell/sh "cargo" "metadata" "--format-version" "1"
                                   :dir workspace-dir)
                         :out
                         json/read-str
                         (get "packages")
                         (->> (filter #(= package-name (get % "name"))))
                         first
                         (get "manifest_path"))

        dep-dir (second (re-matches #"(.*)/Cargo.toml" manifest-path))
        backup-manifest-path (fs/path dep-dir "Cargo.rustreplcompilationoriginal.toml")]
   (when-not (fs/exists? backup-manifest-path)
     (fs/copy manifest-path backup-manifest-path {:replace-existing true})
     (spit (str manifest-path)
           ;; rlib supposedly fixes 'error: cannot satisfy dependencies so `X` only shows up once'
           ;; but not effective
           (str "\n[lib]\ncrate-type = ["
                (apply str (interpose ","(map #(str \" % \") (or crate-types ["rlib" "dylib"]))))
                "]\n"
                "path = \"" dep-dir "/src/lib.rs\"")
           :append true))
   (println
    (:err
     (compile-thirdparty-dyn-sh
      {:target-dir target-dir
       :manifest-path manifest-path}
      dep-dir)))
   (when (fs/exists? backup-manifest-path)
     (fs/move backup-manifest-path manifest-path
              {:replace-existing true}))))

(defn compile-all-thirdparty-dyn [{:keys [workspace-dir crate-name] :as ctx}]
  (shell/sh "cargo" "fetch" :dir workspace-dir)
  (let [packages (-> (shell/sh "cargo" "metadata" "--format-version" "1"
                               :dir workspace-dir)
                     :out
                     json/read-str
                     (get "packages"))
        dep-names (-> packages
                      (->> (filter #(= crate-name (get % "name"))))
                      first
                      (get "dependencies")
                      (->> (map #(get % "name")))
                      set)
        dep-entries (-> packages
                        (->> (filter #(dep-names (get % "name")))))]
    (doseq [name' (map #(get % "name") dep-entries)]
           (compile-thirdparty-dyn ctx name'))))

(comment
  (compile-all-thirdparty-dyn
   {:workspace-dir (fs/path workspace-dir "deps")
    :target-dir (fs/path target-dir "deps")
    :crate-name "_deps"
    :crate-types ["rlib" "dylib"]})

  (compile-thirdparty-dyn
   {:workspace-dir (fs/path workspace-dir "deps")
    :target-dir (fs/path target-dir "deps")
    :crate-name "_deps"
    :crate-types ["rlib"]}
   "cfg-if")


  ;;
  )
