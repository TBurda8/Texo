
# Texo Language Specification

**Version:** 0.1.0 (Draft)  
**Status:** In development

## 1. Language Overview

### 1.1 Design

Texo is a general-purpose programming language with lightweight syntax designed to be easy to read and understand.

The language emphasizes minimal boilerplate, consistent constructs, implicit variable declarations, and explicitly terminated blocks. Many declarations and operations are organized around the `in` and `out` keywords.

Texo will use a **tree-walk interpreter written in Java**. Source code will be tokenized, parsed into an **abstract syntax tree (AST)**, and then interpreted.

### 1.2 Language Characteristics

*To be specified.*

## 2. Lexical Structure

### 2.1 Whitespace

Whitespace separates tokens and improves readability. Spaces, tabs, and line breaks are generally ignored except when needed to separate tokens.

Some declarations may have additional whitespace or line-placement requirements. These exceptions will be documented as the language develops.

### 2.2 Comments

Single-line comments begin with `#`.

```texo
# This is a single-line comment.
```

Multiline comments begin with `#/` and end with `/#`.

```texo
#/
This is a multiline comment.
It can span several lines.
/#
```

### 2.3 Identifiers

Identifiers name variables and functions. They are **case-sensitive** and cannot be reserved keywords.

Current proposed rules:

- The first character must be a letter or underscore (`_`).
- Subsequent characters may include letters, digits, and underscores.
- Reserved keywords cannot be used as identifiers.

### 2.4 Reserved Keywords

| Keyword | Purpose |
| --- | --- |
| `start` | Begins a function declaration |
| `in` | Introduces function parameters |
| `out` | Returns a value from a function |
| `end` | Terminates a block |
| `print` | Writes to standard output |
| `if` | Begins conditional execution |
| `then` | Begins a conditional block |
| `else` | Introduces an alternative conditional branch |
| `while` | Begins a conditional loop |
| `do` | Begins a loop body |
| `put` | Reads user input |

Additional keywords may be introduced during development.

### 2.5 Literals

| Literal type | Examples |
| --- | --- |
| Integer | `0`, `10`, `23` |
| Decimal | `3.14`, `0.5` |
| String | `"Hello World!"` |
| Boolean | `true`, `false` |

### 2.6 Operators and Delimiters

| Symbol | Purpose |
| --- | --- |
| `=` | Assignment |
| `+` | Addition |
| `-` | Subtraction |
| `*` | Multiplication |
| `/` | Division |
| `==` | Equality comparison |
| `>` | Greater-than comparison |
| `<` | Less-than comparison |
| `;` | Statement terminator |
| `,` | Separator |
| `(` `)` | Grouping |

Additional operators and delimiters may be added later.

## 3. Expressions

### 3.1 Arithmetic

Texo will support standard arithmetic operations, including addition, subtraction, multiplication, and division.

### 3.2 Operator Precedence

Standard arithmetic precedence is planned. Parentheses override the usual order of operations.

| Operator | Type | Associativity |
| --- | --- | ---|
| () | Grouping | left-to-right |
| ! & ~ ++ | Unary Operators | right-to-left |
| * / % | Arithmetic Operators | left-to-right|
| + - | Arithmetic Operators | left-to-right |



### 3.3 Operator Associativity

Operators with equal precedence will be evaluated from left to right.

### 3.4 Grouping

Parentheses group expressions.

## 4. Statements

### 4.1 Variable Declarations

Variables are created implicitly on their first assignment, without a separate declaration keyword.

**Syntax:**

```texo
identifier = expression;
```

**Example:**

```texo
count = 10;
```

Rules:

- Variable names are case-sensitive.
- Assignment statements end with a semicolon (`;`).
- The right-hand expression is evaluated before the value is assigned.
- Referencing an undefined variable produces an error.

### 4.2 Assignment

Assignments to an existing variable update its stored value.

```texo
count = 10;
count = count + 5;
```

### 4.3 Output

The `print` statement evaluates an expression and displays its value.

```texo
print count;
```

### 4.4 Conditional Statements

**Single-line form:**

```texo
if condition, statement;
```

**Multiline form:**

```texo
if condition then
    statements
else
    statements
end
```

### 4.5 Loops

*Syntax and behavior to be specified.*

## 5. Types and Values

*To be specified.*

## 6. Scoping and Functions

*To be specified.*

## 7. Formal Grammar
```
digit = "0" | "1" | "2" | "3" | "4" 
      | "5" | "6" | "7" | "8" | "9" ;
     
integer = digit, {digit} ;

letter = "a" | "b" | ... | "z"
       | "A" | "B" | ... | "Z" ;
       
identifier = letter, {letter | digit | "_"} ;

expression = term, {("+" | "-"), term} ;

term = factor, {("*" | "/"), factor} ;

factor = integer | identifier | "(", expression, ")" ;

print_statement = "print", expression ;

function = "start", identifier,
           ["in", parameter_list],
           newline,
           {statement},
           "end" ;
           
parameter_list = identifier, {",", identifier} ;

statement = assignment
          | print_statement
          | return_statement ;

return_statement = "out", expression ;
```

## 8. Error Handling

*To be specified.*
