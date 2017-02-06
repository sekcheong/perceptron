package perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


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


class Instance {
	public String name;
	public int label;
	public int[] features;
}


class DataReader {

	private enum State {
		READ_FEATURE_NUMBER, 
		READ_FEATURE_VALUE, 
		READ_CLASS_LABELS, 
		READ_EXAMPLE_COUNT, 
		READ_EXAMPLES
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
		//System.out.println(line);
		String[] pieces = line.split(" ");
		Instance instance = new Instance();
		instance.name = pieces[0];
		instance.label = lbls.indexOfLabel(pieces[1]);
		instance.features = new int[pieces.length - 2];
		for (int i = 0; i < instance.features.length; i++) {
			instance.features[i] = features.get(i).indexOfValue(pieces[i + 2]);
		}
		return instance;
	}

	@SuppressWarnings("unused")
	private void printExample(Instance example) {
		System.out.print(example.name);
		System.out.print(" ");
		System.out.print(example.label);
		System.out.print(" ");
		for (int i = 0; i < example.features.length; i++) {
			System.out.print(example.features[i] + " ");
		}
		System.out.println();
	}
	

	public void readData() {
		String line = "";
		State readState = State.READ_FEATURE_NUMBER;

		int numFeature = 0;
		int numExamples = 0;
		int featuresRead = 0;

		Feature feature = null;
		Instance instance = null;

		List<Feature> features = new ArrayList<Feature>();
		Labels labels = new Labels();
		labels.names = new ArrayList<String>();
		List<Instance> examples = new ArrayList<Instance>();

		boolean backup = false;

		try {
			while (true) {

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
					//System.out.println("# of feature" + numFeature);
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
						//System.out.println("# of examples:" + numExamples);
						readState = State.READ_EXAMPLES;
						break;
					} else {
						//System.out.println("label:" + line);
						labels.names.add(line);
					}
					break;

				case READ_EXAMPLE_COUNT:

					break;

				case READ_EXAMPLES:
					instance = readOneInstance(line, labels, features);
					examples.add(instance);

					break;

				default:
					break;

				}
			}

			for (int i = 0; i < examples.size(); i++) {
				//printExample(examples.get(i));
			}

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

}




public class perceptron {
	
	private static boolean fileExist(String path) {
		File f = new File(path);
		if(f.exists() && !f.isDirectory()) { 
		    return true;
		}
		return false;
	}

	public static void main(String[] args) {
		//System.out.println("Working Directory = " + System.getProperty("user.dir"));
		if (args.length<1) {
			System.out.println("Usage: perceptron [train] [tune] [test]");
		}
		
		if (!fileExist(args[0])) {
			System.out.println("'" + args[0] + "' does not exist.");
		}
		
		try {
			DataReader reader = new DataReader(args[0]);
			reader.readData();
		}
		catch (Exception ex) {
			System.out.print("Error reading data file:'" + args[0] + "'.");
		}
		
	}

}