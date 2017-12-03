/* 
 * checkSum.java 
 * Revisions: Initial Version
 *    
 */
/**
 * This class consists of methods which in calculating the checksum at both
 * sender side and the reciever side
 *
 * @author Sanket Agarwal
 */
public class checkSum {

	public  String decToBin(int num){
		/*
		 *This function converts decimal to binary and returns binary string  of 16 bits
		 */
		String bin = "";
		while(num > 1){
			int rem = num%2;
			bin = rem + "" + bin;
			num = num/2;
		}
		bin = num + "" + bin;
		int no_bits_left = 8-bin.length();
		while(no_bits_left>0){
			bin = 0 + "" + bin;	
			no_bits_left--;
		}
		bin = "00000000"+""+bin; // Adding extra 8 bits to make 16 bits

		return bin;
	}

	public  String binAddition(String b1, String b2){
		/*
		 * This method calculates the sum of two binary strings and return
		 * a binary string of 16 bits
		 */
		int carry = 0;
		String sum = "";
		for(int i=b1.length()-1;i>=0;i--){  
			int s = 0;
			char f_bit = b1.charAt(i);
			char sec_bit = b2.charAt(i);
			int f_int = (f_bit=='0'?0:1);
			int sec_int = (sec_bit=='0'?0:1);
			s = f_int+sec_int+carry;
			sum = (s%2)+""+sum;
			carry = s/2;

		}
		if(carry == 1)
			sum = carry+""+sum;
		if(sum.length() > 16){
			return binAddition("000000000000000"+sum.substring(0, 1),sum.substring(1,sum.length()));
		}

		return sum;
	}

	public  String cal_sum(byte[] b){
		/*
		 * This method just calculates the cheksum before inverting it. 
		 * It calculates for the reciever side.
		 * 
		 */
		String sum="0000000000000000";  // 16 bits
		for(int i=0;i<b.length;i++){
			String bits = decToBin(b[i]);
			sum = binAddition(sum,bits);
		}

		return sum;
	}

	public String make_cheksum(byte[] b){
		/*
		 * This method calculates the inverted checksum for the sender side
		 * 
		 */
		String sum = cal_sum(b);
		String cheksum="";
		for(int i=0;i<sum.length();i++){
			if(sum.charAt(i)=='0')cheksum += "1";
			else cheksum += "0";
		}

		return cheksum;
	}
}
