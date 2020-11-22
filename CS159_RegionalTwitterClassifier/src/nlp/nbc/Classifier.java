package nlp.nbc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

/**
 * Class which predicts the label (aka U.S. state) for any inputted tweet
 * 
 * @author Magali Ngouabou, Helen Paulini, Mercy Bickell
 * CS159 - Final Project
 *
 */
public class Classifier {
	
	ModelTrainer model;
	
	// Lambda value for smoothing
	private double lambda = 0;
	int correctCount = 0;
	int correctMajorityCount = 0;
	int totalPredictions = 0;
	private String majorityState;
	private HashMap<String, Integer> correctCountByState = new HashMap<>();
	private HashMap<String, Integer> totalOccurrencesByState = new HashMap<>();
	
	/**
	 * Constructor which initializes data and predicts the label
	 * for each tweet given in the test set
	 * 
	 * @param model The trained model which is used to predict the label
	 * @param lambda The lambda value which is used to smooth the data
	 * @param testSetFileName The name of the file that contains the test tweets
	 */
	public Classifier(ModelTrainer model, double lambda, String testSetFileName) {
		this.model = model;
		this.lambda = lambda;
		majorityState = majorityState();
		
		for (String label : model.getTrainingSetLabels()) {
			correctCountByState.put(label, 0);
			totalOccurrencesByState.put(label, 0);
		}
		
		try {
			BufferedReader testDataReader = new BufferedReader(new FileReader(testSetFileName));	
			String tweetLine = testDataReader.readLine();

			while (tweetLine != null) {
				String[] splitLine = tweetLine.split("\t");
				if (splitLine.length == 4 && splitLine[0].matches("[0-9]+") && splitLine[1].matches("[0-9]+")) { // match to model trainer
					String testTweet = splitLine[2];
					predictLabel(testTweet, splitLine[0]);  // Pass in the tweet and the person ID
				} else {
					System.out.println("No prediction due to improper formatting");
				}
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
		
		double totalWordsInLabel = model.getLabelWordCounts().get(label) + (lambda * model.getVocab().size());
		return wordCount / totalWordsInLabel;
	}
	
	/**
	 * Prints out the predicted label and the log-prob of that
	 * label for a given sentence
	 * 
	 * @param tweet Tweet to be classified
	 */
	public void predictLabel(String tweet, String personID) {
		if (model.getIDLocations().get(personID) != null) {
			String realLocation = model.getIDLocations().get(personID); // get the real location of the person

			tweet.toLowerCase();
			StringReader tweetText = new StringReader(tweet);


			PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(tweetText,
					new CoreLabelTokenFactory(), "americanize=false,untokenizable=noneDelete");

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

			HashMap<String, Double> logProbSums = new HashMap<>();

			// Populating based off labels that occur in the training set
			// Prevents issues with trying to predict for labels
			// That weren't in the training set
			for (String label : model.getTrainingSetLabels()) {
				logProbSums.put(label, 0.0);
			}

			// Note that a lambda value > 0 MUST be used to prevent 
			// log of 0 operations
			for (String word : wordOccurrences.keySet()) {
				if (model.getVocab().contains(word)) {

					// Calculate theta value for this word with every label
					// And add it to the running log prob sum for that label
					for (String label : logProbSums.keySet()) {
						double thetaValue = calculateTheta(label, word);

						// Debugging print statement - remove before submission
						//System.out.println("Theta value for label: " + label + " and word: " + word + ": " + thetaValue);
						double product = wordOccurrences.get(word) * Math.log10(thetaValue);
						logProbSums.put(label, logProbSums.get(label) + product);
					}
				}
			}

			// We may just be able to reuse logProbSums instead making this new hashmap
			// But we are erring on the side of caution
			HashMap<String, Double> finalLogProbs = new HashMap<>();

			for (String label : logProbSums.keySet()) {
				double labelProb = Math.log10(model.getLabelProbs().get(label));

				// Debugging print statement - remove before submission
				// System.out.println("Label prob:" + labelProb+  " for label: " + label);			

				double finalLogProb = logProbSums.get(label) + labelProb;
				finalLogProbs.put(label, finalLogProb);
			}

			// Now, take the max of the values and print it with its key
			Map.Entry<String, Double> maxEntry = null;

			for (Map.Entry<String, Double> entry : finalLogProbs.entrySet()) {
				if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
					maxEntry = entry;
				}
			}
			
			trackAccuracy(maxEntry.getKey(), realLocation);

			// Write into file instead
			System.out.println(maxEntry.getKey() + "\t" + maxEntry.getValue());
		}
	}
	
	/**
	 * Track correct counts and total counts overall and by state
	 * for accuracy evaluation
	 * 
	 * @param predictedLabel The label the model predicted a tweet to have
	 * @param actualLabel The label the tweet actually had
	 */
	public void trackAccuracy(String predictedLabel, String actualLabel) {
		if(predictedLabel.equals(actualLabel)) {
			correctCount++;
			correctCountByState.put(actualLabel, correctCountByState.get(actualLabel) + 1);
		}

		if (majorityState.equals(actualLabel)) {
			correctMajorityCount++;
		}

		totalPredictions++;
		totalOccurrencesByState.put(actualLabel, totalOccurrencesByState.get(actualLabel) + 1);
	}
	
	/**
	 * Finds the state which occurred the most times in the training data
	 * 
	 * @return The label/state that occurs the most
	 */
	public String majorityState() {
		Map.Entry<String, Double> maxEntry = null;

		for (Map.Entry<String, Double> entry : model.getLabelProbs().entrySet()) {
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
				maxEntry = entry;
			}
		}
		
		return maxEntry.getKey();
	}
	
