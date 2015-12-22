package Email_predict;
/*
 * this is email Inbox reader module. 
 * this will read your email object in "Inbox" folder.
 * parse the email contents and save it into MessageData object.
 * the collection of MessageData will be saved as serialized data into current project folder.
 * the class takes two argument, userID(email address) and password
 * you need to enable IMAP in your email account other wise the request will be rejected.
 * for gmail account, setting -> Fowarding and POP/IMAP -> IMAP Access -> enable IMAP.
 * 
 */
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
	
	public static void main(String args[]) {	
		
		/*
		 * Message List is collection of MessageData, integer -> MessageData
		 * descending order from latest email.
		 */
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
		
		/*
		 * checking local class path contains saved data. \
		 * this can be replaced as Username + "saved_message.ser" for multiple user
		 */
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

		/*
		 * downloading process.
		 */
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
			
			// READ_ONLY  / choosing READ_WRITE affects message flag being "read" so be careful
			inbox.open(Folder.READ_ONLY);
			Message messages[] = inbox.getMessages();
			int messageSize = messages.length;
			int progress = messageSize / 50;
			
			/*
			 * Start downloading message
			 */
			System.out.println("Total mail:"+ messageSize + "  mails per #" + progress );
			System.out.println("		Loading... \n ==================================================");	
			
			
			Message message;
			String content = "";
			
			//message loop starts here =======================================================================================================================
			
			//from latest mail to earliest 
			for(int k = MessageList.size(); k < messageSize ; k++) {
				message = messages[k];
				System.out.println("Subject: "+message.getSubject());
				MessageData md = new MessageData(message);
				MessageList.put(k, md);
				content = md.getContent();
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
				FileOutputStream fos  = new FileOutputStream("saved_messages.ser");
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(MessageList);
				oos.close();
				fos.close();
			}
			//============================================================== end of message loop
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

