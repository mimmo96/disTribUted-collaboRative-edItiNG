import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 * @author DOMENICO PROFUMO 533695 CORSO B
 * 
 */

public class Server {
	private static int DEFAULT_PORT = 1919;
	private static int DEFAULT_DIM = 1024;
	private static String user;
	private static String pass;
	private static User userlist;
	private static ArrayList <String> address = new ArrayList <String>();
	private static ArrayList <Integer> connecteduser = new ArrayList <Integer>();
	
	public static void main(String args[]) throws Exception {
		
		userlist = new UserImpl();
	
		try {	
			//creo il registro nella porta 9999 e inserisco la lista utenti
			LocateRegistry.createRegistry(9999);
			Registry r=LocateRegistry.getRegistry(9999);
			r.rebind(User.SERVICE_NAME, userlist);
			System.out.println("Lista utenti nel registro!");
		} 
		
		//se qualche evento mi ha lanciato un eccezione stampo a schermo errore
		catch (Exception e) {
			System.out.println("Arresto anomalo server");
	 		e.printStackTrace();	
	 		return;
		}
		
			//configuro i parametri da utilizzare per la connessione TCP su numero di porta DEFAULT
	 		int port = DEFAULT_PORT; 
	 		System.out.println("Listening for connections on port " + port);
	 		ServerSocketChannel serverChannel;
	 		Selector selector;
	 		
	 		//avvio tcp
	 		try {
	 			//creo il socket e registro il selettore per l'ascolto di eventuali client connessi
	 			serverChannel = ServerSocketChannel.open();
	 			ServerSocket ss = serverChannel.socket();
	 			InetSocketAddress address = new InetSocketAddress(port);
	 			ss.bind(address);
	 			serverChannel.configureBlocking(false);
	 			selector = Selector.open();
	 			
	 			//registro nel selettore il socket del server
	 			serverChannel.register(selector,SelectionKey.OP_ACCEPT);
	 		} 
	 		//se qualcosa è andato storto, stampo errore e termino
	 		catch (Exception ex) {
	 			ex.printStackTrace();
	 			return;
	 		}
	 
	 		//se la configurazione è andata a buon fine mi metto in ascolto di eventuali richieste 
	 		while (true) {
	 			try {
	 				selector.select();
	 			} catch (IOException ex) {
	 				ex.printStackTrace();
	 				break;
	 			}
	
	 			//associo l'insieme delle chiavi pronte e un iteratore per scorrerle 
	 			Set <SelectionKey> readyKeys = selector.selectedKeys();
	 			Iterator <SelectionKey> iterator = readyKeys.iterator();
	 			
	 			//finchè ho delle chiavi pronte in ascolto, le rimuovo ed eseguo la richiesta
	 			while (iterator.hasNext()) {
	 				//salvo in key la prossima chiave pronta
	 				SelectionKey key = iterator.next();
	 				// rimuove la chiave dal Selected Set, ma non dal registered Set
	 				iterator.remove();			
	 				try {
	 					//se la chiave è in ascolto di nuove connessioni, la registro al selettore
	 					if (key.isAcceptable()) {
	 						//prendo la chiave dal chanel
	 						ServerSocketChannel server = (ServerSocketChannel) key.channel();
	 						//creo un nuovo socket per la comunicazione con il relativo client
	 						SocketChannel client = server.accept();
	 						System.out.println("Accepted connection from " + client);
	 						client.configureBlocking(false);
	 					
	 						//registra il client che si è messo in ascolto
	 						key = client.register(selector, SelectionKey.OP_READ);
	 					}
	 					
	 					//se la chiave è in stato di read, mi metto in ascolto 
	 					else if(key.isValid() && key.isReadable()) {
	 						//recupero il socket associato al client
	 						 SocketChannel client = (SocketChannel) key.channel();
	 						 //reperisco l'oggetto attachments 
	 					   	 Attachments save=new Attachments();
	 						 
	 						 //inizializzo il buffer e memorizzo la richiesta del client
	 						 ByteBuffer myBuffer = ByteBuffer.allocate(DEFAULT_DIM);	
	 						  client.read(myBuffer);	
	 						  String data = new String(myBuffer.array()).trim();
	 						  myBuffer.clear();
	 						  
	 						  //se ho letto qualcosa effettuo il parsing per gestire le varie operazioni
	 						  if (data.length() > 0) {
	 							 //faccio il parsing della stringa ricevuta, salvo in operation l'operazione richiesta
	 							 user=data.substring(1, data.indexOf(")"));
	 							 String operation=data.substring(data.indexOf(")")+1, data.indexOf(":"));
	 							 
	 							 switch(operation) {
	 							 	case "login": {
	 							 	//effettuo il parsing della stringa ricevuta per salvare il nome utente e la password
	 							 	//del client che ne ha fatto richiesta
	 								 user=data.substring(data.indexOf(":")+1, data.indexOf("-"));	 
		 							 pass=data.substring(data.indexOf("-")+1);
		 							 System.out.printf("Message Received.....: %s %s-%s\n",operation, user,pass);
		 							 
		 							 save.user=user;
		 							 save.password=pass;
		 							 save.op=operation;
		 							 	
		 							 String msg;
		 							 
		 							 //controllo se l'utente era già registrato
			 						 if(userlist.isregistred(user, pass)==true) {
			 							if(userlist.login(user, pass))  msg="Login Eseguito Correttamente";
			 							else msg="Login Errato";
			 						 }
			 						 else msg="Login Errato";
			 						 
			 						 //inizializzo il buffer e invio l'esito della richiesta
			 						 ByteBuffer Buffer= (ByteBuffer) ByteBuffer.allocate(DEFAULT_DIM);
			 					     Buffer.put(msg.getBytes());
			 					     Buffer.flip(); 
			 					     save.buff=Buffer;
			 					     save.mess=msg;
			 					     //registro in scrittura la chiave associata al client 
		 							 key=client.register(selector, SelectionKey.OP_WRITE,save);
		 							
			 					     break;
	 							 	}
	 							 	
	 							 	case"createdocument":{
	 							 		//effettuo il parsing della stringa ricevuta per salvare il documento e la sezione
		 							 	//del client che ne ha fatto richiesta
	 							 		String cartella=data.substring(data.indexOf(":")+1, data.indexOf("-"));
			 							int  sezioni=Integer.parseInt(data.substring(data.indexOf("-")+1));
			 							System.out.printf("Message Received.....: %s %s-%s\n",operation, cartella ,sezioni);
			 							 
			 							//creo il nuovo documento
			 							String msg="";
			 							File theDir = new File("Documenti/".concat(cartella));
			 							
			 							//se già il documento esisteva salvo il msg il messaggio di errore
			 							if (theDir.exists()) {
			 								 msg="Documento "+cartella + " già esistente";
			 							}
			 							else {
			 							 //creo il nuovo documento
			 								Documento d =new Documento(user,cartella,sezioni); 
			 							 	msg="Documento " + cartella + " creato con successo!";
			 							 	//aggiungo il documento alla lista dei documenti dell'utente user
			 							 	userlist.getuser(user).addDoc(d);
			 							 } 
				 						
			 							//inizializzo il buffer e comunico al client l'esito della richiesta di creazione nuovo documento
				 						 ByteBuffer Buffer= (ByteBuffer) ByteBuffer.allocate(DEFAULT_DIM);
				 					     Buffer.put(msg.getBytes());
				 					     Buffer.flip();
				 					     save.user=user;
			 							 save.password=pass;
			 							 save.op=operation;
				 					     save.buff=Buffer;
				 					     save.mess=msg;
				 					    
				 					    //registro in scrittura la chiave associata al client
				 					    key=client.register(selector, SelectionKey.OP_WRITE,save);
				 					    break;
	 							 	}
	 							 	
	 							 	case"edit":{
	 							 		//effettuo il parsing della stringa ricevuta per salvare il documento e la sezione da editare
		 							 	//del client che ne ha fatto richiesta
	 							 		String cartella=data.substring(data.indexOf(":")+1, data.indexOf("-"));
			 							int  sezione=Integer.parseInt(data.substring(data.indexOf("-")+1));
			 							System.out.printf("Message Received.....: %s %s-%s\n",operation, cartella ,sezione);
			 							
			 							try {
			 							//recupero il documento dell'utente user
			 							Documento x=userlist.getuser(user).getDoc(cartella);
			 							//controllo se l'utente possedeva nella sua lista il documento x
			 							if(x!=null) {
	 							 	
	 							 			//se l'utente ha diritto di editare quel documento prendo l'accesso
	 							 			if(x.edit(user, x, sezione)) {
	 							 				//setto l'indirizzo multicast associato al documento ed incrremento il numero di utenti connessi
	 							 				configureaddress(x);
	 							 				//setto il numero di porta
	 							 				x.openConnection();
	 							 				ByteBuffer buffer3 = ByteBuffer.allocate(DEFAULT_DIM);
	 							 				//mando indirizzo e la porta in ascolto per il multicast(chat)
												String esito="" + x.getAddress() +":"+ x.getSocket().getLocalPort();
												buffer3.put(esito.getBytes());
												
	 											buffer3.flip();
	 											save.user=user;
	 				 							save.password=pass;
	 				 							save.op=operation;
	 											save.buff=buffer3;
	 											save.Patch=cartella;
	 											save.section=sezione;
	 							 			}
	 							 			else {
	 							 				//non ho i diritti di accedere,comunico l'esito
	 							 				save.user=user;
	 				 							save.password=pass;
	 				 							save.op="sendEsito";
	 				 							save.esito="Accesso Negato";
	 							 				
	 							 			}
	 							 		}
	 							 	
	 							 		else {
	 							 			save.user=user;
 				 							save.password=pass;
 				 							save.op="sendEsito";
 				 							save.esito="Documento non trovato";	
	 							 		}	
	 							 		}
	 							 		catch(Exception e) {
	 							 			//sezione sbagliata!
	 							 			save.user=user;
 				 							save.password=pass;
 				 							save.op="sendEsito";
	 							 			save.esito="Sezione non presente!";				 			
	 							 		}
			 							//registro in scrittura la chiave associata al client
				 					    key=client.register(selector, SelectionKey.OP_WRITE,save);
	 							 	break;
	 							 	}
	 							 	case"endedit":{
	 							 		//effettuo il parsing della stringa ricevuta per salvare il documento e la sezione per end-edit
		 							 	//del client che ne ha fatto richiesta
	 							 		String cartella=data.substring(data.indexOf(":")+1, data.indexOf("-"));
			 							int  sezione=Integer.parseInt(data.substring(data.indexOf("-")+1));
			 							System.out.printf("Message Received.....: %s %s-%s\n",operation, cartella ,sezione);
			 							 
	 							 		//recupero il documento dell'utente user
	 							 		Documento x=userlist.getuser(user).getDoc(cartella);
	 							 		//controllo se l'utente possedeva nella sua lista il documento x
	 							 		if(x!=null) {
	 							 			//se l'utente ha diritto di editare quel documento prendo l'accesso
	 							 			if(x.endEdit(user, x, sezione)) {
	 							 				String patch="Documenti/".concat(cartella).concat("/").concat("sezione" + sezione + ".txt");
	 							 				//aggiorno il contenuto della sezione originaria
	 							 				receivefile(key,patch);
	 							 				
	 							 			//salvo l'indirizzo della chat
	 							 				String ind=x.getadd();
	 							 				int pos=address.indexOf(ind);
	 							 				//se ero l'unico connesso chiudo il socket ed elimino l'indirizzo alla chat
	 							 				if(connecteduser.get(pos)<=1) {
	 								 				connecteduser.remove(pos);
	 												address.remove(ind);
	 												x.closeConnection();
	 							 				}
	 							 				//se ci sono altre persone connesse decremento il numero
	 							 				else connecteduser.add(pos,connecteduser.get(pos)-1);
	 							 			}
	 							 		}
	 							 	 save.inuso=false;
	 							   	 save.user=user;
			 						 save.password=pass;
			 						 save.op="end-edit";
	 							 	 //registro in lettura la chiave associata al client
	 							 	 key=client.register(selector, SelectionKey.OP_READ,save);
	 
	 							 	 break;
	 							 	}
	 							 	case "show":{
	 							 		//effettuo il parsing della stringa ricevuta per salvare il documento e la sezione da mostrare
	 							 		String cartella=data.substring(data.indexOf(":")+1, data.indexOf("-"));
			 							int sezione=Integer.parseInt(data.substring(data.indexOf("-")+1));
			 							System.out.printf("Message Received.....: %s %s-%s\n",operation, cartella ,sezione);
			 							 
	 							 	   //controllo se l'utente possedeva nella sua lista il documento x e se la sezione rientra nel range 
	 							 	   if((userlist.getuser(user).getDoc(cartella)!=null) && ( sezione<1 || sezione>userlist.getuser(user).getDoc(cartella).getnumsezioni())) {
	 							 			save.user=user;
 				 							save.password=pass;
 				 							save.op="sendEsito";
 				 							save.esito="Sezione non presente!";
	 							 		}
			 						   
	 							 	   //recupero il documento dell'utente user
	 							 	   else if(userlist.getuser(user).getDoc(cartella)!=null) {
	 							 			save.user=user;
 				 							save.password=pass;
 				 							save.op="show";
	 							 			save.Patch=cartella;
	 							 			save.section=sezione;
	 							 		}   
	 			
	 							 	   else { 
	 							 			//invio l'esito
	 							 			save.user=user;
 				 							save.password=pass;
 				 							save.op="sendEsito";
 				 							save.esito="Documento non presente!";
	 							 	   }
	 							     //registro in lettura la chiave associata al client
	 							 	 key=client.register(selector, SelectionKey.OP_WRITE,save);
	 							 	 break;
	 							 	}
	 							 	case "showD":{
	 							 	//effettuo il parsing della stringa ricevuta per salvare il documento da mostrare
	 							 		System.out.println("utente: "+ user);
	 							 		String cartella=data.substring(data.indexOf(":")+1);
			 							System.out.printf("Message Received.....: op:%s documento:%s\n",operation, cartella);
			 							 
	 							 		//recupero il documento dell'utente user
	 							 		Documento x=userlist.getuser(user).getDoc(cartella);
	 							 		//controllo se l'utente possedeva nella sua lista il documento x
	 							 		if(x!=null) {
	 							 			save.user=user;
 				 							save.password=pass;
 				 							save.op="showD";
	 							 			save.Patch=cartella;
	 							 			save.section=x.getnumsezioni();
	 							 		}
	 							 		//non ho trovato il documento
	 							 		else { 
	 							 			System.out.println("Documento non presente mando il messaggio");
	 							 			//invio l'esito della richiesta
	 							 			save.user=user;
 				 							save.password=pass;
 				 							save.op="sendEsito";
 				 							save.esito="Documento non presente!";
 				 							
	 							 		}
	 							 	 key=client.register(selector, SelectionKey.OP_WRITE,save);
	 							 	  break;
	 							 	}
	 							 	case "share":{
	 							 	//effettuo il parsing della stringa ricevuta e dell'utente per codividere il documento
		 								String doc=data.substring(data.indexOf(":")+1, data.indexOf("-"));
			 							String utente=data.substring(data.indexOf("-")+1);
			 							System.out.printf("Message Received.....: %s %s-%s\n",operation, doc,utente);
			 							
			 							String msg=null;
			 							
			 							//recupero il documento dalla lista dell'utente che ha fatto richiesta
			 							Documento x=userlist.getuser(user).getDoc(doc);
	 							 		
			 							//controllo se l'utente possedeva nella sua lista il documento x
	 							 		if(x!=null) {
	 							 			//controllo se l'utente a cui devo inviare il documento esiste
	 							 			Utente u=userlist.getuser(utente);
	 							 			if(u!=null && x.addcollaboratori(user, utente, x)) {
	 							 				u.addDoc(x);
	 							 				msg="Documento "+doc+" condiviso a "+ utente + " con successo!";
	 							 			}
	 							 			else msg="Utente "+ utente + " non registrato!";
			 							 }
	 							 		else msg="Documento "+ doc + " non esistente!";
				 						 
				 						 //mando la risposta
				 						 ByteBuffer Buffer= (ByteBuffer) ByteBuffer.allocate(DEFAULT_DIM);
				 					     Buffer.put(msg.getBytes());
				 					     Buffer.flip();
				 					     save.user=user;
			 							 save.password=pass;
			 							 save.op=operation;
				 					     save.buff=Buffer;
				 					     save.mess=msg;
				 					     //registro in scrittura la chiave associata al client 
			 							 key=client.register(selector, SelectionKey.OP_WRITE,save);
			 							
				 					     break;
	 							 	}
	 							 	case "list":{
	 							 		//eseguo funzione per mostrare i documenti relativi all'utente che ha fatto richiesta
	 							 		System.out.printf("Eseguo list\n"); 
	 							 		user=data.substring(data.indexOf(":")+1);
	 							 		System.out.printf("Message Received.....: %s %s\n",operation, user);
	 							 		String msg=null;
			 							
	 							 		//recupero il documento dalla lista dell'utente che ha fatto richiesta
	 							 		Utente u=userlist.getuser(user);
	 							 		String namedoc=" ";
	 							 		
	 							 		//controllo se l'utente a cui devo inviare il documento esiste
	 							 		if(u!=null) {
	 							 			System.out.println("Utente esistente, recupero lista documenti");
	 							 			//recupero la lista dei documenti associata all'utente
	 							 			ArrayList<Documento> k=userlist.getuser(user).allDoc();
	 							 			
	 							 			System.out.println("Lista documenti di dimensione: " + k.size());
				 							
	 							 			//per tutti i documenti associati
		 							 		for(int i=0;i<k.size();i++) {
		 							 			//recupero i collaboratori e gli autori del documento relativo
		 							 			namedoc=namedoc + "\nDocumento:"+k.get(i).getName()+"\n Autore: " + k.get(i).getAutor();
		 							 			ArrayList<String> f=k.get(i).getCollaboratori();
		 							 			namedoc=namedoc+ "\n Collaboratori:";
		 							 			for(int l=0;l<f.size();l++) {
		 							 				namedoc=namedoc + f.get(l) + " ";
		 							 				System.out.print(namedoc);
		 							 			}
		 							 			//namedoc contiene la stringa con tutte le info salvate del documento
		 							 			namedoc=namedoc + "\n";
		 							 			System.out.println("");
		 							 			System.out.println(namedoc);
		 							 			System.out.println("");
		 							 		}
	 							 			msg=namedoc;
	 							 		}
	 							 		else msg="Utente non registrato!";
				 						 
	 							 		//mando la stringa contenente la lista di tutti i documenti con le relative info associate
				 						ByteBuffer Buffer= (ByteBuffer) ByteBuffer.allocate(DEFAULT_DIM);
				 					    Buffer.put(msg.getBytes());
				 					    Buffer.flip();
				 					    save.user=user;
			 							save.password=pass;
			 							save.op=operation;
				 					    save.buff=Buffer;
				 					    save.mess=msg;
				 					     //registro in scrittura la chiave associata al client 
			 							 key=client.register(selector, SelectionKey.OP_WRITE,save);
			 							
				 					    break;
	 							 		 
	 							 	}
	 							 	case "logout": {
	 							 		//effettuo parsing del nome utente che vuole fare il logout
		 								user=data.substring(data.indexOf(":")+1);
			 							System.out.printf("Message Received.....: %s %s\n",operation, user);
			 							
			 							String msg;
				 						
			 							//se l'utente è registrato, lo effettuo
			 							if(userlist.isregistred(user, pass)==true) {
				 							if(userlist.logout(user, pass))  msg="Logout Eseguito Correttamente";
				 							else msg="Logout Errato";
				 						 }
				 						else msg="Logout Errato";
				 						 
			 							///invio esito al client
				 						ByteBuffer Buffer= (ByteBuffer) ByteBuffer.allocate(DEFAULT_DIM);
				 					    Buffer.put(msg.getBytes());
				 					    Buffer.flip();
				 					    save.user=user;
			 							save.password=pass;
			 							save.op=operation;
				 					    save.buff=Buffer;
				 					    save.mess=msg;
				 					    //registro in scrittura la chiave associata al client 
			 							key=client.register(selector, SelectionKey.OP_WRITE,save);
			 							
				 					    break;
		 							 }
	 							 	default:{
	 							 		System.out.println("Ho ricevuto: " + operation);
	 							 		
	 							 	}
	 							 	
	 							 	
	 							 }
	 	  
	 						  }
	 					}

	 					else if (key.isValid() && key.isWritable()){  						
	 						SocketChannel client = (SocketChannel) key.channel();
	 						Attachments send=(Attachments) key.attachment();
	 						
	 						switch(send.op) {
	 						case "login":{
	 							while (send.buff.hasRemaining())
		 							client.write(send.buff);
		 					     
		 					    System.out.printf("Sending Message...: %s\n",send.mess);
		 					     
		 					    //registro il lettura la chiave associata al client
		 					    key=client.register(selector, SelectionKey.OP_READ,send);
		 					   break;
	 						}
	 						case "edit":{
	 							while (send.buff.hasRemaining())
										client.write(send.buff);
									
					 			//invio il file(sezione) all'utente
					 			String patch="Documenti/".concat(send.Patch).concat("/").concat("sezione" + send.section + ".txt");
					 			sendFile(key,patch);
					 			send.inuso=true;
					 			key=client.register(selector, SelectionKey.OP_READ,send);
					 			  break;
	 						}
	 						case "show":{
	 							sendEsito(client,"Invio documento");
						 		System.out.printf("Sending Message...: Invio documento\n");
						 		//invio il file all'utente
						 		String patch="Documenti/".concat(send.Patch).concat("/").concat("sezione" + send.section + ".txt");
						 		//comunico se la sezione relativa è in uso
						 		ArrayList<Boolean> inuso=userlist.getuser(send.user).getDoc(send.Patch).getinuso();
						 		if(inuso.get(send.section).equals(false)) sendEsito(client,"Sezione non in uso");
						 		else  sendEsito(client,"Sezione in uso");
						 		sendFile(key,patch);	
						 		key=client.register(selector, SelectionKey.OP_READ,send);
						 		  break;
	 							
	 						}
	 						case "showD":{
	 							sendEsito(client,"Invio documento");
						 		System.out.printf("Sending Message...: Invio documento\n");
						 		//invio il file all'utente
						 		String patch="Documenti/".concat(send.Patch);
						 		sendDocument(key,patch,send.section,user);	
						 		key=client.register(selector, SelectionKey.OP_READ,send);
						 		  break;
	 						}
	 					
	 						case"createdocument":{
	 							while (send.buff.hasRemaining())
		 							client.write(send.buff);
		 					     
		 					    System.out.printf("Sending Message...: %s\n",send.mess);
		 					     
		 					    //registro il lettura la chiave associata al client
		 					    key=client.register(selector, SelectionKey.OP_READ,send);
		 					   break;
	 							
	 						}
	 						case "list": {
	 							while (send.buff.hasRemaining())
		 							client.write(send.buff);
		 					     
		 					    System.out.printf("Sending Message...: %s\n",send.mess);
		 					     
		 					    //registro il lettura la chiave associata al client
		 					    key=client.register(selector, SelectionKey.OP_READ,send);
		 					   break;
	 						}
	 						case "share": {
	 							while (send.buff.hasRemaining())
		 							client.write(send.buff);
		 					     
		 					    System.out.printf("Sending Message...: %s\n",send.mess);
		 					     
		 					    //registro il lettura la chiave associata al client
		 					    key=client.register(selector, SelectionKey.OP_READ,send);
		 					   break;
	 							
	 						}
	 						case "logout":{
	 							while (send.buff.hasRemaining())
		 							client.write(send.buff);
		 					     
		 					    System.out.printf("Sending Message...: %s\n",send.mess);
		 					     
		 					    //registro il lettura la chiave associata al client
		 					    key=client.register(selector, SelectionKey.OP_READ,send);
		 					   break;
	 							
	 						}
	 						case "sendEsito":{
	 							sendEsito(client,send.esito);
				 				System.out.printf("Sending Message...: " + send.esito + "\n");
				 				key=client.register(selector, SelectionKey.OP_READ,send);
				 				break;
	 						}
	 						default:
	 						}
 
	 					}
	 				} 
	 				
	 				catch (Exception ex) { 
	 					//recupero il nome associato alla key corrispondente
 						Attachments info=(Attachments) key.attachment();
	 					
	 					//se stavo editando un documento rilascio l'accesso (end-edit)
	 					if(info.inuso) {
	 						info.inuso=false;
	 						Documento docu=userlist.getuser(user).getDoc(info.Patch);
	 						docu.endEdit(info.user, docu, info.section);
	 				
	 						//salvo l'indirizzo della chat
				 				String ind=docu.getAddress().getCanonicalHostName();
				 				int pos=address.indexOf(ind);
				 				//se ero l'unico connesso chiudo il socket ed elimino l'indirizzo alla chat
				 				if(connecteduser.get(pos)<=1) {
					 				connecteduser.remove(pos);
									address.remove(ind);
									docu.closeConnection();
				 				}
				 				//se ci sono altre persone connesse decremento il numero
				 				else connecteduser.add(pos,connecteduser.get(pos)-1);
	 					}
	 					
	 					System.out.println("Client "+ info.user +" ha staccato");
	 					//disconnetto automaticamente l'utente e chiudo la chiave associata

	 				try{ 
	 					userlist.logout(info.user, null);
	 					key.cancel();	
	 					key.channel().close(); 
	 				}
	 					
	 				catch (Exception cex) {} 
	 				}
	 			}
	 		}
	 	}

	
	/**
	 * oggetto attachment utilizzato per salvare info relative al client all'interno del channel 
	 */
	static class Attachments{
		public String user;
		public String password;
		public ByteBuffer buff;
		public String op;
		public String mess;
		public boolean inuso=false;
		public int section;
		public String Patch;
		public String esito;
		public Attachments() {

		}
	}
		
