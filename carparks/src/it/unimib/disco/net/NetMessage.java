package it.unimib.disco.net;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ParcheggioNetMessage.class, name = "ParcheggioNetMessage"),

    @JsonSubTypes.Type(value = ClientNetMessage.class, name = "ClientNetMessage") }
)
public class NetMessage 
{
	protected NetMessageType type;
	
	public NetMessage()
	{
		//Per jackson
	}
	
	public NetMessage(NetMessageType type)
	{
		this.type = type;
	}
	
	public NetMessageType getType()
	{
		return this.type;
	}
	
	public void setType(NetMessageType type)
	{
		this.type = type;
	}
}
