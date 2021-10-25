# Monadium ![GitHub Repo stars](https://img.shields.io/github/stars/yuxuanchiadm/monadium?style=flat-square) ![GitHub](https://img.shields.io/github/license/yuxuanchiadm/monadium?style=flat-square) ![GitHub Workflow Status](https://img.shields.io/github/workflow/status/yuxuanchiadm/monadium/Java%20CI%20with%20Maven?style=flat-square) ![GitHub all releases](https://img.shields.io/github/downloads/yuxuanchiadm/monadium/total?style=flat-square)

Monadic functional library for Java.

# Build

**IMPORTANT**: Please use `OpenJDK 11` to build this project.

Although all sourcecode is written in `Java 8` and compiled to `Java 8` bytecode.
But `OpenJDK 8` has some bugs in type inference.
So some polymorphic type variables won't inferred correctly.
Therefore some code won't compile using `OpenJDK 8`.