		/**
		 * funzione utilizzata per inviare la sezione relativa al documento
		 * 
		 * @param key chiave del canale dov'è in ascolto il client
		 * @param patch indirizzo relativo alla sezione del documento da inviare
		 * @throws IOException lanciata per problemi di I/O
		 */
		public static void sendFile(SelectionKey key,String patch) throws IOException {
			//inizzializzo il buffer e recupero il socket del client associato alla chiave passata come parametro
			ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_DIM);
			SocketChannel client = (SocketChannel) key.channel();
			
			//salvo in fileName il patch del file da inviare
			String fileName = patch;
				
			System.out.println("[FTP server] file richiesto " + fileName + " " + fileName.length());
			buffer.clear();
			FileChannel file = null;
					
			try {
				//apro in lettura il file
				file = FileChannel.open(Paths.get(fileName),StandardOpenOption.READ);
						
				System.out.println("[FTP server] invio file di grandezza " + file.size());
				buffer.clear();
				
				//mando la dimensione del file da inviare
				buffer.putLong(file.size());
				buffer.flip();
				while (buffer.hasRemaining())
					client.write(buffer);
				buffer.clear();
				
				//mando il contenuto
				long pos = file.position();
				long toCopy = file.size();
				System.out.println("Il file da inviare è grande: " + toCopy);
				 while (toCopy > 0) {
				  long bytes = file.transferTo(pos, toCopy, client);
				  pos += bytes;
				  toCopy -= bytes;
				 }
				
				file.close();
				
				System.out.println("ho trasferito: " + pos + " byte");
				System.out.println("[FTP server] fine transferimento");
		
			}		
			
