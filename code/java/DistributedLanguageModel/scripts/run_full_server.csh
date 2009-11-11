java -Xmx32g -Xms32g -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.server.Server -arch per_level_switch_sharing -B ./runs/full/ -cl 2 -nw 140083 -port 4038  &
tail -f ./runs/full/dhpyp.140083.2.4038.log
