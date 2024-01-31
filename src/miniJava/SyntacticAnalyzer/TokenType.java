package miniJava.SyntacticAnalyzer;

import java.util.Arrays;
import java.util.HashSet;

// TODO: Enumate the types of tokens we have.
//   Consider taking a look at the terminals in the Grammar.
//   What types of tokens do we want to be able to differentiate between?
//   E.g., I know "class" and "while" will result in different syntax, so
//   it makes sense for those reserved words to be their own token types.
//
// This may result in the question "what doesn't result in different syntax?"
//   By example, if binary operations are always "x binop y"
//   Then it makes sense for -,+,*,etc. to be one TokenType "operator" that can be accepted,
//      (E.g. compare accepting the stream: Expression Operator Expression Semicolon
//       compare against accepting stream: Expression (Plus|Minus|Multiply) Expression Semicolon.)
//   and then in a later assignment, we can peek at the Token's underlying text
//   to differentiate between them.

public enum TokenType {


    //syntacticals
    //punctuation
    lparen, rparen, lcurly, rcurly, lsquare, rsquare, comma, dot, semicolon, lchevron, rchevron,
    question, colon, ellipsis, //ternary operator, variadic args
    //misc
    id, binOp, unOp, assignment, compOp, incOp,
    assignOp,  //things like /=, *=, +=, etc. only for one-character operators, no &&= or ||=
    genOp, // "GENERAL OPERATOR": +/- AS BINARY OR UNARY OPERATORS IS AMBIGUOUS, REQUIRES CONTEXT
    
    
    //keywords
    //unique keywords
    classKeyword, whileKeyword, voidKeyword, ifKeyword, forKeyword, returnKeyword, 
    newKeyword, elseKeyword, thisKeyword, packageKeyword, importKeyword,
    throwsKeyword, throwKeyword, //DIFFERENT KEYWORDS: throws is for method declaration, throw actually throws the error
    extendsKeyword, implementsKeyword,
    tryKeyword, catchKeyword, finallyKeyword,
    breakKeyword,continueKeyword,
    doKeyword,
    switchKeyword, caseKeyword,

    //special keyword groups (distinction doesn't matter for validity)
    protection, //public, private, protected, package, etc.
    modifier, //static, final, volatile, etc


    //primitives
    intPrimitive, floatPrimitive, doublePrimitive, charPrimitive, stringPrimitive, boolPrimitive,

    //literals
    intLiteral, stringLiteral, boolLiteral, charLiteral, floatLiteral,

    //EOT
    EOT,

}


