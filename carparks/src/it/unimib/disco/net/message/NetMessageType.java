package it.unimib.disco.net.message;

public enum NetMessageType {
	
	NONE,
	
	// @see Parcheggio Messages
	SNAPSHOT_UPDATE,
	
	// @see Automobilista Messages
	GET_AVAILABLE_SNAPSHOTS, 
	RESERVE_TIME_SLOT
}