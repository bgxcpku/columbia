java -Xmx1g -Xms1g -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.server.Server -arch per_level_switch_sharing -B ./runs/small/ -cl 2 -nw 140082 -port 4040  &
tail -f ./runs/small/dhpyp.140082.2.4040.log
