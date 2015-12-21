package Email_predict;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.HashMap;


public class InboxReader {
	
	private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
	private static final String PATH_INBOX_TEXT = CURRENT_DIRECTORY + "\\InboxText\\";
	private static final String PATH_INBOX_SENDER = CURRENT_DIRECTORY + "\\InboxSender\\";
	private static final String PATH_INBOX_READ_FLAG = CURRENT_DIRECTORY + "\\InboxReadFlag\\";
	
	//private static final String PATH_SENTBOX_TEXT = CURRENT_DIRECTORY + "\\SentBoxText\\";
	//private static final String PATH_SENTBOX_RECIPIENT = CURRENT_DIRECTORY + "\\SentBoxRecipient\\";
	public static void main(String args[]) {	

		HashMap<Integer,MessageData> MessageList = new HashMap<Integer,MessageData>();
		
		
		Properties props = System.getProperties();
		String ID = "";
		String Password = "";
		
		if(args.length < 2)
		{
			System.out.println("need Arguments as email xxx@xxx.xxx and password");
			System.exit(0);
		}
		ID = args[0];
		Password = args[1];
		props.setProperty("mail.store.protocol", "imaps");
		
		try
	      {
	         FileInputStream fis = new FileInputStream("saved_messages.ser");
	         ObjectInputStream ois = new ObjectInputStream(fis);
	         MessageList = (HashMap) ois.readObject();
	         ois.close();
	         fis.close();
	      }catch(IOException ioe)
	      {
	         ioe.printStackTrace();
	      }catch(ClassNotFoundException c)
	      {
	         System.out.println("Class not found");
	      }
		
		System.out.println("Size of loaded message"+MessageList.size());
		for(MessageData med:MessageList.values())
		{
			System.out.println("Loaded: "+med.getSubject());
		}
		try {

			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("imaps");
				
			store.connect("imap.gmail.com", ID, Password);
			
			if (!store.isConnected())
			{
				System.out.println("connection failed, check email address and password");
				System.exit(0);
			}
			System.out.println(store);
			
			
			Folder inbox = store.getFolder("inbox");
			
			// READ_ONLY  / READ_WRITE -> affact message to be read becareful
			inbox.open(Folder.READ_ONLY);
			Message messages[] = inbox.getMessages();
			
		
			Folder[] f = store.getDefaultFolder().list("*");
			
			for(Folder fd:f)
			{
				System.out.println(">> "+fd.getFullName());
				
			}
				
			
			int messageSize = messages.length;
			int progress = messageSize / 50;
			/*
			 * Start of message loop
			 */
			System.out.println("Total mail:"+ messageSize + "  mails per #" + progress );
			System.out.println("		Loading... \n ==================================================");	
			
			
			Message message;
			String content = "";
			
			//message loop starts here =======================================================================================================================
			
			//from latest mail to earliest 
			for(int k = MessageList.size() ; k < messageSize ; k++) {
				message = messages[k];
	
				
	
				System.out.println("Subject: "+message.getSubject());
				
				MessageData md = new MessageData(message);
				MessageList.put(k, md);
				content = md.getContent();
				//System.out.println(content);
				
				
			
				Address[] senderList = message.getFrom();
				ArrayList<String> senderArray = new ArrayList<String>();
				
				for (int i = 0; i < senderList.length; i++)
				{
					String senderString = senderList[i].toString();
					int startOfAddress = senderString.indexOf("<");
					int endOfAddress = senderString.indexOf(">");
					if (startOfAddress != -1 && endOfAddress != -1) //Remove extra info if exists
					{
						senderString = senderString.substring(startOfAddress+1,endOfAddress);
					}
					senderArray.add(senderString);
				}
				
				//System.out.println("Does InboxText exist?: " + Files.exists(PATH_INBOX)); //DEBUG
				
				//Make folders if necessary, won't overwrite anything if already exists
				File newDirectory = new File(PATH_INBOX_TEXT);
				newDirectory.mkdir();
				newDirectory = new File(PATH_INBOX_SENDER);
				newDirectory.mkdir();
				newDirectory = new File(PATH_INBOX_READ_FLAG);
				newDirectory.mkdir();
				//newDirectory = new File(PATH_SENTBOX_TEXT);
				//newDirectory.mkdir();
				//newDirectory = new File(PATH_SENTBOX_RECIPIENT);
				//newDirectory.mkdir();
				
				FileOutputStream fos  = new FileOutputStream("saved_messages.ser");
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(MessageList);
				oos.close();
				fos.close();
//				System.in.read();

				
			}
			//============================================================== end of message loop
			
			
			//since all connection proceed in try we need to handle runtime errors
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	
	}

}

