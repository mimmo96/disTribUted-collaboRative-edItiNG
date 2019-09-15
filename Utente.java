import java.util.ArrayList;

/**
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 * @author DOMENICO PROFUMO 533695 CORSO B
 * 
 */

public class Utente {

	String nome;
	String password;
	boolean online=false;
	ArrayList<Documento> listadoc;
	
	/**
	 * metodo costruttore: crea la struttura uetente con nome 
	 * @param user nome dell'utente
	 * @param password password utente
	 * @throws NullPointerException lanciata se n o p==null
	 */
	public Utente(String user,String password) throws NullPointerException {
		if(user==null || password==null) throw new NullPointerException();
		this.nome=user;
		this.password=password;
		this.listadoc=new ArrayList<Documento>();
	}
	
	/**
	 * 
	 * @return restituisce l'username dell'utente
	 */
	public String getusername() {
		return this.nome;
	}
	
	/**
	 * 
	 * @return restituisce la password dell'utente
	 */
	public String getpassword() {
		return this.password;
	}
	
	/**
	 * setta l'utente online
	 */
	void setonline(){
		this.online=true;
	}
	
	/**
	 * setta l'utente offline
	 */
	void setoffline(){
		this.online=false;
	}
	
	/**
	 * 
	 * @return restuisce true se l'utente è online, false altrimenti
	 */
	public boolean isonline() {
		if(this.online) return true;
		return false;
	}
	
	/**
	 * inserisce doc nella lista dei documenti dell'utente
	 * @param doc documento associato all'utente
	 * @throws NullPointerException lanciata se doc==null
	 */
	public void addDoc(Documento doc)throws NullPointerException {
		if(doc==null) throw new NullPointerException();
		this.listadoc.add(doc);
	}
	
	//
	/**
	 * veifica se l'utente contiene il documento di nome x nella sua lista
	 * 
	 * @param doc documento associato all'utente
	 * @throws NullPointerException lanciata se doc==null
	 * @return restituisce il documento doc se presente nella lista, null altrimenti
	 */
	public Documento getDoc(String doc) throws NullPointerException {
		if(doc==null) throw new NullPointerException();
		
		for(int i=0;i<listadoc.size();i++) {
			if(listadoc.get(i).getName().equals(doc)) {
				return listadoc.get(i);
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return restituisce la lista di tutti i documenti relativi all'utente se presenti
	 */
	public ArrayList<Documento> allDoc(){
		ArrayList<Documento> k= new ArrayList<Documento>();
		for(int i=0;i<listadoc.size();i++) {
			k.add(listadoc.get(i));
		}
		return k;
	}
}
