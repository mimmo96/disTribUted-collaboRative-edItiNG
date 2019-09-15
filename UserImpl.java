import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 * @author DOMENICO PROFUMO 533695 CORSO B
 * 
 */

public class UserImpl extends UnicastRemoteObject implements User {
	private ConcurrentHashMap <String,Utente>  hashmap;
	private static final long serialVersionUID = 1L;
	
	/**
	 * metodo costruttore:crea hashmap(nome utente,struttura utente) inizialmente vuota
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 */
	public UserImpl() throws RemoteException {
		super();
		hashmap=new ConcurrentHashMap<String,Utente>();
	}

	/**
	 * registra l'utente con username e password
	 * @param username nome dell'utente
	 * @param password password dell'utente
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 * @throws NullPointerException lanciata se username o password==null
	 * @return restituisce true se la registrazione è andata a buon fine, false altrimenti
	 */
	public boolean Registrazione(String username, String password) throws RemoteException,NullPointerException {
		if(username==null || password==null) throw new NullPointerException();
		
		//controllo se l'username appartiene alla tabella hash, se non c'è lo inserisco
		//passando come chiave il nome utente e come valore la nuova struttura utente associata
		if(!hashmap.containsKey(username)) {
			hashmap.put(username, new Utente(username,password));
			System.out.println("Utente " + username +" aggiunto alla lista registrati!");
			return true;
		}
		else System.out.println("Utente già registrato!");
		return false;
	}
	
	/**
	 * 
	 * @return restituisce il numero di utenti registrati
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 */
	public int getsize() {
		return hashmap.size();
	}
	
	/**
	 * stampa la lista di tutti gli username registrati
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 */
	public void stamparegistrati() {
		Iterator<Utente> k= hashmap.values().iterator();
		//scorro la lista degli utenti registrati(il campo valore della mia hashmap)
		if(k.hasNext()) {
			System.out.println("Stampo lista utenti registrati:");
			for(int i=0;i<hashmap.size();i++) {
				System.out.println(k.next().getusername());
			}
		}
		else System.out.println("Nessun utente registrato!");
	}
	
	/**
	 * controllo se l'utente è registrato
	 * 
	 * @param username nome dell'utente
	 * @param password password dell'utente
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 * @throws NullPointerException lanciata se username o password==null
	 * @return restituisce true se l'utente è registrato e la password è corretta, false altrimenti
	 */
	public boolean isregistred(String username,String password) throws RemoteException,NullPointerException{
		if(username==null || password==null) throw new NullPointerException();
		
		if(hashmap.containsKey(username)) {	 	
			if(hashmap.get(username).getpassword().equals(password)) return true;
		}
		return false;
	}
	
	/**
	 * effettua il login dell'utente
	 * 
	 * @param username nome dell'utente
	 * @param password password dell'utente
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 * @throws NullPointerException lanciata se username o password==null
	 * @return restituisce true se il login è andato a buon fine, false altrimenti
	 */
	public boolean login(String username, String password)throws RemoteException,NullPointerException{
		if(username==null || password==null) throw new NullPointerException();
		
		if(hashmap.get(username)!=null) {
			if(!hashmap.get(username).isonline()) {
				hashmap.get(username).setonline();
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * effettua il logout dell'utente
	 * 
	 * @param username nome dell'utente
	 * @param password password dell'utente
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 * @throws NullPointerException lanciata se username==null
	 * @return restituisce true se il logout è andato a buon fine, false altrimenti
	 */
	public boolean logout(String username, String password) throws RemoteException,NullPointerException{
		if(username==null) throw new NullPointerException();
		
		if(hashmap.get(username)!=null) {
			hashmap.get(username).setoffline();
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param username nome dell'utente
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 * @throws NullPointerException lanciata se username o password==null
	 * @return restituisce l'oggetto Utente associato al'username se presente, null altrimenti
	 */
	public Utente getuser(String username) throws RemoteException,NullPointerException{
		if(username==null) throw new NullPointerException();
		
		if(hashmap.containsKey(username)) {	 
			return hashmap.get(username);
		}
		return null;
		
	}
	
}
