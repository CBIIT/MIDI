package gov.nih.nci.midi;


import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
public class RunProcess {

	@Autowired
	private FileProcessorInterface instance;

	public void processMidi(String run, String component, String path, String crossWalkFile, String outputDirectory) {
        ApplicationContext context 
        = new ClassPathXmlApplicationContext(
          "applicationContext.xml");
        FileProcessorInterface instance= (FileProcessorInterface) context.getBean("FileProcessor");
        instance.processMidi(run, component, path, crossWalkFile, outputDirectory);
	}
	
    public static void main(String[] args) {

    	//instance.parseFile("e:/a/1.dcm");
    	if (args.length<5) {
    		System.out.println("Must include parameters for run, component, directory, crosswalk file and output directory");
    		System.exit(1);
    	} 

        FileProcessor instance= new FileProcessor();
        instance.processMidi(args[0], args[1], args[2], args[3], args[4]); 

	

    }
	
	
}
