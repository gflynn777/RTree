JFLAGS = -d bin -classpath src
JC = javac
RM = rm
.SUFFIXES: .java .class
ALL: Rectangle.class Tuple.class Node.class RTree.class

Rectangle.class: ./src/main/Rectangle.java
	$(JC) $(JFLAGS) src/main/Rectangle.java
	
Tuple.class: ./src/main/Tuple.java
	$(JC) $(JFLAGS) src/main/Tuple.java
	
Node.class: ./src/main/Node.java
	$(JC) $(JFLAGS) src/main/Node.java

RTree.class: ./src/main/RTree.java
	$(JC) $(JFLAGS) src/main/RTree.java

clean:
	$(RM) *.class