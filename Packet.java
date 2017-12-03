
import java.io.Serializable;
/* 
 * Packet.java 
 * 
 * Revisions: Initial Version
 *    
 */
/**
 * This class makes the Packet and consists the attributes of 
 * a packet. This classs also provides the methods to retrieve these attributes.
 *
 * @author Sanket Agarwal
 */

public class Packet implements Serializable{
	
	private static final long serialVersionUID = 1L;
		private String payload;
		private String checksum;
		private int seq;
		private String ack;
		
	 Packet(String payload,String checksum,int seq,String ack){
		 /*
		  * Initializing the Packet attributes
		  */
		 this.payload = payload;
		 this.checksum = checksum;
		 this.seq = seq;
		 this.ack = ack;
	 }
	 
	 public void corruptPayload(){
		 /*
		  * Corrupting the packetload
		  */
	
		this.payload = this.payload +"r";
	 }
	 public void correctPayload(){
		 /*
		  * Correcting the packet load
		  */
			
			this.payload = this.payload.substring(0, this.payload.length()-1);
		 }
	 public String get_checkSum(){
		 /*
		  * Getter method for checksum
		  */
		 return checksum;
	 }
	 
	 public String get_payLoad(){
		 /*
		  * Getter method for payload
		  */
		 return payload;
	 }
	 
	 
	 public int get_seq(){
		 /*
		  * getter method for sequence number
		  */
		 return seq;
	 }
	 public String getAck(){
		 /*
		  * Getter method for ACK
		  */
		 return ack;
	 }

}
