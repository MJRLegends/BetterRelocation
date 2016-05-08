package net.kaikk.mc.br;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.bukkit.plugin.java.JavaPlugin;

public class DataStore {
	private JavaPlugin instance;
	private String dbUrl;
	private String username;
	private String password;
	protected Connection db;
	
	DataStore(BetterRelocation instance) throws Exception {
		this.instance=instance;
		this.dbUrl = "jdbc:mysql://"+instance.config.dbHostname+"/"+instance.config.dbDatabase;
		this.username = instance.config.dbUsername;
		this.password = instance.config.dbPassword;
		
		try {
			//load the java driver for mySQL
			Class.forName("com.mysql.jdbc.Driver");
		} catch(Exception e) {
			this.instance.getLogger().severe("Unable to load Java's mySQL database driver.  Check to make sure you've installed it properly.");
			throw e;
		}
		
		try {
			this.dbCheck();
		} catch(Exception e) {
			this.instance.getLogger().severe("Unable to connect to database.  Check your config file settings. Details: \n"+e.getMessage());
			throw e;
		}
		
		Statement statement = db.createStatement();

		try {
			// Creates tables on the database
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS betterrelocation (id int(11) NOT NULL AUTO_INCREMENT,player binary(16) NOT NULL,srv char(8) NOT NULL,storetime int(11) NOT NULL,dat mediumtext NOT NULL,PRIMARY KEY (id),KEY ps (player,srv));");
		} catch(Exception e) {
			this.instance.getLogger().severe("Unable to create the necessary database table. Details: \n"+e.getMessage());
			throw e;
		}
		
		statement.executeUpdate("DELETE FROM betterrelocation WHERE srv = \""+instance.config.serverName+"\" AND storetime < "+(Utils.epoch()-instance.config.timeLimit));
	}

	public Statement statement() throws SQLException {
		this.dbCheck();
		return this.db.createStatement();
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		this.dbCheck();
		return this.db.prepareStatement(sql);
	}
	
	public void dbCheck() throws SQLException {
		if(this.db == null || this.db.isClosed()) {
			Properties connectionProps = new Properties();
			connectionProps.put("user", this.username);
			connectionProps.put("password", this.password);
			
			this.db = DriverManager.getConnection(this.dbUrl, connectionProps);
		}
	}
	
	void startTransaction() throws SQLException {
		this.statement().executeUpdate("START TRANSACTION");
	}
	
	void commit() throws SQLException {
		this.statement().executeUpdate("COMMIT");
	}
	
	void rollback() {
		try {
			this.statement().executeUpdate("ROLLBACK");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	void dbClose()  {
		try {
			if (!this.db.isClosed()) {
				this.db.close();
				this.db=null;
			}
		} catch (SQLException e) {
			
		}
	}
}
