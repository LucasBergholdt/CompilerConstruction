In sample-ast-expected.out we changed the very last lines (157-159) from:
LiteralExpr
  Cast_To Number
  "4"

To:
Cast_To Number
  LiteralExpr
    "4"

We have chosen to represent a cast_to in the AST as a seperate expression node (Expr.Cast).
Before it seemed like a cast_to was a part of an expression. We think that by representing it as a seperate Cast node, we more directly follow the semantics of the grammar where a cast is an optional prefix operator applied to an expression (rather than a property of that expression).

This also means that we don't need fields in expressions that are often null and have a seperation of concerns, where expressions don't have to worry about or even know whether a cast was specified, while a Cast object can rely on polymorphism to abstract the specifics of the type of Expression it is holding.

Furthermode, we think that implementing cast_to as its own node, fits nicely with the visitor pattern used in the ASTPrinter, as we can implement a visitCastExpr, which can then rely on the visitor pattern to call the right function for the expression it is holding.