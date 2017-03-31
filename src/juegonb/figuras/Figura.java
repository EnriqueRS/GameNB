package juegonb.figuras;
/**
 * @author Enrique Rios Santos
 * 77452845T
 */

public abstract class Figura {
	float dt;
	float[] velocidades = new float[3]; 
	int identificador; 
	
	public abstract void inicializar(float x, float y, float z);
	
	public abstract void actualizar(float _dt); 
	
	public abstract void mostrar();  
	
}

