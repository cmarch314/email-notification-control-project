package Email_predict;
//Ho Choi 53734155 choih4@uci.edu
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A collection of utility methods for text processing.
 */
public class Utilities {
	public static ArrayList<String> tokenizeFile(File input) {
		// TODO Write body!
		String buffer = "";
		int a;
		ArrayList<String> TokenList = new ArrayList<String>();

		File f = new File(input.getAbsolutePath());

		FileReader fr = null;

		try
		{
			fr = new FileReader(f);	
			a = fr.read();
			while (a != -1)
			{
				while((a >= 'A' && a <= 'Z') || (a >= 'a' && a <= 'z'))
				{
					if(a >= 'A' && a <= 'Z')
					{
						a = a - 'A' + 'a';
					}
					
					buffer += (char)a;
					a = fr.read();
				}
				if (buffer.length() > 0)
				{
					TokenList.add(buffer);
					buffer = "";
				}
				a = fr.read();
			}
			fr.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println("Error; File not found");
			System.exit(-2);
		}		
		return TokenList;
	}

	public static ArrayList<String> tokenizeString(String input) {
		String buffer = "";
		int a;
		ArrayList<String> TokenList = new ArrayList<String>();
		
		if (input != null)
		{
			input = input.toLowerCase();

			for(int i = 0; i < input.length() ; i++)
			{
				a = input.charAt(i);
				if((a >= 'a' && a <= 'z'))
				{
					buffer += (char)a;
				}
				if(!(a >= 'a' && a <= 'z'))
				{
					if(buffer.length() > 1)
					{
						TokenList.add(buffer);
						buffer = "";
					}
					else
					{
						buffer = "";
					}
				}
			}
		}
		//Empty if input is null
		return TokenList;
	}
	public static HashMap<String,Integer> getFreqHashDict(ArrayList<String> wordList)
	{
		HashMap<String,Integer> buffDict = new HashMap<String,Integer>();
		for(String word : wordList)
		{
			if(buffDict.containsKey(word))
				buffDict.put(word, buffDict.get(word)+1);
			else
				buffDict.put(word, 1);
		}
		return buffDict;
	}
	
	
	
}
