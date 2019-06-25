package it.unimib.disco.net.message;

import java.util.List;

import it.unimib.disco.domain.Parcheggio.Snapshot;
/**
 * This class allows communication between clients the server.
 * 
 * @author Farjad
 *
 */
public class ClientNetMessage extends NetMessage {
	
	//
	private List<Snapshot> snapshots = null;
	private Snapshot selectedSnapshot = null;
	
	public ClientNetMessage()
	{
		//Per jackson
	}
	
	public ClientNetMessage(NetMessageType type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

	public ClientNetMessage(NetMessageType type, Snapshot toBook, int slot)
	{
		super(type, slot);
		selectedSnapshot = toBook;
	}
	
	public ClientNetMessage(NetMessageType type, List<Snapshot> snapshots, int slot)
	{
		super(type, slot);
		this.snapshots = snapshots;
	}
	
	public ClientNetMessage(NetMessageType type, List<Snapshot> snapshots)
	{
		super(type);
		this.snapshots = snapshots;
	}

	@Override
	public NetMessageType getType() {
		return this.type;
	}

	/**
	 * @return the snapshots
	 */
	public List<Snapshot> getSnapshots() {
		return snapshots;
	}

	/**
	 * @return the slot
	 */
	public int getSlot() {
		return slot;
	}

	/**
	 * @param snapshots the snapshots to set
	 */
	public void setSnapshots(List<Snapshot> snapshots) {
		this.snapshots = snapshots;
	}

	/**
	 * @param slot the slot to set
	 */
	public void setSlot(int slot) {
		this.slot = slot;
	}

	/**
	 * @return the selectedSnapshot
	 */
	public Snapshot getSelectedSnapshot() {
		return selectedSnapshot;
	}

	/**
	 * @param selectedSnapshot the selectedSnapshot to set
	 */
	public void setSelectedSnapshot(Snapshot selectedSnapshot) {
		this.selectedSnapshot = selectedSnapshot;
	}

}
