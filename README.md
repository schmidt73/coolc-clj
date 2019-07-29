# COOL Compiler in Clojure

COOL is an object oriented programming language with a static typing system
that is *almost* a subset of Java. The compiler written here 
is inspired by the [Stanford Compilers Course](https://lagunita.stanford.edu/courses/Engineering/Compilers/Fall2014/course/)
which builds a COOL compiler from scratch.

## Installation

The compiler is straightforward to install with [Leiningen](https://leiningen.org/).

Simply download the git repo and run:

```
lein install
```

in the root directory of the project.

## Differences Between Stanford Course

The Stanford course builds the compiler incrementally, where
each assignment tackles one phase (e.g. parsing or lexing). Students
are given a skeleton for each assignment and must fill in the
relevant details.

In contrast, the compiler here is written completely from scratch, using no
external libraries for lexing, parsing, code generation, or
optimization. It is also written in Clojure :) whereas the course
expects one to use C++ (eww) or Java (less eww).

