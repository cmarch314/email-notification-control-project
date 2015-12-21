package Email_predict;

import java.util.Random;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

public class email_decisionTree {
	
	/**
	 * list of MessageData object that contains all values of java.mail.message collected from user account
	 */
	private SavedData MessageList;
	/**
	 * list of Message id to be tested
	 */
	private ArrayList<Integer> recentMsgIDs = new ArrayList<Integer>();
	/**
	 * list of Message Id used as training set
	 */
	private ArrayList<Integer> TrainingMsgIDs = new ArrayList<Integer>();
	/**
	 * links sender to message id
	 * each unique sender key returns list of Message ID that belongs
	 * e.g "example@uci.edu" -> [1,55,23,2 ... , 92, 424] 
	 */
	private HashMap<String, HashSet<Integer>> Senders 			= new HashMap<String,HashSet<Integer>>();
	/**
	 * links domain to message id
	 * each unique domain key returns list of Message ID that belongs 
	 * e.g "@uci.edu" -> [1,55,23,2 ... , 92, 424] 
	 */
	private HashMap<String,HashSet<Integer>> Domains 			= new HashMap<String,HashSet<Integer>>();
	
	/**
	 * each key holds words collected from messages
	 * each words holds list of integer that maps to MessageList as Key
	 * this index constructed to improve speed of iteration
	 * e.g "Welcome" -> [5,55,12 ... 600,244]
	 */
	private HashMap<String,HashSet<Integer>> Word_map	 		= new HashMap<String,HashSet<Integer>>();
	private int Recent = 5;
	
	/**
	 * 
	 * constructor of decisionTree
	 * requires email account, Password
	 * you may require to enable IMAX on your account manually
	 */
	public email_decisionTree(SavedData sd)
	{
		this.MessageList = sd;
//		System.out.println("welcome to email_decisionTree");
		//To test accuracy
		//if you want to see the function is Symmetric
		//for(int i = 0 ; i < 21 ; i++)System.out.println(i + " = " + this.entropy_log(20-i, i));
		
		
		
	}//end of constructor
	public MessageData get(int msgID)
	{
		return this.MessageList.get(msgID);
	}
	public void setRecent(int number)
	{
		this.Recent = number;
	}
	public void download(String ID, String Password)
	{
		MessageList.download(ID, Password);
	}
	public void save()
	{
		MessageList.save();
	}
	public void load()
	{
	}
	public HashMap<Integer,Boolean> run()
	{
		this.construct_msg_index(this.Recent);
		this.construct_all_index();
		HashMap<Integer,Boolean> report = new HashMap<Integer,Boolean>();
		boolean result = true;
		for(int msgID : this.recentMsgIDs)
		{
			//copy data
			ArrayList<Integer> msgSubset = new ArrayList<Integer>();
			msgSubset.addAll(TrainingMsgIDs);
			HashSet<String> attrSubset = new HashSet<String>();
			attrSubset = this.find_attr(msgID);

			//System.out.println("Test Subject:"+this.MessageList.get(msgID).getSubject());
			//will be the result of value that is pure or not
			// true = Seen, not deterministic, False = notSeen
			result = this.ID3(TrainingMsgIDs,attrSubset,msgID);
			if(result)
			{
				//System.out.println("Notify");
			}
			else
			{
				//System.out.println("egnore");
			}
			report.put(msgID, result);
		}
		return report;
	}
	public double five_folder_test()
	{
		double avgAcc = 0;
		for(int i = 0 ; i < 5; i++)
		{
			avgAcc+= accuracyTest();
		}
		avgAcc = avgAcc/5;
		System.out.println("avgAccuracy = "+avgAcc);
		return avgAcc;
	}
	/**
	 * this is helper function for five folder test
	 * @return
	 
	 */
	private double accuracyTest()
	{
		this.set_test_set();
		
		boolean 	result = true;
		int 		progress = 0;
		int 		total = this.recentMsgIDs.size();
		int 		T_correct = 0;
		int 		F_correct = 0;
		int			false_alarm = 0;
		int			false_ignored = 0;
		
		for(Integer ID:this.recentMsgIDs)
		{
			progress ++;
			if (progress%5 == 0)
			{
				System.out.print(".");
			}
			result = this.ID3(this.TrainingMsgIDs, this.find_attr(ID), ID);
			if(result==this.MessageList.get(ID).getFlagSeen() && result ==true)
			{
				T_correct ++;
			}else if(result==this.MessageList.get(ID).getFlagSeen()&& result ==false)
			{
				F_correct ++;
			}
			else if(result == true) // false alarm!! wrong to be true 
			{
				false_alarm++;
			}else if(result == false) // false ignore!! wrong to be false 
			{
				false_ignored++;
			}
		}
		
		System.out.println(" Accuracy: "+(T_correct+F_correct) + "/" + total + 
				"  false alarm: "+false_alarm+"/"+(F_correct+false_alarm)+
				"  false ignore: "+false_ignored+"/"+(T_correct+false_ignored));
		return (double)(T_correct+F_correct)/total;
	}
	/**
	 * get random 100 number from recent msg
	 * @return
	 */
	private void set_test_set()
	{
		int index = 0;
		this.TrainingMsgIDs = new ArrayList<Integer>();
		this.recentMsgIDs = new ArrayList<Integer>();
		ArrayList<Integer> temp = new ArrayList<Integer>();
		
		for(Integer i :this.MessageList.keySet())
		{
			temp.add(i);
		}
		while(this.recentMsgIDs.size() < 100)
		{
			Random r = new Random();
			index = r.nextInt(temp.size()-1);
			this.recentMsgIDs.add(temp.get(index));
			temp.remove(index);
		}
		this.TrainingMsgIDs = temp;
		this.construct_all_index();
	}
	/**
	 * constructs Training set 
	 * the number of Recent of most recent messages will be excluded from training set
	 */
	private void construct_msg_index(int Recent_amount)
	{
		int count = 0;
		int index = 0;
		while(count < MessageList.size()-Recent_amount)
		{
			while(!MessageList.containsKey(index))
			{
				index ++;
			}
			this.TrainingMsgIDs.add(index);
			count++;
			index++;
		}
		while(count < MessageList.size())
		{
			while(!MessageList.containsKey(index))
			{
				index ++;
			}
			this.recentMsgIDs.add(index);
			count++;
			index++;
		}

	}

