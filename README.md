# Texo

## What Texo is

Texo is a experimental intrepreted programming language built from scratch in Java as a CS final project. 
Right now, it is a small readable language and uses a lexer, parser, ASTs, and a intrepreter. 
Texo runs source files from the terminal.

![System diagram](docs/Project_Diagram.png)

## V1 Features

Texo can currently use functions, variables, has error reporting, and uses a runtime enviroment for scope.
It can also do the typical stuff you would expect a basic language to do, like math. 
More documentation is in the docs folder.

## Tech Stack

- **Language:** Bash / Java
- **Libraries:** JUnit, JSL
- **External tools:** IntelliJ Idea, Git, Github

## Setup

Requirements: JDK 17+, Bash

```bash
git clone https://github.com/TBurda8/Texo
cd texo
javac -d out src/texo/*.java
java -cp out texo.Main
```

## Usage

```bash
git clone https://github.com/TBurda8/Texo.git
cd Texo
./scripts/run.sh examples/hello.texo
```

## Future Roadmap

- [x] Define languague grammar and syntax
- [x] Implement lexer and tokenizer
- [x] Develop parser
- [x] Implement ASTs
- [x] Support for variables and expressions
- [x] Support conditionals and loops
- [x] Support errors
- [x] Implement functions and recursion
- [x] Support scope

## Documentation

docs/language-spec.md - language syntax and grammar
docs/design.md - design choices and reasoning
examples/ - texo programs
tests/ - tests for the language

## Author

Thomas Haraburda, https://github.com/TBurda8/
