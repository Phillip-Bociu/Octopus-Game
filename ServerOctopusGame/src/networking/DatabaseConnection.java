package networking;

import java.sql.*;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;

public class DatabaseConnection {

	public Connection con;

	public DatabaseConnection() {
		final String connStr = "jdbc:mysql://localhost:3306/accounts";
		final String username = "root";
		final String password = "12345";

		try {
			con = DriverManager.getConnection(connStr, username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void ResolveElo(int loserID, int winnerID)
	{
		String SQLloser = "select elo from users where ID = " + loserID;
		String SQLwinner = "select elo from users where ID = " + winnerID;
		int loserElo = 0;
		int winnerElo = 0;
		
		try {
			Statement idQuery = con.createStatement();
			ResultSet rs = idQuery.executeQuery(SQLloser);
			if (rs.next()) {
				loserElo = rs.getInt(1);
			}
			idQuery.close();
			
			idQuery = con.createStatement();
			rs = idQuery.executeQuery(SQLwinner);
			if (rs.next()) {
				winnerElo = rs.getInt(1);
			}
			idQuery.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		int diff = Math.abs(winnerElo - loserElo) / 10;
		int change = 10;
		
		if(winnerElo > loserElo)
		{
			change = Math.min(1, change - diff);
		} else
		{			
			change = change + diff;
		}
		
		SQLloser = "update users set elo = " + (loserElo - change) + " where ID = " + loserID;
		SQLwinner = "update users set elo = " + (winnerElo + change) + " where ID = " + winnerID;
		
		try {
				PreparedStatement st = con.prepareStatement(SQLloser);
				st.executeUpdate();
				st.close();
				st = con.prepareStatement(SQLwinner);
				st.executeUpdate();
				st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	public int FindAccountBy(String column, String value)
	{
		int retval = -1;
		
		String SQL = "select ID from users where " + column + " = '" + value + "'";
		
		try {
			Statement idQuery = con.createStatement();
			ResultSet rs = idQuery.executeQuery(SQL);
			if (rs.next()) {
				retval = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return retval;
	}
	
	public boolean IsInQueue(int ID) {
		boolean retval = false;

		String SQLID = "select inQueue from users where id = " + ID;

		try {
			Statement idQuery = con.createStatement();
			ResultSet rs = idQuery.executeQuery(SQLID);
			if (rs.next()) {
				retval = (rs.getInt(1) == 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retval;
	}

	public String GetIP(int ID) {
		String retval = null;
		String sql = "select ip from users where ID = " + ID;

		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			if (rs.next())
				retval = new String(rs.getString("ip"));
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retval;
	}

	public boolean RegisterAccount(String username, String password) {
		boolean retval = false;
		String SQL = "insert into users (username, passwd, elo) values('" + username + "', '" + password + "', 1000)";
		String SQLID = "select ID from users where username = '" + username + "'";

		try {
			Statement idQuery = con.createStatement();
			ResultSet rs = idQuery.executeQuery(SQLID);
			if (!rs.next()) {
				idQuery.close();
				PreparedStatement st = con.prepareStatement(SQL);
				st.executeUpdate();
				st.close();
				idQuery = con.createStatement();
				rs = idQuery.executeQuery(SQLID);
				if (rs.next())
					retval = true;
			}

			idQuery.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}

	public int LoginAccount(String username, String password, String IP) {
		int retval = -1;

		String SQL = "select ID from users where username = '" + username + "' and passwd = '" + password + "'";

		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(SQL);
			if (rs.next()) {
				retval = rs.getInt(1);
				st.close();
				String ipQuery = "Update users set ip = '" + IP + "' where id = " + retval;
				System.out.println(ipQuery);
				PreparedStatement ps = con.prepareStatement(ipQuery);
				ps.executeUpdate();
				ps.close();
			} else
				st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retval;
	}

	public Pair<Integer, Integer> Match() {
		Pair<Integer, Integer> retval = null;
		Integer a = null, b = null;

		String SQL = "select ID from users where inQueue = 1 " + "ORDER BY elo limit 2";

		Statement st;
		try {
			st = con.createStatement();
			ResultSet rs = st.executeQuery(SQL);
			if (rs.next()) {
				a = rs.getInt(1);
			}
			if (rs.next()) {
				b = rs.getInt(1);
			}
			st.close();

			if (a == null || b == null) {
				return null;
			}

			retval = new Pair<Integer, Integer>(a, b);
			String update = "update users set inQueue = 0 where ID = " + a + " or ID = " + b;
			PreparedStatement ps = con.prepareStatement(update);
			ps.executeUpdate();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return retval;
	}

	public void QueueUp(int ID) {
		String SQL = "Update users " + "set inQueue = 1 " + "where ID = " + ID;

		try {
			PreparedStatement st = con.prepareStatement(SQL);
			st.executeUpdate();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void Dequeue(int ID) {
		String SQL = "Update users " + "set inQueue = 0 " + "where ID = " + ID;

		try {
			PreparedStatement st = con.prepareStatement(SQL);
			st.executeUpdate();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int FindOponent(int yourID) {
		int retval = -1;

		int elo = QueryAccount(yourID).elo;
		System.out.println("ELO: " + elo);
		String SQL = "select ID from users where ID != " + yourID + " and inQueue = 1 " + "ORDER BY ABS(elo - " + elo
				+ ") limit 1;";

		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(SQL);
			if (rs.next()) {
				retval = rs.getInt(1);
			}
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retval;
	}

	public Account QueryAccount(int ID) {
		Account retval = null;
		String SQL = "select username, elo from users where ID = " + ID;

		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(SQL);
			if (rs.next()) {
				retval = new Account();
				retval.username = rs.getString("username");
				retval.elo = rs.getInt("elo");
			}
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}
}
