package mail;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;  
import javax.mail.*;  
import javax.mail.internet.*;  
import javax.activation.*;  



public class SendEmail  
{  


public static void main(String errorMessage){  
		Properties prop = new Properties();
		try{
		prop.load(new FileInputStream("config.properties"));
		}catch(IOException e){
			System.out.println("Cannot find config.properties");
		}

      String to = prop.getProperty("distribution");
      String from = prop.getProperty("sentFrom"); 
      String host = prop.getProperty("SMTPserver");  
      String messageText = prop.getProperty("messageText") + "\n\n" + errorMessage;
      String messageSubject = prop.getProperty("messageSubject");
      
     //Get the session object  
      Properties properties = System.getProperties();  
      properties.setProperty("mail.smtp.host", host);  
      Session session = Session.getDefaultInstance(properties);  
  
     //compose the message  
      try{  
         MimeMessage message = new MimeMessage(session);  
         message.setFrom(new InternetAddress(from));  
         message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
         message.setSubject(messageSubject);  
         message.setText(messageText);  
  
         // Send message  
         Transport.send(message);  
      }catch (MessagingException mex) {
    	  mex.printStackTrace();
    	  System.out.println(mex.getMessage());

    	  }  
   }  
}  
