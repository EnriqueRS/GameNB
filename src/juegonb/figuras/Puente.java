package juegonb.figuras;
/**
 * @author Enrique Rios Santos
 * 77452845T
 */
import java.util.ArrayList;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.picking.PickTool;
import javax.media.j3d.Appearance;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import juegonb.Juego;


public class Puente extends Figura {
	private Cylinder puente;
	float[] posiciones=new float[3];
	Juego juego;
	private Matrix3f matrizPuente = new Matrix3f();
	TransformGroup desplazamientoPuente;
	private int sentido=1;
	private float incremento=0f;
	
	public Puente(float radio, float altura, ArrayList<Figura> lo, Juego j){
		juego=j;
                Appearance apariencia = new Appearance();
                Texture tex = new TextureLoader(System.getProperty("user.dir") + "/img/metal.jpeg", juego).getTexture();
                apariencia.setTexture(tex);
                TextureAttributes texAttr = new TextureAttributes();
                texAttr.setTextureMode(TextureAttributes.MODULATE);
                apariencia.setTextureAttributes(texAttr);
		puente=new Cylinder(radio,altura,apariencia);
		puente.setUserData("plataforma_puente");
		puente.setPickable(true);
		int i=0;
		while(puente.getShape(i)!=null){
			PickTool.setCapabilities(puente.getShape(i), PickTool.INTERSECT_FULL);
			puente.getShape(i).setUserData("plataforma_puente");
			puente.getShape(i).setPickable(true);
			i++;
		}
		BranchGroup bgPuente = new BranchGroup();
		desplazamientoPuente = new TransformGroup();
		desplazamientoPuente.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); 
		desplazamientoPuente.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		juego.bgPlataforma.addChild(bgPuente);
		bgPuente.addChild(desplazamientoPuente);
		desplazamientoPuente.addChild(puente);
		lo.add(this);
		identificador=1;
		inicializar(0,4f,6f);
	}
	
	@Override
	public void inicializar(float x, float y, float z) {
		posiciones[0]=x; posiciones[1]=y; posiciones[2]=z; 
		float anguloInicial= 0; 
		matrizPuente.set( new AxisAngle4d(0, 1, 0, anguloInicial) );
	}

	@Override
	public void actualizar(float _dt) {
		dt=_dt;
		Transform3D t3dPuente = new Transform3D();
		desplazamientoPuente.getTransform(t3dPuente);
		if(posiciones[2]<5.5f || posiciones[2]>14.0f)
			sentido=-sentido;
		boolean tecla=juego.personaje.adelante||
				juego.personaje.atras||
				juego.personaje.izquierda||
				juego.personaje.derecha;
		if(tecla)
			incremento=0.010f*dt*sentido;
		else
			incremento=0.0025f*dt*sentido;
		posiciones[2] = posiciones[2]+incremento;
                if(posiciones[2]>10f)
                    posiciones[1] = posiciones[1]-incremento;
		t3dPuente.get(matrizPuente);
	}

	@Override
	public void mostrar() {
            Transform3D inip = new Transform3D( matrizPuente, new Vector3f(posiciones[0], posiciones[1], posiciones[2]),  1f );
            desplazamientoPuente.setTransform(inip);
	}

}
