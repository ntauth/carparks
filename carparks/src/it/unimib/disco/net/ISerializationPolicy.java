package it.unimib.disco.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @brief Defines a strategy for serializing/deserializing objects
 *
 */
public interface ISerializationPolicy {

	default public byte[] serialize(Object obj) throws IOException {
		
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    
	    return out.toByteArray();
	}
	
	default public Object deserialize(byte[] data, Class<?> archetype) throws IOException, ClassNotFoundException {
		
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);	
		Object obj =  is.readObject();
		
		assert obj.getClass() == archetype;
		
		return obj;
	}
}
