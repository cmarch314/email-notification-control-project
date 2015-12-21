package Email_predict;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

public class SavedData {
	private TreeMap<Integer,MessageData> MessageList;
	private String userID = "";
	public SavedData()
	{
		this.MessageList = new TreeMap<Integer,MessageData>();
	}
	
	public SavedData(String ID)
	{
		this.userID = (String) ID.subSequence(0, ID.indexOf("@"));
		this.MessageList = new TreeMap<Integer,MessageData>();
	}
	
	public void setUserID(String ID)
	{
		this.userID = ID;
		this.MessageList = new TreeMap<Integer,MessageData>();
	}
	public String getUserID()
	{
		return this.userID;
	}
	public MessageData get(int key)
	{
		return this.MessageList.get(key);
	}

	public Set<Integer> keySet()
	{
		return this.MessageList.keySet();
	}

	public Object clone()
	{
		return this.MessageList.clone();
	}
	public boolean isEmpty()
	{
		return this.MessageList.isEmpty();
	}
	public Object entrySet()
	{
		return this.MessageList.entrySet();
	}
	public void remove(int arg0)
	{
		this.MessageList.remove(arg0);
	}
	
	public boolean containsKey(int arg0)
	{
		return this.MessageList.containsKey(arg0);
	}
	public boolean containsValue(int arg0)
	{
		return this.MessageList.containsValue(arg0);
	}
	
	public void put(int key, MessageData MsgData)
	{
		this.MessageList.put(key, MsgData);
	}
	public Integer size()
	{
		return this.MessageList.size();
	}
	@SuppressWarnings("unchecked")
	public void convertHashVer()
	{
		HashMap<Integer,MessageData> Old_version = new HashMap<Integer,MessageData>();
		String localID = this.userID;
		try
	    {
			FileInputStream fis = new FileInputStream(localID + "saved_messages.ser");
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        Old_version = (HashMap<Integer,MessageData>) ois.readObject();
	        for (int key : Old_version.keySet())
			{
				this.MessageList.put(key, Old_version.get(key));
			}
	        ois.close();
	        fis.close();
	    }
		catch(IOException ioe)
	    {
	    	System.out.println("Old version Saved File not found");
	    }
		catch(ClassNotFoundException c)
	    {
			System.out.println("Class not found");
	    }

	}
	
	@SuppressWarnings("unchecked")
	public void load(String ID)
	{
		
		this.userID = (String) ID.subSequence(0, ID.indexOf("@"));
		try
	    {
			FileInputStream fis = new FileInputStream(this.userID + "_Data.ser");
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        MessageList = (TreeMap<Integer,MessageData>) ois.readObject();
	        ois.close();
	        fis.close();
	        System.out.println("Size of loaded message: "+MessageList.size());
	    }
		catch(IOException ioe)
	    {
	    	System.out.println("Saved File not found, try for old version");
	    	this.convertHashVer();
	    }
		catch(ClassNotFoundException c)
	    {
			System.out.println("Class not found");
	    }
	}
	/**
	 * Saves hashmap of message objects to local path of the project
	 * name follows same as load message
	 * 
	 * @param localID
	 */
	public void save()
	{
		String localID =this.userID;
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(localID + "_Data.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(MessageList);
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void save(String ID)
	{
		String localID = (String) ID.subSequence(0, ID.indexOf("@"));
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(localID + "saved_messages.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(MessageList);
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
/**
 * downloads messages from user account (IMAX should be enabled)
 * data will be stored into Hash map MessageList
 * @param ID
 * @param Password
 */
	public void download(String ID, String Password)
	{	
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		
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
			
			int messageSize = messages.length;
			System.out.println("Total mail: "+ messageSize);
			if(MessageList.size() < messageSize)
			{
				System.out.println("		Loading... \n ==================================================");	
			}
			else
			{
				System.out.println("all Messages are up to date");	
			}
			Message message;
			for(int k = MessageList.size(); k < messageSize ; k++) 
			{
				message = messages[k];
				System.out.println(k + ": Downloading msg : "+message.getSubject());
				MessageData md = new MessageData(message);
				MessageList.put(k, md);
				save(); //UNCOMMENT THIS IF YOU WANT RESUMABLE DATA (Saves after each message is loaded rather than at the end)
			}

			//since all connection proceed in try we need to handle runtime errors
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
}
