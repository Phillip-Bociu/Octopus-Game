package main;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import networking.*;

public class Main {

	public static void main(String[] args)
	{
		PacketHandler ph = new PacketHandler();
		if(ph.socket == null)
			return;
		ph.start();
//		
		Scanner s = new Scanner(System.in);
//		int host = s.nextInt();
//		if(host == 1)
//		{
//			
//		} else
//		{
//			byte[] data = new byte[1024];
//			DatagramSocket socket = null;
//			
//			InetAddress ip = null;
//			try {
//				DatagramPacket packet = new DatagramPacket(data, data.length);				
//				socket = new DatagramSocket();
//				ip = InetAddress.getByName("127.0.0.1");
//				LoginPacket l = new LoginPacket("phillip", "1234");
//				packet.setAddress(ip);
//				packet.setPort(1331);
//				packet.setData(l.toBytes());
//				socket.send(packet);
//				socket.receive(packet);
//				LoginConfirmationPacket p = new LoginConfirmationPacket();
//				p.readFromBytes(packet.getData());
//				
//				if(p.ID != -1)
//					System.out.println("Logged in with ID: " + p.ID);
//				else
//					System.out.println("Login failure");
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			} 
//			
//		}
//
		int wait = s.nextInt();
		ph.socket.close();
		//		
//		return;
	}
	
}