	/**
	 * Calculates the accuracy of the model if it just predicted
	 * the state that occurs the most all the time
	 * 
	 * @return The majority accuracy of the model as a decimal proportion
	 */
	public double majorityAccuracy() {
		System.out.println("Correct majority label predictions count: " + this.correctMajorityCount);
		return (double) correctMajorityCount / (double) totalPredictions;
	}
	
	/**
	 * Calculates the accuracy per state, then
	 * averages these accuracies
	 * 
	 * @return The average of the accuracy of each state 
	 */
	public double microAccuracy() {
		double sumOfAccuracies = 0.0;
		int validLabelCount = 0;
		for (String label : model.getTrainingSetLabels()) {
			if (totalOccurrencesByState.get(label) > 0) {
				double accuracy = (double) correctCountByState.get(label) / (double) totalOccurrencesByState.get(label);
				System.out.println("Accuracy for " + label + " :" + accuracy);
				sumOfAccuracies += accuracy;
				validLabelCount++;
			} else {
				System.out.println("No occurrences of " + label + " in test data, so excluded from micro-accuracy");
			}
		}
		
		return sumOfAccuracies / (double) validLabelCount;
	}
	
	/**
	 * Gives the accuracy of the model as measured by
	 * the amount of correct predictions over the total amount
	 * of predictions
	 * 
	 * @return The accuracy as a decimal number
	 */
	public double macroAccuracy() {
		System.out.println("Correct predictions count: " + this.correctCount);
		System.out.println("Total predictions: " + this.totalPredictions);
		return (double) correctCount/(double) totalPredictions;
	}
	
	public void mostPredictiveWords() {
		
		
	}
	
	/**
	 * Main method for running code
	 * 
	 * @param args The training set users file, the training set for the model, the stoplist,
	 * the lambda value, and then the file containing the test set of tweets
	 */
	public static void main(String[] args) {
		ModelTrainer model = new ModelTrainer("data/training_set_users.txt", "data/training_set_tweets_2mil.txt",
				"data/smallStoplist");
		Classifier classifier = new Classifier(model, 0.01, "data/test_set_tweets_10k.txt");
		System.out.println("Macro accuracy: " + classifier.macroAccuracy());
		System.out.println("Majority accuracy: " + classifier.majorityAccuracy());
		System.out.println("Micro accuracy: " + classifier.microAccuracy());
		System.out.print(classifier.majorityState());
	}
}
