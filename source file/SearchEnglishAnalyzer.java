package com.lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class SearchEnglishAnalyzer {
	
	private SearchEnglishAnalyzer() {}

	 
	  public static void main(String[] args) throws Exception {
	    String usage =
	      "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
	    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
	      System.out.println(usage);
	      System.exit(0);
	    }

	    String index = "IndexEnglishAnalyzeroutput";
	    String field = "contents";
	    String queries = null;
	    int repeat = 0;
	    boolean raw = false;
	    String queryString = null;
	    int hitsPerPage = 10;
	    
	    for(int i = 0;i < args.length;i++) {
	      if ("-index".equals(args[i])) {
	        index = args[i+1];
	        i++;
	      } else if ("-field".equals(args[i])) {
	        field = args[i+1];
	        i++;
	      } else if ("-queries".equals(args[i])) {
	        queries = args[i+1];
	        i++;
	      } else if ("-query".equals(args[i])) {
	        queryString = args[i+1];
	        i++;
	      } else if ("-repeat".equals(args[i])) {
	        repeat = Integer.parseInt(args[i+1]);
	        i++;
	      } else if ("-raw".equals(args[i])) {
	        raw = true;
	      } else if ("-paging".equals(args[i])) {
	        hitsPerPage = Integer.parseInt(args[i+1]);
	        if (hitsPerPage <= 0) {
	          System.err.println("There must be at least 1 hit per page.");
	          System.exit(1);
	        }
	        i++;
	      }
	    }
	    
	    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new EnglishAnalyzer();

	    BufferedReader in = null;
	    if (queries != null) {
	      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
	    } else {
	      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
	    }
	    QueryParser parser = new QueryParser(field, analyzer);
	    while (true) {
	      if (queries == null && queryString == null) {                        // prompt the user
	        System.out.println("Enter query: ");
	      }

	      String line = queryString != null ? queryString : in.readLine();

	      if (line == null || line.length() == -1) {
	        break;
	      }

	      line = line.trim();
	      if (line.length() == 0) {
	        break;
	      }
	      
	      Query query = parser.parse(line);
	      System.out.println("Searching for: " + query.toString(field));
	            
	      if (repeat > 0) {                           
	        Date start = new Date();
	        for (int i = 0; i < repeat; i++) {
	          searcher.search(query, 100);
	        }
	        Date end = new Date();
	        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
	      }

	      doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);

	      if (queryString != null) {
	        break;
	      }
	    }
	    reader.close();
	  }

	 
	  public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
	                                     int hitsPerPage, boolean raw, boolean interactive) throws IOException {
	 
		  Document doc=null;
		  List<Document> documents = new ArrayList<Document>();
		 
		  final String ID ="id";
			final String TITLE="title";
			final String AUTHOR="author";
			final String BIBILIO="bibilio";
			final String ABSTRACT="abstract";
	    
	    TopDocs results = searcher.search(query, 5 * hitsPerPage);
	    ScoreDoc[] hits = results.scoreDocs;
	    Date starttime = new Date();
	    
	    for(ScoreDoc scoredoc:hits){
			try {
				doc = searcher.doc(scoredoc.doc);
				documents.add(doc);
			} catch (IOException e) {
				
			}
	    }
	    Date endtime = new Date();
	    long time=endtime.getTime() - starttime.getTime();
	    System.out.println("Time taken to execute query is : " + time + " total milliseconds");
	    int i1=0;
	    
	    int numTotalHits = results.totalHits;
	    File f = new File("displayenglishanalyzeroutput.html");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
      
        String docId=new String();
        bw.write("<html>");
        bw.write("<body>");
        bw.write("<table>");
        bw.write("<tr>");
        bw.write("<td>Time taken</td><td><input type=text value="+time+"ms"+"></td>");
        bw.write("</tr>");
        bw.write("<tr>");
        bw.write("<td>TOTAL HITS</td><td><input type=text value="+numTotalHits+"></td>");
        bw.write("</tr>");
        bw.write("</table>");
        bw.write("<table border="+1+" >");
        bw.write("<tr>");
        bw.write("<td>ID</td>");
        bw.write("<td>AUTHOR</td>");
        bw.write("<td>TITLE</td>");
       // bw.write("<td>BIBILIO</td>");
       // bw.write("<td>ABSTRACT</td>");
        bw.write("</tr>");
			for(Document doc1:documents){
				
				System.out.println(doc1.get(ID));
				System.out.println(doc1.get(AUTHOR));
				System.out.println(doc1.get(TITLE));
				System.out.println(doc1.get(BIBILIO));
				System.out.println(doc1.get(ABSTRACT));
				//  out.println( doc1.get(ID) +" \t "+  doc1.get(AUTHOR) +" \t "+  doc1.get(TITLE) +" \t "+  doc1.get(BIBILIO) +" \t "+  doc1.get(ABSTRACT) );
				//System.out.printf("%-30.30s  %-30.30s%n  %-30.30s  %-30.30s%n  %-30.30s", doc1.get(ID), doc1.get(AUTHOR),doc1.get(TITLE),doc1.get(BIBILIO),doc1.get(ABSTRACT));
				

			        bw.write("<tr>");
			        bw.write("<td>"+doc1.get(ID)+"</td>");
			        bw.write("<td>"+doc1.get(AUTHOR)+"</td>");
			        bw.write("<td>"+doc1.get(TITLE)+"</td>");    // ---------------------------------- HERE PASS is to be displayed
			      //  bw.write("<td>"+doc1.get(BIBILIO)+"</td>");      
			       // bw.write("<td>"+doc1.get(ABSTRACT)+"</td>");      
			        bw.write("</tr>");
			        docId+=doc1.get(ID);
			        docId+=",";

			        
			}
		
			 bw.write("<tr>");
			bw.write("<td>DocIDs</td><td><input type=text value="+docId.toString()+" size="+100+"></td>");
			 bw.write("<tr>");
			bw.write("</table>");
	        bw.write("</body>");
	        bw.write("</html>");
	        bw.close();
	        i1++;
	        
	    
	    System.out.println(numTotalHits + " total matching documents");

	    int start = 0;
	    int end = Math.min(numTotalHits, hitsPerPage);
	        
	    while (true) {
	      if (end > hits.length) {
	        System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
	        System.out.println("Collect more (y/n) ?");
	        String line = in.readLine();
	        if (line.length() == 0 || line.charAt(0) == 'n') {
	          break;
	        }

	        hits = searcher.search(query, numTotalHits).scoreDocs;
	      }
	      
	      end = Math.min(hits.length, start + hitsPerPage);
	      
	      for (int i = start; i < end; i++) {
	        if (raw) {                              // output raw format
	          System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
	          continue;
	        }
//
//	        Document doc2 = searcher.doc(hits[i].doc);
//	        String path = doc2.get("path");
//	        if (path != null) {
//	          System.out.println((i+1) + ". " + path);
//	          String title = doc2.get("title");
//	          if (title != null) {
//	            System.out.println("   Title: " + doc2.get("title"));
//	          }
//	        } else {
//	          System.out.println((i+1) + ". " + "No path for this document");
//	        }
	                  
	      }

	      if (!interactive || end == 0) {
	        break;
	      }

	      if (numTotalHits >= end) {
	        boolean quit = false;
	        while (true) {
	          System.out.print("Press ");
	          if (start - hitsPerPage >= 0) {
	            System.out.print("(p)revious page, ");  
	          }
	          if (start + hitsPerPage < numTotalHits) {
	            System.out.print("(n)ext page, ");
	          }
	          System.out.println("(q)uit or enter number to jump to a page.");
	          
	          String line = in.readLine();
	          if (line.length() == 0 || line.charAt(0)=='q') {
	            quit = true;
	            break;
	          }
	          if (line.charAt(0) == 'p') {
	            start = Math.max(0, start - hitsPerPage);
	            break;
	          } else if (line.charAt(0) == 'n') {
	            if (start + hitsPerPage < numTotalHits) {
	              start+=hitsPerPage;
	            }
	            break;
	          } else {
	            int page = Integer.parseInt(line);
	            if ((page - 1) * hitsPerPage < numTotalHits) {
	              start = (page - 1) * hitsPerPage;
	              break;
	            } else {
	              System.out.println("No such page");
	            }
	          }
	        }
	        if (quit) break;
	        end = Math.min(numTotalHits, start + hitsPerPage);
	      }
	    }
	  }

}
