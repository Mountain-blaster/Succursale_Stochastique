package stochastique;
import umontreal.ssj.rng.*;
import java.util.ArrayList;

public class Conseiller {
	
	double r; // Probabilite d'avoir rendez-vous sur une plage
	int numeroConseillerD;
	
	RandomStream probR = new MRG32k3a();
	RandomStream time = new MRG32k3a();
	
	ArrayList<Plage> liste_des_reunions = new ArrayList<Plage>();
	
	//Constructor;
	public Conseiller(int numeroConseillerD) {
		
		this.numeroConseillerD = numeroConseillerD;
	}
	
	
	public ArrayList<Plage> getReunions() {
		
		Plage reunion;
		
		for(int i = 1; i<=12 ; i++) {
			
			double u = probR.nextDouble();
			
			if(u <= r) {
				
				double heureRv = time.nextDouble();
				
				RendezVous Rv = new RendezVous(heureRv, i);
				
				reunion = new Plage(Rv, i);
				
			}
			
			else {
				
				RendezVous Rv = null;
				
				reunion = new Plage(Rv, i);
				
			}
			
			liste_des_reunions.add(reunion);
			
		}
		
		return liste_des_reunions;
		
	}	
}
