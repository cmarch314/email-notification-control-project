package Email_predict;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Flags.Flag;











import org.jsoup.Jsoup;


public class MessageData implements java.io.Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<String> recipients = new ArrayList<String>();
	private HashSet<String> senders = new HashSet<String>();
	
	private String subject = "";
	private String content = "";
	private Date sentDate;
	private HashMap<String,Integer> content_word_freq = new HashMap<String,Integer>();
	private HashMap<String,Integer> subject_word_freq = new HashMap<String,Integer>();
	
	
	private boolean flag_reply = false;
	private boolean flag_image = false;
	private boolean flag_attachment = false;
	private boolean flag_seen = false;
	
	public MessageData()
	{
		
	}
	public MessageData(Message message) throws IOException,NoSuchProviderException,MessagingException
	{	
		sentDate = message.getSentDate();
		subject = message.getSubject();
		flag_seen = message.isSet(Flag.SEEN);
		flag_reply = message.isSet(Flag.ANSWERED);
		senders = this.getAddresses(message.getFrom());
		
		//recipients = this.getAddresses(message.getRecipients(Message.RecipientType.TO));
		subject_word_freq = Utilities.getFreqHashDict(Utilities.tokenizeString(subject));
		content = this.getText(message);
		content_word_freq = Utilities.getFreqHashDict(Utilities.tokenizeString(content));
		
	}
	/**
	 * Return the primary text content of the message.
	 */
	public boolean getFlagAttachment()
	{
		return this.flag_attachment;
	}
	public boolean getFlagimage()
	{
		return this.flag_image;
	}
	public boolean getFlagReply()
	{
		return this.flag_reply;
	}
	public boolean getFlagSeen()
	{
		return this.flag_seen;
	}
	public HashSet<String> getSenders()
	{
		return senders;
	}
	public ArrayList<String> getRecipients()
	{
		return recipients;
	}
	public String getContent()
	{
		return this.content;
	}
	public HashMap<String, Integer> getContentFreq()
	{
		return this.content_word_freq;
	}
	public String getSubject()
	{
		return this.subject;
	}
	public HashMap<String, Integer> getSubjectFreq()
	{
		return this.subject_word_freq;
	}
	public Date getSentDate()
	{
		return this.sentDate;
	}
	
	
	
	
	private HashSet<String> getAddresses(Address[] addresses)
	{
		HashSet<String> senderArray = new HashSet<String>();
		
		
		
		for (int i = 0; i < addresses.length; i++)
		{
			String senderString = addresses[i].toString();
			int startOfAddress = senderString.indexOf("<");
			int endOfAddress = senderString.indexOf(">");
			if (startOfAddress != -1 && endOfAddress != -1) //Remove extra info if exists
			{
				senderString = senderString.substring(startOfAddress+1,endOfAddress);
			}
			senderArray.add(senderString);
		}
		
		return senderArray;
	}
	
	
	
	
	
	private String getText(Message message) throws MessagingException, IOException 
	{
		int depth = 0;
		String content = "";
		
		if(message.isMimeType("multipart/*"))
		{
			
//			System.out.println("Multipart :" + message.getContentType());
			Multipart multipart = (Multipart) message.getContent();
			int j = 0;
			if(message.isMimeType("multipart/alternative"))
			{
				j = 1;
//				System.out.println("Alternative skipped");
			
			}
			while( j < multipart.getCount()) 
			{	
				BodyPart bodyPart = multipart.getBodyPart(j);
				String disposition = bodyPart.getDisposition();
				if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) 
				{        
					this.flag_attachment = true;
	//				System.out.println("Attachment! ");
				}
				content += getText(bodyPart,depth+1);
				j++;
			}
			
			
		}
		else if(message.isMimeType("text/plain"))
		{
			 //System.out.println("SinglePart "+message.getContentType());
			 content = (message.getContent()).toString();
		}
		else if(message.isMimeType("text/html"))
		{
			 //System.out.println("SinglePart "+message.getContentType());
			 content = (message.getContent()).toString();
			 content = Jsoup.parse(content).text();
		}
		else
		{
			//System.out.println("SomeThingElse: "+message.getContentType());	
		}
		return content;
	}
		
	private String getText(Part p,int depth) throws MessagingException, IOException 
	{
		String s = "";	
		/*
		for(int i = 0 ; i < depth ; i++)
			System.out.print("     ");
		System.out.println("getText Part:"+p.getContentType());
		*/
		if (p.isMimeType("TEXT/plain"))
		{	
			 s = (p.getContent()).toString();
			 return s;
		}
		else if (p.isMimeType("TEXT/HTML"))
		{
			 s = (p.getContent()).toString();
			 s = Jsoup.parse(s).text();
			 return s;
		}
		else if (p.isMimeType("multipart/alternative")) 
		{
			Multipart mp = (Multipart)p.getContent();
			//System.out.println("Alternative Skipped");
			for (int i = 1; i < mp.getCount(); i++) 
			{
			    s += getText(mp.getBodyPart(i),depth+1);
			}
		}
		else if (p.isMimeType("multipart/*")) 
		{
			Multipart mp = (Multipart)p.getContent();
			for (int i = 0; i < mp.getCount(); i++) 
			{
				s += getText(mp.getBodyPart(i),depth+1);
			}
		}
		else if(p.isMimeType("image/*"))
		{
			this.flag_image = true;
			//System.out.println("image Found");
		}
		
		return s;
	}
	public HashSet<String> getSubjectSet() {
		HashSet<String> temp = new HashSet<String>();
		for(String s : this.subject_word_freq.keySet())
		{
			temp.add(s);
		}
		return temp;
	}
	public HashSet<String> getContentSet() {
		HashSet<String> temp = new HashSet<String>();
		for(String s : this.content_word_freq.keySet())
		{
			temp.add(s);
		}
		return temp;
	}
	
}
