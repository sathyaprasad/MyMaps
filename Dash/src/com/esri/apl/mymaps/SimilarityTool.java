package com.esri.apl.mymaps;

import java.util.ArrayList;
import java.util.Locale;

public class SimilarityTool {

	public double CompareStrings(String str1, String str2) {
		ArrayList<String> pairs1 = WordLetterPairs(str1);
		ArrayList<String> pairs2 = WordLetterPairs(str2);
		int intersection = 0;
		int union = pairs1.size() + pairs2.size();
		for (int i = 0; i < pairs1.size(); i++) {
			for (int j = 0; j < pairs2.size(); j++) {
				if (pairs1.get(i) == pairs2.get(j)) {
					intersection++;
					pairs2.remove(j);
					break;
				}
			}
		}
		return (2.0 * intersection) / union;
	}

	private ArrayList<String> WordLetterPairs(String str) {
		ArrayList<String> allPairs = new ArrayList<String>();
		String[] words = str.toUpperCase(Locale.ENGLISH).split("\\s");
		for (int i = 0; i < words.length; i++) {
			if (words[i] == null || words[i].isEmpty()) {
				String[] pairsInWord = LetterPairs(words[i]);
				for (int j = 0; j < pairsInWord.length; j++) {
					allPairs.add(pairsInWord[j]);
				}
			}
		}
		return allPairs;
	}

	private String[] LetterPairs(String str) {
		int numPairs = str.length() - 1;
		String[] pairs = new String[numPairs];
		for (int i = 0; i < numPairs; i++) {
			pairs[i] = str.substring(i, 2);
		}
		return pairs;
	}
}
