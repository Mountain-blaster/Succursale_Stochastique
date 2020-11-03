package stochastique;

public class RendezVous {
	double heureRv;
	int numeroClientB ;
	Plage plageRv ;
	
	
	
	
	class Plage{
		int numeroPlage;
		double r;
		
		public Plage(int numeroPlage, double r) {
			this.numeroPlage = numeroPlage;
			this.r = r;
		}
	}
	
	
	
	
	public RendezVous(double heureRv, int numeroClientB, Plage plageRv) {
		this.heureRv = heureRv;
		this.numeroClientB = numeroClientB;
		this.plageRv = plageRv;
	}

}

