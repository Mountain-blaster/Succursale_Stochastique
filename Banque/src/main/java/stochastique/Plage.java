package stochastique;

public class Plage {
	
	double r;
	RendezVous Rv;
	int numeroPlage;

		class RendezVous {
			double heureRv;
			int numeroClientB;
			
		}
		
		public Plage (double r, RendezVous Rv, int numeroPlage) {
			this.r = r;
			this.Rv = Rv;
			this.numeroPlage = numeroPlage;
		}
}
