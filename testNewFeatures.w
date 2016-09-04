register i := 0; 
def A[5];
while(i<=4) do
(
 A[i] := i;
 i:=i+1
);
for j:=1 to 4 do
(
 write(A[j]);
 writeln
);
write(true | false)
