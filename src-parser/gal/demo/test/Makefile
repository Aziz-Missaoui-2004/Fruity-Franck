JAVAPROJECT = ../../../..
CLASSPATH = $(JAVAPROJECT)/bin
PARSER = gal.parser.Parser

help:
	@echo "1. INSTALL JavaCC with command: apt get install javacc"
	@echo "2. RUN JavaCC + JavaCompiler with command: make parser"
	@echo "3. PARSE file.gal with command: make file.txt, make file.aut.jpg, make file.ast.jpg"  

parser:
	@make -C $(JAVAPROJECT)/src-parser/gal/parser/

%.txt: %.gal 
	@java -cp $(CLASSPATH) $(PARSER) -file $< -txt	

%.aut.dot: %.gal 
	@java -cp $(CLASSPATH) $(PARSER) -file $< -aut > $@

%.ast.dot: %.gal 
	@java -cp $(CLASSPATH) $(PARSER) -file $< -ast > $@

%.jpg: %.dot
	@dot -Tjpg $< > $@
	@xdg-open $@
	
clean:
	-@rm -f *.jpg *.dot