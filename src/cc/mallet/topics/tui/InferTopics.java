package cc.mallet.topics.tui;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.topics.*;

import java.io.*;

public class InferTopics {

    static CommandOption.String inferencerFilename = new CommandOption.String
        (InferTopics.class, "inferencer", "FILENAME", true, null,
		 "A serialized topic inferencer from a trained topic model.\n" + 
         "By default this is null, indicating that no file will be read.", null);

	static CommandOption.String inputFile = new CommandOption.String
		(InferTopics.class, "input", "FILENAME", true, null,
		 "The filename from which to read the list of instances\n" +
		 "for which topics should be inferred.  Use - for stdin.  " +
		 "The instances must be FeatureSequence or FeatureSequenceWithBigrams, not FeatureVector", null);
	
    static CommandOption.String docTopicsFile = new CommandOption.String
        (InferTopics.class, "output-doc-topics", "FILENAME", true, null,
         "The filename in which to write the inferred topic\n" +
		 "proportions per document.  " +
         "By default this is null, indicating that no file will be written.", null);

    static CommandOption.Double docTopicsThreshold = new CommandOption.Double
		(InferTopics.class, "doc-topics-threshold", "DECIMAL", true, 0.0,
         "When writing topic proportions per document with --output-doc-topics, " +
         "do not print topics with proportions less than this threshold value.", null);

    static CommandOption.Integer docTopicsMax = new CommandOption.Integer
        (InferTopics.class, "doc-topics-max", "INTEGER", true, -1,
         "When writing topic proportions per document with --output-doc-topics, " +
         "do not print more than INTEGER number of topics.  "+
         "A negative value indicates that all topics should be printed.", null);

	static CommandOption.Integer numIterations = new CommandOption.Integer
        (InferTopics.class, "num-iterations", "INTEGER", true, 100,
         "The number of iterations of Gibbs sampling.", null);

    static CommandOption.Integer sampleInterval = new CommandOption.Integer
        (InferTopics.class, "sample-interval", "INTEGER", true, 10,
         "The number of iterations between saved samples.", null);

    static CommandOption.Integer burnInIterations = new CommandOption.Integer
        (InferTopics.class, "burn-in", "INTEGER", true, 10,
         "The number of iterations before the first sample is saved.", null);

	//This was not quite as advertised.
	//The default 0 was using the random seed stored in the trained model.
	//Modified 2015/10/29 to make it generate a new random seed from the clock. -ES
    static CommandOption.Integer randomSeed = new CommandOption.Integer
        (InferTopics.class, "random-seed", "INTEGER", true, 0,
         "The random seed for the Gibbs sampler.  Default is 0, which will use the clock.", null);


	//added 2015/10/08 -ES	
    static CommandOption.String topicKeysFilepath = new CommandOption.String
        (InferTopics.class, "topic-keys-filepath", "FILENAME", true, null,
         "The trainX-llda.keys filepath that lists topics and their words and weights\n" +
         "By default this is null, indicating that the file cannot be used.", null);


	public static void main (String[] args) {

        // Process the command-line options                                                                           
		CommandOption.setSummary (InferTopics.class,
                                  "Use an existing topic model to infer topic distributions for new documents");
        CommandOption.process (InferTopics.class, args);
		
		if (inferencerFilename.value == null) {
			System.err.println("You must specify a serialized topic inferencer. Use --help to list options.");
			System.exit(0);
		}

		if (inputFile.value == null) {
			System.err.println("You must specify a serialized instance list. Use --help to list options.");
			System.exit(0);
		}

		try {
			
			//The inferencer stored to a file will retain its random seed Randoms.
			TopicInferencer inferencer = TopicInferencer.read(new File(inferencerFilename.value));

			InstanceList instances = InstanceList.load (new File(inputFile.value));

			if (randomSeed.value != 0) {
				System.out.println("randomSeed.value is " + randomSeed.value);
				inferencer.setRandomSeed(randomSeed.value);
			}
			//This added 2015/010/29 to make the topic inferencer generate a new random seed from the clock
			//if a non-zero value is not passed in.
			//This could still be modified to look for a negative number or something if we want to 
			//re-use the seed stored with the inferencer in inferencerFilename.
			//-ES 2015/10/29
			else{
				inferencer.setNewRandomSeedFromClock();
			}
			System.out.println("randomSeed: " + inferencer.getRandoms() + "   " + inferencer.getRandoms().nextInt(100000));
			System.out.println("randomSeed: " + inferencer.getRandoms() + "   " + inferencer.getRandoms().nextInt(100000));
			

			inferencer.writeInferredDistributions(instances, new File(docTopicsFile.value),
												  numIterations.value, sampleInterval.value,
												  burnInIterations.value,
												  docTopicsThreshold.value, docTopicsMax.value, topicKeysFilepath.value);
			

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}
}
