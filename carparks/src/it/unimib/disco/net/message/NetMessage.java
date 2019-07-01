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
	protected int timeSlotStart;
	protected int timeSlotEnd;
	protected Ticket ticket;
	
	/**
	 * @note Necessary for serialization/deserialization
	 */
	public NetMessage() {
		
	}
	
	public NetMessage(NetMessageType type) {
		this(type, 0, Integer.MAX_VALUE);
	}
	
	public NetMessage(NetMessageType type, int timeSlotStart, int timeSlotEnd) {
		this(type, null, timeSlotStart, timeSlotEnd);
	}
	
	public NetMessage(NetMessageType type, Ticket ticket, int timeSlotStart, int timeSlotEnd) {
		
		this.type = type;
		this.ticket = ticket;
		this.timeSlotStart = timeSlotStart;
		this.timeSlotEnd = timeSlotEnd;
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

	public int getTimeSlotStart() {
		return timeSlotStart;
	}

	public void setTimeSlotStart(int timeSlotStart) {
		this.timeSlotStart = timeSlotStart;
	}

	public int getTimeSlotEnd() {
		return timeSlotEnd;
	}

	public void setTimeSlotEnd(int timeSlotEnd) {
		this.timeSlotEnd = timeSlotEnd;
	}
	
}
