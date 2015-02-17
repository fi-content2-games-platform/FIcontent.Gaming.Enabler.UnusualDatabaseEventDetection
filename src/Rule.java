import java.util.Vector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class Rule {
	public String description;
	public String mysqlQuery;
	public float minValue;
	public float maxValue;
	public boolean lastCheckAlert; // the last time this was checked an alert was raised (set this yourself)

	public Rule() {
		lastCheckAlert = false;
	}

	public static boolean readFile(String fileName, Vector<Rule> v) {
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			StringBuilder sb = new StringBuilder();
			int state = 0;
			Rule r = new Rule();

			String line;
			while ((line = br.readLine()) != null) {
				switch(state) {
					case 0: r.description = line;
						break;
					case 1: r.mysqlQuery = line;
						break;
					case 2: r.minValue = Float.parseFloat(line);
						break;
					case 3:
						r.maxValue = Float.parseFloat(line);
						v.addElement(r);
						if (Uded.DEBUG) System.out.println("added rule for: " + r.description);
						break;
				}
				if (state < 3) {
					state++;
				} else {
					r = new Rule();
					state = 0;
				}
			}
			String everything = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}