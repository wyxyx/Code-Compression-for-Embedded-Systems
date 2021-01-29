package compression;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.stream.Collectors;


public class Compression {
	static String []data ;
	static String []dictionary;
	static String []reference;
	static int [] flagRLE;
	static String []comprdata;
	static String decomprdata="";
	static HashMap<String, Integer> map ;
	static int globalBuffer = 0;
	
	public static void main(String[] args) throws IOException {
		//if(args.length == 1 )
		//{
		//	if(args[0].equals("1"))
				//Compress
		        execCompress();
		//	else if(args[0].equals("2"))
		        //Decompress
		        execDecompress();
		//}
		//else {
		//	System.out.println("error.");
		//}
	}
	public static void execCompress() throws IOException {
		//Compress
		readOrig();
		sortByFrequency();
		buildDictionary();
		compressData();
		writeCompressed();
		
	}
	public static void execDecompress() throws IOException {
		//Decompress
		readCompr();
		extractInfo();//continually process function mapDictionary()
		writeDecompressed();
		
	}
	static int getLenOrig() throws IOException {
		Scanner input = new Scanner( new FileReader("src\\orig.txt"  ) );
        String s;
        int i=0;
        while ( input.hasNext() ) {
        	s = input.next();
        	i++;
        }
        return i;
	}
	static void readOrig() throws IOException {
		int len = getLenOrig();
		data = new String[len];
		Scanner input = new Scanner( new FileReader("src\\orig.txt"  ) );
	      
        String s;
        int i=0;
        while ( input.hasNext() ) {
        	s = input.next();
        	data[i] = s;
        	i++;
        }
        
	}
	static void sortByFrequency() {
		map = new HashMap<>(); 
		
		for(String s : data) 
		{
			if(map.containsKey(s))
			{
				map.put(s, map.get(s)+1);
			}
			else 
			{
				map.put(s,1);
			}
		}
	}
	
