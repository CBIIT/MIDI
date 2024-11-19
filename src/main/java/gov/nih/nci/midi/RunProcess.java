package gov.nih.nci.midi;



public class RunProcess {




	public void processMidi(String run, String component, String path, String crossWalkFile, String outputDirectory, String projectName, String zipFileName, String outputBucket) {

        FileProcessor instance= new FileProcessor();
        instance.processMidi(run, component, path, crossWalkFile, outputDirectory, projectName, zipFileName, outputBucket);
	}
	
    public static void main(String[] args) {

    	//instance.parseFile("e:/a/1.dcm");
    	if (args.length<5) {
    		System.out.println("Must include parameters for run, component, directory, crosswalk file and output directory");
    		System.exit(1);
    	} 

        FileProcessor instance= new FileProcessor();
        if (args.length==5) {
        	instance.processMidi(args[0], args[1], args[2], args[3], args[4], null, null, null); 
        } else {
            instance.processMidi(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]); 
        }

	

    }
	
	
}
