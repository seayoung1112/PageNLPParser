package learnbyread.core;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class TextProcessor {

	public TextProcessor() {
		super();
		_wordsCount = 0;
	}
	
	private int _wordsCount;
	private static TreebankLanguagePack tlp;

	private static final String EMPTY_SENTENCE = "";
	private static final String APOSTROPHE = "\u0027";
	private static final String LEFT_DOUBLE_QUOTATION_MARK = "``";
	private static final String RIGHT_DOUBLE_QUOTATION_MARK = "''";
	private static final String LEFT_DOUBLE_QUOTATION = "Ò";
	private static final String RIGHT_DOUBLE_QUOTATION = "Ó";
	private static final String SPACE = " ";
	private static final char CHAR_SPACE = ' ';

	public List<String> process(String article) {

		StringReader reader = new StringReader(article);

		// init nlp module
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		tlp = new PennTreebankLanguagePack();
		TokenizerFactory<? extends HasWord> tf = tlp.getTokenizerFactory();
		dp.setTokenizerFactory(tf);

		// rebuild sentences
		List<String> result = new ArrayList<String>();
		String tempSentence = EMPTY_SENTENCE;
		for (List<HasWord> wordList : dp) {
			tempSentence += sentenceBuilder(wordList);
			// if the quotation isn't finished, append SPACE after the sentence and continue.
			if (!quotationFinished(tempSentence)) {
				tempSentence += SPACE;
				continue;
			}
			result.add(tempSentence);
			tempSentence = EMPTY_SENTENCE;
		}

		return result;
	}
	
	public int getWordsCount()
	{
		return _wordsCount;
	}
	/**
	 * Build sentence with words and punctuation marks.
	 * 
	 */
	private String sentenceBuilder(List<HasWord> wordList) {
		StringBuffer sb = new StringBuffer();
		for (HasWord word : wordList) {
			// left quotation mark
			if (word.word().equals(LEFT_DOUBLE_QUOTATION_MARK)) {
				sb.append(LEFT_DOUBLE_QUOTATION);
			}
			else if (word.word().equals(RIGHT_DOUBLE_QUOTATION_MARK)) {
				sb = deleteLastSpace(sb);
				sb.append(RIGHT_DOUBLE_QUOTATION);
				sb.append(SPACE);
			}
			// Two cases:
			// 1. apostrophe in between, i.e. it's
			// 2. apostrophe in the end, i.e. workers'
			else if (word.word().contains(APOSTROPHE)) {
				sb = deleteLastSpace(sb);
				sb.append(word);
				sb.append(SPACE);
			}
			// String which contains letter
			else if (Pattern.compile("(?i)[a-z0-9]").matcher(word.word()).find()) {
				sb.append(word);
				sb.append(SPACE);
				_wordsCount++;
			}
			// Other punctuation marks
			else {
				sb = deleteLastSpace(sb);
				sb.append(word);
				sb.append(SPACE);
			}
		}
		sb = deleteLastSpace(sb);
		return sb.toString();
	}

	/**
	 * Delete last char if it's SPACE.
	 * 
	 */
	private StringBuffer deleteLastSpace(StringBuffer sb) {
		if (sb == null || sb.length() == 0)
			return sb;
		if (sb.charAt(sb.length() - 1) == CHAR_SPACE)
			sb.deleteCharAt(sb.length() - 1);
		return sb;
	}

	/**
	 * Test if the quotation is finished.
	 * 
	 */
	private boolean quotationFinished(String sentence) {
		if (sentence.contains(LEFT_DOUBLE_QUOTATION)
				&& sentence.contains(RIGHT_DOUBLE_QUOTATION))
			return true;
		if (sentence.contains(LEFT_DOUBLE_QUOTATION)
				&& !sentence.contains(RIGHT_DOUBLE_QUOTATION))
			return false;
		return true;
	}
}
