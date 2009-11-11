java -Xmx8g -Xms8g -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.server.Server -arch per_level_switch_sharing -B ./runs/brown_sou/ -cl 2 -nw 51851 -port 4030  &
tail -f ./runs/brown_sou/dhpyp.51851.2.4030.log
