# COOL Compiler in Clojure

COOL is an object oriented programming language with a static
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

The Stanford course makes the problem a bit easier on the students
by giving them a a skeleton for each part of the assignment and
having students fill in the relevant details.

The compiler here is written completely from scratch, using no
external libraries for lexing, parsing, code generation, or
optimization. It is also written in Clojure :) whereas the course
expects one to use C++ (eww) or Java (less eww).

