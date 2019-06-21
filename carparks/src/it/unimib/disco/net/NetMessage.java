package it.unimib.disco.net;

public abstract class NetMessage 
{
	protected NetMessageType type;
	
	public NetMessage(NetMessageType type)
	{
		this.type = type;
	}
	
	public abstract NetMessageType getType();
}
