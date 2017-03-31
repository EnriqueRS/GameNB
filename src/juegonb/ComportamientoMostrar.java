package juegonb;

import java.util.Enumeration;
import javax.media.j3d.WakeupOnElapsedFrames;

import javax.media.j3d.WakeupOnElapsedTime;

public class ComportamientoMostrar extends javax.media.j3d.Behavior { 
	Juego juego;
	WakeupOnElapsedTime timewake = new
			WakeupOnElapsedTime(600);
        WakeupOnElapsedFrames framewake = new
			WakeupOnElapsedFrames(60);
        boolean animado;
	
	public ComportamientoMostrar(Juego j){
		juego=j;
	}
	
	public void initialize() { 
            /*if(juego.ciervo.correr)
                
                juego.ciervo.ab.playAnimation("deer:crun", true);
            else
                juego.ciervo.ab.playAnimation("deer:cwalk", true);*/
            
            wakeupOn( framewake);} 
	
	public void processStimulus(Enumeration criteria) {
            /*
           juego.ciervo.actualizar(0.2f);
           if(!animado)
               animar();
           juego.ciervo.mostrar();*/
            wakeupOn( framewake);
	} 
        private void animar(){
            animado=true;
            juego.ciervo.ab.playAnimation("deer:cwalk", true);
        }
}
