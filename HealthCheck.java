// Import.
import java.util.Iterator;
import java.util.Properties;
import java.util.stream.Stream;

import javax.security.auth.Subject;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.ObjectStoreSet;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.*;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;


import java.io.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;


import nomad.*;
import mail.*;



public class HealthCheck {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Properties prop = new Properties();
		try{
		prop.load(new FileInputStream("config.properties"));
		}catch(IOException e){
			System.out.println("Cannot find config.properties");
		}
		
		Date date = new Date();
		 // Set connection parameters; substitute for the placeholders.
	    String uri = prop.getProperty("uri");
	    String username = prop.getProperty("username");
	    String uploadFile = prop.getProperty("uploadFile");
	    String password = AESencrp.decrypt(prop.getProperty("password"));
	    String searchPredicate = prop.getProperty("searchPredicate");
	    String searchTimeout = prop.getProperty("searchTimeout");
	    
	    boolean doSearch = Boolean.valueOf(prop.getProperty("doSearch"));
	    boolean doDocUpload = Boolean.valueOf(prop.getProperty("doDocUpload"));
	    boolean doQueueItemExpires = Boolean.valueOf(prop.getProperty("doQueueItemExpires"));
	    
	    String queueItemTimeout = prop.getProperty("queueItemTimeout");
	    String queryTop = prop.getProperty("queryResultsLimit");
	    String hcError = null;
	    String queueItemFoundOS = "";
	    
	    //Timer variables timing add/download
	    long lstartTime;
	    long lendTime;
	    
	    //logging variables
	    File file = new File(prop.getProperty("logFile"));
	    FileWriter fw = new FileWriter(file, true);
	    PrintWriter pw = new PrintWriter(fw);
	    
	    //trending variables
	    
	    long ldomainConnectTime;
	    long lsearchTime;
	    long lqiSearchTime;
	    long luploadTime;
	    long ldeleteTime;
	    
	    //trending report
	    File report = new File(prop.getProperty("trendingReport"));
	    FileWriter fwReport = new FileWriter(report, true);
	    PrintWriter pwReport = new PrintWriter(fwReport);

	    
	    
	    pw.println("\n\n**************************************");
	    pw.println("HealthCheck Start - " + date.toString() + "");   
	    pw.println("**************************************");

	    //print all vars to log
	    pw.println("URI: " + uri);
	    pw.println("username: " + username);
	    pw.println("uploadFile: " + uploadFile);
	    pw.println("Search Enabled: " + doSearch);
	    pw.println("Search Predicate: " + searchPredicate);
	    pw.println("Search Timeout: " + searchTimeout);
	    pw.println("QueueItem Expiration Monitor Enabled: " + doQueueItemExpires);
	    pw.println("QueueItem Expiration Monitor Timeout: " + queueItemTimeout);
	    pw.println("Query Results Limit: " + queryTop);
	    
	    
	    
	    //pw.println("password is " + password);
	    
	    /*
	    //use the following for encrypting/decrypting new credentials.
        String passwordX = "xx";
      
        String passwordEnc = AESencrp.encrypt(passwordX);
        String passwordDec = AESencrp.decrypt(passwordEnc);

        pw.println("Plain Text : " + passwordX);
        pw.println("Encrypted Text : " + passwordEnc);
        pw.println("Decrypted Text : " + passwordDec);
	    */

