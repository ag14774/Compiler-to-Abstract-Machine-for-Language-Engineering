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
writeln
