java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.AddCorpusFromIntFile localhost 4030 3 1 ../../../data/brownsou/brown.int
java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.AddCorpusFromIntFile localhost 4030 3 2 ../../../data/brownsou/sou_train.int
java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.Save localhost 4030 ./runs/brown_sou/brown_1_sou_2_per_level_switch_sharing_initialization.ser
#java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.Sample localhost 4030 100
#java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.Save localhost 4030 ./runs/brown_sou/brown_1_sou_2_per_level_switch_sharing_100_samples.ser
