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

	public ClientNetMessage(NetMessageType type, Snapshot toBook, int slot) {
		
		super(type, slot);
		selectedSnapshot = toBook;
	}
	
	public ClientNetMessage(NetMessageType type, Ticket ticket, int slot) {
		super(type, ticket, slot);
	}
	
	public ClientNetMessage(NetMessageType type, Ticket ticket, int slot, Snapshot selectedSnapshot) {
		
		super(type, ticket, slot);
		this.selectedSnapshot = selectedSnapshot;
	}
	
	public ClientNetMessage(NetMessageType type, List<Snapshot> snapshots, int slot) {
		
		super(type, slot);
		this.snapshots = snapshots;
	}
	
	public ClientNetMessage(NetMessageType type, List<Snapshot> snapshots) {
		
		super(type);
		this.snapshots = snapshots;
	}

	@Override
	public NetMessageType getType() {
		return this.type;
	}

	public List<Snapshot> getSnapshots() {
		return snapshots;
	}

	public int getSlot() {
		return slot;
	}

	public void setSnapshots(List<Snapshot> snapshots) {
		this.snapshots = snapshots;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public Snapshot getSelectedSnapshot() {
		return selectedSnapshot;
	}

	public void setSelectedSnapshot(Snapshot selectedSnapshot) {
		this.selectedSnapshot = selectedSnapshot;
	}

}
