package learnbyread;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;

import learnbyread.core.TextProcessor;

public class PageParser {
	static final String TITLE_TAG = "\\[--TITLE--\\]";
	private String _bookname;
	private int _wordPerPage;
	private int _index;
	private int _words;
	private Mongo _mongoClient;
	private DB _db;
	private DBCollection _coll;
	private BasicDBObject _doc;
	private BasicDBList _sentences;
	private boolean _hasTitle = false;
	
	public PageParser(String bookname, int wordPerPage) throws UnknownHostException
	{
		_bookname = bookname;
		_wordPerPage = wordPerPage;
		_index = 0;
		// initialize database 
		_mongoClient = new Mongo();
		_db = _mongoClient.getDB("learnbyread");
		_coll = _db.getCollection("pages");
		newPage();
	}
	
	public void parseParagraph(String content){
		Matcher m = Pattern.compile(TITLE_TAG).matcher(content); 
		if(m.find()){
			content = content.replaceAll(TITLE_TAG, "");
			// end page if a title already exists or it's not the first line
			if(_hasTitle || _sentences.size() > 0){
				endPage();
			}
			_doc.append("title", content);
			_hasTitle = true;
		}
		else{
			TextProcessor tp = new TextProcessor();
			List<String> result = null;
			result = tp.process(content);
			_sentences.addAll(result);
			// add separator to end of paragraph
			_sentences.add("");
			_words += tp.getWordsCount();
			if(_words > _wordPerPage)
				endPage();
		}
	}
	
	public void finish(){
		endPage();
		DBCollection bookColl = _db.getCollection("books");
		BasicDBObject query = new BasicDBObject("title", _bookname);
		BasicDBObject doc = new BasicDBObject("title", _bookname).append("pageNum", _index - 1);
		String[] parts = _bookname.split("[\\[\\]]");
		doc.append("mainTitle", parts[0]);
		if(parts.length == 3)
			doc.append("ep", parts[1]).append("subTitle", parts[2]);
		bookColl.update(query, doc, true, false);
	}
	
	private void endPage()
	{
		_doc.append("sentences", _sentences);
		// save to db
		BasicDBObject query = new BasicDBObject("book", _bookname);
	    _coll.update(query.append("index", _index), _doc, true, false);
		// start a new page
		newPage();
	}
	
	private void newPage()
	{
		_hasTitle = false;
		_words = 0;
		_index++;
		_doc = new BasicDBObject("book", _bookname).append("index", _index);
		_sentences = new BasicDBList();
	}
	
	public static void main(String[] args) throws IOException {
		// get file name
		if(args.length < 1)
		{
			System.out.println("please enter a file name");
			return;
		}
		String fileName = args[0];
		int wordPerPage = 300;
		if(args.length >= 2)
		{
			wordPerPage = Math.max(100, Integer.parseInt(args[1]));
		}
		// paragraphs are separated by two \n
		Scanner s = new Scanner(new File(fileName), "UTF-8").useDelimiter("\n\n");
		String bookname = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length() - 4);
		PageParser parser = new PageParser(bookname, wordPerPage);
		System.out.println("start processing");
		// each of hasNext is a paragraph
		while(s.hasNext())
		{
			String article = s.next();
			parser.parseParagraph(article);
		}
		parser.finish();
	}
}