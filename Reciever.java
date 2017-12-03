import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
//import java.util.Scanner;
/* 
 * Reciever.java 
 * 
 * Revisions: Initial Version
 *    
 */
/**
 * The Reciever class handles all the functinalities in the Reciever side
 * of a RDT 3.0 protocol.
 *
 * @author Sanket Agarwal
 */

public class Reciever{
	DatagramSocket socket=null;
	DatagramPacket receivePacket = null;
	DatagramPacket sendpacket = null;
	int port;
	InetAddress address=null;
	int seq=0;
	static String ack="ACK";
	private static byte[] out_data;
	private static byte[] in_data;

	public Reciever(){
		/*
		 * Initializing the datgram Socket object at port 8082
		 */
		try {
			socket = new DatagramSocket(8082);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void rdt_recieve(){
		/*
		 * This method actually handles all the receiving of packets
		 * and sending of acknowldgements plus depicting of few losing scenarios.
		 */
		out_data = new byte[1024];
		in_data = new byte[1024];
		ObjectOutputStream out;
		
		boolean corrupted=false; // Keeping track of if a packet is corrupted
		int c=0;
		try{
			while(true){ // running infinitely
				
				corrupted=false;
				receivePacket = new DatagramPacket(in_data, in_data.length);
				int timeout = 2010;  // timeout duration 4010 milli-secs

				socket.receive(receivePacket); // receiving packet from sender
				
				byte[] rcv_stream = receivePacket.getData();
				ByteArrayInputStream bs = new ByteArrayInputStream(rcv_stream);
				ObjectInputStream out_s = new ObjectInputStream(bs);
				Packet pkt = (Packet) out_s.readObject();
				String data = pkt.get_payLoad();

				String ckhsum_rcvd = pkt.get_checkSum();
				int seq_rcvd = pkt.get_seq();
				
				if(seq_rcvd==seq && c!=0){
					System.out.println("--------Detect duplicate packet----------)");
					c--; // Ignoring the duplicate
				}
				checkSum cksm = new checkSum();  // Initializing the checkSum class for checkSum calculation
				
				// calculating the checksum (just the sum) for seeing if packet is corrupted
				String chksum_claculated = cksm.cal_sum(data.getBytes()); 
				// adding the cheksums
				String sum_of_both = cksm.binAddition(ckhsum_rcvd, chksum_claculated);
				//System.out.println("Sum of the the two checksums "+sum_of_both);
				
				seq = seq_rcvd;  // setting sequence number to be sent for acknowledgement to the current acknowledged packet sequence number
				for(int i=0;i<sum_of_both.length();i++){
					if(sum_of_both.charAt(i)!='1'){
						if(seq_rcvd == 0)seq=1; // If packet is corrupted send the sequence no. of the previous packet
						else seq = 0;
						corrupted=true;

						System.out.println("packet "+c + " received corrupted");	
						System.out.println("resend ACK for packet "+(c-1));
						c--;
						break;
					}
				}


				Packet pkt_obj = make_pkt(seq); // making the ACK packet
				address = receivePacket.getAddress();
				port = receivePacket.getPort();

				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				out = new ObjectOutputStream(bo);
				out.writeObject(pkt_obj);
				out.flush();
				out_data = bo.toByteArray();
				
				// creating the Datagram packet to be sent
				sendpacket = new DatagramPacket(out_data, out_data.length,address,port);
				Random randomno = new Random();
				// generating random number for depicting the ACK lost and delaying packet scenario
				int randomEvent = randomno.nextInt(20); 

				if(randomEvent==10){  // depicting premature time out by delaying the ACK sent
					Thread.sleep(timeout);  
					System.out.println("----Depicting premature timeout----");
				}
				if(seq_rcvd==seq){
					System.out.println("packet "+c + " received correctly");
					System.out.println("recieved payload "+pkt.get_payLoad());
					System.out.println("recieved sequence "+pkt.get_seq());
					System.out.println("send ACK for packet "+c);
					System.out.println("--------------------------------------------------");
					System.out.println();
				}
				// Not sending ACK to depict the ACL lost scenario
				if(randomEvent==11)
					System.out.println("Lost ACK");
				if(randomEvent!=11 && !corrupted){ 
					socket.send(sendpacket);
				}
				

				c++; // Increasing the number of counter
			}


		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			socket.close();
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			socket.close();
		}
		catch(InterruptedException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			socket.close();
		}
		socket.close();

	}

	public static Packet make_pkt(int seq){

		Packet snd_pkt = new Packet(null,null,seq,ack);
		return snd_pkt;

	}
	public  void closeSocket(){
		socket.close();
		System.exit(1);
	}
	public static void main(String[] args){
		Reciever r = new Reciever();
		System.out.println("-------------Receiver Screen--------------");
		System.out.println("*******************************************");
		System.out.println("*******************************************");
		System.out.println("*******************************************");
		System.out.println("*******************************************");
		r.rdt_recieve();
		/*int exit;
		Scanner user_input = new Scanner( System.in );
		exit = user_input.nextInt();
		if(exit==1)
			user_input.close();
			r.closeSocket();*/
		
	}


}
