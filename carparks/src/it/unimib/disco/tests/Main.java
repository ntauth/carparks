package it.unimib.disco.tests;
import it.unimib.disco.net.JsonSerializationPolicy;

public class Main {

	public static void main(String[] args) throws Exception
	{
		JsonSerializationPolicy policy = new JsonSerializationPolicy();
		
		String s = new String(policy.serialize("Ciao come va"));
		
		System.out.println(s);
	}

}
