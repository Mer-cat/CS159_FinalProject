package nlp.nbc;

import java.io.*;
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

	public ModelTrainer(String filename) {
		try {
			BufferedReader trainingDataReader = new BufferedReader(new FileReader(filename));	
			String tweetLine = trainingDataReader.readLine();

			while (tweetLine != null) {

				String[] splitLine = tweetLine.split("\t");
				StringReader tweetText = new StringReader(splitLine[2]);

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
