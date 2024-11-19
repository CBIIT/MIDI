package gov.nih.nci.midi;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
public class StreamObjectDownload {


	  public static void streamObjectDownload(
	      String projectId, String bucketName, String objectName, String targetFile)
	      throws IOException {
	    // The ID of your GCP project
	    // String projectId = "your-project-id";

	    // The ID of your GCS bucket
	    // String bucketName = "your-unique-bucket-name";

	    // The ID of your GCS object
	    // String objectName = "your-object-name";

	    // The path to the file to download the object to
	    // String targetFile = "path/to/your/file";
	    Path targetFilePath = Paths.get(targetFile);

	    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
	    try (ReadChannel reader = storage.reader(BlobId.of(bucketName, objectName));
	        FileChannel targetFileChannel =
	            FileChannel.open(targetFilePath, StandardOpenOption.WRITE)) {

	      ByteStreams.copy(reader, targetFileChannel);

	      System.out.println(
	          "Downloaded object "
	              + objectName
	              + " from bucket "
	              + bucketName
	              + " to "
	              + targetFile
	              + " using a ReadChannel.");
	    }
	  }
	  
	  public static void main(String[] args) {
		  try {
			streamObjectDownload(args[0], args[1], args[2], args[3]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

}
