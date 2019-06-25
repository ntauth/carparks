package it.unimib.disco.net.message;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import it.unimib.disco.domain.Ticket;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ParcheggioNetMessage.class, name = "ParcheggioNetMessage"),

    @JsonSubTypes.Type(value = ClientNetMessage.class, name = "ClientNetMessage") }
)
public class NetMessage 
{
	protected NetMessageType type;
	protected int slot;
	protected Ticket ticket;
	
	public NetMessage()
	{
		//Per jackson
	}
	
	public NetMessage(NetMessageType type)
	{
		this.type = type;
	}
	
	public NetMessage(NetMessageType type, int slot)
	{
		this.slot = slot;
		this.type = type;
	}
	
	public NetMessage(NetMessageType type, Ticket ticket, int slot)
	{
		this.ticket = ticket;
		this.slot = slot;
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

	/**
	 * @return the ticket
	 */
	public Ticket getTicket() {
		return ticket;
	}

	/**
	 * @param ticket the ticket to set
	 */
	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	/**
	 * @return the slot
	 */
	public int getSlot() {
		return slot;
	}

	/**
	 * @param slot the slot to set
	 */
	public void setSlot(int slot) {
		this.slot = slot;
	}
}
