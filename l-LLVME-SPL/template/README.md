### -- GROUP T05 --

All members contributed equally. 

**CHANGE IN sample-simple.spl:**

"var c = x < y" in the first line did not fit with the logic of our implementation, as we assume (deemed reasonable by Sandra), that all evaluating variable expressions correctly would require their assignment first. Otherwise, we assume it is an error.  

Therefore we declare the variables first, in the simple-sample.cf file. 

**HOW DID WE PERFORM THE l-LLVM EMISSION?**

We aspired to achieve SSA and 3AC.

This required us to be able to create temporary variables. 

Achieving SSA:

The idea is, whenever a (any) variable is referred to once, e.g on assignment, we store that in a symbol table. Whenever that variable needs to be updated, in an assignment expression, we map that variable to a new temporary one. 

Achieving 3AC:

The idea is to utilize temporary variables in the evaluation of binary and logical expressions. We make sure to that any result is stored in a new temp variable. 

We also only evaluate binary expressions sequentially, returning the intermediate result temporary, to be used in the adjacent computation. This implements 3AC in nested (multiple) binary expressions in one line.

**THOUGHTS ON FURTHER OPTIMZATIONS:**

An example of further optimization could be *constant folding* and *constant propagation*. 

We have a lot of computations that evaluates to a constant. Instead of representing that constant as a variable, we could just represent that variable as its constant value, therefore saving time not looking up the value of a variable when our program is run (constant propagation).

For constant folding, we would just evaluate the result of the expressions with constants, and then replace the expression with the constant value.


