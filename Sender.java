import java.util.Random;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;





public class Sender{
	
	private static byte[] out_data;
	private static byte[] in_data;
	 protected DatagramSocket socket = null;
	 InetAddress address=null;
	 DatagramPacket send_packet=null;
	 DatagramPacket receive_Packet = null;

	
	public Sender(){
		try {
			socket = new DatagramSocket();

			address = InetAddress.getByName("localhost");	
	}
		catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	
	public  void udt_send(int c, int seq,Packet pkt_obj,int timeout){  //main method for sending
		//int c = 0;
		//int seq = 0;
		checkSum chk = new checkSum();
		ObjectOutputStream out;
		try{
			   
			   byte[] outBuf = new byte[1024];
				/*out_data = new byte[1024];
				String data = dataGenerator();
				out_data = data.getBytes();
				String checksum = chk.make_cheksum(out_data);
				Packet pkt_obj = make_pkt(seq,data,checksum);*/

				ByteArrayOutputStream bo = new ByteArrayOutputStream();
					 out = new ObjectOutputStream(bo);
					 out.writeObject(pkt_obj);
					 out.flush();	
				outBuf = bo.toByteArray();
				send_packet = new DatagramPacket(outBuf, outBuf.length, address, 8082);
				
				socket.send(send_packet);
				socket.setSoTimeout(timeout);
				
				/*Timer thing
				 * 
				 * 
				 */
												
				/*	receive_Packet = new DatagramPacket(in_data, in_data.length);
					socket.receive(receive_Packet);
					byte[] rcv_stream = receive_Packet.getData();
					ByteArrayInputStream bs = new ByteArrayInputStream(rcv_stream);
					ObjectInputStream in_s = new ObjectInputStream(bs);
					Packet pkt = (Packet) in_s.readObject();
					String ack_statement = pkt.getAck();
					int seq_recvd = pkt.get_seq();
				
				
				System.out.println(receive_Packet.getData());*/
		
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
            // timeout exception.
            //System.out.println("Timeout reached!!! ");
            
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
	Random randomno = new Random();
	int timeout = 4000;

	int randLoss = randomno.nextInt(15);
	if(randLoss==6){
		System.out.println("Packet "+c+" is lost");
	}
	if(randLoss!=6 && randLoss !=10){
		System.out.println();
		System.out.println();
		System.out.println("----------------------------------------------------");
		System.out.println("sending packet "+c);

		udt_send(c, seq, pkt_obj,timeout);
	}
	else if(randLoss==10){
		System.out.println();
		System.out.println();
		System.out.println("----------------------------------------------------");
		System.out.println("sending packet "+c);
		System.out.println("corrupting "+ c);
		pkt_obj.corruptPayload();
		udt_send(c, seq, pkt_obj,timeout);
	}
		
}

public void recieve_ACK_simulator(){
	
}
	
	public void udt_send_runner(){
		int c = 0;  // Packet number
		int seq = 0;  //sequence number of packet
		int prev_seq = 1;
		checkSum chk = new checkSum();
		boolean delayed = false;
		String checksum=null;
		String data = null;
		Packet pkt_obj=null;
		

			while(c<25){
				Packet pkt = null;
				out_data = new byte[1024];
				if(!delayed){
					data = dataGenerator();
					 out_data = data.getBytes();
					 checksum = chk.make_cheksum(out_data);
					 pkt_obj = make_pkt(seq,data,checksum);
				}
				 delayed=false;
				
				
				 send_simulator(c, seq, pkt_obj);
				 
				  pkt = rdt_recieve();
				 
				  if(pkt==null){
						System.out.println("timeout for packet "+c);
						delayed = true;
						c--;
						seq = (seq^1);
					}
				  else if(pkt.get_seq()==prev_seq){
						System.out.println("Corrupted packet was recieved for packet "+c);
						delayed = true;
						pkt.correctPayload();
						c--;
						seq = (seq^1);
					}
				  
				   else {
					String ack_statement = pkt.getAck();
					int seq_recvd = pkt.get_seq();
					System.out.println("recieved ACK for packet "+c);
				}

				 c++;
				 prev_seq = seq; // Storing previously acked sequence
				seq = (seq^1);
			}
			
	}
	
	public static String dataGenerator(){  // depicting application layer
		String[] data = {"hello","day","mango","apple","star","moon"};
		Random randomno = new Random();
		return data[randomno.nextInt(data.length)];
	}
	
	
	public static Packet make_pkt(int seq, String data, String checksum){
		
		Packet snd_pkt = new Packet(data,checksum,seq,null);
		return snd_pkt;

	}


	public static void main(String[] args){
		Sender s = new Sender();
		s.udt_send_runner();
	}
	
	
	
}
