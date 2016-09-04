---------------------------------------------------------------
-- Language Engineering: COMS22201
-- Assessed Coursework 2: CWK2
-- Question 1: Denotational Semantics of While with read/write
---------------------------------------------------------------
-- This stub file provides a set of Haskell type definitions
-- you should use to implement various functions and examples 
-- associated with the denotational semantics of While with 
-- read and write statements as previously used in CWK1.
-- 
-- To answer this question, upload one file "cwk2.hs" to the 
-- "CWK2" unit component in SAFE by midnight on 01/05/2016.
--
-- You should ensure your file loads in GHCI with no errors 
-- and it does not import any modules (other than Prelude).
--
-- Please note that you will loose marks if your submission 
-- is late, incorrectly named, generates load errors, 
-- or if you modify any of the type definitions below.
--
-- For further information see the brief at the following URL:
-- https://www.cs.bris.ac.uk/Teaching/Resources/COMS22201/cwk2.pdf
---------------------------------------------------------------

import Prelude hiding (Num)
import qualified Prelude (Num)
  
type Num = Integer
type Var = String
type Z = Integer
type T = Bool
type State = Var -> Z
type Input  = [Integer]  -- to denote the values read by a program
type Output = [String]   -- to denote the strings written by a program
type IOState = (Input,Output,State)  -- to denote the combined inputs, outputs and state of a program

data Aexp = N Num | V Var | Add Aexp Aexp | Mult Aexp Aexp | Sub Aexp Aexp deriving (Show, Eq, Read)
data Bexp = TRUE | FALSE | Eq Aexp Aexp | Le Aexp Aexp | Neg Bexp | And Bexp Bexp deriving (Show, Eq, Read)
data Stm  = Ass Var Aexp | Skip | Comp Stm Stm | If Bexp Stm Stm | While Bexp Stm 
          | Read Var       -- for reading in the value of a variable
          | WriteA Aexp    -- for writing out the value of an arithmetic expression
          | WriteB Bexp    -- for writing out the value of a Boolean expression
          | WriteS String  -- for writing out a given string
          | WriteLn        -- for writing out a string consisting of a newline character
          deriving (Show, Eq, Read)

---------------------------------------------------------------
-- Part A)
--
-- Begin by adding your definitions of the following functions
-- that you previously wrote as part of CWK2p1 and CWk2p2:
---------------------------------------------------------------

(#) :: Eq a => [a] -> [a] -> [a]
(#) x y = (toSet x) ++ [i | i <- (toSet y), not $ i `elem` (toSet x)]
    where toSet::Eq a => [a] -> [a]
          toSet []     = []
          toSet (j:js) = if j `elem` js then (toSet js) else j:(toSet js)

fv_aexp :: Aexp -> [Var]
fv_aexp (N n)      = []
fv_aexp (V x)      = [x]
fv_aexp (Add  x y) = (fv_aexp x) # (fv_aexp y)
fv_aexp (Sub  x y) = (fv_aexp x) # (fv_aexp y)
fv_aexp (Mult x y) = (fv_aexp x) # (fv_aexp y)

fv_bexp :: Bexp -> [Var]
fv_bexp  TRUE     = []
fv_bexp  FALSE    = []
fv_bexp (Eq  x y) = (fv_aexp x) # (fv_aexp y)
fv_bexp (Le  x y) = (fv_aexp x) # (fv_aexp y)
fv_bexp (Neg x  ) =  fv_bexp x
fv_bexp (And x y) = (fv_bexp x) # (fv_bexp y)

a_val :: Aexp -> State -> Z
a_val      (N x) _ =   x
a_val      (V x) q = q x
a_val (Mult x y) q = (a_val x q) * (a_val y q)
a_val (Add  x y) q = (a_val x q) + (a_val y q)
a_val (Sub  x y) q = (a_val x q) - (a_val y q)

b_val :: Bexp -> State -> T
b_val  TRUE      _  = True
b_val  FALSE     _  = False
b_val (Eq  x y ) q  = (a_val x q) == (a_val y q)
b_val (Le  x y ) q  = (a_val x q) <= (a_val y q)
b_val (Neg x   ) q  = not (b_val x q)
b_val (And x y ) q  = (b_val x q) && (b_val y q)

cond :: (a->T, a->a, a->a) -> (a->a)
cond (b, p, q) s = if (b s) then (p s) else (q s)

fix :: (a -> a) -> a
fix f = f (fix f)

update :: State -> Z -> Var -> State
update s i v v2 = if v2 == v then i else s v2

---------------------------------------------------------------
-- Part B))
--
-- Write a function fv_stm with the following signature such that 
-- fv_stm p returns the set of (free) variables appearing in p:  
---------------------------------------------------------------

fv_stm :: Stm -> [Var]
fv_stm (Ass v a     )    = (fv_aexp a ) # [v]
fv_stm (Skip        )    = [ ]
fv_stm (Comp ss1 ss2)    = (fv_stm ss1) # (fv_stm ss2)
fv_stm (If b ss1 ss2)    = (fv_stm ss1) # (fv_stm ss2) # (fv_bexp b)
fv_stm (While b  ss )    = (fv_bexp b ) # (fv_stm ss ) 
fv_stm (WriteA a    )    = (fv_aexp a )
fv_stm (WriteB b    )    = (fv_bexp b )
fv_stm (WriteS s    )    = [ ]
fv_stm (WriteLn     )    = [ ]
fv_stm (Read v      )    = [v]

