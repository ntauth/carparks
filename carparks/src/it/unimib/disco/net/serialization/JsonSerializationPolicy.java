package it.unimib.disco.net.serialization;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @brief JSON Serialization Policy (based on Jackson)
 *
 */
public class JsonSerializationPolicy implements ISerializationPolicy {

	protected final ObjectMapper objectMapper;
	
	public JsonSerializationPolicy(ObjectMapper objectMapper) {
		
		this.objectMapper = objectMapper;
	}
	
	public JsonSerializationPolicy() {
		
		this(new ObjectMapper());
	}
	
	@Override
	public byte[] serialize(Object obj) throws IOException {

		return objectMapper.writeValueAsString(obj).getBytes();
	}

	@Override
	public Object deserialize(byte[] data, Class<?> archetype) throws IOException, ClassNotFoundException {
		
		return objectMapper.readValue(new String(data), archetype);
	}

}
