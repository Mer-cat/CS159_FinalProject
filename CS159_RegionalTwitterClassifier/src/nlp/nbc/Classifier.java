package nlp.nbc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

/**
 * 
 * @author Magali Ngouabou, Helen Paulini, Mercy Bickell
 * CS159 - Final Project
 *
 */
public class Classifier {
	
	ModelTrainer model;
	
	// Lambda value for smoothing
	private double lambda = 0;
	
	// Problems with IL!
	public Classifier(ModelTrainer model, double lambda, String testSetFileName) {
		this.model = model;
		this.lambda = lambda;
		
		try {
			BufferedReader testDataReader = new BufferedReader(new FileReader(testSetFileName));	
			String tweetLine = testDataReader.readLine();
			
			while (tweetLine != null) {
				String[] splitLine = tweetLine.split("\t");
				String testTweet = splitLine[2];
				predictLabel(testTweet);
				tweetLine = testDataReader.readLine();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gives the theta value of a specific word and label
	 * with lambda smoothing
	 * 
	 * @param label The label whose tweets are to be looked at
	 * @param word The word whose theta we are calculating
	 * @return The theta value for the given word and label
	 */
	public double calculateTheta(String label, String word) {		
		double wordCount = 0.0;
		if (model.getLabelWords().get(label).get(word) != null) {
			wordCount = model.getLabelWords().get(label).get(word) + lambda;
		} else {
			wordCount = lambda;
		}
		double totalWordsInLabel = model.getLabelWordCounts().get(label) + lambda * model.getVocab().size();
		return wordCount / totalWordsInLabel;
	}
	
	/**
	 * Prints out the predicted label and the log-prob of that
	 * label for a given sentence
	 * 
	 * @param tweet Tweet to be classified
	 */
	public void predictLabel(String tweet) {
		tweet.toLowerCase();
		StringReader tweetText = new StringReader(tweet);

		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(tweetText,
				new CoreLabelTokenFactory(), "americanize=false");
		
		Hashtable<String, Double> wordOccurrences = new Hashtable<>();
		
		// Goes through each word of the tweet 
		// and adds it to appropriate data structures
		while (ptbt.hasNext()) {
			CoreLabel word = ptbt.next();
			String wordAsString = word.toString();
			if (wordOccurrences.get(wordAsString) == null) {
				wordOccurrences.put(wordAsString, 1.0);
			} else {
				wordOccurrences.put(wordAsString, wordOccurrences.get(wordAsString) + 1);
			}
			
		}
		
		
			
		double posLogProbSum = 0;
		double negLogProbSum = 0;
		HashMap<String, Double> logProbSums = new HashMap<>();
		
		for (String label : model.getIDLocations().values()) {
			logProbSums.put(label, 0.0);
		}
		
		for (String word : wordOccurrences.keySet()) {
			if (model.getVocab().contains(word)) {
				
				// Calculate theta value for this word with every label
				// And add it to the running log prob sum for that label
				for (String label : logProbSums.keySet()) {
					double thetaValue = calculateTheta(label, word);
					double product = wordOccurrences.get(word) * Math.log10(thetaValue);
					logProbSums.put(label, logProbSums.get(label) + product);
				}
			}
		}
		
		// We may just be able to reuse logProbSums instead making this new hashmap
		// But we are erring on the side of caution
		HashMap<String, Double> finalLogProbs = new HashMap<>();
		
		for (String label : logProbSums.keySet()) {
			double finalLogProb = logProbSums.get(label) + Math.log10(model.getLabelProbs().get(label));
			finalLogProbs.put(label, finalLogProb);
		}
		
		// Now, take the max of the values and print it with its key
		Map.Entry<String, Double> maxEntry = null;

		for (Map.Entry<String, Double> entry : finalLogProbs.entrySet()) {
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
				maxEntry = entry;
			}
		}
		
		System.out.println(maxEntry.getKey() + "\t" + maxEntry.getValue());
	}
	
	public static void main(String[] args) {
		ModelTrainer model = new ModelTrainer("data/test_locs.txt", "data/testFile.txt");
		Classifier classifier = new Classifier(model, 0.0, "data/testFile.txt");
	}

}
