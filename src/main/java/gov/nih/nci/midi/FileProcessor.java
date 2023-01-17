package gov.nih.nci.midi;

import java.util.List;
import java.util.*;
//import org.hibernate.SQLQuery;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;


import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
public class FileProcessor extends DomainOperation implements FileProcessorInterface{

    private String run;
    private String crossWalkFile;
    private String component;
    private String path;
    private Map<String, String> crossWalkHash;
    private String outputDirectory;
    private CSVPrinter printer;
    private Map<String, Integer>sequenceMap=new HashMap<String, Integer>();
    private String tagSequence;
    private String tagNameSequence;
    private String previousTag;
    private String previousTagName;
    private boolean inSequence;
	public String getRun() {
		return run;
	}
	public void setRun(String run) {
		this.run = run;
	}
	public String getCrossWalkFile() {
		return crossWalkFile;
	}
	public void setCrossWalkFile(String crossWalkFile) {
		this.crossWalkFile = crossWalkFile;
		readCrosswalkFile();
	}
	public String getComponent() {
		return component;
	}
	public void setComponent(String component) {
		this.component = component;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getOutputDirectory() {
		return outputDirectory;
	}
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	private int getTagSequence(String tag) {

		if (sequenceMap.get(tag)==null) {
			sequenceMap.put(tag, 0);
			return 0;
		} else {
			int newValue=sequenceMap.get(tag)+1;
			sequenceMap.put(tag, newValue);
			return newValue;
		}

	}
	private String parseFile(File dicomFile) {
        if (dicomFile!=null)
        	System.out.println("in parse file");
        {
        	String uid=null;
        	NCIADicomTextObject dicomObject;
        	//if (dicomFileCount<maxDicomFiles)
        	//{
			  try {
				if (dicomFile.exists())
				{
				   List<DicomTagDTO> tags=NCIADicomTextObject.getTagElements(dicomFile);
				   if (tags!=null)
					{
					   String originalUID="";
						for (DicomTagDTO tag : tags)
						{
							if (tag.getData()!=null&&(tag.getData().length()>1))
							{
							   String elementName=tag.getName();
							  // System.out.println(elementName + " - " + tag.getData());
							  // System.out.println(element + " - " + tag.getData());
							   if (elementName.equalsIgnoreCase("SOP Instance UID")) {
								   uid=tag.getData();
								   originalUID=uid;
								   System.out.println("found uid");
						    	   if (!crossWalkFile.equalsIgnoreCase("none")) {
						    	       originalUID=crossWalkHash.get(uid);
						    	   }
								   
								   break;
							   }

		 					}
						}
						for (DicomTagDTO tag : tags)
						{
			//				if (tag.getData()!=null&&(tag.getData().length()>1))
			//				{
								   //insertTagToDB(tag.getName(), tag.getElement(),  tag.getData(), uid, dicomFile.getAbsolutePath());
								printRecord(tag.getName(), tag.getElement(), tag.getData(), dicomFile.getAbsolutePath(), uid, originalUID);
								
		 	//				}
						}
						
					}
				}
			  } catch (Exception e) {
				e.printStackTrace();
			  }
        	
        }
        return "ok";
	}
	private String getRealUID(String currentUID) {
		if (getCrossWalkFile()==null) {
			return currentUID;
		}
		return crossWalkHash.get(currentUID);
	}
	private void readCrosswalkFile() {
		boolean allMatch = true;
		StringBuffer sb = new StringBuffer();

		String line = "";
		String splitBy = ",";
		crossWalkHash=new HashMap<String, String>();
		String fileName = getCrossWalkFile();

		File f = new File(fileName);
		if (f.exists()) {
			try {
				// parsing a CSV file into BufferedReader class constructor
				BufferedReader br = new BufferedReader(new FileReader(fileName));

				int lc = 0;
				while ((line = br.readLine()) != null) // returns a Boolean value
				{
					if (lc == 0) {
						++lc;
					} else {
						String[] hashInfo = line.split(splitBy); // use comma as separator
						// System.out.println("series instance uid =" + seriesInfo[seriesUidColumnNum]);
						String oldId = hashInfo[0];
						String newId = hashInfo[1];
						crossWalkHash.put(newId, oldId);
						++lc;


					}
				}
				br.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			sb.append("Didn't find crosswalk.csv");
		}

	}
	
    private void printRecord(String tagName, String tag, String tagValue, String fileName, String instanceUID, String orginalUID){
		try {
//			System.out.println("tag-"+tag+"-"+tagValue);
			if ((tag==null)||(tag.isEmpty())) {
				return;
			}
//			System.out.println();
//			System.out.println("printing record"+"-tagName-"+ tagName +"-tag-"+ tag +"-tagValue-"+ tagValue +"-fileName-"+ fileName +"-instanceUID-"+ instanceUID +"-orginalUID-"+ orginalUID);
			if (tag.startsWith(">")) {
				if (inSequence) {
					tag=tagSequence+"-"+tag;
					tagName=tagNameSequence+"-"+tagName;
				} else {
					tagSequence=previousTag;
					tagNameSequence=previousTagName;
					tag=tagSequence+"-"+tag;
					tagName=tagNameSequence+"-"+tagName;
					inSequence=true;
				}
			} else {
				inSequence=false;
				previousTag=tag;
				previousTagName=tagName;
			}
			int sequence=getTagSequence(tag);
			
			printer.printRecord(run,component,fileName,tag,tagName,tagValue,sequence, instanceUID, instanceUID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }
/**	@Transactional(propagation=Propagation.REQUIRED)
	private void insertTagToDB(String tagName, String tag, String tagValue, String uid, String fileName) throws Exception {
		String SQLQueryForOrginal="insert into dicom_tags(run, originalfilename, instanceuid, tag, tagname, originalvalue) values(:run, :filename, :uid, :tag, :tagname, :tagvalue)";
		String SQLQueryForMidi="update dicom_tags set run=:run, midifilename=:filename, midivalue=:tagvalue where instanceuid=:uid and run=:run";
		String SQLQueryForTCIA="update dicom_tags set run=:run, tciafilename=:filename, tciavalue=:tagvalue where instanceuid=:uid and run=:run";
		String finalUID=null;

		if (getComponent()==null) {
			throw new Exception("component is null");
		}
		System.out.println("component"+getComponent());
		if (getComponent().equalsIgnoreCase("original")) {
			finalUID=uid;
			SQLQuery query=this.getHibernateTemplate().getSessionFactory().getCurrentSession().createSQLQuery(SQLQueryForOrginal);
			query.setParameter("run", run);
			query.setParameter("originalfilename", fileName);
			query.setParameter("uid", uid);
			query.setParameter("tag", tag);		
            query.setParameter("tagname", tagName);	
            query.setParameter("originalvalue", tagValue);	
            int value=query.executeUpdate();
            this.getHibernateTemplate().getSessionFactory().getCurrentSession().getTransaction().commit();
            System.out.println("execute update-"+value);
		}
		if (getComponent().equalsIgnoreCase("midi")) {
			finalUID=getRealUID(uid);
			SQLQuery query=this.getHibernateTemplate().getSessionFactory().getCurrentSession().createSQLQuery(SQLQueryForMidi);
			query.setParameter("filename", fileName);
			query.setParameter("uid", uid);
			query.setParameter("run", run);		
            query.setParameter("tagvalue", tagValue);	
            query.executeUpdate();
		}
		if (getComponent().equalsIgnoreCase("tcia")) {
			finalUID=getRealUID(uid);
			SQLQuery query=this.getHibernateTemplate().getSessionFactory().getCurrentSession().createSQLQuery(SQLQueryForMidi);
			query.setParameter("filename", fileName);
			query.setParameter("uid", uid);
			query.setParameter("run", run);		
            query.setParameter("tagvalue", tagValue);	
            query.executeUpdate();
		}
	}
	    **/
	public void processDirectory() throws Exception{
		File inputDirectory = new File(this.getPath());
		
		System.out.println("in process directory");
		System.out.println("directory-"+this.getPath());
		System.out.println("run-"+this.getRun());
		System.out.println("component-"+this.getComponent());
		if (!inputDirectory.exists()) {
			throw new Exception("Directory "+this.getPath()+" not available");
		}
		String outputFile=this.getOutputDirectory()+"/"+this.getComponent()+"-"+run+".csv";
		System.out.println("outputFile-"+outputFile);
		System.out.println("input-"+inputDirectory.getPath());
		System.out.println("input count-"+inputDirectory.listFiles().length);
 	   if (!crossWalkFile.equalsIgnoreCase("none")) {
	       readCrosswalkFile();
	   }
		printer = new CSVPrinter(new FileWriter(outputFile), CSVFormat.DEFAULT);
		printer.printRecord("run","component","filename","tag","tagname","value","sequence_number", "original_instance_uid", "updated_instance_uid");
		processFiles(inputDirectory.listFiles());
		printer.close();
	}
    public void processFiles(File[] files)
    {
        for (File filename : files) {
        	System.out.println("File: " + filename.getName());
            if (filename.isDirectory()) {
                System.out.println("Sub Directory: " + filename.getName());
                processFiles(filename.listFiles());
             //   break;
            }
 
            // Printing the file name present in given path
            else {
               if (filename.getName().toLowerCase().endsWith(".dcm")) {
            	   parseFile(filename);
            	   System.out.println("Parsing file: " + filename.getName());
            	//   break;
               }
            }
        }
    }
  //  @Transactional(propagation=Propagation.REQUIRED)

    public  void processMidi(String run, String component, String path, String crossWalkFile, String outputDirectory) {
    	   setCrossWalkFile(crossWalkFile);
      	   setRun(run);
    	   setComponent(component);
           setPath(path);
           setOutputDirectory(outputDirectory);
    	try {
    	   if (!crossWalkFile.equalsIgnoreCase("no Crosswalk")) {
    	       readCrosswalkFile();
    	   }
           processDirectory();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public static void main(String[] args) {
    	FileProcessor instance=new FileProcessor();
    	//instance.parseFile("e:/a/1.dcm");
    	if (args.length<4) {
    		System.out.println("Must include parameters for run, component, directory and crosswalk file");
    	} else {
    		instance.setCrossWalkFile(args[3]);
    	}
    	instance.setRun(args[0]);
    	instance.setComponent(args[1]);
    	instance.setPath(args[2]);
    	try {
			instance.processDirectory();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	

    }
	
} 

