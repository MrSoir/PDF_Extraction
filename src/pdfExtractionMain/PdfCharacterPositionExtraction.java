package pdfExtractionMain;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import com.google.gson.Gson;

public class PdfCharacterPositionExtraction extends PDFTextStripper {
	
	final static Charset charset = Charset.forName("UTF-16");
    BufferedWriter writer;
	 
    public PdfCharacterPositionExtraction() throws IOException {
    }
 
    /**
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws IOException    {
    	int pdfType = 1; // 0==LSU.pdf | 1==MachineLearning.pdf
    	String fileName;

    	if(pdfType==0){
    		fileName = "/home/bigdaddy/Documents/LSU_PDFs/133965.pdf";
    	}else{
    		fileName = "/home/bigdaddy/Documents/Machine Learning/[Andreas_C._Müller,_Sarah_Guido]_Introduction_to_(BookZZ.org).pdf";
    	}
        try (PDDocument document = PDDocument.load( new File(fileName) ) ){
            PDFTextStripper stripper = new PdfCharacterPositionExtraction();
            stripper.setSortByPosition( true );
        	if(pdfType==0){
              stripper.setStartPage( 0 );
              stripper.setEndPage( document.getNumberOfPages() );
        	}else{
                stripper.setStartPage( 145 );
                stripper.setEndPage( 145 );
        	}
 
            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(document, dummy);

        	stripper = new PDFTextStripper();
            stripper.setSortByPosition( true );
        	if(pdfType==0){
                stripper.setStartPage( 0 );
                stripper.setEndPage( document.getNumberOfPages() );
          	}else{
                  stripper.setStartPage( 145 );
                  stripper.setEndPage( 145 );
          	}
            String pdfTxt = stripper.getText(document);
            System.out.println(pdfTxt);
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("/home/bigdaddy/Documents/Machine Learning/pdfTextGesamt.txt"), charset)) {
            	writer.write(pdfTxt);
            	writer.close();
            } catch (IOException x) {
        	    System.err.format("IOException: %s%n", x);
        	}
        }catch(IOException e){}
        
    }
 


    @Override
    public void writeText(PDDocument doc,Writer outputStream) throws IOException{
    	try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("/home/bigdaddy/Documents/Machine Learning/pdfTextWithCoords.txt"), charset)) {
    		this.writer = writer;
    		super.writeText(doc, outputStream);
    	} catch (IOException x) {
    	    System.err.format("IOException: %s%n", x);
    	}
    }
    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
//    	StringBuilder strB = new StringBuilder();
    	List<CharObject> chrObjcts = new ArrayList<>();
	    for (TextPosition text : textPositions) {
//	    	String s = String.format("%s	[(X=%.3f,	Y=%.3f)	height=%.3f	width=%.3f]", 
//	    			text.getUnicode(), 
//	    			text.getXDirAdj(), text.getYDirAdj(),
//	    			text.getHeightDir(), text.getWidthDirAdj());
        	CharObject chrObj = new CharObject(text);
        	chrObjcts.add(chrObj);
//        	String JSON_Str = chrObj.createJSON();
//        	System.out.println(JSON_Str);
//            if(writer != null){
//            	writer.append(JSON_Str+System.lineSeparator(), 0, JSON_Str.length()+1);
//            	writer.append(s+System.lineSeparator(), 0, s.length()+1);
//            }
//            strB.append(text.getUnicode());
        }
	    CharObjectLine chrObjectLine = new CharObjectLine(chrObjcts);
	    if(writer != null){
	    	writer.write(chrObjectLine.createJSON());
	    }
	    System.out.println(chrObjectLine.createJSON());
//	    System.out.println(strB.toString());
	    if(writer != null){
        	writer.append(System.lineSeparator(), 0, 1);
        }
    }
    
    private class CharObjectLine{
    	List<CharObject> chrObjcts = new ArrayList<>();
    	private CharObjectLine(List<CharObject> chrObjcts){
    		this.chrObjcts = chrObjcts;
    	}
    	public String createJSON(){
    		return new Gson().toJson(this);
    	}
    }
    
    private class CharObject{
    	public String chr;
    	public float x,y,width,height;
    	public String font;
    	public float fontSize;
    	private CharObject(TextPosition t){
    		chr = t.getUnicode();
    		x = t.getXDirAdj();
    		y = t.getYDirAdj();
    		width = t.getWidthDirAdj();
    		height = t.getHeightDir();
    		font = t.getFont().getName();
    		fontSize = t.getFontSize();
    	}
    	
    	public String createJSON(){
    		return new Gson().toJson(this);
    	}
    }
}
