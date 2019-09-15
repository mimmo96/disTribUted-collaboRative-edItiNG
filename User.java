import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 * @author DOMENICO PROFUMO 533695 CORSO B
 * 
 */

public interface User extends Remote {
	String SERVICE_NAME = "UserList";
	
	/**
	 * registra l'utente con username e password
	 * @param username nome dell'utente
	 * @param password password dell'utente
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 * @throws NullPointerException lanciata se username o password==null
	 * @return restituisce true se la registrazione è andata a buon fine, false altrimenti
	 */
	public boolean Registrazione(String username,String password) throws RemoteException,NullPointerException;
	
	/**
	 * 
	 * @return restituisce il numero di utenti registrati
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 */
	public int getsize() throws RemoteException;
	
	/**
	 * stampa la lista di tutti gli username registrati
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 */
	public void stamparegistrati() throws RemoteException;
	
	/**
	 * controllo se l'utente è registrato
	 * 
	 * @param username nome dell'utente
	 * @param password password dell'utente
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 * @throws NullPointerException lanciata se username o password==null
	 * @return restituisce true se l'utente è registrato e la password è corretta, false altrimenti
	 */
	public boolean isregistred(String username,String password) throws RemoteException,NullPointerException;
	
	/**
	 * effettua il login dell'utente
	 * 
	 * @param username nome dell'utente
	 * @param password password dell'utente
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 * @throws NullPointerException lanciata se username o password==null
	 * @return restituisce true se il login è andato a buon fine, false altrimenti
	 */
	public boolean login(String username, String password) throws RemoteException,NullPointerException;
	
	/**
	 * effettua il logout dell'utente
	 * 
	 * @param username nome dell'utente
	 * @param password password dell'utente
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 * @throws NullPointerException lanciata se username o password==null
	 * @return restituisce true se il logout è andato a buon fine, false altrimenti
	 */
	public boolean logout(String username, String password) throws RemoteException,NullPointerException;

	/**
	 * 
	 * @param username nome dell'utente
	 * @throws RemoteException lanciata se fallisce una chiamata all'oggetto remoto
	 * @throws NullPointerException lanciata se username o password==null
	 * @return restituisce l'oggetto Utente associato al'username se presente, null altrimenti
	 */
	public Utente getuser(String username) throws RemoteException,NullPointerException;
	
}