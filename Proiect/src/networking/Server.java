package networking;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import map.Map;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server extends Thread
{
	
	private DatagramSocket socket;
	public InetAddress ipAddress;
	public int port;
	public boolean isRunning;
	public Server()
	{
		try {
			
			this.socket = new DatagramSocket(1000);
			isRunning = false;
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		
		try {
			socket.receive(packet);
			ipAddress = packet.getAddress();
			port = packet.getPort();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		ProjectilePacket proj = new ProjectilePacket();
		
		isRunning = true;
		
		while(isRunning)
		{
			
			try {
				synchronized(Map.lastSentPlayerPacket)
				{					
					packet.setData(Map.lastSentPlayerPacket.toBytes());
				}
				socket.send(packet);
	
				synchronized(Map.lastSentProjectilePacket)
				{					
					packet.setData(Map.lastSentProjectilePacket.toBytes());
					Map.lastSentProjectilePacket.speed = 0;
				}
				socket.send(packet);
	
				socket.receive(packet);
				synchronized(Map.lastReceivedPlayerPacket)
				{
					Map.lastReceivedPlayerPacket.readFromBytes(packet.getData());
				}
				socket.receive(packet);
				
				proj.readFromBytes(packet.getData());
				if(proj.speed != 0)
					synchronized(Map.projQueue)
					{
						ProjectilePacket p = new ProjectilePacket();
						p.readFromBytes(proj.toBytes());
						Map.projQueue.add(p);
					}
				
					
			}catch(SocketTimeoutException e)
			{
					 
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		socket.close();
	}
	
	
	public void sendData(byte[] data, InetAddress ipAddress, int port)
	{
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
