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
	private HashMap<String, String> idLocations = new HashMap<>();
	
	// All the labels that occur in the training set
	private HashSet<String> trainingSetLabels = new HashSet<>();
	
	// Maps from location label to word to count of how many times that word occurs in that label
	// Note that these is not lambda-smoothed by default in order to save space
	private HashMap<String, HashMap<String, Integer>> labelWords = new HashMap<>();
	
	// Maps from location label to how many tweets had that location label in the training data
	private HashMap<String, Integer> labelCounts = new HashMap<>();
	
	// Maps from location label to the probability that any given tweet is a certain label
	private HashMap<String, Double> labelProbs = new HashMap<>();
	
	// A set of all the words that appear in the tweets
	private HashSet<String> vocab = new HashSet<>();
	
	// How many total words occur in each location label
	// Note that this is not lambda-smoothed by default
	private HashMap<String, Integer> labelWordCounts = new HashMap<>();
	
	public ModelTrainer(String locationsFileName, String tweetsFileName) {
		populateIDLocations(locationsFileName);
		
		// Pre-populate hashmaps with all locations from our data set as labels
		for (String locationLabel : idLocations.values()) {
			HashMap<String, Integer> inner = new HashMap<>();
			labelWords.put(locationLabel, inner);
			
			labelCounts.put(locationLabel, 0);
			labelWordCounts.put(locationLabel, 0);
		}
		
		try {
			BufferedReader trainingDataReader = new BufferedReader(new FileReader(tweetsFileName));	
			String tweetLine = trainingDataReader.readLine();
			int lineCount = 0;

			// Look at tweets line by line
			while (tweetLine != null) {
				lineCount++;
				String[] splitLine = tweetLine.split("\t");

				// Stop list processing???
				// Remove hashtags and treat the hashtag as a normal word?

				// Pass in lower-cased tweet text to String Reader so that in
				// can be tokenized 
				if (splitLine.length == 4 && splitLine[0].matches("[0-9]+") && splitLine[1].matches("[0-9]+")) {
					StringReader tweetText = new StringReader(splitLine[2].toLowerCase());

					PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(tweetText,
							new CoreLabelTokenFactory(), "americanize=false,untokenizable=noneDelete");

					String location = idLocations.get(splitLine[0]);
					if (idLocations.get(splitLine[0]) == null) {
						System.out.println("Null user ID on line " + lineCount + "; formatting error suspected");
						
					}
					
					trainingSetLabels.add(location);

					// Goes through each word of the tweet 
					// and adds it to appropriate data structures
					while (ptbt.hasNext()) {
						CoreLabel word = ptbt.next();

						// Add to vocab and appropriate hashtable entry
						vocab.add(word.toString());
						addToLabelWords(word.toString(), location);

						// Increment total amount of words in that label
						labelWordCounts.put(location, labelWordCounts.get(location) + 1);
					}

					labelCounts.put(location, labelCounts.get(location) + 1);
				}

				tweetLine = trainingDataReader.readLine();
			}
			
			populateLabelProbs();
			
			trainingDataReader.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Populate idLocations hashmap using the file provided
	 * @param fileName The name of the file that contains tweet IDs and their locations
	 */
	public void populateIDLocations(String fileName) {
		try {
			BufferedReader trainingDataReader = new BufferedReader(new FileReader(fileName));	
			String idLoc = trainingDataReader.readLine();

			while (idLoc != null) {
				String[] splitLine = idLoc.split("\t");
				
				// If we assume all same format, we can also split on ", "
				String[] splitLoc = splitLine[1].split(" ");
				
				idLocations.put(splitLine[0], splitLoc[splitLoc.length-1]);
				idLoc = trainingDataReader.readLine();
			}
			trainingDataReader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Populate the labelWords hashmap with the word in question
	 * associated with the particular label and its count 
	 * 
	 * @param word
	 * @param locationLabel
	 */
	public void addToLabelWords(String word, String locationLabel) {
		if (!labelWords.containsKey(locationLabel)) {
			// Not a location we've seen before
			// Decide how to handle it
		} else {
			if (labelWords.get(locationLabel).get(word) == null) {
				HashMap<String, Integer> inner = labelWords.get(locationLabel);
				inner.put(word, 1);
			} else {
				HashMap<String, Integer> inner = labelWords.get(locationLabel);
				inner.put(word, labelWords.get(locationLabel).get(word) + 1);
			}
		}
	}
	
	/*
	 * Calculates the probability of each label
	 * and populate the labelProbs hashmap
	 */
	public void populateLabelProbs() {
		// Populate label probs
		int denominator = 0;
		for (String locationLabel : labelCounts.keySet()) {
			denominator += labelCounts.get(locationLabel);
		}
		
		for (String locationLabel : labelCounts.keySet()) {
			int numerator = labelCounts.get(locationLabel);
			double prob = (double) numerator / (double) denominator;
			labelProbs.put(locationLabel, prob);
		}
	}
	
	public HashSet<String> getVocab() {
		return vocab;
	}
	
	public HashMap<String, HashMap<String, Integer>> getLabelWords() {
		return labelWords;
	}
	
	public HashMap<String, Integer> getLabelWordCounts() {
		return labelWordCounts;
	}
	
	public HashMap<String, String> getIDLocations() {
		return idLocations;
	}
	
	public HashMap<String, Double> getLabelProbs() {
		return labelProbs;
	}
	
	public HashSet<String> getTrainingSetLabels() {
		return trainingSetLabels;
	}
	
	public static void main(String[] args) {
		ModelTrainer tester = new ModelTrainer("data/training_set_users.txt", "data/training_set_tweets_1k.txt");
		// System.out.println(tester.idLocations);
	}
}
