(ns utils)

(def ^:private digit->hex
  (merge {10 "a" 11 "b" 12 "c" 13 "d" 14 "e" 15 "f"}
         (zipmap (range 0 10) (map str (range 0 10)))))

(def ^:private hex->digit
  (zipmap (vals digit->hex) (keys digit->hex)))

(defn byte->hex
  [b]
  (apply str (map digit->hex (map #(% b 16) [quot rem]))))

(defn rgb->hex
  [r g b]
  (apply str (map byte->hex [r g b])))

(defn hex-color->rgb-bytes
  [hex-color]
  (let [parts (partition 2 hex-color)]
    (map (fn [[hb lb]]
           (+ (* 16 (hex->digit (str hb)))
              (hex->digit (str lb))))
         parts)))

(comment
  (rgb->hex 129 32 45)
  (partition 2 "ffffff")

  (hex-color->rgb-bytes "feefff")
  )