			 catch (Exception e) {
				System.out.println("[FTP server] File not found");
				sendEsito(client,"Documento non presente!");
			}		
		}
		
		/**
		 * funzione per aggiornare il contenuto della sezione dopo aver eseguito editing della stessa
		 * 
		 * @param key chiave del canale dov'è in ascolto il client 
		 * @param Patch indirizzo relativo alla sezione del documento da aggiornare
		 * @throws IOException lanciata per problemi di I/O
		 */
		public static void receivefile(SelectionKey key,String Patch) throws IOException{
			System.out.println("eseguo operazione di fine edit");
			
			try {
				//apro in scrittura il file dove devo scrivere
				FileChannel out = FileChannel.open(Paths.get(Patch),StandardOpenOption.WRITE);
				System.out.println("apro il file dove devo scrivere: " + Patch);
				ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_DIM);
				buffer.clear();
				
				//recupero il socket del client associato alla chiave passata come parametro
				SocketChannel client = (SocketChannel) key.channel();
			
				//legge dim del file
				client.read(buffer);
				buffer.flip();
				
				//stampo la dimensione del file da ricevere
				long fileSize = buffer.getLong();
				System.out.println("[Ftp server] file size is " + fileSize);
				
				//inizializzo la dimensione della sezione del file dove devo scrivere
				int read = 0;
				read += out.write(buffer);
				buffer.compact();
				int eos = 0;
				
				//leggo il contenuto del file dal client e lo inserisco all'interno della sezione(file) che avevo aperto precedentemente
				while ((read < fileSize) && (eos != -1)) {
					eos = client.read(buffer);
					read += eos;
					buffer.flip();
					out.write(buffer);
					buffer.compact();
				}				
				
				//chiudo il file e cancello il contenuto del buffer
				System.out.println("[Ftp server] file aggiornato correttamente.");
				out.close();
				buffer.clear();
			
			}		
			 catch (java.nio.file.NoSuchFileException e) {
				System.out.println("[FTP server] File not found");
			}
		}
		
		/**
		 * funzione utilizzata per inviare il documento richiesto al client che ne ha fatto richiesta(tutte le sezioni)
		 * 
		 * @param key chiave del canale dov'è in ascolto il client 
		 * @param Patch indirizzo del documento
		 * @param numsez numero sezioni del documento 
		 * @param utente che ne ha fatto richiesta
		 * @throws IOException lanciata per problemi di I/O (es la write)
		 */
		public static void sendDocument(SelectionKey key,String Patch,int numsez,String utente) throws IOException {
			//inizializzo il buffer da utilizzare per la comunicazione e recupero il socket associato al client tramite la chiave(key) 
			//ho aumentato la dimensione del buffer 
			ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_DIM*5);
			SocketChannel client = (SocketChannel) key.channel();
			buffer.clear();
	
			//inserisco nel buffer il numero delle sezioni da comunicare
			System.out.println("mando il numero di file da inviare");
			buffer.putInt(numsez);
			buffer.flip();
			//comunico il buffer
			while (buffer.hasRemaining())
				client.write(buffer);
			buffer.clear();		
			String docum=Patch;
			
			int sezione=1;
				
			//recupero l'array contenente le varie sezoni del documento
			Utente pazzo=userlist.getuser(utente);		
			Documento docc=pazzo.getDoc(docum.substring(docum.indexOf("/")+1));
			ArrayList<Boolean> sez=	docc.getinuso();
			buffer.clear();
				
			//per tutte le sezioni del documento, salvo in buffer il loro contenuto
			for(sezione=1;sezione<=numsez;sezione++) {	
				String patch=Patch;
				//associo all'indirizzo del documento passato alla funzione, il patch della relativa sezione				
				patch=docum.concat("/").concat("sezione" + sezione + ".txt");
					 
				System.out.println("Invio la sezione");
					
				//comunico se la sezione relativa è in uso
				if(sez.get(sezione).equals(false)) {
					//inserisco lo stato della sezione nel buffer
		 			String g="\nSezione " +sezione + " non in uso\n";
					buffer.put(g.getBytes());	
		 		}
		 		else {
		 			//inserisco lo stato della sezione nel buffer
		 			String g="\nSezione " +sezione + " in uso\n";
					buffer.put(g.getBytes());	
			 	}
				
				//apro in lettura il file relativo alla sezione 
				System.out.println(patch);
				try {
					//salvo il contenuto della sezione nel buffer
					FileReader f=new FileReader(patch);
					BufferedReader b=new BufferedReader(f);
				
					String s;
				
					//leggo il conenuto della sezione i e lo salvo nel buffer
					while(true) {
						s=b.readLine();
						if(s==null)
							break;
						s=s.concat("\n");
						buffer.put(s.getBytes());
					}
					b.close();
				}
				catch (Exception e) {
					e.printStackTrace();
					System.out.println("[FTP server] File not found");
				}	
			}
			buffer.flip();
				
			//comunico le sezioni al client
			while(buffer.hasRemaining()) {
				client.write(buffer);
			}
		}

		/**
		 * funzione per comunicare l'esito della richiesta al client
		 * @param client socket associato al client
		 * @param esi stringa contenente l'esito da comunicare
		 * @throws IOException lanciata per problemi di I/O (es la write)
		 */
		public static void sendEsito(SocketChannel client,String esi) throws IOException {
			//inizializzo il buffer da utilizzare per comunicare e metto la striga sottoforma di byte
			ByteBuffer buffer3 = ByteBuffer.allocate(DEFAULT_DIM);
			buffer3.clear();
			buffer3.put(esi.getBytes());	
			buffer3.flip();
			
			//comunico il buffer tramite il socket associato al client	
			while (buffer3.hasRemaining())
					client.write(buffer3);
			
			//cancello il contenuto del buffer
			buffer3.clear();
		}
		
		/**
		 * 
		 * @param x: Documento in cui devo settare l'indirizzo
		 * @throws UnknownHostException - se non è stato trovato alcun indirizzo IP per l'host 
		 * @throws SecurityException - se esiste un gestore di sicurezza e il suo metodo checkConnect non consente l'operazione
		 */
		public static void configureaddress(Documento x) throws UnknownHostException, SecurityException {

			//stringa utilizzata per la memorizzare gli indirizzi generati
			String indirizzo=genero();	
			
			//aggiungo alla coda l'insieme degli indirizzi che non posso usare
			address.add("224.0.0.1");
			connecteduser.add(0);
			address.add("224.0.0.2");
			connecteduser.add(0);
			address.add("224.0.1.1");
			connecteduser.add(0);
			
			//controllo che l'indirizzo generato precedentemente non appartiene a nessuna chat altrimenti ne genero un altro
			while(address.contains(indirizzo)) {
				indirizzo=genero();
			}
			
			//controllo se avevo già l'indirizzo, se non era associato lo associo e lo inserisco nella lista degli indirizzi
			if(x.setAddress(indirizzo)) {
				address.add(indirizzo);
				connecteduser.add(1); 
				System.out.println("Ottengo l'indirizzo " + indirizzo);
				System.out.println("Ci sono  "+ connecteduser.get(address.indexOf(indirizzo)) + " utenti connessi");
			}
			//se l'indirizzo era già stato associato, incremento il numero di utenti attivi
			else { 
				String addr=x.getadd();
				System.out.println("recupero l'indirizzo "+ addr);
				connecteduser.add(address.indexOf(addr), connecteduser.get(address.indexOf(addr))+1);
				System.out.println("Ci sono  "+ connecteduser.get(address.indexOf(addr)) + " utenti connessi");
			}
		}
		
		/**
		 * genera l'indirizzo in modo random compreso nel range 224.0.0.0-238.255.255.255
		 * @return stringa contenente indirizzo generato
		 */
		public static String genero() {
			Random rand = new Random();	
			String x="";
				
			//range di indirizzi generati: 224.0.0.0-238.255.255.255
			int x1=rand.nextInt(14);
			x1=x1+224;
			x=x.concat(x1+".");
			int x2=rand.nextInt(255);
			x=x.concat(x2+".");
			int x3=rand.nextInt(255);
			x=x.concat(x3+".");
			int x4=rand.nextInt(255);
			x=x.concat(x4+"");
			
			return x;
		}
			
}

