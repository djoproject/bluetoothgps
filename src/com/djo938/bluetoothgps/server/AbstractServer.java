package com.djo938.bluetoothgps.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.djo938.bluetoothgps.server.client.ClientInterface;


public abstract class AbstractServer implements Runnable
{
	protected boolean running, pause;
	protected List<ClientInterface> clientList;
	protected int maxClientCount;
	private final String serverName;
	private List<ServerEvent> eventClients;
	private AbstractServer This;
	
	public AbstractServer(String serverName,int maxClientCount)
	{
		this.running = false;
		this.maxClientCount = maxClientCount;
		this.clientList = new LinkedList<ClientInterface>();
		this.pause = false;
		this.serverName = serverName;
		this.eventClients = new LinkedList<ServerEvent>();
		this.This = this;
	}
	
	public void addEventClient(ServerEvent client)
	{
		eventClients.add(client);
	}
	
	public void start() throws IOException
	{
		if(!running)
		{
			onStart();
			running = true;
			new Thread(this).start();
			this.fireStart();
		}
	}
	
	public void stop() throws IOException
	{
		if(running)
		{
			running = false;
			onStop();
			this.fireStop();
		}
	}
	
	@Override
	public void run() 
	{
		while(this.running && !this.pause)
		{
			//reach max client count?
			if (maxClientCount > 0 && clientList.size() >= this.maxClientCount)
			{
				try 
				{
					onPause();
					this.pause = true;
					this.firePause();
				} 
				catch (IOException e) 
				{
					this.running = false;
					Log.e(""+this.getClass().getName()+" : pause", e.getMessage(), e);
					fireError("pause : "+e.getMessage(),0);
				}
									
				break;
			}
			
			try 
			{
				ClientInterface client = this.acceptClient();
				synchronized(This)
				{
					clientList.add(client);
				}
				this.fireAddClient(client);
				/*manageClient(client);*/
			} 
			catch (IOException e) 
			{
				this.running = false;
				Log.e(""+this.getClass().getName()+" : acceptClient", e.getMessage(), e);
				fireError("acceptClient : "+e.getMessage(),0);
			}
		}
	}
	
	protected abstract ClientInterface acceptClient() throws IOException; 
	protected abstract void onStop() throws IOException;
	protected abstract void onStart() throws IOException;
	protected abstract void onPause() throws IOException;
	protected abstract void onRestart() throws IOException;
	
	public synchronized void broadcast(byte data[])
	{
		/*broadcast to client*/
		for(int i = 0; i < clientList.size();i += 1)
		{
			ClientInterface client = clientList.get(i);
			try 
			{
				OutputStream ous = client.getOutputStream();
				ous.write(data);
				ous.flush();
			}
			catch (IOException e)  //catch also the socketexception
			{
				Log.e("broadcast", e.getMessage(), e);
				
				try { client.close(); }catch (IOException e1) {}
				
				clientList.remove(i);
				i -= 1;
				fireRemoveClient(client);
			}
		}
		
		/*need to restart server?*/
		if(this.pause && ((maxClientCount > 0 && clientList.size() <= maxClientCount-1) || maxClientCount == 0)  )
		{
			try 
			{
				onRestart();
			} 
			catch (IOException ex) 
			{
				this.running = false;
				Log.e(""+this.getClass().getName()+" : restart", ex.getMessage(), ex);
				fireError("restart : "+ex.getMessage(),0);
				return;
			}
			
			this.pause = false;
			fireRestart();
			new Thread(this).start();
		}
	}
	
	/*protected void manageClient(final ClientInterface client)
	{
		Runnable run = new Runnable() { @Override public void run() {
			int i = 0;
			try 
			{
				client.closeInput();
				while(true)
				{
					PrintStream ps = new PrintStream(client.getOutputStream());
					//Log.v("send to client", "toto");
					ps.println("toto : "+i++);
					
					if(ps.checkError()) //only way to check an end of connection on tcp/ip 
						throw new IOException("an error has occured");
					
					Thread.sleep(2000);
				}
			} 
			catch (IOException e) 
			{
				Log.e("manageClient", e.getMessage(), e);
				fireError("manageClient : "+e.getMessage(),0);
			} 
			catch (InterruptedException e) 
			{
				Log.e("manageClient", e.getMessage(), e);
				fireError("manageClient : "+e.getMessage(),0);
			}
			
			try 
			{
				client.close();
			}
			catch (IOException e1) 
			{}
			
			clientList.remove(client);
			fireRemoveClient(client);
			
			if(maxClientCount > 0 && clientList.size() == maxClientCount-1)
			{
				try 
				{
					onRestart();
				} 
				catch (IOException e) 
				{
					running = false;
					Log.e(""+this.getClass().getName()+" : restart", e.getMessage(), e);
					fireError("restart : "+e.getMessage(),0);
					return;
				}
				
				pause = false;
				fireRestart();
				new Thread(This).start();
			}
		}};
		
		new Thread(run).start();
	}*/
	
	protected void fireStart()
	{
		for( ServerEvent se : this.eventClients )
		{
			se.serverStart(this);
		}
	}
	protected void fireStop()
	{
		for( ServerEvent se : this.eventClients )
		{
			se.serverStop(this);
		}
	}
	protected void firePause()
	{
		for( ServerEvent se : this.eventClients )
		{
			se.serverPause(this);
		}
	}
	protected void fireRestart()
	{
		for( ServerEvent se : this.eventClients )
		{
			se.serverRestart(this);
		}
	}
	protected void fireAddClient(ClientInterface client)
	{
		for( ServerEvent se : this.eventClients )
		{
			se.serverAddClient(this,client);
		}
	}
	protected void fireRemoveClient(ClientInterface client)
	{
		for( ServerEvent se : this.eventClients )
		{
			se.serverRemoveClient(this,client);
		}
	}
	protected void fireError(String error,int scale)
	{
		for( ServerEvent se : this.eventClients )
		{
			se.serverError(this,scale,error);
		}
	}

	public String getServerName() {
		return serverName;
	}
	
}
