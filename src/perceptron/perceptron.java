package perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


class Feature {
	public String name;
	public List<String> values;

	public int indexOfValue(String valueName) {
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i).compareTo(valueName) == 0) {
				return i;
			}
		}
		return -1;
	}
}


class Labels {
	public List<String> names;

	public int indexOfLabel(String labelName) {
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).compareTo(labelName) == 0) {
				return i;
			}
		}
		return -1;
	}
}


class NerualNet {
	public double η = 0.0005;

	public double[] initialWeights(Instance instance) {

		int size = instance.x.length;
		double w = 0.0005; //Math.sqrt(1 / ((double) size));

		Random rand = new Random();

		double[] weights = new double[size];

		for (int i = 0; i < weights.length; i++) {
			weights[i] = rand.nextDouble()*0.5;
		}
		weights[0]=1;
		printWeight(weights);
		System.out.println("*****");
		return weights;
	}

	public double sigmoid(double x) {
		return ((double) 1) / (1 + Math.exp(-x));
	}

	public double dsigmoid(double x) {
		return sigmoid(x) * (1 - sigmoid(x));
	}

	public double dotProduct(double w[], double x[]) {
		double result = 0;
		for (int i = 0; i < w.length; i++) {
			result = result + w[i] * x[i];
		}
		return result;
	}

	public void printWeight(double weights[]) {
		System.out.printf("w = [");
		for (int i = 0; i < weights.length; i++) {
			System.out.printf("%2.8f ", weights[i]);
		}
		System.out.printf("]\n");
	}

	public void train(double weights[], List<Instance> examples, List<Instance> tune) {
		Instance inst;
		double y_hat;
		double delta_w;
		double fx;
		double y;
		double e;

		int maxloop = 10;

		for (int n = 0; n < maxloop; n++) {
			int correct = 0;

			for (int i = 0; i < examples.size(); i++) {
				
				inst = examples.get(i);
				y_hat = (dotProduct(weights, inst.x));
				fx = Math.signum(y_hat);
				y = (inst.label == 0) ? -1.0 : 1.0;
				e = y - fx;
				System.out.println("e:"+e);
				for (int k = 0; k < weights.length; k++) {
					weights[k] = weights[k] + η * e * weights[k] * inst.x[k];
				}
			}
			double acc = accuracy(weights, tune);
			System.out.printf("%2.4f\n", acc);
			printWeight(weights);
		}
	}

	public double accuracy(double weights[], List<Instance> test) {
		int correct = 0;
		for (int i = 0; i < test.size(); i++) {
			Instance inst = test.get(i);
			double y_hat = predict(weights, inst);
			//System.out.println(y);
			double y = (inst.label == 0) ? -1.0 : 1.0;
			//System.out.println("y_hat:" + y_hat);
			if (y == y_hat) {
				correct++;
			}
		}
		double rate = ((double) correct) / test.size();
		System.out.printf("correct:%d, size:%d, rate:%2.4f\n", correct, test.size(), rate);
		return rate;
	}

	public double predict(double weights[], Instance instance) {
		double y_hat = dotProduct(weights, instance.x);
		double fx = Math.signum(y_hat);
		return fx;
	}

}

class Instance {
	public String name;
	public int label;

	public void setFeatures(int[] features) {
		x = new double[features.length + 1];
		x[0] = 1;
		for (int i = 0; i < features.length; i++) {
			x[i + 1] = features[i];
		}
	}

	public double[] x = null;
}

class DataReader {

	private enum State {
		READ_FEATURE_NUMBER, READ_FEATURE_VALUE, READ_CLASS_LABELS, READ_EXAMPLE_COUNT, READ_EXAMPLES
	}

	private BufferedReader _buffReader;
	private Labels _labels;
	private List<Feature> _features;
	private List<Instance> _examples;

	public DataReader(String fileName) {
		try {
			FileReader freader = new FileReader(fileName);
			_buffReader = new BufferedReader(freader);
		}
		catch (Exception ex) {
			System.out.println("Error:" + ex.getMessage());
		}
	}

