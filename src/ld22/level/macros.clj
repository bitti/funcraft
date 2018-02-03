(ns ld22.level.macros)

(defmacro >>
  ([n] `(bit-shift-right ~n 1))
  ([n b] `(bit-shift-right ~n ~b)))

(defmacro <<
  ([n] `(bit-shift-left ~n 1))
  ([n b] `(bit-shift-left ~n ~b)))
