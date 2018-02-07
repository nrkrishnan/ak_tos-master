package com.matson.tos.messageHandler;

import com.matson.tos.exception.TosException;

public interface IMessageHandler {
	
	public void setTextStr( String text) throws TosException;
	public void setXmlStr( String xml) throws TosException;
	public void setXmlObj( Object xmlObj);
	public void setTextObj( Object textObj);
	public void setDirection( int dir);
	
	public String getTextStr() throws TosException;
	public String getXmlStr() throws TosException;
	public Object getXmlObj() throws TosException;
	public Object getTextObj() throws TosException;
	public int getDirection();
}
