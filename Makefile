all:
	./antlr3 Lex.g
	./antlr3 Syn.g
	javac TokenConv.java
	java TokenConv Syn.tokens Irt.java 
	java TokenConv Syn.tokens HaskellParser.java 
	./antlr3 *.java
	./antlr3 camle testsk.w
	./assmule testsk.ass
	touch compiled

build:
	./antlr3 Lex.g
	./antlr3 Syn.g
	javac TokenConv.java
	java TokenConv Syn.tokens Irt.java 
	java TokenConv Syn.tokens HaskellParser.java
	./antlr3 *.java
	touch compiled
	
clean:
	rm -f *.class
	rm -f *.txt
	rm -f *.tokens
	rm -f *.ass
	rm -f Lex.java
	rm -f Syn.java
	rm compiled