	static void buildDictionary() {
		
		String []ndata = new String [map.size()];
		dictionary = new String [8];
		List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue() - o1.getValue());
			}
		});
		HashMap<Integer,Integer> mp = new HashMap<Integer,Integer>();
		
		int num = 0;
		for(Entry<String, Integer> t:list){
			if(mp.containsKey(t.getValue())) {
				mp.put(t.getValue(), mp.get(t.getValue())+1);
			}else {
				mp.put(t.getValue(),1);
			}
		}
		for(int key:mp.keySet())
		{
			//if(mp.get(key)>1)//>1
			//{
				//System.out.println(key+" "+mp.get(key));
				Map<Integer, List<Map.Entry<String,Integer>>>result= map.entrySet().stream().collect(Collectors.groupingBy(c -> c.getValue()));

				String s[] = new String [mp.get(key)];
				int i = 0;
				for(Entry<String, Integer> t:result.get(key)){
					//System.out.println(t.getKey()+":"+t.getValue());
					s[i] = t.getKey();
					i++;
				}
				//sort s[]
				for(int m = 0; m < s.length - 1; m++)
					for(int n = 0; n < s.length - 1 - m; n++)
				    {
						getFirstArrival(s,n,n+1);
				    }
				for(int j = 0; j < s.length ; j++)
			    {
					ndata[num] = s[j];
					num++;
			    }
	    }
		
		int k = 0;
		//build dictionary(8 entries)
		for(int j = num-1;j >= num-8;j--) {
			dictionary[k] = ndata[j];
			k++;
		}
	}
	
	static void getFirstArrival(String []s,int m,int n){
		int arrivalA = -1;
		int arrivalB = -1;
		for (int i = 0; i < data.length; ++i)
		{
			if(s[m] == data[i]){
				arrivalA = i;
				break;
			}
		}
		for (int i = 0; i < data.length; ++i)
		{
			if(s[n] == data[i]){
				arrivalB = i;
				break;
			}
		}
		if (arrivalB > arrivalA)
		{
			String temp = s[n];
			s[n] = s[m];
			s[m] = temp;
		}
	}
	static void RLE() {
		flagRLE = new int[data.length];
		for(int i = 0;i < data.length;i++)
		    flagRLE[i] = -1;//initialize ( not use RLE)
		int num = 1;
		for(int i = 0; i < data.length-1; i++){
			if(data[i].equals(data[i+1])) {
				//String line = data[i];
				num++;
			}else {
				num = 1;
			}
			if(num > 1)
			{
				i++;
				while(i < data.length-1) {
					if(num == 5)
						break;
					if(data[i].equals( data[i+1])) {
						i++;
						num++;
					}else {
						break;
					}
				}
	            //System.out.println("start point is"+(i-num+1)) ;
	            
	           // System.out.println( "count is : "+ num) ;
	            
	            for(int j = i-num+2; j < i; j++)
	            {
	            	flagRLE[j] = 0;//0 represents empty rows
	            }
	            flagRLE[i] = num;//num>1  represents RLE code
	            num = 1;
			}
		}
	}
	
	static String padBits(String num,int bits) {
		String res = "";
		// padded with 0
		if(num.length() < bits) {
			for(int i = 0;i < bits-num.length();i++) {
				res +="0";
			}
		}
		res = res + num;
		
		return res;
	}
	
	static void compressData() {
		RLE();
		reference = new String[data.length];
		
		int flag = 0;
		for(int i = 0;i < data.length;i++){
			if(flagRLE[i] == 0)
				reference[i] = "";
			if(flagRLE[i] > 0)
				reference[i] = "000 "+ padBits(Integer.toBinaryString((flagRLE[i]-2))+"",2);
			
			if(flagRLE[i] == -1) {
			
			for(int j = 0; j < dictionary.length ; j++){
				if(data[i].equals( dictionary[j])){
					
					reference[i] = "101 " + padBits(Integer.toBinaryString(j),3); 
					
					flag = 1;
					break;
				}
				else
					flag=0;
			}
			if(flag == 0)
			{
				int OneMismatch = 0;
				int OneStartingBit = -1;
				
				int ConsecutiveMismatch = 0;
				int AnyWhereMismatch = 0;
				int ConsecutiveStartingBit = -1;
				int AnyWhereFirstBit = -1;
				int AnyWhereSecondBit = -1;
				int MismatchCount = 0;
				
				int BitmaskStartingBit = -1;
				int BitmaskEndingBit = -1;
				int Bitmask = 0;
				
				// 1-bit Mismatch
				for(int j = 0; j != dictionary.length; j++){
					MismatchCount = 0;
					String temp1 = data[i];
					String temp2 = dictionary[j];
					
					for(int k = 0; k < temp1.length() ; k++){
						if(temp1.charAt(k) != temp2.charAt(k)){
							if(MismatchCount == 0){
								OneStartingBit = k;
								OneMismatch = 1;
							}
							MismatchCount++;
						}
					}
					if(MismatchCount == 1 && OneMismatch == 1)
					{
						String TempStartBit = Integer.toBinaryString(OneStartingBit);
						// To convert starting bit to 5 bit number
						reference[i] = "010 "+ padBits(TempStartBit,5) + " " + padBits(Integer.toBinaryString(j),3);
						
						OneMismatch = 1;
						break;
					}
					else
						OneMismatch = 0;
				}
				// 2-bit Consecutive Mismatch
				if(OneMismatch == 0 ) {
				for(int j = 0; j != dictionary.length; j++){
					MismatchCount = 0;
					String temp1 = data[i];	
					String temp2 = dictionary[j];
					
					for(int k = 0; k < temp1.length() ; k++){
						if(temp1.charAt(k) != temp2.charAt(k)){
							
							if(MismatchCount == 0){
								ConsecutiveStartingBit = k;
								if(temp1.charAt(k+1) != temp2.charAt(k+1))
								{
									ConsecutiveMismatch = 1;
								}
							}
							MismatchCount++;
						}
					}
					if(MismatchCount == 2 && ConsecutiveMismatch == 1)
					{
						String TempStartBit = Integer.toBinaryString(ConsecutiveStartingBit);
						// To convert starting bit to 5 bit number
						reference[i] = "011 "+ padBits(TempStartBit,5) + " " + padBits(Integer.toBinaryString(j),3);
						
						ConsecutiveMismatch = 1;
						break;
					}
					else
						ConsecutiveMismatch = 0;
				}
				}
				// bitmask Mismatch
				if(OneMismatch == 0 && ConsecutiveMismatch == 0) {
				for(int j = 0; j != dictionary.length; j++){
					MismatchCount = 0;
					String temp1 = data[i];	
					String temp2 = dictionary[j];
					
					int flg = 0;
				
					for(int k = 0; k<temp1.length() ; k++){
						if(temp1.charAt(k) != temp2.charAt(k)){
							if(MismatchCount == 0){
								BitmaskStartingBit = k;
							}
							if(MismatchCount <= 3){
								BitmaskEndingBit = k;
								Bitmask = 1;
								
								if(MismatchCount == 2) {
									if(temp1.charAt(k+1) != temp2.charAt(k+1))
										//1011
										flg = 1;
								}
							}
							MismatchCount ++;
							}
						}
						if(MismatchCount == 2 && Bitmask == 1)
						{
							int interval = BitmaskEndingBit - BitmaskStartingBit;
							if( interval >= 2 && interval <= 3)
							{
								Bitmask = 1;//satisfy bitmask
								if(interval == 2)
								{
									String TempStartBitOne = Integer.toBinaryString(BitmaskStartingBit);
									reference[i] = "001 "+ padBits(TempStartBitOne,5) + " " + "1010" +" "+padBits(Integer.toBinaryString(j),3);
							        break;
								}
								else //if(interval == 3)
								{
									String TempStartBitOne = Integer.toBinaryString(BitmaskStartingBit);
									
							        reference[i] = "001 "+ padBits(TempStartBitOne,5) + " " + "1001" +" "+padBits(Integer.toBinaryString(j),3);
							        break;
								}
							}
							else {
								Bitmask = 0;
							}
						}
						else if(MismatchCount == 3 && Bitmask == 1)
						{
							int interval = BitmaskEndingBit - BitmaskStartingBit;
							if( interval >= 2 && interval <= 3)
							{
								Bitmask = 1;//satisfy bitmask
								if(interval == 2)
								{
									String TempStartBitOne = Integer.toBinaryString(BitmaskStartingBit);
							        reference[i] = "001 "+ padBits(TempStartBitOne,5) + " " + "1110" +" "+padBits(Integer.toBinaryString(j),3);
							       
							        break;
								}
								else //if(interval == 3)
								{
									String TempStartBitOne = Integer.toBinaryString(BitmaskStartingBit);
									if(flg == 1)
							            reference[i] = "001 "+ padBits(TempStartBitOne,5) + " " + "1011" +" "+padBits(Integer.toBinaryString(j),3);
									else
										reference[i] = "001 "+ padBits(TempStartBitOne,5) + " " + "1101" +" "+padBits(Integer.toBinaryString(j),3);
							        break;
								}
							}
							else {
								Bitmask = 0;
							}
						}
						else if(MismatchCount == 4 && Bitmask == 1)
						{
							String TempStartBitOne = Integer.toBinaryString(BitmaskStartingBit);
					        reference[i] = "001 "+ padBits(TempStartBitOne,5) + " " + "1111" +" "+padBits(Integer.toBinaryString(j),3);
					        break;
						}
						else
							Bitmask=0;
					}
			    }
				// 2-bit Anywhere Mismatch
				//may have conflict with Bitmask Mismatch, but Bitmask has high priority
				if(ConsecutiveMismatch == 0 && OneMismatch == 0 && Bitmask == 0){
					for(int j = 0; j != dictionary.length; j++){
					MismatchCount = 0;
					String temp1 = data[i];	
					String temp2 = dictionary[j];
				
					for(int k=0; k<temp1.length() ; k++){
						if(temp1.charAt(k) != temp2.charAt(k)){
							if(MismatchCount == 0){
								AnyWhereFirstBit = k;
							}
							if(MismatchCount == 1){
								AnyWhereSecondBit = k;
								AnyWhereMismatch = 1;
							}
							MismatchCount ++;
							}
							
						}
						if(MismatchCount==2 && AnyWhereMismatch == 1)
						{
							String TempStartBitOne = Integer.toBinaryString(AnyWhereFirstBit);
							String TempStartBitTwo = Integer.toBinaryString(AnyWhereSecondBit);
							reference[i] = "100 "+ padBits(TempStartBitOne,5) + " " + padBits(TempStartBitTwo,5) +" "+padBits(Integer.toBinaryString(j),3);
							break;
						}
						else
							AnyWhereMismatch=0;
					}
					
				}
				// No Match
				if( Bitmask == 0 && OneMismatch == 0 && AnyWhereMismatch == 0 && ConsecutiveMismatch == 0)
				{
					reference[i] = "110 "+ data[i];
				}
			}
		  }
		}
		for(int i = 0;i < data.length;i++)
			System.out.println(i+": "+reference[i]) ;
	}
	static void writeCompressed() throws IOException {
		//first take off space
		int k = 0;
		int full = 32;
		char[] temp = new char[32];
		
		String dataComp = "";
		for(int i = 0; i < data.length; i++)
		{
			String line = reference[i].replace(" ", "");
			for(int j = 0; j < line.length(); j++)
			{
				if(k < 32)
				{
					temp[k] = line.charAt(j);
					k++;
					full--;
					if(k == 32) {
						dataComp += new String(temp);
						
						dataComp += "\n";
						temp = new char[32];
						k = 0;
						full = 32;
					}
				}
			}
		}
		//Padding with 1's
		if(full != 32){
			for(int i = 31;i > 31-full; i--)
				temp[i]='1';
			dataComp += new String(temp);
		}
		//Now Pushing Dictionary Values
		dataComp += "\nxxxx\n";
		for(int i = 0; i < dictionary.length-1; i++){
			dataComp += dictionary[i]+"\n";
		}
		dataComp += dictionary[dictionary.length-1];
		//System.out.println(dataComp);
		String fileName = "src\\cout.txt";
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(dataComp);
		writer.close();
	}
	
	//////////////decompress///////////////////
	
	static int getLenCompr() throws IOException {
		Scanner input = new Scanner( new FileReader("src\\cout.txt"  ) );
        String s;
        int i=0;
        while ( input.hasNext() ) {
        	s = input.next();
        	i++;
        }
        return i;
	}
	static void readCompr() throws IOException {
		int len = getLenCompr();
		comprdata = new String[len];
		Scanner input = new Scanner( new FileReader("src\\cout.txt"  ) );
        String s;
        int i=0;
        while ( input.hasNext() ) {
        	s = input.next();
        	comprdata[i] = s;
        	//System.out.println(i+"   :  "+comprdata[i]);
        	i++;
        }
	}
	static void extractInfo() {
		
		int flag=0;
		int m = 0, n = 0;
		for(int i = 0; i < comprdata.length; i++){
			String temp = comprdata[i];
			if(flag == 0 && !temp.equals( "xxxx")){
				reference[m] = comprdata[i];
			    m++;
			}
			if(flag==1 && !temp.equals( "xxxx")){
				dictionary[n] = comprdata[i];
			    n++;
			}
			if(temp.equals( "xxxx")){
				flag=1;
			}
		}
		
		System.out.println(m);
		System.out.println(n);
		
		int sizeCharArray = reference.length * 32;
		char[] temp = new char[sizeCharArray];
		globalBuffer = sizeCharArray;//
		
		int k = 0;
		for(int i = 0; i < reference.length; i++){
			String tempRef = reference[i];
			for(int j = 0; j < tempRef.length(); j++){
					if(tempRef.charAt(j) != ' ' && tempRef.charAt(j) != '\n'){
						if(k<sizeCharArray){
							temp[k] = tempRef.charAt(j);
							
							//System.out.print(temp[k]); 
							k++;
						}
				}
			}	
	}
	//Now we have everything in a single array and parsing will be easy
	mapDictionary(temp,0);
	
	}
	static void mapDictionary(char []arr , int curr) {
		int currentposition = curr;
		char first = arr[currentposition];
		char second = arr[currentposition + 1];
		char third = arr[currentposition + 2];
		if(first == '0'){
			if(second == '0'){
				if(third == '0')
				{
					runLengthEncoding(arr,currentposition);
				}
				if(third == '1')
				{
					bitmaskBased(arr,currentposition); 
				}
			}
			if(second == '1'){
				if(third == '0')
				{
					oneBitMismatch(arr,currentposition);
				}
				if(third == '1')
				{
					twoBitConsecutive(arr,currentposition); 
				}
			}
		}
		if(first == '1'){
			if(second == '0'){
				if(third == '0')
				{
					twoBitAnywhere(arr,currentposition);
				}
				if(third == '1')
				{
					 directMatch(arr,currentposition); 
				}
			}
			if(second == '1'){
				if(third == '0')
				{
					 originalMatch(arr,currentposition);
				}
			}
		}
	}
	private static void runLengthEncoding(char[] arr, int currentposition) {
		int buffer = 5;
		globalBuffer = globalBuffer - buffer;		
		String bufferString = "";
		
		int k = 0;
		for(int i = 0; i < 6; i++){
			if(i == 3){
				bufferString += ' ';
			}
			else
			{
				bufferString += arr[currentposition + k];
				k++;
			}
		}
		if(globalBuffer > -1){
			decompress(bufferString, 0); // String + Mode
			mapDictionary(arr, currentposition + buffer);
		}
	}
	private static void bitmaskBased(char[] arr, int currentposition) {
		int buffer = 15;
		globalBuffer = globalBuffer - buffer;
		// Might put an if/else condition to check if global buffer goes out of bounds
		String bufferString = "";// "xxx xxxxx xxxx xxx";
		int k = 0;
		for(int i = 0; i < 18; i++){
			if(i == 3 || i == 9 || i == 14){
				bufferString += ' ';
			}
			else
			{
				bufferString += arr[currentposition + k];
				k++;
			}
		}
		if(globalBuffer>-1){
			
			decompress(bufferString, 1); // String + Mode
			mapDictionary(arr, currentposition + buffer);
		}
		
	}
	private static void oneBitMismatch(char[] arr, int currentposition) {
		int buffer = 11;
		globalBuffer = globalBuffer - buffer;
		// Might put an if/else condition to check if global buffer goes out of bounds
		String bufferString = "";// "xxx xxxxx xxx";
		int k=0;
		for(int i = 0; i < 13; i++){
			if(i == 3 || i == 9){
				bufferString += ' ';
			}
			else
			{
				bufferString += arr[currentposition + k];
				k++;
			}
		}
		if(globalBuffer>-1){
			
			decompress(bufferString, 2); // String + Mode
			mapDictionary(arr,currentposition + buffer);
		}
		
	}
	private static void twoBitConsecutive(char[] arr, int currentposition) {
		int buffer = 11;
		globalBuffer = globalBuffer - buffer;
		// Might put an if/else condition to check if global buffer goes out of bounds
		String bufferString = "";// "xxx xxxxx xxx";
		int k=0;
		for(int i = 0; i < 13; i++){
			if(i == 3 || i == 9){
				bufferString += ' ';
			}
			else
			{
				bufferString += arr[currentposition + k];
				k++;
			}
		}
		if(globalBuffer>-1){
			
			decompress(bufferString, 3); // String + Mode
			mapDictionary(arr,currentposition + buffer);
		}
	}
	private static void twoBitAnywhere(char[] arr, int currentposition) {
		int buffer = 16;
		globalBuffer = globalBuffer - buffer;
		// Might put an if/else condition to check if global buffer goes out of bounds
		String bufferString = "";//"xxx xxxxx xxxxx xxx";
		
		int k = 0;
		for(int i = 0; i < 19; i++){
			if(i == 3 || i == 9 || i == 15){
				bufferString += ' ';
			}
			else
			{
				bufferString += arr[currentposition + k];
				k++;
			}
		}
		if(globalBuffer>-1){
			
			decompress(bufferString,4);
			mapDictionary(arr,currentposition+buffer);
		}
	}
	
	private static void directMatch(char[] arr, int currentposition) {
		int buffer = 6;
		globalBuffer = globalBuffer - buffer;
		// Might put an if/else condition to check if global buffer goes out of bounds
		String bufferString = "";
		
		int k = 0;
		for(int i = 0; i < 7; i++){
			if(i == 3){
				bufferString += " ";
			}
			else
			{
				bufferString += arr[currentposition + k];
				k++;
			}
		}
		if(globalBuffer > -1){
			decompress(bufferString, 5); // String + Mode
			mapDictionary(arr, currentposition + buffer);
			
		}
	}
	
	private static void originalMatch(char[] arr, int currentposition) {
		int buffer = 35;
		globalBuffer = globalBuffer - buffer;
		// Might put an if/else condition to check if global buffer goes out of bounds
		String bufferString = "";
		
		int k = 0;
		for(int i = 0; i < 36; i++){
			if(i == 3){
				bufferString += " ";
			}
			else
			{
				bufferString += arr[currentposition + k];
				k++;
			}
		}
		if(globalBuffer > -1){
			decompress(bufferString, 6); // String + Mode
			mapDictionary(arr, currentposition + buffer);
		}
	}
	
	/////////
	static String findDictionary(String index) {
		String dicdata = "";
		if(index.equals( "000")) {
			dicdata += dictionary[0];
		}
		else if(index.equals("001")) {
			dicdata +=  dictionary[1];
		}
		else if(index.equals("010")) {
			dicdata +=  dictionary[2];
		}
        else if(index.equals( "011")) {
        	dicdata +=  dictionary[3];
		}
        else if(index.equals("100")) {
        	dicdata +=  dictionary[4];
		}
        else if(index.equals("101")) {
        	dicdata +=  dictionary[5];
		}
        else if(index.equals("110")) {
        	dicdata +=  dictionary[6];
		}
        else if(index.equals("111")) {
        	dicdata +=  dictionary[7];
		}
		return dicdata;
	}
	
    private static int binaryToDecimal(String binStr){
    	 int j = 0;
         for(int i = 0; i< binStr.length();i++)
         	if(binStr.charAt(i) == '1')
         	{
         		j = i;
         		break;
         	}
         String binStr1 ;
         binStr1 = binStr.substring(j,binStr.length() );
         int sum = 0;
         int len = binStr1.length();
         for (int i = 1; i <=len; i++){
             int dt = Integer.parseInt(binStr1.substring(i-1,i));
             sum += (int)Math.pow(2,len-i) * dt;
         }
         return  sum;
    }
	
	private static void decompress(String dedata, int val) {
		
		String DictionaryData = "";
		String DictionaryMatch = "";
		char StartBit[] = new char [5];
		char FirstBit[]  = new char [5]; 
		char SecondBit[] = new char [5]; 
		
		char BitMaskBit[] = new char [4]; 
		
		int StartBitLocation;
		int FirstBitLocation;
		int SecondBitLocation;
		
		int RepetitionCount ;

		switch(val){
		    case 0 ://Format of the Run Length Encoding (RLE)
		        DictionaryMatch += dedata.charAt(4);
				DictionaryMatch += dedata.charAt(5);
				// consecutive repetition of the same instruction
				RepetitionCount = binaryToDecimal(DictionaryMatch) + 1;
				     
				decomprdata += "x" +RepetitionCount +"\n";
		    break;
			case 1 : //Format of bitmask-based compression
				DictionaryMatch += dedata.charAt(15);
				DictionaryMatch += dedata.charAt(16);
				DictionaryMatch += dedata.charAt(17);
				 
				DictionaryData = findDictionary(DictionaryMatch);
				
				//Start Bit Map
				StartBit[0] = dedata.charAt(4);
				StartBit[1] = dedata.charAt(5);
				StartBit[2] = dedata.charAt(6);
				StartBit[3] = dedata.charAt(7);
				StartBit[4] = dedata.charAt(8);
				StartBitLocation = binaryToDecimal(new String(StartBit));
				
				//use 4 bit Mask to decompress
				BitMaskBit[0] = dedata.charAt(10);
				BitMaskBit[1] = dedata.charAt(11);
				BitMaskBit[2] = dedata.charAt(12);
				BitMaskBit[3] = dedata.charAt(13);
				
				String origdic = DictionaryData;
				//System.out.println(origdic);
				
				for(int j = 0; j < 4; j++)
				{
					if(origdic.charAt(StartBitLocation + j) == BitMaskBit[j])
					{ 
						StringBuilder strBuilder = new StringBuilder(DictionaryData);
						
						strBuilder.setCharAt(StartBitLocation+j, '0');
						DictionaryData = strBuilder.toString();
					
					}
					else if(origdic.charAt(StartBitLocation + j) != BitMaskBit[j])
					{ 
						StringBuilder strBuilder = new StringBuilder(DictionaryData);
						
						strBuilder.setCharAt(StartBitLocation+j, '1');
						DictionaryData = strBuilder.toString();
					
					}
				}
				decomprdata += DictionaryData + "\n";
				
			break;
			case 2 : // Format of the 1 bit Mismatch 
				DictionaryMatch += dedata.charAt(10);
				DictionaryMatch += dedata.charAt(11);
				DictionaryMatch += dedata.charAt(12);
				 
				DictionaryData = findDictionary(DictionaryMatch);
				
				 //Start Bit Map
				 StartBit[0] = dedata.charAt(4);
				 StartBit[1] = dedata.charAt(5);
				 StartBit[2] = dedata.charAt(6);
				 StartBit[3] = dedata.charAt(7);
				 StartBit[4] = dedata.charAt(8);
				 StartBitLocation = binaryToDecimal(new String(StartBit));
				 if(DictionaryData.charAt(StartBitLocation) == '0'){
				 	StringBuilder strBuilder = new StringBuilder(DictionaryData);
				 	strBuilder.setCharAt(StartBitLocation, '1');
				 	DictionaryData = strBuilder.toString();
				 }
				 else{
					 StringBuilder strBuilder = new StringBuilder(DictionaryData);
					 strBuilder.setCharAt(StartBitLocation, '0');
					 DictionaryData = strBuilder.toString();
				 }
				 decomprdata += DictionaryData + "\n";
			break;
			case 3 : //Format of the 2 bit consecutive mismatches 
				 DictionaryMatch += dedata.charAt(10);
				 DictionaryMatch += dedata.charAt(11);
				 DictionaryMatch += dedata.charAt(12);
				 
				 DictionaryData = findDictionary(DictionaryMatch);
				 
				 //Start Bit Map
				 StartBit[0] = dedata.charAt(4);
				 StartBit[1] = dedata.charAt(5);
				 StartBit[2] = dedata.charAt(6);
				 StartBit[3] = dedata.charAt(7);
				 StartBit[4] = dedata.charAt(8);
				 
				 StartBitLocation = binaryToDecimal(new String(StartBit));
				 
				 if(DictionaryData.charAt(StartBitLocation) == '0'){
				 	StringBuilder strBuilder = new StringBuilder(DictionaryData);
				 	strBuilder.setCharAt(StartBitLocation, '1');
				 	DictionaryData = strBuilder.toString();
				 }
				 else{
					 StringBuilder strBuilder = new StringBuilder(DictionaryData);
					 strBuilder.setCharAt(StartBitLocation, '0');
					 DictionaryData = strBuilder.toString();
				 }
				 if(DictionaryData.charAt(StartBitLocation + 1) == '0'){
					 StringBuilder strBuilder = new StringBuilder(DictionaryData);
					 strBuilder.setCharAt(StartBitLocation + 1, '1');
					 DictionaryData = strBuilder.toString();
				 }
				 else{
					 StringBuilder strBuilder = new StringBuilder(DictionaryData);
					 strBuilder.setCharAt(StartBitLocation + 1, '0');
					 DictionaryData = strBuilder.toString();
				 }
				 decomprdata += DictionaryData + "\n";
			break;
			case 4 : //Format of the 2 bit mismatches anywhere 
				 DictionaryMatch += dedata.charAt(16);
				 DictionaryMatch += dedata.charAt(17);
				 DictionaryMatch += dedata.charAt(18);
				 
				 DictionaryData += findDictionary(DictionaryMatch);
				 
				 //Changing First Bit
				 FirstBit[0] = dedata.charAt(4);
				 FirstBit[1] = dedata.charAt(5);
				 FirstBit[2] = dedata.charAt(6);
				 FirstBit[3] = dedata.charAt(7);
				 FirstBit[4] = dedata.charAt(8);
				 
				 FirstBitLocation = binaryToDecimal(new String(FirstBit));
				 if(DictionaryData.charAt(FirstBitLocation) == '0'){
					 StringBuilder strBuilder = new StringBuilder(DictionaryData);
					 strBuilder.setCharAt(FirstBitLocation, '1');
					 DictionaryData = strBuilder.toString();
				 }
				 else{
					 StringBuilder strBuilder = new StringBuilder(DictionaryData);
					 strBuilder.setCharAt(FirstBitLocation, '0');
					 DictionaryData = strBuilder.toString();
				 }
				 //Changing Second Bit
				 SecondBit[0] = dedata.charAt(10);
				 SecondBit[1] = dedata.charAt(11);
				 SecondBit[2] = dedata.charAt(12);
				 SecondBit[3] = dedata.charAt(13);
				 SecondBit[4] = dedata.charAt(14);
				 SecondBitLocation = binaryToDecimal(new String(SecondBit));
				 if(DictionaryData.charAt(SecondBitLocation) == '0'){
					 StringBuilder strBuilder = new StringBuilder(DictionaryData);
					 strBuilder.setCharAt(SecondBitLocation, '1');
					 DictionaryData = strBuilder.toString();
				 }
				 else{
					 StringBuilder strBuilder = new StringBuilder(DictionaryData);
					 strBuilder.setCharAt(SecondBitLocation, '0');
					 DictionaryData = strBuilder.toString();
				 }
				 decomprdata += DictionaryData + "\n";
					
			break;
			case 5: //Format of the Direct Matching 
				 DictionaryMatch += dedata.charAt(4);
				 DictionaryMatch += dedata.charAt(5);
				 DictionaryMatch += dedata.charAt(6);
			
				 DictionaryData += findDictionary(DictionaryMatch);
				 decomprdata += DictionaryData + "\n";
				 
			break;
			case 6: //Format of the Original Binaries   
				  decomprdata += dedata.substring(4) + "\n";
			break;
		}
	}
	
	private static String convertRLE() {
		String finaldata = "";
		ArrayList<Integer> indexes = new ArrayList<>();
		ArrayList<Integer> counts = new ArrayList<>();
		String[] lines = decomprdata.split("\\r?\\n");
		int num = 0;
	    for (String line : lines) {
	    	//System.out.println("line "  + " : " + line);
	        num++;
	    }
	    for(int i = 0; i < num; i++) {
	    	if(lines[i].charAt(0) == 'x') {
	    		int index = i-1;
	    		int count = Integer.parseInt(String.valueOf(lines[i].charAt(1))) ;
	    	 
	    		indexes.add(index);
	    		counts.add(count);
	    	 }
	     }
	     for(int i = 0; i < num-1; i++) {
	        
	    	 if(lines[i].charAt(0) == 'x') { 
	    		 for(int j = 0; j < indexes.size(); j++){
	    			 if(i == indexes.get(j)+1)
	    			     for(int k = 0; k < counts.get(j); k++)
	    		             finaldata += lines[indexes.get(j)]+"\n";
	    		 }
	    	 }
	    	 else {
	    		 finaldata += lines[i]+"\n";
	    	 }
	     }
	     finaldata += lines[num-1];
	     return finaldata;
		
	}
	static void writeDecompressed() throws IOException {
		String finaldata = convertRLE() ;
		
		String fileName = "src\\dout.txt";
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(finaldata);
		writer.close();
	}
}