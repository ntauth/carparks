package it.unimib.disco.net.serialization;

import java.io.IOException;

/**
 * @brief Defines a strategy for serializing/deserializing objects
 *
 */
public interface ISerializationPolicy {

	public byte[] serialize(Object obj) throws IOException;
	
	public Object deserialize(byte[] data, Class<?> archetype) throws IOException, ClassNotFoundException;
}
