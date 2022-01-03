package networking;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import map.Map;

public class Client extends Thread
{
	
	private InetAddress ipAddress;
	private DatagramSocket socket;
	public boolean isRunning;
	
	public Client(String ipAddress)
	{
		try {
			
			this.socket = new DatagramSocket(1000);
			this.ipAddress = InetAddress.getByName(ipAddress);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		isRunning = false;
	}
	
	public void run()
	{
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1000);
		try {
			synchronized(Map.lastSentPlayerPacket)
			{					
				packet.setData(Map.lastSentPlayerPacket.toBytes());
			}
			socket.send(packet);
			socket.setSoTimeout(2000);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		ProjectilePacket proj = new ProjectilePacket();
		isRunning = true;
		while(isRunning)
		{
			try {
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
			}
			catch(SocketTimeoutException e)
			{
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		socket.close();
	}
}
