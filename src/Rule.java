// A rule is a mySQL query string that should return a number, a range of expected values and a brief description.

/** \page ExampleRulesFile
 Example rules file.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
max(ARTreasureHunt totalTime)
SELECT MAX(totalTime) FROM mygame.ARTreasureHunt
2000
10000
min(augmentedresistance totalTime)
SELECT MIN(totalTime) FROM mygame.ARTreasureHunt
2000
10000
max(ARTreasureHunt puzzleTime)
SELECT MAX(puzzleTime) FROM mygame.ARTreasureHunt
200
2000
min(augmentedresistance puzzleTime)
SELECT MIN(puzzleTime) FROM mygame.ARTreasureHunt
200
2000
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*/

import java.util.Vector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
A rule is a mySQL query string that should return a number, a range of expected values and a brief description.
Rules are declared in a text file.
 */

public class Rule {
	public String description;
	public String mysqlQuery;
	public float minValue;
	public float maxValue;
	public boolean lastCheckAlert; // the last time this was checked an alert was raised (set this yourself)
	public HistoryBuffer histBuff; // to keep a log of values

	static final int statusHTMLWidth = 800; // in pixels
	static final int statusHTMLHeight = 120;

	public final int MAX_HIST_ENTRIES = 100; // how many entries are cached/saved at most

	public Rule() {
		lastCheckAlert = false;
		histBuff = new HistoryBuffer(MAX_HIST_ENTRIES);
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

	private float convertY(float v, float yMin, float yMax) {
		return (v-minValue)/(maxValue-minValue) * (yMax-yMin) + yMin;
	}
	StringBuilder statusHTML() {
		StringBuilder result = new StringBuilder(40);
		result.append("<div>");
		result.append(description + "<br>");

		float range = maxValue-minValue;

		// coordinate system for svg is pixels, (0,0) is top left
		result.append("<svg width=\""+statusHTMLWidth+"\" height=\""+statusHTMLHeight+"\" style=\"background-color:#EEE\">");

		int x0 = 80; // position of x = 0
		int yMin = (int) (0.8*statusHTMLHeight);
		result.append("<text x=\""+(x0-8)+"\" y=\""+yMin+"\" font-size=\"16px\" text-anchor=\"end\" dominant-baseline=\"middle\">"+minValue+"</text>");
		result.append("<line x1=\""+(x0-5)+"\" y1=\""+yMin+"\" x2=\""+statusHTMLWidth+"\" y2=\""+yMin+"\" style=\"stroke:#002266;\"/>");
		int yMax = (int) (0.2*statusHTMLHeight);
		result.append("<text x=\""+(x0-8)+"\" y=\""+yMax+"\" font-size=\"16px\" text-anchor=\"end\" dominant-baseline=\"middle\">"+maxValue+"</text>");
		result.append("<line x1=\""+(x0-5)+"\" y1=\""+yMax+"\" x2=\""+statusHTMLWidth+"\" y2=\""+yMax+"\" style=\"stroke:#002266;\"/>");

		float xw = (statusHTMLWidth-x0)/(float) histBuff.linkedlist.size();

		float xPos = x0;
		for (HistoryBufferEntry h : Reversed.reversed(histBuff.linkedlist)) {
			//result.append((!Float.isNaN(h.v) ? h.v : "-") + ", ");
			if (!Float.isNaN(h.v)) {
				// value exists/could be read
				float y1 = convertY(0, yMin, yMax);
				float y2 = convertY(h.v, yMin, yMax);
				result.append("<rect x=\""+xPos+"\" y=\""+y2+"\" width=\""+(0.9*xw)+"\" height=\""+(y1-y2)+"\" style=\""+ (h.isInRange?"fill:rgb(100,255,120);stroke:rgb(0,255,0);stroke-width:1px":"fill:rgb(255,150,100);stroke:rgb(255,0,0);stroke-width:1px") +"\" />");
			} else {
				// error
				result.append("<rect x=\""+xPos+"\" y=\"2\" width=\"0.9\" height=\""+statusHTMLHeight+"\" style=\"fill:rgb(255,100,100);stroke-width:1px\" />");
			}
			xPos+=xw;
		}

		result.append("Sorry, your browser does not support inline SVG.</svg>");

		result.append("</div>\n");
		return result;
	}
}