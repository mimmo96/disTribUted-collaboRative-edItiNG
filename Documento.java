import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 * @author DOMENICO PROFUMO 533695 CORSO B
 * 
 */

public class Documento {
	private String name;
	private String autore;
	private ArrayList<String> collaboratori;
	private int numsezioni;
	private ArrayList<Boolean> inuso;
	private InetAddress address; 
	private String add;
	private MulticastSocket socket;
	
	/**
	 * 
	 * @param autor nome dell'autore del documento
	 * @param name nome del documento
	 * @param sezioni numero sezioni del documento
	 * @throws NullPointerException se autor o name==null
	 */
	public Documento(String autor,String name,int sezioni) throws NullPointerException {
		if(autor==null || name==null) throw new NullPointerException();
		this.name=name;
		this.numsezioni=sezioni;
		this.collaboratori=new ArrayList<String>();
		this.inuso=new ArrayList<Boolean> (numsezioni);
		
		//setto a false i bit di uso delle sezioni
		for(int i=0;i<=numsezioni;i++) {
			inuso.add(false);
		}
		
			//creo la directory "Documenti" che conterrà tutti i documenti degli utenti
			this.autore=autor;
			File mkdir = new File("Documenti");
			mkdir.mkdir();
			File theDir = new File("Documenti/".concat(name));
			theDir.mkdir();
			
			// se la directory non esiste la creo
			if (!theDir.exists()) {
				System.out.println("creo la directory: " + theDir.getName());
				boolean result = false;
		        theDir.mkdir();
		        result = true;
		   
		        if(result) {    
		        	System.out.println("Cartella creata");
		        }
		        else  System.out.println("DIR già creata");
			} 
	
		for(int i=1;i<=this.numsezioni;i++) {
			String path=theDir.getPath().concat("/").concat("sezione" + i + ".txt");
			try {
			File file = new File(path);

				if (file.exists())
					System.out.println("Il file " + path + " esiste");
				else if (file.createNewFile())
					System.out.println("Il file " + path + " è stato creato");
				else
					System.out.println("Il file " + path + " non può essere creato");
		}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

		this.address=null;
		this.add=null;
		this.socket=null;
	}

	/**
	 * 
	 * @return restituisce il nome del documento
	 */
	
	public String getName() {
		return this.name;
	}
	
	/**
	 * 
	 * @return restituisce il nome dell'autore del documento
	 */
	public String getAutor() {
		return this.autore;
	}
	
	/**
	 * 
	 * @return resituisce il numero di sezioni presenti nel docuemento
	 */
	public int getnumsezioni() {
		return this.numsezioni;
	}
	
	/**
	 * 
	 * @return restituisce un arraylist contenente la lista di tutti i collaboratori associati al documento
	 */
	public ArrayList<String> getCollaboratori(){
		return this.collaboratori;
	}
	
	/**
	 * 
	 * @return restituisce un arraylist contenente le sezioni in uso
	 */
	public ArrayList<Boolean> getinuso() {
		return this.inuso;
	}
	
	/**
	 * verifica che l'autore del documento passato sia autor e aggiunge ai collaboratori name
	 * 
	 * @param autor nome dell'autore del documento
	 * @param name nome dell'utente collaboratore 
	 * @param doc nome del documento
	 * @return restituisce true se l'operazione è andata a buon fine, false altrimenti
	 * @throws NullPointerException
	 */
	public boolean addcollaboratori(String autor,String name,Documento doc) throws NullPointerException {
		if(autor==null || name==null || doc==null) throw new NullPointerException();
		if(doc.getAutor().equals(autor)) {
			doc.getCollaboratori().add(name);
			return true;
		}
		return false;
	}
	
	/**
	 * funzione di edit per il documento
	 * 
	 * @param utente username dell'utente che sta editando
	 * @param doc documento da editare
	 * @param section sezione del documento da editare
	 * @return restituisce true se l'edit del documento doc è andato a buon fine, false altrimenti
	 * @throws NullPointerException se utente o doc sono null
	 * @throws IndexOutOfBoundsException se section non rientra nel range delle sezioni del documento
	 */
	public boolean edit(String utente,Documento doc,int section) throws NullPointerException,IndexOutOfBoundsException {
		if(utente==null || doc==null) throw new NullPointerException();
		if(section<=0 || section>this.numsezioni) throw new IndexOutOfBoundsException();
		int ok=0;
		
		//verifico che l'utente sia l'autore e che nessuno sta già editando quella sezione 
		if(doc.getAutor().equals(utente)) 
			if(doc.getinuso().get(section).equals(false)) ok=1;
		
		//verifico se è tra i collaboratori e che nessuno sta già editando quella sezione 
		for(int i=0;i<doc.getCollaboratori().size();i++) {
			if(doc.getCollaboratori().get(i).equals(utente)) {
				if(doc.getinuso().get(section).equals(false)) ok=1;
			}
		}
		
		//se è autore o collaboratori allora è andata a buon fine
		if(ok==1) {
			//setto la sezione a true
			doc.getinuso().set(section, true);	
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param utente username dell'utente che sta editando
	 * @param doc documento da cui devo fare end-edit
	 * @param section sezione del documento che stavo editando
	 * @return restituisce true se l'end-edit del documento doc è andato a buon fine, false altrimenti
	 * @throws NullPointerException se utente o doc sono null
	 * @throws IndexOutOfBoundsException se section non rientra nel range delle sezioni del documento
	 */
	public boolean endEdit(String utente,Documento doc,int section) throws NullPointerException,IndexOutOfBoundsException {
		if(utente==null || doc==null) throw new NullPointerException();
		if(section<=0 || section>this.numsezioni) throw new IndexOutOfBoundsException();
		
		int ok=0;
		//verifico che l'utente sia l'autore
		if(doc.getAutor().equals(utente)) ok=1;
		
		//verifico se è tra i collaboratori
		for(int i=0;i<doc.getCollaboratori().size();i++) {
			if(doc.getCollaboratori().get(i).equals(utente)) ok=1;
		}
		
		//se è autore o collaboratori allora è andata a buon fine
		if(ok==1) {
			//setto il bit di uso della sezione a false
			doc.getinuso().set(section, false);	
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param utente username dell'utente
	 * @param doc documento da cui devo fare la show
	 * @param section sezione del documento da mostrare
	 * @return restituisce true se l'utente aveva il diritto di accedere alla sezione del documento e la lettura è andata a buon fine, false altrimenti
	 * @throws NullPointerException se utente o doc sono null
	 * @throws IOException se la lettura della sezione non è andata a buon fine
	 * @throws IndexOutOfBoundsExceptionse section non rientra nel range delle sezioni del documento
	 */
	public boolean showS(String utente,Documento doc, int section) throws NullPointerException,IndexOutOfBoundsException, IOException {
		if(utente==null || doc==null) throw new NullPointerException();
		if(section<=0 || section>this.numsezioni) throw new IndexOutOfBoundsException();
		
		int ok=0;
		//verifico che l'utente sia l'autore
		if(doc.getAutor().equals(utente)) ok=1;
		
		//verifico se è tra i collaboratori
		for(int i=0;i<doc.getCollaboratori().size();i++) {
			if(doc.getCollaboratori().get(i).equals(utente)) ok=1;
		}
		
		//se è autore o collaboratori allora è andata a buon fine
		if(ok==1) {
			Boolean editIn=doc.getinuso().get(section).booleanValue();
			
			//modifica la sezione
			String patch="Documenti/".concat(doc.getName()).concat("/").concat("sezione" + section + ".txt");
			
			//leggo il testo
			read(patch);
			
			if(editIn) {
				System.out.println("Qualcuno editando");
			}
			
			System.out.println("Nessuno sta editando");
			
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param Path percorso del file dove devo scrivere
	 * @param buff buffer contenente i byte da scrivere
	 * @throws IOException lanciata se c'è stato un errore nella scrittura del file
	 * @throws FileNotFoundException se il file in path non è stato trovato
	 */
	public static void write(String Path,ByteBuffer buff) throws IOException,FileNotFoundException {
		RandomAccessFile fis    = new RandomAccessFile(Path, "rw");
		FileChannel outChannel = fis.getChannel();
		while (buff.hasRemaining())
			outChannel.write(buff);
		buff.clear();
		outChannel.close(); 
		fis.close();
	}
	
	/**
	 * 
	 * @param Path percorso del file dove devo leggere
	 * @throws IOException lanciata se c'è stato un errore nella lettura del file
	 * @throws FileNotFoundException se il file in path non è stato trovato
	 */
	public static void read(String Path) throws IOException,FileNotFoundException {
		RandomAccessFile fis    = new RandomAccessFile(Path, "r");
		FileChannel inChannel = fis.getChannel();
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		
		while (inChannel.read(buffer)>0){ 
			buffer.flip(); //flip passa da modalità scrittura a lettura del buffer
			while (buffer.hasRemaining()) {
			byte b = buffer.get();
		     System.out.print((char) b);
			}
			System.out.println("");
			buffer.clear();
		}
	
		inChannel.close(); 
		fis.close();
		
	}
	
	/**
	 * 
	 * @return restituisce l'indirizzo(InetAddress) associato al documento
	 */
	public InetAddress getAddress(){
		return this.address;
	}
	/**
	 * 
	 * @return restituisce l'indirizzo sottoforma di stringa
	 */
	public String getadd(){
		return this.add;
	}
	
	/**
	 * @param indirizzo da settare
	 * @return restituisce true se l'indirizzo è stato associato con successo, false altrimenti
	 * @throws UnknownHostException se non è stato trovato alcun indirizzo IP per l'host 
	 * @throws SecurityException se esiste un gestore di sicurezza e il suo metodo checkConnect non consente l'operazione
	 */
	public boolean setAddress(String indirizzo) throws UnknownHostException,SecurityException{
		
		//controllo che già non avevo associato un indirizzo in precedenza
		if(address==null) {
			add=indirizzo;
			address= InetAddress.getByName(indirizzo);
			return true;
		}
		return false;	
	}

	/**
	 * 
	 * @throws IOException se c'è stato un errore nella creazione del socket
	 */
	public void openConnection() throws IOException {
		//se non ho alcun socket associato, lo creo, settando il TTL a 1
		if(socket==null) {
			socket = new MulticastSocket();
			socket.setSoTimeout(100000000);
			socket.setTimeToLive(1);
		}
	}
	
	/**
	 * 
	 * @return restituisce il socket associato al documento
	 */
	public MulticastSocket getSocket() {
		return this.socket;
	}
	
	/**
	 * chiudo la chat(socket) associata al documento
	 */
	public void closeConnection() {
		System.out.println("Non c'è nessuno oltre a me, chiudo il socket");
		socket.close();
		socket=null;
		//cancello l'indirizzo 
		address=null;	
	}
	
	
}
