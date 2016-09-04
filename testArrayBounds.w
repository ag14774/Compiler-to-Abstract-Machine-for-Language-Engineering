def A[5];
A[0] := 0;
A[1] := 1;
A[2] := 2;
A[3] := 3;
A[4] := 4;
write('An array of 5 integers is defined(0-4). Try accessing something out of bounds: ');
register i := 0;
read(i);
write(A[i])
