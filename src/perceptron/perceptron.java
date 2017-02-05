package perceptron;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class Feature {
	public String name;
	public List<String> values;
   // HashMap<Integer, String> hmap = new HashMap<Integer, String>();

    
	public int indexOfValue(String valueName) {
		for (int i=0; i<values.size(); i++) {
			if (values.get(i).compareTo(valueName)==0) {
				return i;
			}
		}
		return -1;
	}
}


class Labels {
	public String[] names;
	public int indexOf(String labelName) {
		for (int i=0; i<names.length; i++) {
			if (names[i].compareTo(labelName)==0) {
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
		READ_FEATURE_NUMBER, READ_FEATURE_VALUE, READ_CLASS_LABELS, READ_EXAMPLE_COUNT, READ_EXAMPLES
	}

	
	private BufferedReader _buffReader;

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
		f.values =  values;
		return f;
	}
	
	
	private Labels readLables(String line) {
		return null;
	}

	
	public void readData() {
		String line;
		State readState = State.READ_FEATURE_NUMBER;

		int numFeature = 0;
		int featuresRead = 0;
		Feature f = null;
		List<Feature> features = new ArrayList<Feature>();
		Labels labels;
		
		try {
			while ((line = _buffReader.readLine()) != null) {
				
				if (line.trim().startsWith("//"))
					continue; // skip comment lines;
				System.out.println(line);

				line = line.trim();
				if (line.length()==0) continue;

				switch (readState) {

				case READ_FEATURE_NUMBER:
					numFeature = Integer.parseInt(line);
					System.out.println("# of feature" + numFeature);
					readState = State.READ_FEATURE_VALUE;
					break;

				case READ_FEATURE_VALUE:
					f = readOneFeature(line);
					features.add(f);
					featuresRead++;
					if (featuresRead == numFeature)
						readState = State.READ_CLASS_LABELS;
					break;

				case READ_CLASS_LABELS:
					
					break;

				case READ_EXAMPLE_COUNT:
					
					break;
					
				case READ_EXAMPLES:
					
					break;

				default:
					break;

				}
			}
		}
		catch (Exception ex) {
			System.out.println("Error:" + ex.getMessage());
		}
	}
}


public class perceptron {

	public static void main(String[] args) {
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		DataReader reader = new DataReader(args[0]);
		reader.readData();
	}

}