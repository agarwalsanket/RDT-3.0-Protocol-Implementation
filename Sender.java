import java.util.Random;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
//import java.util.Scanner;
/* 
 * Sender.java 
 * 
 * Revisions: Initial Version
 *    
 */
/**
 * The Sender class handles all the functinalities in the Sender side
 * of a RDT 3.0 protocol.
 *
 * @author Sanket Agarwal
 */

public class Sender{

	private static byte[] out_data;
	private static byte[] in_data;
	protected DatagramSocket socket = null;
	InetAddress address=null;
	DatagramPacket send_packet=null;
	DatagramPacket receive_Packet = null;


	public Sender(){
		/*
		 * Initislizing the datagram socket and making the address object
		 */
		try {
			socket = new DatagramSocket(); 

			address = InetAddress.getByName("localhost");	
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (SocketException e) {
			e.printStackTrace();
		}

	}


	public  void udt_send(int c, int seq,Packet pkt_obj,int timeout){  
		/*
		 * This method just sends the datagram packet to the reciever port
		 */
		ObjectOutputStream out;
		try{
			byte[] outBuf = new byte[1024];
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bo);
			out.writeObject(pkt_obj);
			out.flush();	
			outBuf = bo.toByteArray();
			send_packet = new DatagramPacket(outBuf, outBuf.length, address, 8082);

			socket.send(send_packet);
			// setting the socket for timeout after a certain time
			socket.setSoTimeout(timeout); 
		}
		catch (NotSerializableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			socket.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			socket.close();
		}
	}

	public Packet rdt_recieve(){
		/*
		 * This method just recieves ACK until a certain amount of timeout time
		 */
		in_data = new byte[1024];
		Packet pkt = null;
		try{
			receive_Packet = new DatagramPacket(in_data, in_data.length);
			socket.receive(receive_Packet);
			byte[] rcv_stream = receive_Packet.getData();
			ByteArrayInputStream bs = new ByteArrayInputStream(rcv_stream);
			ObjectInputStream in_s = new ObjectInputStream(bs);
			pkt = (Packet) in_s.readObject();

		}
		catch (SocketTimeoutException e) {
			/*
			 * If the timeout happens control comes in this 
			 * catch block.
			 * It just returns Null
			 */
			return pkt;
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
		return pkt;
	}
	
	public void send_simulator(int c, int seq,Packet pkt_obj){
		/*
		 * Medium loss simulator
		 */
		Random randomno = new Random();
		int timeout = 2000;
		// Generating random numbers for random loss and corruption depiction
		int randLoss = randomno.nextInt(15);
		
		if(randLoss==6){
			// doesn't send tha packet at all
			System.out.println();
			System.out.println();
			System.out.println("----------------------------------------------------");
			System.out.println("sending packet "+c);
			System.out.println("sending packet "+pkt_obj.get_payLoad());
			System.out.println("Sending sequence "+pkt_obj.get_seq());
			System.out.println("Packet "+c+" is lost");
		}
		if(randLoss!=6 && randLoss !=10){
			//Normal sending of the packet
			System.out.println();
			System.out.println();
			System.out.println("----------------------------------------------------");
			System.out.println("sending packet "+c);
			System.out.println("sending packet "+pkt_obj.get_payLoad());
			System.out.println("Sending sequence "+pkt_obj.get_seq());
			udt_send(c, seq, pkt_obj,timeout);

		}
		else if(randLoss==10){
			System.out.println();
			System.out.println();
			System.out.println("----------------------------------------------------");
			System.out.println("sending packet "+c);
			System.out.println("sending packet payload"+pkt_obj.get_payLoad());
			System.out.println("Sending sequence "+pkt_obj.get_seq());
			System.out.println("corrupting packet"+ c);
			pkt_obj.corruptPayload();  //Corrupting the payload
			udt_send(c, seq, pkt_obj,timeout);
			pkt_obj.correctPayload();  //correcting it for sending it again
		}

	}
	
	public void udt_send_runner(){
		/*
		 * This is the main method which is running
		 * an infinite loop for sending packets and recieving ACKs
		 */
		int c = 0;  // Packet number
		int seq = 0;  //sequence number of packet 
		int prev_seq = 1;
		checkSum chk = new checkSum();
		boolean delayed = false;
		String checksum=null;
		String data = null;
		Packet pkt_obj=null;
		Packet pkt = null;
		while(true){ // Infinite loop
			out_data = new byte[1024];
			
			if(!delayed){ // Executes only if original pkt to be sent
				data = dataGenerator(); // getting data to be sent from upper layer depiction
				out_data = data.getBytes();
				checksum = chk.make_cheksum(out_data);
				pkt_obj = make_pkt(seq,data,checksum);
			}
			
			delayed=false;
			// sending the packet to the sending simulator to in turn send to the reciever
			send_simulator(c, seq, pkt_obj); 
			pkt = rdt_recieve();
			
			//Premature timeout checking
			if(pkt !=null && pkt.get_seq()==prev_seq && pkt.get_seq() != pkt_obj.get_seq()){ 

				//If premature timeout do nothing on the acknowledgement received again for the same sequence
				pkt = rdt_recieve(); //receiving the ACK for currently sent packet and do nothing
			}
			
			if(pkt==null){ // timeout
				System.out.println("timeout for packet "+c);
				delayed = true;
				c--; // resending the same packet again
				seq = (seq^1); // toggling 0 and 1
			}

			else {
				System.out.println("recieved ACK for packet "+c);
			}

			c++;
			prev_seq = seq; // Storing previously acked sequence
			seq = (seq^1); // Toggling between 0 and 1
		}

	}

	public static String dataGenerator(){ 
		/*
		 * This method generates data to be sent to the 
		 * network. It depicts the Application layer
		 */
		String[] data = {"hello","day","mango","apple","star","moon","banana","rose","RIT","Sanket","234"};
		
		// Random number to select data randomly from the data list
		Random randomno = new Random(); 
		return data[randomno.nextInt(data.length)];
	}


	public static Packet make_pkt(int seq, String data, String checksum){
		/*
		 * This mathod calls the Packet construvtor and makes the Packet to be sent
		 */
		Packet snd_pkt = new Packet(data,checksum,seq,null);
		return snd_pkt;

	}
	public  void closeSocket(){
		socket.close();
		System.exit(1);
	}

	public static void main(String[] args){
		/*
		 * This is the main method
		 */
		Sender s = new Sender();
		System.out.println("-------------Sender Screen--------------");
		System.out.println("*******************************************");
		System.out.println("*******************************************");
		System.out.println("*******************************************");
		System.out.println("*******************************************");
		s.udt_send_runner(); // calling the sending loop method
		/*int exit;
		Scanner user_input = new Scanner( System.in );
		exit = user_input.nextInt();
		if(exit==1)
			user_input.close();
			s.closeSocket();*/
	}



}
