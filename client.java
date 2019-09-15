import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 * @author DOMENICO PROFUMO 533695 CORSO B
 * 
 */

public class client {
	private static int DEFAULT_PORT = 1919;
	private static int DEFAULT_DIM = 1024;	
	private static ArrayBlockingQueue<String> memory;

	public static void main(String[] args) throws Exception {
		
		//reperisco l'oggetto remoto contenete la lista di utenti nella porta 9999
		Registry reg= LocateRegistry.getRegistry(9999);
		User listautenti = (User) reg.lookup(User.SERVICE_NAME);
		
		//leggo l'input da schermo
		Scanner x = new Scanner(System.in);
		String[] op;
		
		System.out.println("Cosa vuoi fare?");
		System.out.println("turing register <username > <password > registra l' utente\r\n" + 
				"turing login <username > <password > effettua il login\r\n");
		op=x.nextLine().split(" ");
		
		//controllo che il primo comando sia "turing" e che il secondo sia diverso da null
		while(op[0].equals("turing") && op.length>1) {
			
			//gestione messaggio di help
			if(op[1].equals("--help"))
				System.out.println("\nusage : turing COMMAND [ ARGS ...]\r\n\n" + 
						"commands :\r\n" + 
						"register <username > <password > registra l' utente\r\n" + 
						"login <username > <password > effettua il login\r\n" + 
						"logout effettua il logout\r\n\n" + 
						"create <doc > <numsezioni > crea un documento\r\n" + 
						"share <doc > <username > condivide il documento\r\n" + 
						"show <doc > <sec > mostra una sezione del documento\r\n" + 
						"show <doc > mostra l' intero documento\r\n" + 
						"list mostra la lista dei documenti\r\n\n" + 
						"edit <doc > <sec > modifica una sezione del documento\r\n" + 
						"end-edit fine modifica della sezione del doc.\r\n\n" + 
						"send <msg > invia un msg sulla chat\r\n" + 
						"receive visualizza i msg ricevuti sulla chat");
			System.out.println("--");
			
			//controllo l'operazione richiesta (stringa dopo "turing")
			switch(op[1]) {
			
				//fase di registrazione
				case "register":{
					System.out.println("--");
					
					//registro due nomi
					String usrn=op[2];
					String pass=op[3];
					System.out.println("Eseguo fase di registrazione con nome utente/password: " + usrn + "/" + pass);
					
					//controllo se l'utente è già stato inserito nella lista utenti registrati
					if(listautenti.Registrazione(usrn,pass)) {
						System.out.println("Utente "+ usrn+" inserito correttamente!");
					}
					else System.out.println("Utente già registrato!");
					
					//eseguo nuovo comando
					System.out.println("----Cosa vuoi fare?----");
					System.out.println("turing register <username > <password > registra l' utente\r\n" + 
							"turing login <username > <password > effettua il login\r\n");
					System.out.println("-----------------------");
					op=x.nextLine().split(" ");
					continue;
				}
				
				//fase di login
				case "login":{
					//controllo che ho dato in input almeno 4 parametri
					if(op.length<5 && op.length>2) {
					 try { 
						 //TCP
						 //setto l'indirizzo e apro il socket
						 SocketAddress address = new InetSocketAddress("localhost", DEFAULT_PORT);
						 System.out.println(address);
						 SocketChannel client = SocketChannel.open(address);
						 ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_DIM);
						 System.out.println("Inizio fase log in");

						 //memorizzo in usrn l'username dell'utente che sta effettuando il login e in pass la password associata
						 String usrn=op[2];
						 String pass=op[3];
						 String operazione="login:";
						 
						 //comunico al server la richiesta appena ricevuta(login)
						 String mando="(" + usrn +")" + operazione.concat(usrn).concat("-").concat(pass);
						 buffer.put(mando.getBytes());
						 buffer.flip();
						 while(buffer.hasRemaining())
							 client.write(buffer);
						 
						//ricevo la risposta dal server
						 ByteBuffer myBuffer2 = ByteBuffer.allocate(DEFAULT_DIM);
					        client.read(myBuffer2);
					        String data = new String(myBuffer2.array()).trim();
					        if (data.length() > 0) {
					         System.out.println(String.format("Message Received.....: %s\n", data));
					        }
					        
					        //controllo se la richiesta del login è andata a buon fine
					        if(data.equals("Login Eseguito Correttamente")) {
					        	
					        //setto il bit stop per uscire dalla fase di login se qualcosa va storto
					        boolean stop=true;	
					        	while(stop) {
					        		System.out.println("-----Quali altre operazioni vuoi fare?-----");
					        		System.out.println("turing create <doc > <numsezioni > crea un documento\r\n" + 
					        				"turing share <doc > <username > condivide il documento\r\n" + 
					        				"turing show <doc > <sec > mostra una sezione del documento\r\n" + 
					        				"turing show <doc > mostra l' intero documento\r\n" + 
					        				"turing list mostra la lista dei documenti\r\n" + 
					        				"turing edit <doc > <sec > modifica una sezione del documento\r\n" +
					        				"turing logout effettua il logout");
					        		System.out.println("---------------------------------------------");
					        		
					        		//ricevo in input il comando desiderato
					        		buffer.clear();	 	
					        		op=x.nextLine().split(" ");
									
					        		//controllo se ho inserito almeno due parametri "turing compreso"
					        		if(op.length==1) {
					        			System.out.println("Errore operazione!");
					        			continue;
					        		}
					        		else System.out.println("Hai selezionato: " + op[1]);
									
					        		//faccio uno switch con le varie operazioni inserite
									switch(op[1]) {
										case "create":{		
											System.out.println("Eseguo creazione documento");
											
											//controllo se ho dato in input i 4 parametri richiesti
											if(op.length<5 && op.length>2) {
												//memorizzo nella variabile cartella il nome del documento da creare e in sezioni il num di sezioni
												String cartella=op[2];		
												int sezioni;
												try {
													sezioni=Integer.parseInt(op[3]);
												}catch(Exception e) {
													System.out.println("errore, la sezione deve essere un intero");
													continue;
												}
												
												//salvo in buffer il comando da dare al server e lo comunico
												ByteBuffer buffer3 = ByteBuffer.allocate(DEFAULT_DIM);
												String a=cartella +"-"+ sezioni ;
												mando="(" + usrn +")" + "createdocument:".concat(a);
												buffer3.put(mando.getBytes());
												buffer3.flip();
												while(buffer3.hasRemaining())
													 client.write(buffer3);
												
												//ricevo la risposta dal server
												myBuffer2.clear();
												myBuffer2 = ByteBuffer.allocate(DEFAULT_DIM);
											    client.read(myBuffer2);
											    data = new String(myBuffer2.array()).trim();
											    
											    //leggo l'esito della richiesta
											     if (data.length() > 0) {
											       System.out.println(String.format("Message Received.....: %s\n", data));
											     }
											}
										     stop=true;
										     continue;
										}
										//eseguo edit
										case "edit":{
											System.out.println("Eseguo editing");
											
											//controllo se ho passato il numero giusto di parametri
											if(op.length>=5 || op.length<4) {
											//se non ho inserito i parametri corretti do errore
											  System.out.println("Errore parametri!");
											  stop=true;
											  continue;
											}
												
											//memorizzo il nome del documento d editare e 
											String doc=op[2];
											int sez;
											
											try {
												sez=Integer.parseInt(op[3]);
											}catch(NumberFormatException e) {
												System.out.println("errore, la sezione deve essere un intero");
												continue;
											}
											
											
											//mando la richiesta al server
											ByteBuffer buffer3 = ByteBuffer.allocate(DEFAULT_DIM);
											String a=doc +"-"+ sez ;
											mando="(" + usrn +")" + "edit:".concat(a);
											buffer3.put(mando.getBytes());
											buffer3.flip();
											while(buffer3.hasRemaining())
												 client.write(buffer3);
											
											//leggo l'esito della richiesta
											ByteBuffer buffer5 =  ByteBuffer.allocate(DEFAULT_DIM);
											client.read(buffer5);
											data = new String(buffer5.array()).trim();
										     
										     if (data.length() > 0) {
										       System.out.println(String.format("Message Received.....: %s\n", data));
										     }
											
											buffer5.clear();
											
											//se l'esito non è andato a buon fine continuo
											if(data.equals("Documento non trovato") || data.equals("Accesso Negato") || data.equals("Sezione non presente!")) {
												  stop=true;
												  continue;
											}
											
											//se la richiesta è andata a buon fine
											else {
												//recupero indirizzo ip e porta per la chat(comunicati dal server
												String adress=data.substring(1, data.indexOf(":"));
												int  port=Integer.parseInt(data.substring(data.indexOf(":")+1));
												
												//scarico il file da editare ed avvio l'editor
												downloadFile(client,doc,sez);
												buffer3.clear();
												int fermo=0;
												
												//apro la chat in UDP
												ExecutorService es = Executors.newSingleThreadExecutor();
												
												//passo l'indirizzo e la porta al thread in ascolto
												ChatMulticastReceiver chat=new ChatMulticastReceiver(adress,port);
												memory=chat.getArray();
												es.submit(chat);
												
												 while(fermo!=-1) {
													 System.out.println("-------Operazioni della fase di EDITING----------");
													 System.out.println("turing send <msg > invia un msg sulla chat\r\n" + 
													 		"turing receive visualizza i msg ricevuti sulla chat\n" +
													 		"turing end-edit fine modifica della sezione del doc.\n");
													 System.out.println("--------------------------------------------------");
													
													 //leggo da input la richiesta
													 String strin=x.nextLine();
													 
													 //elimino dalla stringa la parola turing
													 String[] operation=strin.split(" ");
													
													 if(!operation[0].equals("turing") || operation.length<2) {
														 System.out.println("Errore operazione\n NOTA: ricordati di inserire la parola turing\n");
														 continue;
													 }
														 
													 switch(operation[1]) {
													 //effettuo l'invio di un messaggio nella chat
													 case "send":{
														 System.out.println("--Eseguo send--");
														 //setto il socket e l'indirizzo multicast comunicati dal server
														 MulticastSocket s= new MulticastSocket(port);
														InetAddress  multiaddress= InetAddress.getByName(adress);
														
														//elimino le parole: turing e send dalla stringa in input
														strin=strin.substring(strin.indexOf(" ")+1);
														strin=strin.substring(strin.indexOf(" ")+1);
														
														//ottengo il messaggio da inviare
														String msg=strin;
														System.out.println("Invio il messaggio: " + msg);
														
														//creo il datagrampachet contenente il messaggio da inviare
														DatagramPacket p= new DatagramPacket(
																msg.getBytes("UTF-8"),0,
																msg.getBytes("UTF-8").length,
																multiaddress,port);
														
														//mando il messaggio tramite il multicastsocket e chiudo
														s.send(p);	
														s.close();
														
														System.out.println("Messaggio inviato all'indirizzo: "+adress+ " con successo!");
														continue;
													 }
													 case "receive":{
														 //ricevo i messaggi ricevuti in chat
														 System.out.println("--Eseguo receive--");
														 
														 //finchè la lista dei messaggi non è vuota, li rimuovo e stampo il contenuto
														 while(!memory.isEmpty()) {
																System.out.println(memory.peek());
																memory.remove();
															}
														 System.out.println("-END-");
														 continue;
													 }
													 case "end-edit":{
														 System.out.println("Eseguo END-editing");
															
														 //mando al server la richiesta di end-edit
														 mando="(" + usrn +")" + "endedit:".concat(a);
														 buffer3.clear();
														 buffer3.put(mando.getBytes());
														 buffer3.flip();
														 while(buffer3.hasRemaining())
															 client.write(buffer3);
														 String patch="tempEdit-"+doc+"-"+sez+".txt";
				 							 			
														 //aggiorno il file 
														 uploadFile(patch,client);
														 buffer3.clear();
														
														 //termina il thread in ascolto dei messaggi
														es.shutdownNow();
														fermo=-1;
														continue;
													 }
														 default:{
															 //messaggio non valido continuo nella fase di edit
															 System.out.println("Ricevuto messaggio non valido");
															 continue;
														 }
													 }
												 }
										     stop=true;
										     continue;
										   }
										}
										
										case "show":{
											if (op.length<3) {
												System.out.println("Operazione show non valida, pochi argomenti");
												stop=true;
												continue;
											}
											//se ho dato in input solo 3 parametri allora eseguo la show del documento
											if(op.length<4) {
												System.out.println("Eseguo Show(D)");
												//salvo in doc il nome del documento
												String doc=op[2];
												
												//comunico al server l'operazione 
												ByteBuffer buffer3 = ByteBuffer.allocate(DEFAULT_DIM);
												mando="(" + usrn +")" + "showD:".concat(doc);
												buffer3.clear();
												buffer3.put(mando.getBytes());
												buffer3.flip();
												while(buffer3.hasRemaining())
													 client.write(buffer3);
												
												//leggo l'esito
												ByteBuffer buffer5 =  ByteBuffer.allocate(DEFAULT_DIM);
												client.read(buffer5);
												data = new String(buffer5.array()).trim();
												
												if (data.length() > 0) {
												     System.out.println(String.format("Message Received.....: %s\n", data));
												     
												//se l'esito non è andato a buon fine continuo
												     if(data.equals("Documento non presente!")) {
												    	 stop=true;
												    	 continue;
												     }
												//altrimenti ricevo il documento e lo mostro
												     else showD(client);
												}
												
												stop=true;
												continue;
											}
											else {
												//se ho passato 4 parametri mostro la sezione del documento
												System.out.println("Eseguo Show(S,D)");
												
												//memorizzo in doc il documento e in sez la sezione relativa al documento da mostrare
												String doc=op[2];
												int sez;
												
												try {
													sez=Integer.parseInt(op[3]);
												}catch(NumberFormatException e) {
													System.out.println("errore, la sezione deve essere un intero");
													continue;
												}
												
												//comunico la richiesta al server
												ByteBuffer buffer3 = ByteBuffer.allocate(DEFAULT_DIM);
												String a=doc +"-"+ sez ;
												mando="(" + usrn +")" + "show:".concat(a);
												buffer3.clear();
												buffer3.put(mando.getBytes());
												buffer3.flip();
												while(buffer3.hasRemaining())
													 client.write(buffer3);

												//leggo l'esito
												ByteBuffer buffer5 =  ByteBuffer.allocate(DEFAULT_DIM);
												client.read(buffer5);
												data = new String(buffer5.array()).trim();
											
												if (data.length() > 0) {
													System.out.println(String.format("Message Received.....: %s\n", data));
												}
											
												//se l'esito non è andato a buon fine continuo
												if(data.equals("Documento non presente!") || data.equals("Sezione non presente!")) {
													stop=true;
													continue;
												}
												//se l'esito è andato a buon fine leggo se la sezione è in uso e la mostro
												else{
													myBuffer2 = ByteBuffer.allocate(DEFAULT_DIM);
												    client.read(myBuffer2);
												    data = new String(myBuffer2.array()).trim();
												    System.out.println(data);
													show(client);
													stop=true;
													continue;
												}
											}	
										}
									
										case "share":{
											//eseguo operazione di condivisione del documento
											System.out.println("Eseguo Share(D,user)");
											
											if(op.length>4 || op.length<2) {
												System.out.println("Errore parametri!");
												stop=true;
												continue;
											}
											
											//memorizzo in doc il nome del documento da condividere e in user l'username dell'utente
											String doc=op[2];							
											String user=op[3];
											
											//comunico la richiesta al server
											ByteBuffer buffer3 = ByteBuffer.allocate(DEFAULT_DIM);
											String a=doc +"-"+ user ;
											mando="(" + usrn +")" + "share:".concat(a);
											buffer3.clear();
											buffer3.put(mando.getBytes());
											buffer3.flip();
											while(buffer3.hasRemaining())
												 client.write(buffer3);

											//ricevo la risposta
											myBuffer2.clear();
											myBuffer2 = ByteBuffer.allocate(DEFAULT_DIM);
										    client.read(myBuffer2);
										    data = new String(myBuffer2.array()).trim();
										     
										    //stampo l'esito della richiesta
										     if (data.length() > 0) {
										       System.out.println(String.format("Message Received.....: %s\n", data));
										     }
										    stop=true;
										    continue;
										}
										//mostra la lista dei documenti dell'utente che ha fatto il login
										case "list":{
											System.out.println("Eseguo List");
											ByteBuffer buffer3 = ByteBuffer.allocate(DEFAULT_DIM);
											
											//invio la richiesta al server
											mando="(" + usrn +")" + "list:"+usrn;
											buffer3.clear();
											buffer3.put(mando.getBytes());
											buffer3.flip();
											while(buffer3.hasRemaining())
												 client.write(buffer3);

											//ricevo la risposta
											myBuffer2.clear();
											myBuffer2 = ByteBuffer.allocate(DEFAULT_DIM);
										    client.read(myBuffer2);
										    data = new String(myBuffer2.array()).trim();
										     
										    //leggo l'esito della richiesta
										     if (data.length() > 0) {
										       System.out.println(String.format("Message Received.....:\n %s\n", data));
										     }
										     else System.out.println("Nessun documento presente");
										    stop=true;
										    continue;
										}
										
										//effetuo i logout dell'utente
										case "logout":{
											System.out.println("Eseguo logout");
											ByteBuffer buffer3 = ByteBuffer.allocate(DEFAULT_DIM);
											//mando la richiesta di logout al server
											mando="(" + usrn +")" + "logout:"+usrn;
											buffer3.clear();
											buffer3.put(mando.getBytes());
											buffer3.flip();
											while(buffer3.hasRemaining())
												 client.write(buffer3);

											 //ricevo la risposta
											myBuffer2.clear();
											myBuffer2 = ByteBuffer.allocate(DEFAULT_DIM);
										    client.read(myBuffer2);
										    data = new String(myBuffer2.array()).trim();
										     
										    //leggo l'esito della richiesta
										     if (data.length() > 0) {
										       System.out.println(String.format("Message Received.....: %s\n", data));
										     }
										    stop=false;
										    break;
										}
										//se eseguo un comando che non corrisponde a qullo richiesto mando messaggio di errore 
										default:{
											System.out.println("Ricevuto messaggio non valido");
											stop=true; continue;
										}
						
									}
					        	}
					        }
					    
					      //chiudo il socket creato e cancello il buffer utilizzato per comunicare
						 client.close();
						 buffer.clear();
						 
					 } catch(Exception ex) { 
						ex.printStackTrace();
					 	} 
					}
					//se ho sbagliato i parametri mando un messaggio 
					else System.out.println("devi usare: login <username > <password >\n");
				
					System.out.println("----Cosa vuoi fare?----");
					System.out.println("turing register <username > <password > registra l' utente\r\n" + 
							"turing login <username > <password > effettua il login\r\n");
					System.out.println("-----------------------");
					op=x.nextLine().split(" ");
					break;
					} 
				//se ho sbagliato i parametri mando un messaggio 
				default: if(!op[1].equals("--help")) System.out.println("Errore Operazione " + op[1] );
				
				System.out.println("----Cosa vuoi fare?----");
				System.out.println("turing register <username > <password > registra l' utente\r\n" + 
						"turing login <username > <password > effettua il login\r\n");
				System.out.println("-----------------------");
					op=x.nextLine().split(" ");
					break;
				}
		}
		//non ho inserito turing all'inizio, termino..
		System.out.println("comando non valido per il server turing, termino..");
		x.close();
	}
	
	/**
	 * funzione per scaricare la sezione sez relativa al determinato documento doc e copiarla in un file
	 * temporaneo per fare l'editing della stessa
	 * 
	 * @param socket associato alla comunicatione con un determinato client
	 * @param doc documento con cui sto facendo l'edit
	 * @param sez sezione del documento che sto editando
	 */
	public static void downloadFile(SocketChannel socket,String doc, int sez) {
		ByteBuffer buffer =  ByteBuffer.allocate(DEFAULT_DIM);
		
		try {
			//apro il file dove devo scrivere
			FileChannel out = FileChannel.open(Paths.get("tempEdit-"+doc+"-"+sez+".txt"),StandardOpenOption.CREATE,StandardOpenOption.WRITE, StandardOpenOption.READ);
			
			//memorizzo in buffer la dimensione del file da ricevere
			socket.read(buffer);
			buffer.flip();
			
			long fileSize = buffer.getLong();
			System.out.println("[Ftp Client] la dimensione del file è " + fileSize);
			
			//scrivo i byte del buffer nel file out aperto precedentemente
			int read = 0;
			read += out.write(buffer);
			buffer.compact();
			int eos = 0;
			
			//memorizzo i byte ricevuti nel file out
			while ((read < fileSize) && (eos != -1)) {
				eos = socket.read(buffer);
				read += eos;
				buffer.flip();
				out.write(buffer);
				buffer.compact();
			}
			
			System.out.println("[Ftp Client] file ricevuto correttamente.");
			out.close();
			buffer.clear();
				
			//apro il nuovo file creato con il programma di editor
			File a = new File("tempEdit-"+doc+"-"+sez+".txt");
			//apro editore per modificarlo
			if (Desktop.isDesktopSupported()) {
			    Desktop.getDesktop().edit(a);
			    System.out.println("File aperto con l'editor correttamente");  
			} else {
				System.out.println("Errore apertura file con editor");  
			}
		}
		catch(Exception e) {	
       		System.out.println("Errore downloadfile: " + e); 
		}
	}

	/**
	 * funzione utilizzata nell'end-edit per aggiornare la sezione relativa al documento
	 * 
	 * @param FileN patch del file temporaneo 
	 * @param socket associato alla comunicatione con un determinato client
	 */
	public static void uploadFile(String FileN,SocketChannel socket) {
		FileChannel file = null;	
	
		try {
			//apro il file in lettura
			file = FileChannel.open(Paths.get(FileN),StandardOpenOption.READ);
			System.out.println(FileN);
		
			ByteBuffer buf = ByteBuffer.allocate(DEFAULT_DIM);
			buf.clear();
		
			//mando la dimensione
			buf.putLong(file.size());
			buf.flip();
			while (buf.hasRemaining())
				socket.write(buf);
			buf.clear();
		
			//mando il contenuto
			long pos = file.position();
			long toCopy = file.size();
			System.out.println("Il file da inviare è grande: " + toCopy);
			 while (toCopy > 0) {
			  long bytes = file.transferTo(pos, toCopy, socket);
			  pos += bytes;
			  toCopy -= bytes;
			 }
			
			file.close();
			
			System.out.println("ho trasferito: " + pos + " byte");
			System.out.println("[FTP CLient] fine transferimento");
		}  catch(Exception e){ 
			System.out.println("Errore uploadfile: " + e); 
        	} 	
	}

	/**
	 * funzione per leggere una sezione di un documento 
	 * 
	 * @param socket associato alla comunicatione con un determinato client
	 * @throws IOException lanciata per problemi di I/O
	 * @throws NotYetConnectedException lanciata se il canale del socket non è ancora collegato
	 */
	public static void show(SocketChannel socket) throws IOException,NotYetConnectedException {
		ByteBuffer buffer=ByteBuffer.allocate(DEFAULT_DIM);
		
		//leggo la dimensione del contenuto del file da ricevere
		int bytes=0;
		socket.read(buffer);
		buffer.flip();
		long lun=buffer.getLong();
		buffer.clear();
		System.out.println("Ho letto: " + lun);
		
		//se non c'è nessun contenuto(file vuoto) esco
		if(lun==0) {
			System.out.println("---NESSUN CONTENUTO--");
		}
		
		//se ho leto qualcosa 
		else {
			//leggo il contenuto del file e lo memorizzo in buffer
			bytes=socket.read(buffer);
			System.out.println("bytes letti: " + bytes);
			System.out.println("--------------INIZIO LETTURA--------------");
			
			//metto il limite del buffer alla posizione corrente e l'indice di posizione a 0
			buffer.flip();
			
			//finchè il buffer non è vuoto
			while(buffer.hasRemaining()){
				//leggo un byte alla volta
				System.out.print((char) buffer.get()); 
			}
		
			System.out.println("");
			buffer.clear();
		
			System.out.println("--------------FINE LETTURA--------------");
		}
		
	}
	
	/**
	 * funzione per ricevere il documento(sezioni comprese)
	 * @param socket associato alla comunicatione con un determinato client
	 * @throws IOException lanciata per problemi di I/O
	 * @throws NotYetConnectedException lanciata se il canale del socket non è ancora collegato
	 */
	public static void showD(SocketChannel socket) throws IOException,NotYetConnectedException {
		//dichiaro il buffer da usare per la lettura 
		ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_DIM*5);
		buffer.clear();
		
		//leggo il numero di file da ricevere
		socket.read(buffer);
		//metto il limite del buffer alla posizione corrente e l'indice di posizione a 0
		buffer.flip();
		
		//leggo il numero di file che dovrò ricevere
		int value=0;
		value=(int) buffer.getInt(); 
		System.out.println("Mi preparo a ricevere: " + value + " file");
		
		//leggo il documento con le varie sezioni
		buffer.clear();
		socket.read(buffer);
		
		//le stampo a schermo
		stampa(buffer);
	}
			
	/**
	 * 
	 * @param buffer buffer contenente il contenuto da stampare a schermo
	 */
	public static void stampa(ByteBuffer buffer){
		System.out.println("--------------INIZIO LETTURA--------------");
		//metto il limite del buffer alla posizione corrente e l'indice di posizione a 0
		buffer.flip();
		
		//finchè il buffer non è vuoto
		while(buffer.hasRemaining()){
			//leggo un byte alla volta
	      System.out.print((char) buffer.get()); 
		}
				
		System.out.println("");
		
		//cancello il buffer
		buffer.clear();
		System.out.println("--------------FINE LETTURA--------------");
	}

}
