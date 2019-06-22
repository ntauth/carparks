package it.unimib.disco.net;

import java.util.List;

import it.unimib.disco.domain.Parcheggio.Snapshot;

public class ClientNetMessage extends NetMessage {

	private List<Snapshot> snapshots = null;
	private int slot = 0;
	private Snapshot selectedSnapshot = null;
	
	public ClientNetMessage()
	{
		//Per jackson
	}
	
	public ClientNetMessage(NetMessageType type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

	public ClientNetMessage(NetMessageType type, List<Snapshot> snapshots, int slot)
	{
		super(type);
		this.snapshots = snapshots;
		this.slot = slot;
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
