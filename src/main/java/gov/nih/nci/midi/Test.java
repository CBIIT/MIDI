package gov.nih.nci.midi;

public class Test {
    public static void main(String[] args) {

     System.out.println("hello");
     if (args.length<5) {
    	 System.out.println("Must include parameters for run, component, directory, crosswalk file and output directory");
	    System.exit(1);
      } 
          RunProcess process=new RunProcess();
          //process.processMidi(args[0], args[1], args[2], args[3], args[4]);
	

    }
}
