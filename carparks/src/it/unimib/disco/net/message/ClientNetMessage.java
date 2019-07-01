package it.unimib.disco.net.message;

import java.util.List;

import it.unimib.disco.domain.Ticket;
import it.unimib.disco.domain.Parcheggio.Snapshot;

/**
 * This class represents the structure of the messages exchanged
 * between @see AutomobilistaSocketClient and @see SocketServer
 *
 */
public class ClientNetMessage extends NetMessage {
	
	private List<Snapshot> snapshots = null;
	private Snapshot selectedSnapshot = null;
	
	public ClientNetMessage() {

	}
	
	public ClientNetMessage(NetMessageType type) {
		super(type);
	}

	public ClientNetMessage(NetMessageType type, Snapshot toBook, int timeSlotStart, int timeSlotEnd) {
		
		super(type, timeSlotStart, timeSlotEnd);
		this.selectedSnapshot = toBook;
	}
	
	public ClientNetMessage(NetMessageType type, Ticket ticket, int timeSlotStart, int timeSlotEnd) {
		super(type, ticket, timeSlotStart, timeSlotEnd);
	}
	
	public ClientNetMessage(
			NetMessageType type,
			Ticket ticket,
			int timeSlotStart,
			int timeSlotEnd,
			Snapshot selectedSnapshot) {
		
		super(type, ticket, timeSlotStart, timeSlotEnd);
		this.selectedSnapshot = selectedSnapshot;
	}
	
	public ClientNetMessage(NetMessageType type, List<Snapshot> snapshots, int timeSlotStart, int timeSlotEnd) {
		
		super(type, timeSlotStart, timeSlotEnd);
		this.snapshots = snapshots;
	}
	
	public ClientNetMessage(NetMessageType type, List<Snapshot> snapshots) {
		this(type, snapshots, 0, Integer.MAX_VALUE);
	}

	@Override
	public NetMessageType getType() {
		return this.type;
	}

	public List<Snapshot> getSnapshots() {
		return snapshots;
	}


	public void setSnapshots(List<Snapshot> snapshots) {
		this.snapshots = snapshots;
	}

	public Snapshot getSelectedSnapshot() {
		return selectedSnapshot;
	}

	public void setSelectedSnapshot(Snapshot selectedSnapshot) {
		this.selectedSnapshot = selectedSnapshot;
	}

}