	    try
	    {
	    	
	    	pw.println("\nOpening connection to domain");
	        lstartTime = System.currentTimeMillis();    
	        
		   // Make connection.
		   Connection conn = Factory.Connection.getConnection(uri);
		   Subject subject = UserContext.createSubject(conn, username, password, null);
		   UserContext.get().pushSubject(subject);
		   
	       // Get default domain.
	       Domain domain = Factory.Domain.fetchInstance(conn, null, null);
		   lendTime = System.currentTimeMillis();
		   
		   ldomainConnectTime = lendTime - lstartTime;
		   pw.println(new Date () + "\t" + "Connection to Domain made in \t" + ldomainConnectTime + "ms\t[OK]");
		   //pw.println(new Date () + "\t" + "Connection to Domain made in \t" + (lendTime - lstartTime) + "ms\t[OK]");	       
		   pw.println(new Date () + "\t" + "Domain: " + domain.get_Name());

	       // Get object stores for domain.
	       ObjectStoreSet osSet = domain.get_ObjectStores();
	       ObjectStore store;
	       Iterator osIter = osSet.iterator();
	       
   
	       
	       while (osIter.hasNext()) 
	       {
	          store = (ObjectStore) osIter.next();
	          pw.println("\n**************************************");
	          pw.println("Object store: " + store.get_Name() + "");
	          pw.println("**************************************");
	          
	          
	          //SEARCH MODULE
	          
	          if (doSearch) {
	        	  pw.println(new Date () + "\t" + store.get_Name() + "\t" + "Initiating document search on " + searchPredicate + " (top " + queryTop + ")");
	        	  lstartTime = System.currentTimeMillis();
	        	  
	        	  try{
	        	  SearchSQL sqlObject = new SearchSQL("SELECT top " + queryTop + " DocumentTitle, Id, Creator, DateCreated FROM Document WHERE DocumentTitle like '" + searchPredicate + "'" + " OPTIONS(TIMELIMIT " + searchTimeout + ")");
	        	  SearchScope scope = new SearchScope(store);
	        	  IndependentObjectSet set = scope.fetchObjects(sqlObject, 20, null, false);
		        	  for( Iterator i = set.iterator(); i.hasNext(); )
		        	  {
			        	  Document d5 = (Document)i.next();
			        	  pw.println("\t" + d5.getProperties().getStringValue("DocumentTitle") + "\t" + d5.getProperties().getStringValue("Creator") + "\t" + d5.getProperties().getDateTimeValue("DateCreated"));        	  
		        	  }
	        	  }catch(Exception e){
	        		  pw.println("Error in search module for Object Store " + store.get_Name() + "\t[ERROR]");
	       		  	  pw.println("Error Message: ");
	     		  	  pw.println(e.getMessage());
	        		  hcError=e.getMessage();
	        	  }

	        	  lendTime = System.currentTimeMillis();
	        	  lsearchTime = lendTime - lstartTime;
	        	  
	        	  //pw.println(new Date () + "\t" + store.get_Name() + "\t" + "Search completed in \t" + (lendTime - lstartTime) + "ms\t[OK]");
	        	  pw.println(new Date () + "\t" + store.get_Name() + "\t" + "Search completed in \t" + lsearchTime + "ms\t[OK]");
	        	  //trending
	        	  pwReport.print(new Date() + "," + store.get_Name() + "," + ldomainConnectTime + "," + lsearchTime + ",");

	          }
	          
	          
	          //QUEUE ITEM Expiration Module
	          
	          
	          if (doQueueItemExpires) {
	        	  pw.println(new Date () + "\t" + store.get_Name() + "\t" + "Initiating search for Expired Queue Items (top " + queryTop + ")");
	        	  lstartTime = System.currentTimeMillis();
	        	  
	        	  try{
	        	  SearchSQL sqlObject = new SearchSQL("SELECT top " + queryTop + " Id, Creator, DequeueHost, DateCreated FROM EventQueueItem WHERE RetryCount = 0 OPTIONS (TIMELIMIT " + queueItemTimeout + ")");
	        	  SearchScope scope = new SearchScope(store);
	        	  //IndependentObjectSet rowSet = scope.fetchObjects(sqlObject, 20, null, false);
	        	  RepositoryRowSet rowSet = scope.fetchRows(sqlObject, 20, null, false);
	        	  
	        	  //maybe fetch rows on above...
	        	  for( Iterator i = rowSet.iterator(); i.hasNext(); )
	        	  {
	        		  //note error for later email
	        		  if(!queueItemFoundOS.contains(store.get_Name())){
	        			  queueItemFoundOS=queueItemFoundOS + store.get_Name() + " ";
	        			  pw.println(new Date () + "\t" + store.get_Name() + "\t" + "Expired event queue item entries found\t" + "[ERROR]");
	        		  }
	        		  RepositoryRow row = (RepositoryRow)i.next();
	        		  com.filenet.api.property.Properties rProp = row.getProperties();
	        		  pw.println("\t" + rProp.getIdValue("Id") + "\t" + rProp.getStringValue("Creator") + "\t" + rProp.getStringValue("DequeueHost") + "\t" + rProp.getDateTimeValue("DateCreated"));
	        		  

	        	  }
	        	  
	        	  }catch(Exception e){
	        		  
	        		  //e.printStackTrace();
	        		  
	        		  pw.println("Error in Queue Item module for Object Store " + store.get_Name() + "\t[ERROR]");
	       		  	  pw.println("Error Message: ");
	     		  	  pw.println(e.getMessage());
	        		  hcError=e.getMessage();
	        		  
	        	  }

	        	  lendTime = System.currentTimeMillis();
	        	  lqiSearchTime = lendTime - lstartTime;
	        	  
	        	  pw.println(new Date () + "\t" + store.get_Name() + "\t" + "Search completed in \t" + lqiSearchTime + "ms\t[OK]");
	        	  //pw.println(new Date () + "\t" + store.get_Name() + "\t" + "Search completed in \t" + (lendTime - lstartTime) + "ms\t[OK]");
	        	  
	        	  pwReport.print(lqiSearchTime + ",");
	          }	          
	          
	          
	          
	          //DOC UPLOAD MODULE
	          if(doDocUpload){
	          	  pw.println(new Date () + "\t" + store.get_Name() + "\t" + "Initiating document upload test");
	        	  lstartTime = System.currentTimeMillis();
	        	  FileInputStream fis = null;
	        	  try{
	        		  fis = new FileInputStream(uploadFile);
	        	  }catch(Exception e){
	        		  pw.println("Cannot find " + uploadFile);
	       		  	  pw.println("Error Message: ");
	     		  	  pw.println(e.getMessage());
	     		  	  hcError=e.getMessage();
	        	  }
	        	  
	        	  try{
		        	  ContentTransfer ct = Factory.ContentTransfer.createInstance();
		        	  ct.setCaptureSource(fis);
		        	  ct.set_RetrievalName("HealthCheck.pdf");
		        	  ct.set_ContentType("application/pdf ");
		        	  ContentElementList cel = Factory.ContentElement.createList();
		        	  cel.add(ct);
	
		        	  //no R/T
		        	  Document doc = Factory.Document.createInstance(store, null);
		        	  //not required
		        	  doc.getProperties().putValue("DocumentTitle", "HealthCheck.pdf");
		        	  doc.set_ContentElements(cel);
		        	  
		        	  //checkin/save doc
		        	  doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
		        	  doc.save(RefreshMode.REFRESH);
		        	  lendTime = System.currentTimeMillis();
		        	  luploadTime = lendTime - lstartTime;
		        	  
		        	  pw.println(new Date () + "\t" + store.get_Name() + "\t" +"Document upload of completed in \t" + luploadTime + "ms\t[OK]");
		        	  //pw.println(new Date () + "\t" + store.get_Name() + "\t" +"Document upload of completed in \t" + (lendTime - lstartTime) + "ms\t[OK]");
		        	  
		        	  //write timing to trending report
		        	  pwReport.print(luploadTime + ",");
  
		        	  pw.println(new Date () + "\t" + store.get_Name() + "\t" +"Document Properties: " + doc.get_Id() + " " + doc.get_Creator() + " " + doc.get_ContentSize() + " " + doc.get_DateCreated());

		        	  
		        	  pw.println(new Date () + "\t" + store.get_Name() + "\t" +"Deleting " + doc.get_Id() + "...");
		        	  lstartTime = System.currentTimeMillis();
		        	  
		        	  //remove HC doc
		        	  doc.delete();
		        	  doc.save(RefreshMode.NO_REFRESH);
		        	  lendTime = System.currentTimeMillis();
		        	  ldeleteTime = lendTime - lstartTime;
		        	  
		        	  pw.println(new Date () + "\t" + store.get_Name() + "\t" +"Document " + doc.get_Id() + " deleted in \t" + ldeleteTime + "ms\t[OK]");
		        	  //pw.println(new Date () + "\t" + store.get_Name() + "\t" +"Document " + doc.get_Id() + " deleted in \t" + (lendTime - lstartTime) + "ms\t[OK]");
		        	  
		        	  //write timing to trending report
		        	  pwReport.print(ldeleteTime + ",\n");
		        	  
		        	  doc = null;
	        	  }catch(Exception e){
	        		  pw.println("Error in DocUpload module for Object Store " + store.get_Name() + "\t[ERROR]");
	       		  	  pw.println("Error Message: ");
	     		  	  pw.println(e.getMessage());
	     		  	  hcError=e.getMessage();
	        	  }
	          } 
	          

	         
	       }

	    }
	    catch(Exception e)
	    {
 		  	pw.println("Error Message: ");
 		  	pw.println(e.getMessage());
 		  	hcError=e.getMessage();
	    }
		
	    
	    //Email Alerting 
	    /*
	     * invoke and send email if we caught an exception, or if queueItem count !=0 on any object store
	     * 
	     */
	    
	    
	    if(hcError != null){
	    	 pw.println("HealthCheck encountered some ERRORS - "  + new Date () ); 
	    	 SendEmail email = new SendEmail();
	    	 email.main(hcError);
	    	 
	    }else if(!queueItemFoundOS.trim().isEmpty()){
		   	 pw.println("HealthCheck encountered some ERRORS - "  + new Date () ); 
		   	 
		   	 //add logic here to email every # failure
		   	 SendEmail email = new SendEmail();
		   	 email.main("Expired event queue items were found in: " + queueItemFoundOS);

	    }else{
	    	pw.println("HealthCheck Complete - " + new Date () + ""); 
	    }

	    pw.close();
		pw = null;
		prop = null;
		
		pwReport.close();
		pwReport = null;
	}

}


