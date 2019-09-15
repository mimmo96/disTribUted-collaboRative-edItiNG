import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 * @author DOMENICO PROFUMO 533695 CORSO B
 * 
 */

public class ChatMulticastReceiver implements Runnable{
	ArrayBlockingQueue<String> memory=new ArrayBlockingQueue<String>(20);;
	public final int LENGTH=512;
	public int port=0;
	public String address;

	/**
	 * metodo costruttore: setta la porta e l'indirizzo da utilizzare per ricevere i messaggi del gruppo
	 * @param address indirizzo utilizzato per la chat
	 * @param port2 numero porta utilizzata
	 */
	public ChatMulticastReceiver(String address, int port2) {
		this.port=port2;
		this.address=address;
	}

	/**
	 * funzione thead che rimane in ascolto di ricevere eventuali messaggi nella chat finchè non viene interrotto esplicitamente
	 */
	public void run() {
		//cro un nuovo socket con il numero di porta dato
		try(MulticastSocket socket = new MulticastSocket(port);){	
			
			//creo il DatagramPacket
			DatagramPacket packet = new DatagramPacket(
					new byte[LENGTH], LENGTH);
			InetAddress multicastGroup= InetAddress.getByName(address);
			socket.setSoTimeout(100000000);
			
			//faccio la joint nel gruppo tramite l'indirizzo address
			socket.joinGroup(multicastGroup);
			
			//fin quando non ricevo un interruzione
			while(!Thread.interrupted()){
				//mi metto in ascolto di ricevere messaggi
				socket.receive(packet);
				
				//memorizzo l'ora e i minuti attuali
				Calendar cal = Calendar.getInstance();
				int ora = cal.get(Calendar.HOUR_OF_DAY);
				int minuti = cal.get(Calendar.MINUTE);
				
				//aggiungo alla coda il nuovo messaggio ricevuto nel gruppo con la l'ora, i minuti e l'indirizzo di chi lo ha mandato
				this.memory.add(ora+":"+minuti+" "+ new String(
						packet.getData(),
						packet.getOffset(),
						packet.getLength(),
						"UTF-8")+"("+packet.getAddress()+")");
			}
		} catch (IOException e) {
			System.out.println("Errore I/O");
		}
		
	}
	
	/**
	 * 
	 * @return restituisce la lista dei messaggi ricevuti nella chat
	 */
	public ArrayBlockingQueue<String> getArray() {
		return this.memory;
	}

}
