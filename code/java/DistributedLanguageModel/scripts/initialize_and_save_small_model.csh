java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.AddCorpusFromIntFile localhost 4040 3 1 ../../../data/small/euro.en.int
java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.AddCorpusFromIntFile localhost 4040 3 2 ../../../data/small/emea.en.int
java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.Save localhost 4040 ./runs/small/euro_1_emea_2_small_per_level_switch_sharing_initialization.ser
java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.Sample localhost 4040 100
java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.Save localhost 4040 ./runs/small/euro_1_emea_2_small_per_level_switch_sharing_100_samples.ser
