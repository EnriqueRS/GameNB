package juegonb;

import juegonb.figuras.Esqueleto;
import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOr;

public class DeteccionControlPersonaje extends javax.media.j3d.Behavior {

    Esqueleto personaje;
    WakeupOnAWTEvent presionada = new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED);
    WakeupOnAWTEvent liberada = new WakeupOnAWTEvent(KeyEvent.KEY_RELEASED);
    WakeupCondition keepUpCondition = null;
    WakeupCriterion[] continueArray = new WakeupCriterion[2];

    public DeteccionControlPersonaje(Esqueleto _personaje) {
        personaje = _personaje;
        continueArray[0] = liberada;
        continueArray[1] = presionada;
        keepUpCondition = new WakeupOr(continueArray);
    }

    public void initialize() {
        wakeupOn(keepUpCondition);
    }

    public void processStimulus(Enumeration criteria) {
        while (criteria.hasMoreElements()) {
            WakeupCriterion ster = (WakeupCriterion) criteria.nextElement();
            if (ster instanceof WakeupOnAWTEvent) {
                AWTEvent[] events = ((WakeupOnAWTEvent) ster).getAWTEvent();
                for (int n = 0; n < events.length; n++) {
                    if (events[n] instanceof KeyEvent) {
                        KeyEvent ek = (KeyEvent) events[n];
                        if (ek.getID() == KeyEvent.KEY_PRESSED) {
                            if (ek.getKeyChar() == 'w') {
                                personaje.adelante = true;
                            } else if (ek.getKeyChar() == 'a') {
                                personaje.izquierda = true;
                            } else if (ek.getKeyChar() == 'd') {
                                personaje.derecha = true;
                            } else if (ek.getKeyChar() == 's') {
                                personaje.atras = true;
                            } else if (ek.getKeyChar() == 'c') {
                                personaje.juego.camara = !personaje.juego.camara;
                                personaje.juego.camaraCuadrilatero=false;
                                personaje.juego.camaraGlobal=false;
                            } else if (ek.getKeyChar() == 'v') {
                                personaje.juego.camaraCuadrilatero = !personaje.juego.camaraCuadrilatero;
                                personaje.juego.camara=false;
                                personaje.juego.camaraGlobal=false;
                            } else if (ek.getKeyChar() == 'b') {
                                personaje.juego.camaraGlobal = !personaje.juego.camaraGlobal;
                                personaje.juego.camara=false;
                                personaje.juego.camaraCuadrilatero=false;
                            }
                        } else if (ek.getID() == KeyEvent.KEY_RELEASED) {
                            if (ek.getKeyChar() == 'w') {
                                personaje.adelante = false;
                            } else if (ek.getKeyChar() == 'a') {
                                personaje.izquierda = false;
                            } else if (ek.getKeyChar() == 'd') {
                                personaje.derecha = false;
                            } else if (ek.getKeyChar() == 's') {
                                personaje.atras = false;
                            }
                        }
                    }
                }
            }
        }
        wakeupOn(keepUpCondition);
    }
}
