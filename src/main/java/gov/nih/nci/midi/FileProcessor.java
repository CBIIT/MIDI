package gov.nih.nci.midi;

import java.util.List;
import java.util.*;
import java.io.*;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;

import java.nio.channels.Channels;
import java.nio.file.Files;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
public class FileProcessor{

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
    private String projectName;
    private String zipFileName;
    private String outputBucket;
    private int count=0;
    
    
	public String getOutputBucket() {
		return outputBucket;
	}
	public void setOutputBucket(String outputBucket) {
		this.outputBucket = outputBucket;
	}
	public String getZipFileName() {
		return zipFileName;
	}
	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
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
			if (tag.equalsIgnoreCase("(0040,A730)-(0008,0102)")) {
			    System.out.println(tag);
			}
			return 0;
		} else {
			int newValue=sequenceMap.get(tag)+1;
			sequenceMap.put(tag, newValue);
			return newValue;
		}

	}
	private String parseFile(File dicomFile, String fileName) {
		String localFileName=null;
		if (fileName!=null) {
			localFileName=fileName;
		} else {
			localFileName=dicomFile.getAbsolutePath();
		}
        if (dicomFile!=null)
        	//System.out.println("in parse file");
        {
        	count++;
        	if (count%100==0) {
        		System.out.println(count+" files processed");
        	}
        	String uid=null;
        	sequenceMap=new HashMap<String, Integer>();
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
								  // System.out.println("found uid");
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
								printRecord(tag.getName(), tag.getElement(), tag.getData(), localFileName, uid, originalUID);
								
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
						if (hashInfo.length>1) {
						   String oldId = hashInfo[0];
						   String newId = hashInfo[1];
						   crossWalkHash.put(newId, oldId);
						}
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
			int sequence=0;
			if (tag.startsWith(">")) {
				if (inSequence) {
					try {
						tag=tag.substring(tag.indexOf("("));
					} catch (Exception e) {
						e.printStackTrace();
					}
					tag=tagSequence+"-"+tag;
					tagName=tagNameSequence+"-"+tagName;
				} else {
					try {
						tag=tag.substring(tag.indexOf("("));
					} catch (Exception e) {
						e.printStackTrace();
					}
					tagSequence=previousTag;
					tagNameSequence=previousTagName;
					tag=tagSequence+"-"+tag;
					tagName=tagNameSequence+"-"+tagName;
					inSequence=true;
				}
				sequence=getTagSequence(tag);
			} else {
				inSequence=false;
				previousTag=tag;
				previousTagName=tagName;
			}
			//System.out.println();
			//System.out.println("printing record"+"-tagName-"+ tagName +"-tag-"+ tag +"-tagValue-"+ tagValue +"-fileName-"+ fileName +"-instanceUID-"+ instanceUID +"-orginalUID-"+ orginalUID);
			
			printer.printRecord(run,component,fileName,tag,tagName,tagValue,sequence, orginalUID, instanceUID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }

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
	private void processBucket() throws Exception{
		String inputBucket = this.getPath();
		String outputBucket= this.getOutputDirectory();
		System.out.println("in process bucket");
		System.out.println("inputBucket-"+inputBucket);
		System.out.println("run-"+this.getRun());
		System.out.println("component-"+this.getComponent());

		//String outputFile=this.getComponent()+"-"+run+".csv";
		String outputFile=this.getOutputDirectory()+"/"+this.getComponent()+"-"+run+".csv";
		System.out.println("outputFile-"+outputFile);
		//System.out.println("outputBucket-"+outputFile);
 	   if (!crossWalkFile.equalsIgnoreCase("none")) {
	       readCrosswalkFile();
	   }
		printer = new CSVPrinter(new FileWriter(outputFile), CSVFormat.DEFAULT);
		printer.printRecord("run","component","filename","tag","tagname","value","sequence_number", "original_instance_uid", "updated_instance_uid");
	    Storage storage = StorageOptions.newBuilder().setProjectId(projectName).build().getService();
	    ReadChannel reader = storage.reader(BlobId.of(inputBucket, this.getZipFileName()));
	    InputStream inputStream = Channels.newInputStream(reader);
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream);
        TarArchiveEntry tarArchiveEntry;
        while (null != (tarArchiveEntry = tarArchiveInputStream.getNextEntry())) {
        	if (tarArchiveEntry.isFile()) {
            	if (tarArchiveEntry.getName().endsWith("dcm")) {
        			File dicomFile = File.createTempFile("temp", ".dcm");
                    try (OutputStream o = Files.newOutputStream(dicomFile.toPath())){
                    	IOUtils.copy(tarArchiveInputStream, o);
                    }
                    //System.out.println(dicomFile.getAbsolutePath());
            		parseFile(dicomFile, tarArchiveEntry.getName());
            		dicomFile.delete();
            	}
            }
        }
        tarArchiveInputStream.close();
		printer.close();
		uploadFile(getOutputBucket(), outputFile);
	}
    public void processFiles(File[] files)
    {
        for (File filename : files) {
        	//System.out.println("File: " + filename.getName());
            if (filename.isDirectory()) {
             //   System.out.println("Sub Directory: " + filename.getName());
                processFiles(filename.listFiles());
             //   break;
            }
 
            // Printing the file name present in given path
            else {
               if (filename.getName().toLowerCase().endsWith(".dcm")) {
            	   parseFile(filename, null);
            	  // System.out.println("Parsing file: " + filename.getName());
            	//   break;
               }
            }
        }
    }
    public void uploadFile(String bucketName, String filePath) {
    	System.out.println(projectName);
	    Storage storage = StorageOptions.newBuilder().setProjectId(projectName).build().getService();
        // Get the bucket
	    
        Bucket bucket = storage.get(bucketName);

        try {
			// Upload a file to the bucket
			bucket.create(this.getComponent()+"-"+run+".csv", new FileInputStream(filePath),
					Bucket.BlobWriteOption.userProject(projectName));
			System.out.println("File uploaded to bucket.");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
  //  @Transactional(propagation=Propagation.REQUIRED)

    public  void processMidi(String run, String component, String path, String crossWalkFile, String outputDirectory, String projectName, String zipFileName, String outputBucketName) {
    	   setCrossWalkFile(crossWalkFile);
      	   setRun(run);
    	   setComponent(component);
           setPath(path);
           setOutputDirectory(outputDirectory);
       	   setProjectName(projectName);
           setZipFileName(zipFileName);
           setOutputBucket(outputBucketName);
        try {
    	if (projectName==null) {
           processDirectory();
    	} else {
    	   processBucket();
    	}
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

