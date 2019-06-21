package it.unimib.disco.utils.tests;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unimib.disco.net.NetMessage;

public class Main2 {

	public static void main(String[] args) throws Exception
	{
		Socket client = new Socket("localhost", 4242);
		
		PrintWriter pw = new PrintWriter(client.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		String line;
		NetMessage m = null;
		ObjectMapper om = new ObjectMapper();
		String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(m);
		pw.println(m);
	}

}
