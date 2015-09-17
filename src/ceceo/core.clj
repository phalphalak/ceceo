(ns ceceo.core
  (:require [instaparse.core :as i]
            [clojure.edn :as edn]
            [clojure.core :exclude [read]]))

(def parser
  (i/parser "
  SEXPS = (SEXP | SPACE)*
  SEXP = SEXP-NO-SPACE | SEXP-SPACE
  SEXP-NO-SPACE = LIST
  SEXP-SPACE-HELP = 'a' | '1'
  SEXP-SPACE = !(SEXP-SPACE-HELP SEXP-SPACE-HELP) SEXP-SPACE-HELP
  LIST = '(' SEXPS ')'
  SPACE = #'\\s+'
"))

;;(parser "(()(a 11)) (1)")

(def parser
  (i/parser "
  <SEXPS> = SPACE? ((SEXP-NO-SPACE SEXPS) | (SEXP-SPACE SEXPS2?))?
  <SEXPS2> = (SPACE | SEXP-NO-SPACE) SEXPS
  <SEXP> = SEXP-NO-SPACE | SEXP-SPACE
  <SEXP-NO-SPACE> = LIST | VECTOR | MAP | STRING | SET
  <SEXP-SPACE> = INTEGER | DECIMAL | SYMBOL | KEYWORD
  LIST = <'('> SEXPS <')'>
  VECTOR = <'['> SEXPS <']'>
  SET = <'#{'> SEXPS <'}'>
  MAP = <'{'> MAP-EVEN <'}'>
  <MAP-EVEN> = SPACE? ((SEXP-NO-SPACE MAP-ODD) | (SEXP-SPACE MAP-ODD2)) | Epsilon
  <MAP-EVEN2> = (SPACE? SEXP-NO-SPACE MAP-ODD) | (SPACE SEXP-SPACE MAP-ODD2) | Epsilon
  <MAP-ODD>  = SPACE? ((SEXP-NO-SPACE MAP-EVEN) | (SEXP-SPACE MAP-EVEN2))
  <MAP-ODD2> = SPACE? ((SEXP-NO-SPACE MAP-EVEN) | (SPACE SEXP-SPACE MAP-EVEN2))
  STRING = #'\"((\\\\\")|[^\"])*\"'
  STRING2 = <'\"'> (#'([^\"\\\\]|(\\\\.))*')* <'\"'>
  INTEGER = #'0|-?[1-9]\\d*'
  DECIMAL = #'-?(0|[1-9]\\d*)\\.\\d+'
  SYMBOL = SYMBOL-PART (<'/'> SYMBOL-PART)?
  <SYMBOL-PART> = !(#'[\\d\\:\\#]') !(#'-\\d') SYMBOL-HLP
  <SYMBOL-HLP> = #'[\\.\\*\\+\\!\\-\\_\\?\\$\\%\\&\\=\\<\\>\\#\\:\\w]+'
  KEYWORD = <':'> SYMBOL-HLP (<'/'> SYMBOL-PART)?
  <SPACE> = <#'\\s+|,'>
"))

(defn- transform-symbol [type]
  (fn
    ([name] [type {:name name}])
    ([namespace name] [type {:name name
                             :namespace namespace}])))

(defn read [s]
  (->> (parser s)
       (i/transform {:STRING (fn [& args] (prn args) [:STRING (apply edn/read-string args)])
                     :SYMBOL (transform-symbol :SYMBOL)
                     :KEYWORD (transform-symbol :KEYWORD)})))

(defn evaluate [x])
