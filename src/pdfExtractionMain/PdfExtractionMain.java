package pdfExtractionMain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfExtractionMain {

	public static void main(String[] args) {
		System.out.printf("args.length: %s%n", args.length);
		for(String arg: args)
			System.out.printf("	arg: %s%n", arg);
		
		if (args.length < 1){
			System.err.printf("args.length == %s -> must be at least 1 -> first argument must be PDF's target path!%n", args.length);
			System.exit(0);
		}
		
		StringBuilder logSuccessful = new StringBuilder();
		StringBuilder logFailed = new StringBuilder();
		
        try {
//        	File curFold = new File(new File("").getAbsolutePath());
//        	System.out.println(curFold.getAbsolutePath());
//        	System.out.printf("isDir: %s%n", curFold.isDirectory());
        	
        	File tarFold = new File(args[0]);
        	if(!tarFold.isDirectory()){
        		System.err.printf("target path ist not a directory! -> %s%n", args[0]);
    			System.exit(0);
        	}
        	
        	int rnmCntr = 1;
        	for(File file: tarFold.listFiles()){
        		if(file.getAbsolutePath().toLowerCase().endsWith(".pdf")){
        			        			
        			File pdfFile = file;
                	try(PDDocument doc = PDDocument.load(pdfFile)){
	                    String pdfTxt = new PDFTextStripper().getText(doc);
	                    
	                    String TAR_NUMBER = "";
	                    String REF_STR = "Auftrags-Nr. Versender :";
	                    
//	                    System.out.println(pdfTxt);
	                    try(Scanner scanner = new Scanner(pdfTxt)){
		                    while (scanner.hasNextLine()) {
		                    	String line = scanner.nextLine();
		                    	
		                    	if(line.length() > 6){
		                    		String[] subStrngs = line.split(" ");
		                    		if(subStrngs != null && subStrngs.length > 0 && subStrngs[0].length() >= 6){
		                    			if(subStrngs[0].matches("[0-9]+")){
		                    				TAR_NUMBER = subStrngs[0];
		                    			}else if (subStrngs[0].substring(0,6).matches("[0-9]+")){
		                    				TAR_NUMBER = subStrngs[0];
		                    			}
		                    		}
		                    	}
		                    	if(line.startsWith( REF_STR )){
		                    		TAR_NUMBER = line.substring(REF_STR.length(), line.length()).trim();
		                    		break;
		                    	}
		                    }
	                    }
	                    
	                    if( !TAR_NUMBER.equals("") ){
	                    	TAR_NUMBER = TAR_NUMBER.trim();
	                    	File newFile = new File(pdfFile.getParentFile().getAbsolutePath() + File.separator + TAR_NUMBER + ".pdf");
	                    	int counter = 1;
	                    	while(newFile.exists()){
	                    		newFile = new File(pdfFile.getParentFile().getAbsolutePath() + File.separator + TAR_NUMBER + counter + ".pdf");
	                    		counter++;
	                    	}
	                    	String oldFileName = pdfFile.getName();
	                    	pdfFile.renameTo(newFile);
	                    	logSuccessful.append(String.format("	%s		->	%s%n", oldFileName, newFile));

	                    	System.out.printf("renamed pdf n° %s to '%s.pdf'%n", rnmCntr++, TAR_NUMBER);
	                    }else{
	                    	logFailed.append(pdfFile.getName() + System.lineSeparator());
	                    	System.err.printf("didn't find any matching pattern in pdf '%s'!%n", pdfFile.getName());
	                    }
                	}
        		}
        	}
        	
        	Charset charset = Charset.forName("US-ASCII");
        	
        	String logFailedStr = logFailed.toString();
        	String logSuccessfulStr = logSuccessful.toString();
        	String failedCountStr = logFailedStr.split(System.lineSeparator()).length == 1 ? 
        					logFailedStr.split(System.lineSeparator())[0].isEmpty() ? ""+0 : ""+1 :
        						""+logFailedStr.split(System.lineSeparator()).length;
        	String succCountStr = logSuccessfulStr.split(System.lineSeparator()).length == 1 ? 
        			logSuccessfulStr.split(System.lineSeparator())[0].isEmpty() ? ""+0 : ""+1 :
						""+logSuccessfulStr.split(System.lineSeparator()).length;
        	String s = 
        			String.format("Pdfs failed to rename: %s%n", failedCountStr)
        			+ logFailedStr + 
        			"---------------------------------------" + System.lineSeparator()
        			+ String.format("Pdfs successfully renamed: %s%n", succCountStr)
        			+ logSuccessfulStr;
        				
        	try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("pdf_log.txt"), charset)) {
        	    writer.write(s, 0, s.length());
        	} catch (IOException x) {
        	    System.err.format("IOException: %s%n", x);
        	}
        	
        } catch (IOException e) {
            e.printStackTrace();
        } 
	}

}
