{------------------------------------------------------------
 -- Language Engineering: COMS22201
 -- Assessed Coursework 2: CWK2
 -- Question 3: Axiomatic Semantics of While with read/write
 ------------------------------------------------------------
 -- This stub file gives two code fragments (from the test7.w 
 -- source file used in CWK1) that you will need to annotate 
 -- with tableau correctness proofs using the partial axiomatic 
 -- semantics extended with axioms for read/write statements.
 -- 
 -- To answer this question, upload one file "cwk2.w" to the 
 -- "CWK2" unit component in SAFE by midnight on 01/05/2016.
 --
 -- For further information see the brief at the following URL:
 -- https://www.cs.bris.ac.uk/Teaching/Resources/COMS22201/cwk2.pdf
 ------------------------------------------------------------}


{------------------------------------------------------------
 -- Part A)
 --
 -- provide a tableau-based partial correctness proof
 -- of the following program (for computing factorials) 
 -- with respect to the given pre- and post-conditions
 -- by completing the annotation of the program with 
 -- logical formulae enclosed within curly braces:
 ------------------------------------------------------------}

{ head(IN)=n }
  { using lemma a=b => a!=b!. taking factorials on both sides or substituting a with head(IN) and b with n }
{ n!=head(IN)! }
write('Factorial calculator'); writeln;
{ n!=head(IN)! }
write('Enter number: ');
{ n!=head(IN)! }
read(x);
{ n!=x! }
write('Factorial of '); write(x); write(' is ');
{ n!=x! }
  { multiplying by 1(identity) on right side n!=x!=1*x!=n!}
{ n!=1*x! }
y := 1;
{ n!=yx! }
  { by introducing assumption x>0 and because anything->true }
{ x>0 -> n!=yx! }
while !(x=1) do (
{ x>0 -> n!=yx! & !(x=1) }
  { as if (x-1)>0 then x>1 so x>0 and y*x!=n! and (y*x)*(x-1)!=n! }
{ (x-1)>0 -> n!=(y*x)*(x-1)! }
  y := y * x;
{ (x-1)>0 -> n!=y(x-1)! }
  x := x - 1
{ x>0 -> n!=yx! }
);
{ x>0 -> n!=yx! & !!(x=1) }
  { x=1 by eliminating double negation on !!(x=1). So n!=y*x!=y*1!=y }
{ y=n! }
  { using append on both sides append(OUT,[y])=append(OUT,[n!]) }
{ append(OUT,[y])=append(_,[n!]) }
write(y);
{ OUT=append(_,[n!]) }
  { append(OUT,['\n'])=append(append(_,[n!]),['\n']) by append on both sides }
  { append(OUT,['\n'])=append(_,[n!,'\n']) }
{ append(OUT,['\n'])=append(_,[n!,_]) }
writeln;
{ OUT=append(_,[n!,_]) }
  { append(OUT,['\n'])=append(append(_,[n!,_]),['\n']) by adding append(_,['\n']) on both sides}
  { append(OUT,['\n'])=append(_,[n!,_,'\n']) by concatenating two append: append(append(_,[a]),[b])=append(_,[a,b]) }
{ append(OUT,['\n'])=append(_,[n!,_,_]) }
writeln
{ OUT=append(_,[n!,_,_]) }


{------------------------------------------------------------
 -- Part B)
 --
 -- provide a tableau-based partial correctness proof
 -- of the following program (for computing exponents) 
 -- with respect to suitable pre- and post-conditions:
 ------------------------------------------------------------}
{ head(IN)=b & head(tail(IN))=e & head(IN)>=1 & head(tail(IN))>=1 }
  { substitution using lemma a=b and c=d then a^c=b^d }
{ b^e=head(IN)^head(tail(IN)) & head(IN)>0 & head(tail(IN))>=1 }
write('Exponential calculator'); writeln;
{ b^e=head(IN)^head(tail(IN)) & head(IN)>0 & head(tail(IN))>=1 }
write('Enter base: ');
{ b^e=head(IN)^head(tail(IN)) & head(IN)>0 & head(tail(IN))>=1 }
read(base);
{ b^e=base^head(IN) & base>0 & head(IN)>=1 }
if 1 <= base then (
{ b^e=base^head(IN) & base>=1 & head(IN)>=1 & base>0}
  { base>=1 equivalent to base>0 over integers }
{ b^e=base^head(IN) & base>0 & head(IN)>=1 }
  write('Enter exponent: ');
{ b^e=base^head(IN) & base>0 & head(IN)>=1 }
  read(exponent);
{ b^e=base^exponent & base>0 & exponent>=1 }
  { multiplication by 1(identity) }
{ b^e=1*base^exponent & base>0 & exponent>=1 }
  num := 1;
{ b^e=num*base^exponent & base>0 & exponent>=1 }
  count := exponent;
{ b^e=num*base^count & base>0 & count>=1 }
  while 1 <= count do (
{ 1<=count & count>=1 & base>0 & b^e=num*base^count }
{ (count-1)>=0 by subtracting 1 from both sides of 1<=count. b^e=num*base^count=num*base^(count-1+1)=num*base*base^(count-1)=(num*base)*base^(count-1)=b^e }
{ (count-1)>=0 & base>0 & b^e=(num*base)*base^(count-1) }
    num := num * base;
{ (count-1)>=0 & base>0 & b^e=num*base^(count-1) }
    count := count - 1
{ count>=0 & base>0 & b^e=num*base^count }
  );
{ !(1<=count) & count>=0 & base>0 & b^e=num*base^count }
  { count<1 from !(1<=count). count<=0 from count<1. count=0 from count<=0^count>=0. b^e=num*base^count=num*base^0=num*1=num }
{ b^e=num }
  write(base); write(' raised to the power of '); write(exponent); write(' is ');
{ num=b^e }
  { append on both sides : append(OUT,[num]) = append(OUT,[b^e]) }
{ append(OUT,[num]) = append(_,[b^e]) }
  write(num)
{ OUT = append(_,[b^e]) }
) else (
{ b^e=base^head(IN) & !(1<=base) & head(IN)>=1 & base>0 }
  { base<1 from !(1<=base). base<=0 from base<1. base<=0 & base>0 |= False |= anything. (false implies anything) }
{ append(OUT,['Invalid base',base])=append(_,[b^e,_]) }
  write('Invalid base '); write(base)
{ OUT = append(_,[b^e]) }
);
{ OUT = append(_,[b^e]) }
  { append(OUT,['\n'])=append(append(_,[b^e]),['\n']) by append on both sides }
  { append(OUT,['\n'])=append(_,[b^e,'\n']) by concatenating to append into one }
{ append(OUT,['\n'])=append(_,[b^e,_]) }
writeln
{ OUT=append(_,[b^e,_]) }