	/**
	 * 
	 * constructing Indexes that navigate from element to msgID
	 * e.g 	word -> all msgIDs contains that word
	 * 		xxx@uci.edu -> msgIDs that matches to sender
	 * 		@uci.edu -> msgIDs that domain is @uci.edu 
	 * 		all values are HashSet [1,5,20 ... 520 , 611]
	 * saves to private hash maps
	 */
	private void construct_all_index()
	{

		// find appliable attribute
		// senders? domain of sender? subject? content?
		//navigate attr to msg
		this.Word_map 	= new HashMap<String,HashSet<Integer>>();		
		this.Senders 	= new HashMap<String,HashSet<Integer>>();
		this.Domains 	= new HashMap<String,HashSet<Integer>>();
		
		HashSet<String> tempSenders = new HashSet<String>();
		String strBuff = "";
		//constructing Index
		
		for(int msgID : this.TrainingMsgIDs)
		{
			MessageData msgData = this.MessageList.get(msgID);
			tempSenders = msgData.getSenders();
			
			for(String word : msgData.getSubjectSet())
			{
				
				if(!Word_map.containsKey(word))
				{
					HashSet<Integer> temp = new HashSet<Integer>();
					Word_map.put(word, temp);
				}
				Word_map.get(word).add(msgID);
			}
			for(String word : msgData.getContentSet())
			{
				if(!Word_map.containsKey(word))
				{
					HashSet<Integer> temp = new HashSet<Integer>();
					Word_map.put(word, temp);
				}
				Word_map.get(word).add(msgID);
			}
			for(String Sender : tempSenders)
			{
				strBuff = Sender.substring(Sender.indexOf("@"));
				if(strBuff.length() > 0)
				{
					if(!Domains.containsKey(strBuff))
					{
						HashSet<Integer> temp = new HashSet<Integer>();
						Domains.put(strBuff, temp);
					}
					Domains.get(strBuff).add(msgID);
				}
				if(!Senders.containsKey(Sender))
				{
					HashSet<Integer> temp = new HashSet<Integer>();
					Senders.put(Sender, temp);
				}
				Senders.get(Sender).add(msgID);
			}
		}
		File stopwordsFile = new File("stopwords.txt");
		ArrayList<String> Stopwords = Utilities.tokenizeFile(stopwordsFile);
		for(String stopword : Stopwords)
		{
			if(this.Word_map.containsKey(stopword))
			{
				Word_map.remove(stopword);
			}
		}
		
		
	}
	
