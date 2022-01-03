package networking;

public class Account {
	
	public String username;
	public String password;
	public int elo;
	
	public Account(String username, String password, int elo)
	{
		this.username = username;
		this.password = password;
		this.elo = elo;
	}

	public Account()
	{
		this.username = null;
		this.password = null;
		this.elo = 0;
	}
	
}