	private Feature readOneFeature(String feature) throws Exception {
		String[] parts = feature.split("-");

		if (parts.length != 2)
			throw new Exception("Invalid feature format.");
		if (parts[0].length() == 0)
			throw new Exception("Feature name is null.");

		String[] features = parts[1].split(" ");
		List<String> values = new ArrayList<String>();

		for (int i = 0; i < features.length; i++) {
			String s = features[i].trim();
			if (s.length() > 0) {
				values.add(s);
			}
		}

		Feature f = new Feature();
		f.name = parts[0];
		f.values = values;
		return f;
	}

	private Instance readOneInstance(String line, Labels lbls, List<Feature> features) {
		line = line.trim();
		line = line.replaceAll("\\s+", " ");
		// System.out.println(line);
		String[] pieces = line.split(" ");
		Instance instance = new Instance();
		instance.name = pieces[0];
		instance.label = lbls.indexOfLabel(pieces[1]);
		// instance.features = new int[pieces.length - 2];
		int[] x = new int[pieces.length - 2];
		for (int i = 0; i < x.length; i++) {
			x[i] = features.get(i).indexOfValue(pieces[i + 2]);
		}
		instance.setFeatures(x);
		return instance;
	}

	public void readData() {
		String line = "";
		State readState = State.READ_FEATURE_NUMBER;

		int numFeature = 0;
		int numExamples = 0;
		int featuresRead = 0;
		int examplesRead = 0;

		Feature feature = null;
		Instance instance = null;

		List<Feature> features = new ArrayList<Feature>();
		Labels labels = new Labels();
		labels.names = new ArrayList<String>();
		List<Instance> examples = new ArrayList<Instance>();

		boolean backup = false;
		boolean done = false;
		try {
			while (!done) {

				if (!backup) {
					line = _buffReader.readLine();
					if (line == null)
						break;
				}

				// skip commented lines;
				if (line.trim().startsWith("//"))
					continue;

				// System.out.println(line);

				line = line.trim();
				if (line.length() == 0)
					continue;

				switch (readState) {

				case READ_FEATURE_NUMBER:
					numFeature = Integer.parseInt(line);
					// System.out.println("# of feature" + numFeature);
					readState = State.READ_FEATURE_VALUE;
					break;

				case READ_FEATURE_VALUE:
					feature = readOneFeature(line);
					features.add(feature);
					featuresRead++;
					if (featuresRead == numFeature)
						readState = State.READ_CLASS_LABELS;
					break;

				case READ_CLASS_LABELS:
					if (line.matches("^-?\\d+$")) {
						numExamples = Integer.parseInt(line);
						// System.out.println("# of examples:" + numExamples);
						readState = State.READ_EXAMPLES;
						break;
					} else {
						// System.out.println("label:" + line);
						labels.names.add(line);
					}
					break;

				case READ_EXAMPLE_COUNT:

					break;

				case READ_EXAMPLES:
					if (examplesRead == numExamples)
						done = true;
					instance = readOneInstance(line, labels, features);
					examples.add(instance);
					examplesRead++;
					break;

				default:
					break;

				}
			}

			// for (int i = 0; i < examples.size(); i++) {
			// // printExample(examples.get(i));
			// }

			this._features = features;
			this._labels = labels;
			this._examples = examples;

		}
		catch (Exception ex) {
			System.out.println("Error:" + ex.getMessage());
		}
	}

	List<Feature> getFeatures() {
		return this._features;
	}

	Labels getLabels() {
		return this._labels;
	}

	List<Instance> getExamples() {
		return this._examples;
	}

	void createTrainSet() {
		HashMap<Integer, List<Feature>> classes = new HashMap<Integer, List<Feature>>();
		for (int i = 0; i < this._labels.names.size(); i++) {
			classes.put(i, new ArrayList<Feature>());
		}
		// classes.get
		// Map <String, List<Feature>> classes = new Map<String,
		// List<Feature>>();
		// this._labels.names.size();
		// List<Feature> positive = new ArrayList<Feature>();
		// List<Feature> positive = new ArrayList<Feature>();
	}

}

