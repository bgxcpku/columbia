The JAVA LM server requires JAVA version 6 or higher.

Included in the SVN repository is the release JAR.  This may not always be the
most recent build of all of the classes.  If it is not, one should re-build the jar
either using ant or netbeans or javac directly.  The classpath in the following
should be adjusted accordingly.

To launch the JAVA LM server, assuming that the jar file can be found in <jarfiledir>
issue the command:

java -classpath <jarfiledir>/LanguageModel.jar edu.gatsby.nlp.lm.server.Server {args}

Where the set of {args} takes the form:

 -arch <single_switch, per_level_switch_sharing, hpyp_switch>   model "switch" arch (only per_level_switch_sharing is supported currently)
 -B <directory>                                                 base directory
 -cl <num>                                                      context length
 -L <file>                                                      log file (optional - default resides in base directory)
 -nw <num>                                                      num words in dictionary
 -onehpyp                                                       use a single hpyp instead of an hhpyp
 -port <num>                                                    port
 -S <directory>                                                 serialization directory (defaults to base directory)


To avoid conflicts Frank will use port number 4040, Hal will use 4041.

Example clients are provided.  These can be invoked by similar commands, for instance:

java -classpath <jarfiledir>/LanguageModel.jar edu.gatsby.nlp.lm.client.Add <serverhostname> <server port> <domain> <count> <context 1> <context 2> .... <token>

One may replace Add in this example with any of {Save, Load, Shutdown, Sample,
Score, GetParameters, AddCorpusFromIntFile, PredictAndLookupSequence, and Predict}.  Usage documentation for each will appear if invoked with
no arguments.