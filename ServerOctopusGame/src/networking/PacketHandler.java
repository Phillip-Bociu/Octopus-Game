package networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;

public class PacketHandler extends Thread {
	public DatabaseConnection db;
	public DatagramSocket socket;

	public PacketHandler() {
		try {
			this.socket = new DatagramSocket(1331);
			this.db = new DatabaseConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		byte[] data = new byte[1024];
		DatagramPacket p = new DatagramPacket(data, data.length);
		while (true) {
			try {
				socket.receive(p);
				Packet basePacket = new Packet();
				basePacket.readFromBytes(data);

				switch (basePacket.packetID) {
				case Login: {
					LoginPacket login = new LoginPacket();
					System.out.println(p.getAddress().toString()+ ":" + p.getPort());
					login.readFromBytes(data);
					int userID = db.LoginAccount(login.username, login.password, p.getAddress().toString()+ ":" + p.getPort());
					int elo = db.QueryAccount(userID).elo;
					LoginConfirmationPacket conf = new LoginConfirmationPacket(userID, elo);
					p.setData(conf.toBytes());
					socket.send(p);
					p.setData(data);
				}
					break;

				case QueueUp: {
					QueueUpPacket qu = new QueueUpPacket();
					qu.readFromBytes(data);
					db.QueueUp(qu.ID);
				}
					break;
					
				case MatchEnd:
				{
					MatchEndPacket me = new MatchEndPacket();
					me.readFromBytes(data);
					System.out.println("Match Ended: " + me.winnerName + "won, " + me.loserName + " lost");
					
					int winnerID = db.FindAccountBy("username",me.winnerName);
					int loserID = db.FindAccountBy("username", me.loserName);
					
					db.ResolveElo(loserID, winnerID);
					
				}break;

				case Register: {
					RegisterPacket rp = new RegisterPacket();
					rp.readFromBytes(data);
					RegisterConfirmationPacket rc = new RegisterConfirmationPacket();
					rc.registered = db.RegisterAccount(rp.username, rp.password);
					p.setData(rc.toBytes());
					socket.send(p);
					p.setData(data);
				}
					break;

				case DeQueue: {
					DeQueuePacket dq = new DeQueuePacket();
					dq.readFromBytes(data);
					db.Dequeue(dq.ID);
				}
					break;

				default: {

				}
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			Pair<Integer, Integer> matchedPair = db.Match();

			if (matchedPair == null) {
				continue;
			}

			int ID1 = matchedPair.left;
			int ID2 = matchedPair.right;

			String ip1 = db.GetIP(ID1);
			String ip2 = db.GetIP(ID2);

			if(ip1.charAt(0) == '/')
				ip1 = ip1.substring(1);
			if(ip2.charAt(0) == '/')
				ip2 = ip2.substring(1);
			
			String username1 = db.QueryAccount(ID1).username;
			String username2 = db.QueryAccount(ID2).username;
			
			String[] list1 = ip1.split(":");
			String[] list2 = ip2.split(":");
			
			ip1 = list1[0];
			ip2 = list2[0];
			
			int port1 = Integer.parseInt(list1[1]);
			int port2 = Integer.parseInt(list2[1]);
			
			LobbyPacket l1 = new LobbyPacket(true , ip2, username2);
			LobbyPacket l2 = new LobbyPacket(false, ip1, username1);			

			try {
				byte[] d1 = l1.toBytes();
				byte[] d2 = l2.toBytes();
				
				
				DatagramPacket p1 = new DatagramPacket(d1, d1.length, InetAddress.getByName(ip1), port1);
				System.out.println(l1.packetID + " " + l1.otherIp + " " + ip1);
				socket.send(p1);
				
				DatagramPacket p2 = new DatagramPacket(d2, d2.length, InetAddress.getByName(ip2), port2);
				System.out.println(l2.packetID + " " + l2.otherIp + " " + ip2);
				socket.send(p2);

				
//				System.out.println("Sending: " + l2.otherIp + " to " + ip2);
//				p.setAddress(InetAddress.getByName(ip2));
//				p.setData(l2.toBytes());
//				socket.send(p);
//				System.out.println("Sending: " + l1.otherIp + " to " + ip1);
//				p.setAddress(InetAddress.getByName(ip1));
//				p.setData(l1.toBytes());
//				socket.send(p);
//				p.setData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void sendData(byte[] data, InetAddress ipAddress, int port) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
