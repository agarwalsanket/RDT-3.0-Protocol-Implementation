import java.io.Serializable;
import java.util.Random;

public class Packet implements Serializable{
		private String payload;
		private String checksum;
		private int seq;
		private String ack;
		
	 Packet(String payload,String checksum,int seq,String ack){
		 this.payload = payload;
		 this.checksum = checksum;
		 this.seq = seq;
		 this.ack = ack;
	 }
	 
	 public void corruptPayload(){
	
		this.payload = this.payload +"r";
	 }
	 public void correctPayload(){
			
			this.payload = this.payload.substring(0, this.payload.length()-1);
		 }
	 public String get_checkSum(){
		 return checksum;
	 }
	 
	 public String get_payLoad(){
		 return payload;
	 }
	 
	 
	 public int get_seq(){
		 return seq;
	 }
	 public String getAck(){
		 return ack;
	 }

}
