
package juegonb.figuras;

import com.sun.j3d.loaders.Scene;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import juegonb.ComportamientoMostrar;
import juegonb.Juego;
import net.sf.nwn.loader.AnimationBehavior;
import net.sf.nwn.loader.NWNLoader;

/**
 * @author Enrique Rios Santos
 * 77452845T
 */
public class Ciervo extends Figura{
    public float[] posiciones=new float[3];
    //float[] direccion=new float[3];
    Juego juego;
    public Scene ciervo;
    private Matrix3f matrizCiervo = new Matrix3f();
    TransformGroup desplazamientoCiervo;
    private String rutaCarpetaProyecto =System.getProperty("user.dir")+"/" ;
    public boolean correr;
    public AnimationBehavior ab;
    public BranchGroup RamaMDL;
    ComportamientoMostrar cm;
    private int sentido=1;
    private boolean andando=false, corriendo=false;
    float dt;
    
    public Ciervo(BranchGroup conjunto, ArrayList<Figura> listaObjetos, Juego j) {
        juego=j;
         try{NWNLoader nwn2 = new NWNLoader(); 
            nwn2.enableModelCache(true); 
            /// Lectura fichero MDL 
            String mdl = "Deer.mdl"; 
            ciervo = nwn2.load(new URL("file://localhost/"+rutaCarpetaProyecto+"objetosMDL/"+mdl)); 
            RamaMDL= ciervo.getSceneGroup(); 
            //rotaciones
            Transform3D rotacionCombinada= new Transform3D(); 
            rotacionCombinada.rotX(-Math.PI/2); 
            Transform3D correcionTemp= new Transform3D(); 
            correcionTemp.rotZ(Math.PI); 
            rotacionCombinada.mul(correcionTemp); 
            TransformGroup tgRotaCiervo = new TransformGroup(rotacionCombinada);
            desplazamientoCiervo=new TransformGroup(); 
            desplazamientoCiervo.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); 
            desplazamientoCiervo.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            conjunto.addChild(desplazamientoCiervo);
            desplazamientoCiervo.addChild(tgRotaCiervo);
            tgRotaCiervo.addChild(RamaMDL);
        }catch(Exception e){System.out.println(e.toString());}
        ab = (AnimationBehavior) ciervo.getNamedObjects().get("AnimationBehavior");
        listaObjetos.add(this);
        identificador=2;
        //cm = new ComportamientoMostrar(j);
        inicializar(-4f,0,25f);
    }
    
    @Override
    public void inicializar(float x, float y, float z) {
        posiciones[0]=x; posiciones[1]=y; posiciones[2]=z; 
	float anguloInicial= (float)(3f*Math.PI)/2f; 
	matrizCiervo.set( new AxisAngle4d(0, 1, 0, anguloInicial) );
        mostrar();
    }

    @Override
    public void actualizar(float _dt) {
        dt=_dt;
        float radio=0.7f;
        float[] posicionesAnt = posiciones;
        Transform3D t3dCiervo = new Transform3D();
        desplazamientoCiervo.getTransform(t3dCiervo);
       
        float incremento;
        float posPerX=(float) juego.personaje.posiciones[0];
        float posPerY=(float) juego.personaje.moverSuelo;
        float posPerZ=(float) juego.personaje.posiciones[2];
        Random random=new Random();
        Transform3D tRot= new Transform3D();
        boolean tecla=juego.personaje.adelante||
				juego.personaje.atras||
				juego.personaje.izquierda||
				juego.personaje.derecha;
        if(juego.personaje.estaCuadrilatero){
            if(!corriendo)
                animacionCorrer();
            
            if(tecla)
                    incremento= (float) 0.045*_dt;
            else
                    incremento = (float) (0.010*_dt);
            if(juego.personaje.colisionCiervo 
                    || ((posPerZ<25.7f && posPerZ>24.5f)
                    &&((posPerX>posiciones[0]&&posPerX<posiciones[0]+0.80f && sentido<0) 
                    || (posPerX<posiciones[0]&&posPerX<posiciones[0]-0.80f && sentido>0)))){
                animacionCornear();
            }
        }else{
            if(!andando)
                animacionAndar();
            if(tecla)
               incremento= (float) 0.020*_dt;
            else
               incremento = (float) (0.005*_dt);
        }
            
           
        if(posiciones[0]<-4f || posiciones[0]>4f){
            sentido=-sentido;
            rotar(t3dCiervo);
        }

       posiciones[0]-=incremento*sentido;

         //tRot.setRotation(new AxisAngle4f(0, 1f, 0, ((float)Math.PI/random.nextInt(6))));
        t3dCiervo.mul(tRot);
        t3dCiervo.get( matrizCiervo);
        
    }
    private void rotar(Transform3D t3dCiervo){
        float i=0;
        while(i<Math.PI){
            Transform3D t3dNueva = new Transform3D();
            t3dNueva.setRotation(new AxisAngle4f(0, 1f, 0, 0.0001f));
            t3dCiervo.mul(t3dNueva);
            t3dCiervo.get(matrizCiervo);
            mostrar();
            
            i+=0.0001f;
        }
    }
    
    private void animacionCornear(){
        andando=false;
        corriendo=false;
        juego.ciervo.ab.playAnimation("deer:ca1stab", true);
        juego.juegoFinal=-1;
    }
    
    private void animacionCorrer(){
        andando=false;
        corriendo=true;
        juego.ciervo.ab.playAnimation("deer:crun", true);
    }
    
    private void animacionAndar(){
        corriendo=false;
        andando=true;
        juego.ciervo.ab.playAnimation("deer:cwalk", true);
    }

    @Override
    public void mostrar() {
       //System.out.println(posiciones[0]+" "+posiciones[1]+" "+posiciones[2]);
       Transform3D inip = new Transform3D( matrizCiervo, 
               new Vector3f(posiciones[0], posiciones[1], posiciones[2]),  1f );
       desplazamientoCiervo.setTransform(inip);
    }

    
    
}
