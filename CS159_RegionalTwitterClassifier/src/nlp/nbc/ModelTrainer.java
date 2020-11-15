package nlp.nbc;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;

/**
 * 
 * @author Magali Ngouabou, Helen Paulini, Mercy Bickell
 * CS159 - Final Project
 *
 */
public class ModelTrainer {
	
	// Maps from string of twitter user ID to location of user
	HashMap<String, String> idLocations = new HashMap<>();
	
	HashMap<String, HashMap<String, Integer>> labelWords = new HashMap<>();
	HashMap<String, Double> labelProbs = new HashMap<>();
	HashSet<String> vocab = new HashSet<>();
	HashMap<String, Integer> labelWordCounts = new HashMap<>();
	double lambda = 0;
	
	
	public ModelTrainer(String filename) {
		try {
			BufferedReader trainingDataReader = new BufferedReader(new FileReader(filename));	
			String tweetLine = trainingDataReader.readLine();

			while (tweetLine != null) {

				String[] splitLine = tweetLine.split("\t");
				
				// Stop list processing???
				// Remove hashtags and treat the hashtag as a normal word?
				
				// Pass in lower-cased tweet text to String Reader so that in
				// can be tokenized 
				StringReader tweetText = new StringReader(splitLine[2].toLowerCase());

				PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(tweetText,
						new CoreLabelTokenFactory(), "americanize=false");
				while (ptbt.hasNext()) {
					CoreLabel label = ptbt.next();
					System.out.println(label);
					// Add to appropriate hashtable entry
				}
				tweetLine = trainingDataReader.readLine();
			}

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) {
		ModelTrainer tester = new ModelTrainer("data/testFile.txt");
	}
	
}
