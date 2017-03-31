package juegonb;

import juegonb.figuras.Esqueleto;
import juegonb.figuras.Puente;
import juegonb.figuras.Figura;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.LineArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.media.j3d.Background;
import javax.media.j3d.TexCoordGeneration;
import juegonb.figuras.Ciervo;
import juegonb.figuras.Suelo;

/**
 * @author Enrique Rios Santos 77452845T
 */
public class Juego extends JFrame implements Runnable {

    static SimpleUniverse universo;
    public Esqueleto personaje;
    public Ciervo ciervo;
    ArrayList<Figura> listaObjetos = new ArrayList<Figura>();
    public PickTool explorador;
    public BranchGroup bgPlataforma;
    boolean camara, camaraCuadrilatero, camaraGlobal;
    float dt;
    public int juegoFinal=0;

    public Juego(float dt) {
        Canvas3D zonaDibujo = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        universo = new SimpleUniverse(zonaDibujo);
        universo.getViewingPlatform().setNominalViewingTransform();
        getContentPane().add(zonaDibujo);
        this.dt = dt;
        camara = false;
        camaraGlobal = false;
        camaraCuadrilatero = false;
        BranchGroup escena = crearEscena();

        escena.compile();
        universo.addBranchGraph(escena);
    }

    private BranchGroup crearEscena() {
        BranchGroup conjunto = new BranchGroup();
        explorador = new PickTool(conjunto);
        explorador.setMode(PickTool.GEOMETRY_INTERSECT_INFO);

        //Personaje
        personaje = new Esqueleto(conjunto, listaObjetos, this);

        //Ciervo
        ciervo = new Ciervo(conjunto, listaObjetos, this);

        //suelo
        BranchGroup bgEscenario = crearEscenario();
        bgEscenario.compile();
        conjunto.addChild(bgEscenario);

        //Luz
        DirectionalLight luz = new DirectionalLight(new Color3f(1.0f, 1.0f, 1.0f),
                new Vector3f(-0.05f, -0.9f, -2.5f));
        luz.setInfluencingBounds(new BoundingSphere(new Point3d(0.0d, 0.0d, 0.0d), 100.0d));
        DirectionalLight luz2 = new DirectionalLight(new Color3f(1.0f, 1.0f, 1.0f),
                new Vector3f(0.5f, 1.9f, 2.5f));
        luz2.setInfluencingBounds(new BoundingSphere(new Point3d(0.0d, 0.0d, 0.0d), 100.0d));
        BranchGroup bgLuz = new BranchGroup();
        bgLuz.addChild(luz);
        bgLuz.addChild(luz2);
        bgLuz.compile();
        conjunto.addChild(bgLuz);

        DeteccionControlPersonaje mueve = new DeteccionControlPersonaje((Esqueleto) listaObjetos.get(0));
        ComportamientoMostrar mostrar = new ComportamientoMostrar(this);
        mueve.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0));
        mostrar.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0));
        conjunto.addChild(mueve);
        conjunto.addChild(mostrar);

        return conjunto;
    }

    void actualizar(float dt) {
        for (int i = 0; i < this.listaObjetos.size(); i++) {
            listaObjetos.get(i).actualizar(dt);
        }
    }

    public void mostrar() {
        for (int i = 0; i < this.listaObjetos.size(); i++) {
            listaObjetos.get(i).mostrar();
        }
        camara();
    }

    public void run() {
        while (juegoFinal==0) {
            actualizar(dt);
            mostrar();
            try {
                Thread.sleep((int) dt * 1000);
            } catch (Exception e) {
            }
        }
        if(juegoFinal==1){
            System.out.println("¡Has Ganado!");
        }else if(juegoFinal==-1){
            System.out.println("¡Has Perdido!");
        }
    }

    private BranchGroup crearEscenario() {
        BranchGroup objRoot = new BranchGroup();
        Box cajaFinal;
        Box cajaFinalG;

        //plataforma
        bgPlataforma = new BranchGroup();
        //rampa
        Appearance apariencia = new Appearance();
        Texture tex = new TextureLoader(System.getProperty("user.dir") + "/img/metal.jpeg", this).getTexture();
        TexCoordGeneration tcg = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR
            ,TexCoordGeneration.TEXTURE_COORDINATE_2);

        apariencia.setTexCoordGeneration(tcg);
        apariencia.setTexture(tex);
        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        apariencia.setTextureAttributes(texAttr);
        Box rampa = new Box(0.3f, 3f, 0.01f, Box.GENERATE_TEXTURE_COORDS, apariencia);
        rampa.setUserData("plataforma_rampa");
        rampa.setPickable(true);
        int i = 0;
        while (rampa.getShape(i) != null) {
            PickTool.setCapabilities(rampa.getShape(i), PickTool.INTERSECT_FULL);
            rampa.getShape(i).setUserData("plataforma_rampa" + i);
            rampa.getShape(i).setPickable(true);
            i++;
        }

        Transform3D moverRampa = new Transform3D();
        Transform3D rotarRampa = new Transform3D();
        Transform3D peraraRampa = new Transform3D();
        rotarRampa.rotX(Math.PI / 4);
        peraraRampa.set(new Vector3f(0, 3f, 0.4f));
        TransformGroup posicionaRampa = new TransformGroup(moverRampa);
        TransformGroup rotaRampa = new TransformGroup(rotarRampa);
        TransformGroup tgRampa = new TransformGroup(peraraRampa);
        bgPlataforma.addChild(posicionaRampa);
        posicionaRampa.addChild(rotaRampa);
        rotaRampa.addChild(tgRampa);
        tgRampa.addChild(rampa);
        //pilar
        Box pilar = new Box(0.5f, 2f, 0.5f, Box.GENERATE_TEXTURE_COORDS, apariencia);
        pilar.setUserData("plataforma_pilar");
        pilar.setPickable(true);
        i = 0;
        while (pilar.getShape(i) != null) {
            PickTool.setCapabilities(pilar.getShape(i), PickTool.INTERSECT_FULL);
            pilar.getShape(i).setUserData("plataforma_pilar" + i);
            pilar.getShape(i).setPickable(true);
            i++;
        }

        //cajaFinal
        apariencia = new Appearance();
        tex = new TextureLoader(System.getProperty("user.dir") + "/img/final.png", this).getTexture();
        apariencia.setTexture(tex);
        texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        apariencia.setTextureAttributes(texAttr);
        cajaFinalG = new Box(1f, 0.1f, 1f, Box.GENERATE_TEXTURE_COORDS, apariencia);
        cajaFinal = new Box(0.45f, 0.08f, 0.45f, Box.GENERATE_TEXTURE_COORDS, apariencia);
        cajaFinal.setUserData("cajaFinal");
        cajaFinal.setPickable(true);
        cajaFinalG.setPickable(false);
        i = 0;
        while (cajaFinal.getShape(i) != null) {
            PickTool.setCapabilities(cajaFinal.getShape(i), PickTool.INTERSECT_FULL);
            cajaFinal.getShape(i).setUserData("cajaFinal" + i);
            cajaFinal.getShape(i).setPickable(true);
            i++;
        }
        
        Transform3D moverPilar = new Transform3D();
        Transform3D moverCajaFinal = new Transform3D();
        moverPilar.set(new Vector3f(0, 2f, 4.95f));
        moverCajaFinal.set(new Vector3f(4.5f, 0, 27f));
        TransformGroup tgPilar = new TransformGroup(moverPilar);
        TransformGroup tgCajaFinal = new TransformGroup(moverCajaFinal);
        bgPlataforma.addChild(tgPilar);
        tgPilar.addChild(pilar);

        //puente
        Puente puente = new Puente(0.5f, 0.05f, listaObjetos, this);

        //fondo
        Background background = new Background(new Color3f(0.15f, 0.73f, 0.85f));
        BoundingSphere sphere = new BoundingSphere(new Point3d(0, 0, 0), 100000);
        background.setApplicationBounds(sphere);
        objRoot.addChild(background);

        Suelo suelo = new Suelo(this);

        //cuadrilatero
        TransformGroup tgCuadrilatero = crearCuadrilatero(texAttr);

        Transform3D moverSuelo = new Transform3D();
        moverSuelo.set(new Vector3f(0f, 0f, 15f));
        TransformGroup tgSuelo = new TransformGroup();
        tgSuelo.setTransform(moverSuelo);

        objRoot.addChild(tgSuelo);
        objRoot.addChild(bgPlataforma);
        objRoot.addChild(tgCuadrilatero);
        Suelo floor = new Suelo(this);
        objRoot.addChild(floor);
        objRoot.addChild(tgCajaFinal);
        tgCajaFinal.addChild(cajaFinalG);
        tgCajaFinal.addChild(cajaFinal);

        return objRoot;
    }

    public void camara() {
        Point3d posicionCamara = null;
        Point3d objetivoCamara = null;
        float xPersonaje = personaje.posiciones[0];
        float yPersonaje = personaje.posiciones[1];
        float zPersonaje = personaje.posiciones[2];

        if (camara) {
            float cercania = 2f;

            posicionCamara = new Point3d(
                    xPersonaje - personaje.direccion.x * cercania,
                    yPersonaje - personaje.direccion.y * cercania + personaje.moverSuelo,
                    zPersonaje - personaje.direccion.z * cercania);

            objetivoCamara = new Point3d(
                    xPersonaje + personaje.direccion.x,
                    yPersonaje + personaje.direccion.y - personaje.moverSuelo / 2,
                    zPersonaje + personaje.direccion.z);
        } else if (camaraCuadrilatero) {
            posicionCamara = new Point3d(8f, 20f, 30f);

            objetivoCamara = new Point3d(
                    ciervo.posiciones[0],
                    ciervo.posiciones[1] + 1.5f,
                    ciervo.posiciones[2]);
        } else if (camaraGlobal) {
            posicionCamara = new Point3d(-8f, 15f, 46f);
            objetivoCamara = new Point3d(0, 2f, 13f);
        } else {
            posicionCamara = new Point3d(-12, 5, 9);

            objetivoCamara = new Point3d(
                    xPersonaje,
                    yPersonaje + personaje.moverSuelo / 2,
                    zPersonaje);
        }
        colocarCamara(universo, posicionCamara, objetivoCamara);
    }

    void colocarCamara(SimpleUniverse universo, Point3d posCamara, Point3d objetivoCamara) {
        Point3d posicionCamara = new Point3d(posCamara.x + 0.001, posCamara.y + 0.001d,
                posCamara.z + 0.001);
        Transform3D datosConfiguracionCamara = new Transform3D();
        datosConfiguracionCamara.lookAt(posicionCamara, objetivoCamara, new Vector3d(0.001, 1.001, 0.001));
        try {
            datosConfiguracionCamara.invert();
            TransformGroup TGcamara = universo.getViewingPlatform().getViewPlatformTransform();
            TGcamara.setTransform(datosConfiguracionCamara);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    BranchGroup crearEjes() {
        BranchGroup grupoEjes = new BranchGroup();
        // crea los ejes:
        LineArray ejeX = new LineArray(2, LineArray.COORDINATES | LineArray.COLOR_3);
        LineArray ejeY = new LineArray(2, LineArray.COORDINATES | LineArray.COLOR_3);
        LineArray ejeZ = new LineArray(2, LineArray.COORDINATES | LineArray.COLOR_3);
        ejeX.setCoordinate(0, new Point3f(-20f, 0.0f, 0.0f));
        ejeX.setCoordinate(1, new Point3f(20f, 0.0f, 0.0f));
        ejeY.setCoordinate(0, new Point3f(0.0f, -20f, 0.0f));
        ejeY.setCoordinate(1, new Point3f(0.0f, 20f, 0.0f));
        ejeZ.setCoordinate(0, new Point3f(0.0f, 0.0f, -20f));
        ejeZ.setCoordinate(1, new Point3f(0.0f, 0.0f, 20f));
        // les pone color
        Color3f rojo = new Color3f(1.0f, 0.0f, 0.0f);
        Color3f verde = new Color3f(0.0f, 1.0f, 0.0f);
        ejeY.setColor(0, rojo);
        ejeY.setColor(1, verde);
        ejeX.setColor(0, rojo);
        ejeX.setColor(1, verde);
        ejeZ.setColor(0, rojo);
        ejeZ.setColor(1, verde);
        grupoEjes.addChild(new Shape3D(ejeX));
        grupoEjes.addChild(new Shape3D(ejeY));
        grupoEjes.addChild(new Shape3D(ejeZ));
        return grupoEjes;
    }

    private TransformGroup crearCuadrilatero(TextureAttributes texAttr) {
        Appearance aparicenciaCuadrilatero = new Appearance();
        Texture tex = new TextureLoader(System.getProperty("user.dir") + "/img/pared.jpg", this).getTexture();
        aparicenciaCuadrilatero.setTexture(tex);
        aparicenciaCuadrilatero.setTextureAttributes(texAttr);
        Box cuadrilatero1 = new Box(5.5f, 1.5f, 0.3f, Box.GENERATE_TEXTURE_COORDS, aparicenciaCuadrilatero);
        Box cuadrilatero2 = new Box(5.5f, 1.5f, 0.3f, Box.GENERATE_TEXTURE_COORDS, aparicenciaCuadrilatero);
        Box cuadrilatero3 = new Box(0.3f, 1.5f, 9f, Box.GENERATE_TEXTURE_COORDS, aparicenciaCuadrilatero);
        Box cuadrilatero4 = new Box(0.3f, 1.5f, 9f, Box.GENERATE_TEXTURE_COORDS, aparicenciaCuadrilatero);

        cuadrilatero1.setUserData("cuadrilatero_1");
        int i = 0;
        while (cuadrilatero1.getShape(i) != null) {
            PickTool.setCapabilities(cuadrilatero1.getShape(i), PickTool.INTERSECT_FULL);
            cuadrilatero1.getShape(i).setUserData("cuadrilatero_1" + i);
            cuadrilatero1.getShape(i).setPickable(true);
            i++;
        }
        cuadrilatero2.setUserData("cuadrilatero_2");
        i = 0;
        while (cuadrilatero2.getShape(i) != null) {
            PickTool.setCapabilities(cuadrilatero2.getShape(i), PickTool.INTERSECT_FULL);
            cuadrilatero2.getShape(i).setUserData("cuadrilatero_2" + i);
            cuadrilatero2.getShape(i).setPickable(true);
            i++;
        }
        cuadrilatero3.setUserData("cuadrilatero_2");
        i = 0;
        while (cuadrilatero3.getShape(i) != null) {
            PickTool.setCapabilities(cuadrilatero3.getShape(i), PickTool.INTERSECT_FULL);
            cuadrilatero3.getShape(i).setUserData("cuadrilatero_3" + i);
            cuadrilatero3.getShape(i).setPickable(true);
            i++;
        }
        cuadrilatero4.setUserData("cuadrilatero_4");
        i = 0;
        while (cuadrilatero4.getShape(i) != null) {
            PickTool.setCapabilities(cuadrilatero4.getShape(i), PickTool.INTERSECT_FULL);
            cuadrilatero4.getShape(i).setUserData("cuadrilatero_4" + i);
            cuadrilatero4.getShape(i).setPickable(true);
            i++;
        }

        Transform3D mover1 = new Transform3D();
        Transform3D mover2 = new Transform3D();
        Transform3D mover3 = new Transform3D();
        Transform3D mover4 = new Transform3D();
        mover1.set(new Vector3f(0, 1.5f / 2, 10f));
        mover2.set(new Vector3f(0, 1.5f / 2, 28f));
        mover3.set(new Vector3f(5.5f, 1.5f / 2, 19f));
        mover4.set(new Vector3f(-5.5f, 1.5f / 2, 19f));
        TransformGroup tgCuadrilatero = new TransformGroup();
        TransformGroup tgCua1 = new TransformGroup();
        TransformGroup tgCua2 = new TransformGroup();
        TransformGroup tgCua3 = new TransformGroup();
        TransformGroup tgCua4 = new TransformGroup();
        tgCua1.setTransform(mover1);
        tgCua2.setTransform(mover2);
        tgCua3.setTransform(mover3);
        tgCua4.setTransform(mover4);
        tgCuadrilatero.addChild(tgCua1);
        tgCuadrilatero.addChild(tgCua2);
        tgCuadrilatero.addChild(tgCua3);
        tgCuadrilatero.addChild(tgCua4);
        tgCua1.addChild(cuadrilatero1);
        tgCua2.addChild(cuadrilatero2);
        tgCua3.addChild(cuadrilatero3);
        tgCua4.addChild(cuadrilatero4);

        return tgCuadrilatero;
    }

    public static void main(String args[]) {
        //Establecer Velocidad
        //menor -> mas lento ; mayor->mas rapido
        float dt = 0.008f;
        Juego x = new Juego(dt);
        x.setSize(800, 600);
        x.setVisible(true);
        x.colocarCamara(x.universo, new Point3d(-12, 3, 9), new Point3d(0, x.personaje.moverSuelo / 2, 0));
        x.run();
    }
}