public class perceptron {

	private static boolean fileExist(String path) {
		File f = new File(path);
		if (f.exists() && !f.isDirectory()) {
			return true;
		}
		return false;
	}

	public static void printExample(Instance example, Labels labels, List<Feature> features) {
		System.out.printf("%10s", example.name);
		System.out.print(" ");
		System.out.printf("%10s", labels.names.get(example.label));
		System.out.print(" ");
		for (int i = 0; i < example.x.length; i++) {
			// System.out.print(features.get(i).values.get(example.x[i]) + " ");
		}
		System.out.println();
	}

	@SuppressWarnings("unchecked")
	public static List<Instance>[] partitionDataSet(List<Instance> data) {
		List<Instance>[] s = new ArrayList[2];

		s[0] = new ArrayList<Instance>();
		s[1] = new ArrayList<Instance>();

		for (int i = 0; i < data.size(); i++) {
			int index = data.get(i).label;
			s[index].add(data.get(i));
		}

		return s;
	}

	public static void splitDataSet(String filePath) {
		DataReader reader = new DataReader(filePath);
		try {
			reader.readData();
			List<Instance> examples = reader.getExamples();
			Collections.shuffle(examples);
			int train = (int) (examples.size() * 0.8);
			int tune = (int) (examples.size() - train);
			int test = 0;

			System.out.println("**train:" + train);
			List<Instance> trainSet = new ArrayList<Instance>();
			for (int i = 0; i < train; i++) {
				trainSet.add(examples.get(0));
				printExample(examples.get(0), reader.getLabels(), reader.getFeatures());
				examples.remove(0);
			}

			System.out.println("**tune:" + tune);
			List<Instance> tuneSet = new ArrayList<Instance>();
			for (int i = 0; i < tune; i++) {
				tuneSet.add(examples.get(0));
				printExample(examples.get(0), reader.getLabels(), reader.getFeatures());
				examples.remove(0);
			}

			System.out.println("**test:" + test);
			List<Instance> testSet = new ArrayList<Instance>();
			for (int i = 0; i < test; i++) {
				testSet.add(examples.get(0));
				printExample(examples.get(0), reader.getLabels(), reader.getFeatures());
				examples.remove(0);
			}
		}
		catch (Exception ex) {
			System.out.println("Error spliting data set into train, tune, and test set!");
		}
	}

	private static double calculateAccuracy(List<Instance> tuneSet, double[] weights) {
		return 0;
	}

	private static double sigmoid(double x) {
		return ((double) 1) / (1 + Math.exp(-x));
	}

	public static double dsigmoid(double x) {
		return sigmoid(x) * (1 - sigmoid(x));
	}

	public static void main(String[] args) {

		System.out.println(sigmoid(0));
		System.out.println(sigmoid(1));
		System.out.println(dsigmoid(0));

		// System.out.println("Working Directory = " +
		// System.getProperty(be"user.dir"));
		// splitDataSet(args[0]);
		// double w[] = NeuralNet.
		NerualNet net = new NerualNet();

		if (args.length < 3) {
			System.out.println("Usage: perceptron [train] [tune] [test]");
		}

		for (int i = 0; i < 3; i++) {
			if (!fileExist(args[0])) {
				System.out.println("'" + args[i] + "' does not exist.");
			}
		}

		try {
			DataReader train = new DataReader(args[0]);
			train.readData();
			DataReader tune = new DataReader(args[1]);
			tune.readData();
			DataReader test = new DataReader(args[2]);
			test.readData();
			System.out.printf("train:%d\n", train.getExamples().size());
			System.out.printf("tune:%d\n", tune.getExamples().size());
			System.out.printf("test:%d\n", test.getExamples().size());

			double w[] = net.initialWeights(train.getExamples().get(0));
			net.train(w, train.getExamples(), tune.getExamples());
			net.printWeight(w);
			// double acc = net.accuracy(w, test.getExamples());

		}
		catch (Exception ex) {
			System.out.print("Error reading data file:'" + args[0] + "'.\nDetails:" + ex.getMessage());
		}

	}

}