	/**
	 * finds attributes to use
	 * since variable that doen't belongs to test message cannot be used
	 * all messages that has no common attribute to test message will be filtered
	 * @param msgID
	 * @return
	 */
	private HashSet<String> find_attr(int msgID)
	{
		//find appliable attr
		HashSet<String> 		attr = new HashSet<String>();
		MessageData 			msgData = this.MessageList.get(msgID);
		HashSet<String> 		temp_senders = msgData.getSenders();
		HashSet<String> 		temp_subjects = msgData.getSubjectSet();
		HashSet<String> 		temp_contents = msgData.getContentSet();
		
		for(String sender : temp_senders)
		{
			String domain = sender.substring(sender.indexOf("@"));
			//Domains are already constructed by construct_all_index
			if(Domains.containsKey(domain))
			{
				attr.add("Domains");
			}
			//Senders are already constructed by construct_all_index
			if(Senders.containsKey(sender))
			{
				attr.add("Senders");
			}
		}
		//find out the words exist on past subjects
		for(String word : temp_subjects)
		{
			//Word_map is already constructed by construct_all_index
			if(Word_map.containsKey(word))
			{
				attr.add("&"+word);
			}
		}for(String word : temp_contents)
		{
			//Word_map is already constructed by construct_all_index
			if(Word_map.containsKey(word))
			{
				attr.add("&"+word);
			}
		}
		
		return attr;
	}
		
	/**
	 * basic recursive method of decision Tree
	 * it seeks most decisive attribute
	 * filters out not relative messages 
	 * forms subset and call recursively until it finds pure result, or ran out of attributes
	 * 
	 * @param s , Set of message IDs
	 * @param As . Set of attributes
	 * @param depth , number to track recursive depth
	 * @param ID , test Msg's ID
	 * @return True if it belongs to Seen, False if it belongs Notseen, not pure result will be return as seen
	 */
	private boolean ID3(ArrayList<Integer> s, HashSet<String> As, int ID) 
	{
		HashSet<String> 					Aset = 		new HashSet<String>(As); 
		ArrayList<Integer> 					S = 		new ArrayList<Integer>(s); 
		HashMap<String,ArrayList<Integer>> 	subsets = 	new HashMap<String,ArrayList<Integer>>();
		ArrayList<Integer> 					subset = 	new ArrayList<Integer>();
		if(H(S) == 0)
		{
			//we got pure result
			boolean flag_seen = true;
			for(int ii: s)
			{
				flag_seen = this.MessageList.get(ii).getFlagSeen();
			}
			return flag_seen;
		}
		double temp_etp = 0;
		double max_etp = 0;
		String chosen_attr = "";
		for(String A : Aset)
		{
			temp_etp = Gain(S,A);
			if(temp_etp > max_etp)
			{
				max_etp = temp_etp;
				chosen_attr = A;
			}
		}
		if(chosen_attr.equals(""))
		{
			return true;
		}
		//split subset
		
		Aset.remove(chosen_attr);
		subsets = split(S,chosen_attr);

		
		if(chosen_attr.equals("Domains"))
		{
			for (String sender : MessageList.get(ID).getSenders())
			{
				try
				{
					String temp_domain = sender.substring(sender.indexOf("@"));

					subset.addAll(subsets.get(temp_domain));
				}
				catch(NullPointerException e)
				{
					System.out.println("No Domain Found:"+ID);
					System.out.println("check: "+this.MessageList.get(ID).getSubject());
					System.out.println("to str:"+	sender);
					
				}
			}
		}
		else if(chosen_attr.equals("Senders"))
		{
			for (String sender : MessageList.get(ID).getSenders())
			{
//				System.out.println("From " + sender);
				try
				{
					subset.addAll(subsets.get(sender));	
				}
				catch(NullPointerException e)
				{
					System.out.println("No Sender Found:"+ID);
					System.out.println("check"+this.MessageList.get(ID).getSubject());
					System.out.println("to str:"+	sender);
				}
			}
		}
		else if(chosen_attr.contains("&"))
		{
//			System.out.println("+" + chosen_attr);
			subset.addAll(subsets.get(chosen_attr));
		}
		if(subset.size() != 0)
		{
			return ID3(subset,Aset,ID);
		}
		else
		{
			return true;
		}
	}
	
