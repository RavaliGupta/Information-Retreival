package com.lucene;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexFilesAnalyzer {
	
	private IndexFilesAnalyzer() {}
	public static String boostValue="0.8";
	public static String boost="false";
	 final static String ID ="id";
		final static String TITLE="title";
		final static String AUTHOR="author";
		final static String BIBILIO="bibilio";
		final static String ABSTRACT="abstract";
		public static final int DEFAULT_MAX_TOKEN_LENGTH=225;
		
	  /** Index all text files under a directory. 
	 * @throws FileNotFoundException */
	  public static void main(String[] args) throws FileNotFoundException {
	    String usage = "java org.apache.lucene.demo.IndexFiles"
	                 + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
	                 + "This indexes the documents in DOCS_PATH, creating a Lucene index"
	                 + "in INDEX_PATH that can be searched with SearchFiles";
	    String indexPath = "IndexFilesAnalyzeroutput";
	    String docsPath = "filesToIndex";

	    
	    boolean create = true;
	    for(int i=0;i<args.length;i++) {
	      if ("-index".equals(args[i])) {
	        indexPath = args[i+1];
	        i++;
	      } else if ("-docs".equals(args[i])) {
	        docsPath = args[i+1];
	        i++;
	      } else if ("-update".equals(args[i])) {
	        create = false;
	      }
	    }

	    if (docsPath == null) {
	      System.err.println("Usage: " + usage);
	      System.exit(1);
	    }

	    final Path docDir = Paths.get(docsPath);
	    if (!Files.isReadable(docDir)) {
	      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
	  Date start = new Date();
	    try {
	      System.out.println("Indexing to directory '" + indexPath + "'...");

	      Directory dir = FSDirectory.open(Paths.get(indexPath));
	      Analyzer analyzer = new StandardAnalyzer();
	      //TokenStream tokenStream = analyzer.tokenStream("contents", null);
	      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

	      if (create) {
	       
	        iwc.setOpenMode(OpenMode.CREATE);
	      } else {
	       
	        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	      }

	    

	      IndexWriter writer = new IndexWriter(dir, iwc);
	     
	     
	      indexDocs(writer, docDir);
         writer.close();

	      Date end = new Date();
	      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	    } catch (IOException e) {
	      System.out.println(" caught a " + e.getClass() +
	       "\n with message: " + e.getMessage());
	    }
		}
	  
		
		
		public static float getBoostValue() {
			return new Float(boostValue);
		}
		
		public static boolean getBoost() {
			return new Boolean(boost) ;
		}
		
	
	  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
	    if (Files.isDirectory(path)) {
	      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	          try {
	            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
	          } catch (IOException ignore) {
	           
	          }
	          return FileVisitResult.CONTINUE;
	        }
	      });
	    } else {
	      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
	    }
	  }

	
	  static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
	    try (InputStream stream = Files.newInputStream(file)) {
	     
	      Document document = new Document();
	      
	      
	      Field pathField = new StringField("path", file.toString(), Field.Store.YES);
	      document.add(pathField);
	      
	     
	      
	      String line = null;
		    List<Document> documents = new ArrayList<Document>();
			//Document document = null;
			//File file = new File(file);
			Scanner sc = null;
			String[] splitst = null;
			StringBuilder builder = null;
			InputStream inputStream = null;
		    inputStream = Files.newInputStream(file);
			sc = new Scanner(inputStream);
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				if (line.startsWith(".I")) {
					document = new Document();
					splitst = line.split(" ");
					document.add(new StringField(ID, splitst[1], Field.Store.YES));
				} else if (line.startsWith(".T")) {
					builder = new StringBuilder();
					while (!sc.hasNext(".A")) {
						builder.append(sc.nextLine());
					}
					Field field = new TextField(TITLE, builder.toString(), Field.Store.YES);
					if(getBoost()){
						field.setBoost(getBoostValue());
					}
					document.add(field);
				} else if (line.startsWith(".A")) {
					builder = new StringBuilder();
					while (!sc.hasNext(".B")) {
						builder.append(sc.nextLine());
					}
					document.add(new StringField(AUTHOR, builder.toString(), Field.Store.YES));
				} else if (line.startsWith(".B")) {
					builder = new StringBuilder();
					while (!sc.hasNext(".W")) {
						builder.append(sc.nextLine());
					}
					document.add(new StringField(BIBILIO, builder.toString(), Field.Store.YES));
				} else if (line.startsWith(".W")) {
					builder = new StringBuilder();
					while (!sc.hasNext(".I") && sc.hasNextLine()) {
						builder.append(sc.nextLine());
					}
					document.add(new TextField(ABSTRACT, builder.toString(), Field.Store.YES));
					documents.add(document);
				}
			}
		    
				System.out.println(" No. Of Documents " + documents.size());
	      

				document.add(new LongPoint("modified", lastModified));
	      
	    
				document.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
	      
	      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
	      
	        System.out.println("adding " + file);
	        writer.addDocument(document);
	      } else {
	       
	        System.out.println("updating " + file);
	        writer.updateDocument(new Term("path", file.toString()), document);
	      }
	    }
	  }


}
