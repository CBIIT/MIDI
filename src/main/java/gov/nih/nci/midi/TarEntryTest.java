package gov.nih.nci.midi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.nio.file.*;

public class TarEntryTest {

	public void processGZFile(String gzFile) throws Exception{
		File infile=new File(gzFile);
	    InputStream inputStream = new FileInputStream(infile);
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream);
        TarArchiveEntry tarArchiveEntry;
        while (null != (tarArchiveEntry = tarArchiveInputStream.getNextEntry())) {

        		System.out.println(tarArchiveEntry.isFile());
        		System.out.println(tarArchiveEntry.getName());
        		if (tarArchiveEntry.isFile()) {
        			File tempFile = File.createTempFile("temp", ".dcm");
                    try (OutputStream o = Files.newOutputStream(tempFile.toPath())){
                    	IOUtils.copy(tarArchiveInputStream, o);
                    }
                    System.out.println(tempFile.getAbsolutePath());
                    tempFile.delete();
        		}


        }
        tarArchiveInputStream.close();
	}
    private File createTempDicom (FileInputStream in) {
    	File tempFile = null;
    	try {
			tempFile = File.createTempFile("temp", ".dcm");
			tempFile.deleteOnExit();
			FileOutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(in, out);
			in.close();
			out.close();
		} catch (Exception e) {
			System.out.println("Unable to create temp file");
			e.printStackTrace();
		}
    	
		return tempFile;
    }
	  public static void main(String[] args) {
		  try {
			  TarEntryTest test=new TarEntryTest();
			  test.processGZFile(args[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
}