	/**
	 * 
	 * @param s, Set of message ID's
	 * @param attr , Selected attribute to analysis
	 * @return number of entropy can be reduced by selected attribute
	 */
	private double Gain(ArrayList<Integer> s, String attr)
	{
		ArrayList<Integer> S = new ArrayList<Integer>(s);
		//H(S) - sum(H(subS) * |subS|/|S|)
		double sum_H_subs = 0;
		double HS = this.H(S);
		
		HashMap<String,ArrayList<Integer>> subSets = this.split(S, attr); //if word than attr or dummy
		
		
		//sum(HsubS) * |subS|/|S|
		
		for(ArrayList<Integer> subSet:subSets.values())
		{
			sum_H_subs += this.H(subSet) * ( (double)subSet.size() / (double)S.size() );
		}
		
		return HS - sum_H_subs;
	}
	
		
	/**
	 * split splits given set S into subsets determined by attribute attr
	 * @param S , parent Set of msgID to be splited
	 * @param attr , attribute to split parent Set
	 * @return, Hashmap of subsets key = attributes value, value = subSet of msgID, if it is word that key is word or rest of words named as "dummy"
	 * 
	 */
	private HashMap<String,ArrayList<Integer>> split(ArrayList<Integer> S, String attr) 
	{	
		HashMap<String,ArrayList<Integer>> subsets = new HashMap<String,ArrayList<Integer>>();
		
		if(attr.equals("Senders")) // devide subset by senders
		{
			for (int msgID : S)
			{
				for (String sender  : MessageList.get(msgID).getSenders())
				{
					if(!subsets.keySet().contains(sender))
					{
						ArrayList<Integer> subset = new ArrayList<Integer>();
						subsets.put(sender, subset);
					}
					subsets.get(sender).add(msgID);
				}
			}
		}
		else if(attr.equals("Domains")) // devide subset by domains
		{
			for (int msgID : S)
			{
				for (String sender  : MessageList.get(msgID).getSenders())
				{
					String domain = sender.substring(sender.indexOf("@"));
					if(!subsets.keySet().contains(domain))
					{
						ArrayList<Integer> subset = new ArrayList<Integer>();
						subsets.put(domain, subset);
					}
					subsets.get(domain).add(msgID);
				}
			}	
		}
		else if(attr.contains("&")) // devide subset by subject words
		{

			ArrayList<Integer> dummy = new ArrayList<Integer>();
			subsets.put("dummy", dummy);

			String word = attr.substring(attr.indexOf("&")+1);
			for(int msgID: S)
			{
				if(MessageList.get(msgID).getContentSet().contains(word)
						||MessageList.get(msgID).getSubjectSet().contains(word))
				{
					if(!subsets.keySet().contains("&"+word))
					{
						ArrayList<Integer> subset = new ArrayList<Integer>();
						subsets.put("&"+word, subset);
					}
					subsets.get("&"+word).add(msgID);
					
				}
				else
				{
					subsets.get("dummy").add(msgID);
				}
			}
		}
		
		return subsets;
	}
	/**
	 * calculates Heuristic value (entropy) of given messageID set
	 * counting Seen, NotSeen values of given Sets.
	 * @param S , Set of MessageID
	 * @return entropy is symmetric value that close to 0 means decisive, and close to 1 means not decisive either values are Seen or not seen
	 */
	private double H(ArrayList<Integer> S)
	{
		ArrayList<Integer> s = new ArrayList<Integer>(S);
		double Seen = 0;
		double NotSeen = 0;
		double entropy = 0;
		for (int msgID : s)
		{
			if(MessageList.get(msgID).getFlagSeen())
			{
				Seen++;
			}
			else
			{
				NotSeen++;
			}
		}
		entropy = this.entropy_log(Seen, NotSeen);
		return entropy;
	}
	
	/**
	 * -1 (x/x+y) * log_2(x/x+y) + (y/x+y) * log_2(y/x+y)
	 * Returns Entropy value between 1,0 
	 * lower value means decisive
	 * @param s, count of Seen number
	 * @param n, count of Not Seen number
	 * @return value is symmetric, either of s+n = total number of Messsage
	 */
	private double entropy_log(double s, double n)
	{
		double t = s + n;
//		System.out.println("s "+s+" n "+n+" log_2 : " + log_two(s/t) + ", " + log_two(n/t) + " s/t " + s/t + " n/t " + n/t);
		return -1 * (s/t * log_two(s/t) + n/t * log_two(n/t));
		
	}
	/**
	 * 
	 * mathmatic helper function to calculate log_2 ()
	 * @param x
	 * @return
	 */
	private double log_two(double x)
	{
		if(x == 0)
		{ 
			return 0;
		}
		return (double)Math.log(x) / Math.log(2);
	}
	
	
}




