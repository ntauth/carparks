package it.unimib.disco.net.message;

public enum NetMessageType
{	
	NONE,
	
	// @see Parcheggio Message Types
	SNAPSHOT_UPDATE,
	
	// @see Automobilista Message Types
	GET_AVAILABLE_SNAPSHOTS, 
	RESERVE_TIME_SLOT
}