---------------------------------------------------------------
-- Part C)
--
-- Define a constant fac representing the following program 
-- (which you may recall from the file test7.w used in CWK1):
{--------------------------------------------------------------
write('Factorial calculator'); writeln;
write('Enter number: ');
read(x);
write('Factorial of '); write(x); write(' is ');
y := 1;
while !(x=1) do (
  y := y * x;
  x := x - 1
);
write(y);
writeln;
writeln;
---------------------------------------------------------------}

fac :: Stm
fac = (Comp (Comp (Comp (Comp (Comp (Comp (Comp (Comp (Comp (Comp (Comp (WriteS "Factorial calculator") WriteLn) (WriteS "Enter number: ")) (Read "x")) (WriteS "Factorial of ")) (WriteA (V "x"))) (WriteS " is ")) (Ass "y" (N 1))) (While (Neg (Eq (V "x") (N 1))) (Comp (Ass "y" (Mult (V "y") (V "x"))) (Ass "x" (Sub (V "x") (N 1)))))) (WriteA (V "y"))) WriteLn) WriteLn)

---------------------------------------------------------------
-- Part D)
--
-- Define a constant pow representing the following program 
-- (which you may recall from the file test7.w used in CWK1):
{--------------------------------------------------------------
write('Exponential calculator'); writeln;
write('Enter base: ');
read(base);
if 1 <= base then (
  write('Enter exponent: ');
  read(exponent);
  num := 1;
  count := exponent;
  while 1 <= count do (
    num := num * base;
    count := count - 1
  );
  write(base); write(' raised to the power of '); write(exponent); write(' is ');
  write(num)
) else (
  write('Invalid base '); write(base)
);
writeln
---------------------------------------------------------------}

pow :: Stm
pow = (Comp (Comp (Comp (Comp (Comp (WriteS "Exponential calculator") WriteLn) (WriteS "Enter base: ")) (Read "base")) (If (Le (N 1) (V "base")) (Comp (Comp (Comp (Comp (Comp (Comp (Comp (Comp (Comp (WriteS "Enter exponent: ") (Read "exponent")) (Ass "num" (N 1))) (Ass "count" (V "exponent"))) (While (Le (N 1) (V "count")) (Comp (Ass "num" (Mult (V "num") (V "base"))) (Ass "count" (Sub (V "count") (N 1)))))) (WriteA (V "base"))) (WriteS " raised to the power of ")) (WriteA (V "exponent"))) (WriteS " is ")) (WriteA (V "num"))) (Comp (WriteS "Invalid base ") (WriteA (V "base"))))) WriteLn)

---------------------------------------------------------------
-- Part E)
--
-- Write a function s_ds with the following signature such that 
-- s_ds p (i,o,s) returns the result of semantically evaluating 
-- program p in state s with input list i and output list o.
---------------------------------------------------------------

s2io :: (State -> T) -> (IOState -> T)
s2io f (i,o,s) = f s

s_ds :: Stm -> IOState -> IOState
s_ds (Ass v a     ) (i,o,s)  = (i,o,update s (a_val a s) v)
s_ds  Skip ios               = id ios
s_ds (Comp ss1 ss2) (i,o,s)  = ((s_ds ss2).(s_ds ss1)) (i,o,s)
s_ds (If b ss1 ss2) (i,o,s)  = cond (s2io $ b_val b, s_ds ss1, s_ds ss2) (i,o,s)
s_ds (While b  ss1) (i,o,s)  = (fix ff) (i,o,s)
      where ff g = cond (s2io $ b_val b, g . s_ds ss1, id)
s_ds (Read v      ) (i,o,s)  = (t,o ++ ["<" ++ show h ++ ">"],update s h v)
      where h = head i
            t = tail i
s_ds (WriteA a    ) (i,o,s)  = (i,o ++ [show $ a_val a s],s)
s_ds (WriteB b    ) (i,o,s)  = (i,o ++ [show $ b_val b s],s)
s_ds (WriteS st   ) (i,o,s)  = (i,o ++ [st  ],s)
s_ds (WriteLn     ) (i,o,s)  = (i,o ++ ["\n"],s)

---------------------------------------------------------------
-- Part F)
--
-- Write a function eval with the following signature such that 
-- eval p (i,o,s) computes the result of semantically evaluating 
-- program p in state s with input list i and output list o; and 
-- then returns the final input list and output list together 
-- with a list of the variables appearing in the program and 
-- their respective values in the final state.
---------------------------------------------------------------

eval :: Stm -> IOState -> (Input, Output, [Var], [Num])
eval p (i,o,s)            = (evi,evo,fvp,[evs x | x<-fvp])
      where (evi,evo,evs) = s_ds p (i,o,s)
            fvp           = fv_stm p

