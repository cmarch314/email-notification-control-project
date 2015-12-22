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

/*
 * this class is customized email object for this project.
 * from email object, the class stores subject, content, flags and parsed data
 * also implementing Serializable object to be stored in hash map.
 */
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
	/*
	 * this is message Parser, the message content can be nested objects with duplicated contents as different form
	 * it can be text/plain, text/html with same text content. be careful
	 */
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
			
			}
			while( j < multipart.getCount()) 
			{	
				BodyPart bodyPart = multipart.getBodyPart(j);
				String disposition = bodyPart.getDisposition();
				if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) 
				{        
					this.flag_attachment = true;
				}
				content += getText(bodyPart,depth+1);
				j++;
			}
		}
		else if(message.isMimeType("text/plain"))
		{
			 content = (message.getContent()).toString();
		}
		else if(message.isMimeType("text/html"))
		{
			 content = (message.getContent()).toString();
			 content = Jsoup.parse(content).text();
		}
		else
		{
			//System.out.println("SomeThingElse: "+message.getContentType());	
		}
		return content;
	}
	/*
	 * recursive part to deal with nested form.
	 */
	private String getText(Part p,int depth) throws MessagingException, IOException 
	{
		String s = "";	
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
