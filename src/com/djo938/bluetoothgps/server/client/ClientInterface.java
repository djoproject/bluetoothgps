package com.djo938.bluetoothgps.server.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ClientInterface 
{
	public boolean isClosed();
	public void close() throws IOException;
	public InputStream getInputStream() throws IOException;
	public OutputStream getOutputStream() throws IOException;
	public String getIdentity();
	public void closeInput() throws IOException ;
	public void closeOutput() throws IOException ;
}
