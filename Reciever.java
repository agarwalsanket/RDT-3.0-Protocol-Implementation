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
		try {
			socket = new DatagramSocket(8082);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public void rdt_recieve(){
		out_data = new byte[1024];
		in_data = new byte[1024];
		ObjectOutputStream out;
		byte[] buf = new byte[256];
		int c=0;
		try{
			while(c<25){
				
				 receivePacket = new DatagramPacket(in_data, in_data.length);

					socket.receive(receivePacket);
					byte[] rcv_stream = receivePacket.getData();
					ByteArrayInputStream bs = new ByteArrayInputStream(rcv_stream);
					ObjectInputStream out_s = new ObjectInputStream(bs);
					Packet pkt = (Packet) out_s.readObject();
					String data = pkt.get_payLoad();
					
					String ckhsum_rcvd = pkt.get_checkSum();
					int seq_rcvd = pkt.get_seq();
					if(seq_rcvd==seq && c!=0){
						System.out.println("(Detect duplicate packet)");
						c--;
					}
					checkSum cksm = new checkSum();
					
					String chksum_claculated = cksm.cal_sum(data.getBytes());
					String sum_of_both = cksm.binAddition(ckhsum_rcvd, chksum_claculated);
					System.out.println(sum_of_both);
					seq = seq_rcvd;  // setting sequence number to be sent for acknowledgement to the current acknowledged packet sequence number
					for(int i=0;i<sum_of_both.length();i++){
						if(sum_of_both.charAt(i)!='1'){
							if(seq_rcvd == 0)seq=1; // If packet is corrupted send the sequence no. of the previous packet
							else seq = 0;
							
							System.out.println("packet "+c + "received corrupted");	
							System.out.println("resend ACK for packet "+(c-1));
							c--;
							break;
						}
					}
					
						
					Packet pkt_obj = make_pkt(seq);
					 
					  address = receivePacket.getAddress();
			          port = receivePacket.getPort();
			          
			          ByteArrayOutputStream bo = new ByteArrayOutputStream();
							 out = new ObjectOutputStream(bo);
							 out.writeObject(pkt_obj);
							 out.flush();

			          out_data = bo.toByteArray();
					 
					 sendpacket = new DatagramPacket(out_data, out_data.length,address,port);
					 if(seq_rcvd==seq){
							System.out.println("packet "+c + " received correctly");
							System.out.println("recieved "+pkt.get_payLoad());
							System.out.println("send ACK for packet "+c);
							System.out.println("--------------------------------------------------");
							System.out.println();
						}
					    Random randomno = new Random();
					    int randomEvent = randomno.nextInt(15);
					    
					 if(randomEvent==10){
						 Thread.sleep(4010);  // depicting premature time out by delaying the ACK sent
					 }
					 if(randomEvent!=11){
						 socket.send(sendpacket);
					 }
					 
					 c++;
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
public static void main(String[] args){
	Reciever r = new Reciever();
	r.rdt_recieve();
}

	
}
