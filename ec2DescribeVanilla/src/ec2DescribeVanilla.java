import java.util.List;
import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.EmailAddressGrantee;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.Permission;

public class ec2DescribeVanilla {
	
	static AmazonEC2 ec2;

	public static void main(String[] args) throws Exception {
        setCredentials();																			// as it says
		// describeInstances();																		// example of how to pass instance id's via array and get info about them
		// describeImages();																			// example of to filter (via area) describe-info results
		getS3Canonical();
	}
	private static void describeImages() throws Exception {
		try {
			Filter[] filters = new Filter[2]; 														// create a filter array to pass to our request later
			// filters[#] = new Filter().withName("filterName").withValues("value");				
            filters[0] = new Filter().withName("owner-id").withValues("733098333646");
            filters[1] = new Filter().withName("image-id").withValues("ami-2799cf4e");
            DescribeImagesRequest descImgReq = new DescribeImagesRequest().withFilters(filters);	// create the image request (with filters passed)
            DescribeImagesResult descImgRes = ec2.describeImages(descImgReq);						// create the image results
            List<Image> images = descImgRes.getImages();											// dump our describe request into a LIST
            System.out.println("Describe Available Images (#/images: " + images.size() + ")");		// debug: show size of the list (#/images)
            // System.out.println("Image dump: " + Arrays.toString(images.toArray()));					// debug: grab the whole of the content
            System.out.println();
            for (Image i : images) {
            	System.out.println(i.getVirtualizationType());										// loop through the image elements and grab what you want
            }
		} 
		catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Error Type: " + ase.getErrorType());
            System.out.println("Request ID: " + ase.getRequestId());
		}
	}
	private static void describeInstances() throws Exception {
		try {
			ArrayList<String> al = new ArrayList<String>();											// build an array to pass to our Describe instance request
			al.add("i-9de818e6"); //work instance													// populate the array
			al.add("i-4eb26a60"); //blog instance
			al.add("i-93e9c1b3"); // stopped bastion
			DescribeInstancesRequest di = new DescribeInstancesRequest();
            di.setInstanceIds(al);
            DescribeInstancesResult describeInstancesRequest = ec2.describeInstances(di);
            
            List<Reservation> reservations = describeInstancesRequest.getReservations();

            for (Reservation reservation : reservations) {
            	List<Instance> instances = reservation.getInstances();
                for (Instance ins : instances) { 
                	System.out.println("Instance ID: " + ins.getInstanceId() + 
                			           " Status: " + ins.getState().getName());
                	System.out.print("Launch Time: "+ ins.getLaunchTime()); 
                	System.out.println(); 
                	System.out.print("Pub/Priv IP: " + ins.getPublicIpAddress() + 
                			         " / " + ins.getPrivateIpAddress());
                	System.out.println(); 
                	System.out.println();                 	
                }
            }
        } 
		catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Error Type: " + ase.getErrorType());
                System.out.println("Request ID: " + ase.getRequestId());
        }
	}
	private static void getS3Canonical() throws Exception { // get canonical id by setting acct. email as grantee and describing  
		try {
			String bucketName     = "daniep-ami-test";
			String keyName        = "image.manifest.xml";
			String acctEmail 	  = "aws-dev-support-account@amazon.com";
		    // expected result    = 03c32fa251dfbdaf512afeb2de830764603d17e0a9b231f6b1ae63a5409f15b7

			AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());

			AccessControlList acl = new AccessControlList();
			acl.grantPermission(new EmailAddressGrantee(acctEmail), Permission.Read);
			Owner owner = new Owner();
			owner.setId("b6932e976fb769ddf052252cf6cca52434f23555e2866337400e7f0e0c63ee98");
			owner.setDisplayName("daniep@amazon.com");
			acl.setOwner(owner);

			s3client.setObjectAcl(bucketName, keyName, acl);
			
			
			for (Grant grant2 : acl.getGrants()) {
				System.out.println("Grantee: " + grant2.getGrantee());
			}
			for (Grant grant : s3client.getObjectAcl(bucketName, keyName).getGrants()) {
			    System.out.println("Canonical: " + grant.getGrantee().getIdentifier());
			}
			//Collection<Grant> grantCollection = new ArrayList<Grant>();
			//System.out.println("Grants: " + acl.getGrants());
			//System.out.println("getObjectAcl: " + s3client.getObjectAcl(bucketName, keyName));
			//System.out.println("getObjectAcl().getGrants: " + s3client.getObjectAcl(bucketName, keyName).getGrants().toString());
		} 
		catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Error Type: " + ase.getErrorType());
            System.out.println("Request ID: " + ase.getRequestId());
		}
	}
	private static void setCredentials() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = new AmazonEC2Client(credentials);
    }
}