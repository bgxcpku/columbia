java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.AddCorpusFromIntFile localhost 4038 3 1 ../../../data/full/euro.en.int
java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.AddCorpusFromIntFile localhost 4038 3 2 ../../../data/full/emea.en.int
java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.Save localhost 4038 ./runs/full/euro_1_emea_2_full_per_level_switch_sharing_initialization.ser
java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.Sample localhost 4038 100
java -classpath ./dist/LanguageModel.jar:./dist/lib edu.gatsby.nlp.lm.client.Save localhost 4038 ./runs/full/euro_1_emea_2_full_per_level_switch_sharing_100_samples.ser
