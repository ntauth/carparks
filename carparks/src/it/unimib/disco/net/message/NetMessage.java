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
/**
 * This class represents the base structure of the messages exchanged
 * between SocketClients and @see SocketServer
 *
 */
public class NetMessage 
{
	protected NetMessageType type;
	protected int slot;
	protected Ticket ticket;
	
	/**
	 * @note Necessary for serialization/deserialization
	 */
	public NetMessage() {
		
	}
	
	public NetMessage(NetMessageType type) {
		
		this.type = type;
	}
	
	public NetMessage(NetMessageType type, int slot) {
		
		this.slot = slot;
		this.type = type;
	}
	
	public NetMessage(NetMessageType type, Ticket ticket, int slot) {
		
		this.ticket = ticket;
		this.slot = slot;
		this.type = type;
	}
	
	public NetMessageType getType() {
		return this.type;
	}
	
	public void setType(NetMessageType type) {
		this.type = type;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}
	
}
