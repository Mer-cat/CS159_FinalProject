package nlp.nbc;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;

/**
 * Learns a multinomial Naive Bayes model on Twitter data
 * and users' locations to be able to predict the U.S. state
 * a user is located at based on their tweet
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
	
	private HashSet<String> stoplist = new HashSet<>();  // Words to filter out from the data

	/**
	 * Constructor which iterates over training tweets
	 * and trains the model
	 * 
	 * @param locationsFileName Name of file containing twitter user IDs and locations
	 * @param tweetsFileName Name of file containing tweets
	 * @param stoplistFileName Name of file containing words to exclude when processing
	 */
	public ModelTrainer(String locationsFileName, String tweetsFileName, String stoplistFileName) {
		populateIDLocations(locationsFileName);

		// Pre-populate hashmaps with all locations from our data set as labels
		for (String locationLabel : idLocations.values()) {
			HashMap<String, Integer> inner = new HashMap<>();
			labelWords.put(locationLabel, inner);

			labelCounts.put(locationLabel, 0);
			labelWordCounts.put(locationLabel, 0);
		}

		try {
			BufferedReader stoplistReader = new BufferedReader(new FileReader(stoplistFileName));	
			String stopWord = stoplistReader.readLine();

			while (stopWord != null) {
				stoplist.add(stopWord);
				stopWord = stoplistReader.readLine();
			}

			stoplistReader.close();
			
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

					// Skip tweet if user is not accounted for in location data
					if (idLocations.get(splitLine[0]) != null) {

						String location = idLocations.get(splitLine[0]);
						trainingSetLabels.add(location);
						
						// Goes through each word of the tweet 
						// and adds it to appropriate data structures
						while (ptbt.hasNext()) {
							CoreLabel word = ptbt.next();
							String wordAsString = word.toString();
							if (!stoplist.contains(wordAsString) && passesFilter(wordAsString)) {
							
								// Add to vocab and appropriate hashtable entry
								vocab.add(wordAsString);
								addToLabelWords(wordAsString, location);

								// Increment total amount of words in that label
								labelWordCounts.put(location, labelWordCounts.get(location) + 1);
							}
						}
						labelCounts.put(location, labelCounts.get(location) + 1);
					}
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
		int thrownOutUsersCount = 0;
		LocationFilterHelper filterHelper = new LocationFilterHelper();

		try {
			BufferedReader trainingDataReader = new BufferedReader(new FileReader(fileName));	
			String idLoc = trainingDataReader.readLine();

			while (idLoc != null) {
				String[] splitLine = idLoc.split("\t");

				// If we assume all same format, we can also split on ", "
				String[] splitLoc = splitLine[1].split(", ");

				String lastPortion = splitLoc[splitLoc.length-1];

				// Filters out any entries that don't have a state in two-letter format
				if(lastPortion.matches("^[A-Z][A-Z]$")) {
					idLocations.put(splitLine[0], lastPortion);
				} else {

					if (filterHelper.getStateHashMap().get(lastPortion) != null) {
						idLocations.put(splitLine[0], filterHelper.getStateHashMap().get(lastPortion));
					} else if (filterHelper.getCitiesHashMap().get(lastPortion) != null){ 

						idLocations.put(splitLine[0], filterHelper.getCitiesHashMap().get(lastPortion));

					} 
				}

				idLoc = trainingDataReader.readLine();
			}
			trainingDataReader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Indicate whether a word passes filter or not
	 * in order to filter out links, etc.
	 * 
	 * @param word Word to check
	 * @return True if word should not be filtered out, false if it should be filtered out
	 */
	public boolean passesFilter(String word) {
		if (word.matches("^http.+")) {
			return false;
		} else if (word.matches("www\\..+")) {
			return false;
		}
		
		return true;
	}

	/**
	 * Helper method for finding which location was not included in training data
	 */
	public void findMissingState() {
		LocationFilterHelper filterHelper = new LocationFilterHelper();
		
		for (String label: filterHelper.getStateHashMap().values()) {
			if (!trainingSetLabels.contains(label)){
				System.out.println(label);
			}
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

	/**
	 * @return vocabulary hashset from training
	 */
	public HashSet<String> getVocab() {
		return vocab;
	}

	/**
	 * @return labelWords hashmap from training
	 */
	public HashMap<String, HashMap<String, Integer>> getLabelWords() {
		return labelWords;
	}

	/**
	 * @return labelWordCounts hashmap from training
	 */
	public HashMap<String, Integer> getLabelWordCounts() {
		return labelWordCounts;
	}

	/**
	 * @return idLocations hashmap from training which maps from
	 * a user ID to their location as a 2-letter state code
	 */
	public HashMap<String, String> getIDLocations() {
		return idLocations;
	}

	/**
	 * @return labelProbs hashmap from training
	 */
	public HashMap<String, Double> getLabelProbs() {
		return labelProbs;
	}

	/**
	 * @return trainingSetLabels hashset from training
	 * which is all the labels that occurred in the training
	 */
	public HashSet<String> getTrainingSetLabels() {
		return trainingSetLabels;
	}

	/**
	 * Main method for testing
	 * @param args not used
	 */
	public static void main(String[] args) {
		ModelTrainer tester = new ModelTrainer("data/training_set_users.txt", 
				"data/test_set_tweets_360k.txt", "data/smallStoplist");
		System.out.println(tester.trainingSetLabels);
		System.out.println(tester.trainingSetLabels.size());
	}
}
