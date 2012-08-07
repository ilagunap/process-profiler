JAVA=javac
JAVAASSIST=./javaassist/javassist-3.16.1-GA/javassist.jar

CLASSPATH=./bin:$(JAVAASSIST)
SRC=./src
BIN=./bin

all:
#	$(JAVA) -classpath $(CLASSPATH) -sourcepath $(SRC) -d $(BIN) ./src/com/ilaguna/augury/*.java 
	$(JAVA) -classpath $(CLASSPATH) -sourcepath $(SRC) -d $(BIN) ./src/com/ilaguna/test/*.java
	$(JAVA) -classpath $(CLASSPATH) -sourcepath $(SRC) -d $(BIN) ./src/com/ilaguna/metrics/*.java

clean:
	rm -rf $(BIN)/com
