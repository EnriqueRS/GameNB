package juegonb.figuras;
/**
 * @author Enrique Rios Santos
 * 77452845T
 */
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import java.util.ArrayList;
import javax.media.j3d.Shape3D;
import juegonb.Juego;

public class Esqueleto extends Figura{

    public float[] posiciones = new float[3];
    public boolean adelante, atras, izquierda, derecha;
    public TransformGroup desplazamientoFigura = new TransformGroup();
    public int identificador;
    ArrayList<Figura> listaObjetos;
    float longBrazo = 0.3f;
    float longAnteBrazo = 0.3f;
    float longTorso = 0.5f;
    float altoHorquilla = 0.3f;
    float radioRueda = 0.15f;
    float radioCabeza = 0.2f;
    float altoCuello = 0.15f;
    public float moverSuelo = (longTorso / 2) + altoHorquilla + radioRueda;
    float altCabeza = moverSuelo + radioCabeza;
    Matrix3f matrizRotacionPersonaje = new Matrix3f();
    private int sentido = -1;
    private float anguloB = (float) Math.PI;
    private Transform3D rota = new Transform3D();
    private Transform3D preparaRotable = new Transform3D();
    TransformGroup tgEsqueleto;
    TransformGroup tgBrazoI;
    TransformGroup tgAnteBrazoI;
    TransformGroup tgBrazoD;
    TransformGroup tgAnteBrazoD;
    TransformGroup tgRueda;
    Transform3D transformaBrazoD;
    Transform3D transformaBrazoI;
    Cylinder torso;
    public Juego juego;
    boolean sobrePuente = false;
    public boolean estaCuadrilatero, suelo, colisionCiervo;
    public Vector3d direccion;
    private boolean colision = false;
    public Shape3D torsoShape;

    public Esqueleto(BranchGroup conjunto, ArrayList<Figura> _listaObjetos, Juego j) {
        conjunto.addChild(desplazamientoFigura);
        desplazamientoFigura.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        desplazamientoFigura.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        //Escalado
        Transform3D tEscala = new Transform3D();
        tEscala.setScale(0.5f);
        TransformGroup tgEscalado = new TransformGroup(tEscala);
        BranchGroup bgEscalado = new BranchGroup();
        bgEscalado.addChild(tgEscalado);
        desplazamientoFigura.addChild(bgEscalado);
        listaObjetos = _listaObjetos;
        listaObjetos.add(this);
        juego = j;
        identificador = listaObjetos.size() - 1;
        bgEscalado.addChild(crearEsqueleto());
        inicializar(0, 0.7f, -2f);
        transformaBrazoD = new Transform3D();
        transformaBrazoI = new Transform3D();
    }

    public void inicializar(float x, float y, float z) {
        //Dando valor inicial a las posiciones y �ngulo en 0 (tambiencomo parametro) 
        posiciones[0] = x;
        posiciones[1] = y;
        posiciones[2] = z;
        direccion = new Vector3d(x, y - 1.9f, z + 0.8f);
        float anguloInicial = 0;
        matrizRotacionPersonaje.set(new AxisAngle4d(0, 1, 0, anguloInicial));
    }

    public void actualizar(float _dt){
        //Se consulta el Transform3D actual y una copia porque se necesitarán para varias operaciones de actualización
        dt = _dt;
        boolean tecla = derecha || izquierda || adelante || atras;
        if (derecha || izquierda || adelante || atras) {
            camina();
        }

        if (sobrePuente && !tecla) {
            float y = ((Puente) listaObjetos.get(2)).posiciones[1];
            float z = ((Puente) listaObjetos.get(2)).posiciones[2];
            posiciones[1] = y+moverSuelo;
            posiciones[2] = z;
        }
    }

    public void mostrar() {
        //Se crea un transform3D con posiciones y matriz de rotacion actualizadas. 
        //Se usa para la transformacion (es decir presentaci�n)
        Transform3D inip = new Transform3D(matrizRotacionPersonaje,
                new Vector3f(posiciones[0], posiciones[1], posiciones[2]), 1f);
        desplazamientoFigura.setTransform(inip);
    }

    private void camina() {
        //corregir suelo
        float[] posicionesAnt = posiciones;
        Transform3D t3dPersonaje = new Transform3D();
        desplazamientoFigura.getTransform(t3dPersonaje);
        Transform3D copiat3dPersonaje = new Transform3D(t3dPersonaje);

        float deltaVel = 0;
        float deltaAngulo = 0;
        if (derecha) {deltaAngulo = -0.015f * dt;}
        if (izquierda) {deltaAngulo = 0.015f * dt;}
        if (adelante) {deltaVel = 0.02f * dt;}
        if (atras) {deltaVel = -0.02f * dt;}

        float subirBajarSuelo = controlarAlturaSuelo(t3dPersonaje, juego.explorador, moverSuelo);
        boolean salto = false;
        float correccionX = 0.0f;
        float correccionZ = 0.0f;

        Transform3D t3dNueva = new Transform3D();
        if (colision) {
            posiciones[0] = posicionesAnt[0] - (((float) direccion.x)*0.02f);
            posiciones[2] = posicionesAnt[2] - (((float) direccion.z)*0.02f);
            colision = false;
        } else {
            if (-subirBajarSuelo > 0.65f) {
                salto = true;
                posiciones[0] = (float) (posiciones[0] + 0.55f * direccion.x);
                posiciones[2] = (float) (posiciones[2] + 0.55f * direccion.z);
                correccionX = posiciones[0];
                correccionZ = posiciones[2];
                while (posiciones[1] > 0.7f) {
                    t3dNueva = new Transform3D();
                    t3dNueva.setRotation(new AxisAngle4f(0, 1f, 0, -0.01f));
                    t3dPersonaje.mul(t3dNueva);
                    posiciones[1] = posiciones[1] - 0.003f * dt;
                    t3dNueva.set(new Vector3d(posiciones[0], posiciones[1], posiciones[2]));
                    t3dPersonaje.get(matrizRotacionPersonaje,
                            new Vector3d(posiciones[0], posiciones[1], posiciones[2]));
                    mostrar();
                    juego.camara();
                }
                t3dNueva = new Transform3D();
                t3dNueva.set(new Vector3d(0.0d, subirBajarSuelo+0.2f, deltaVel));
            }else{
                t3dNueva.set(new Vector3d(0.0d, subirBajarSuelo, deltaVel));
            }

            rotaBrazos();
           
            t3dNueva.setRotation(new AxisAngle4f(0, 1f, 0, deltaAngulo));
            t3dPersonaje.mul(t3dNueva);
            //Se actualiza la posicion del personaje y de la matriz de rotaci�n
            Vector3d posPersonaje = new Vector3d(0, 0, 0);
            t3dPersonaje.get(matrizRotacionPersonaje, posPersonaje);
            //System.out.println(posPersonaje);
            posiciones[1] = (float) posPersonaje.y;
            if (salto) {
                posiciones[0] = correccionX;
                posiciones[2] = correccionZ;
            } else{
                posiciones[0] = (float) posPersonaje.x;
                posiciones[2] = (float) posPersonaje.z;
                if(posPersonaje.y<moverSuelo)
                    posiciones[1] = moverSuelo;
            }
            if((posiciones[0]<5.5 && posiciones[0]>-5.5)
                &&(posiciones[2]<28 && posiciones[2]>10)){
                estaCuadrilatero=true;
            }else{
                estaCuadrilatero=false;
            }
        }

        //SONAR:   se lanza desde el centro del personaje con direccion Sonar.
        Vector3d posSonar = new Vector3d(0, 0, 0);
        Point3d posActual = new Point3d(posiciones[0], posiciones[1]+0.2f, posiciones[2]);
        Transform3D t3dSonar = new Transform3D(matrizRotacionPersonaje, new Vector3f(0.0f, subirBajarSuelo, deltaVel + 1f), 1f);
        copiat3dPersonaje.mul(t3dSonar);
        copiat3dPersonaje.get(posSonar);
        direccion = new Vector3d(posSonar.x - posiciones[0], posSonar.y - posiciones[1], posSonar.z - posiciones[2]);
        //juego.exploradorH.setShapeCylinderRay(posActual, direccion, 0.15d);
        juego.explorador.setShapeRay(posActual, direccion);
        PickResult objMasCercano = juego.explorador.pickClosest();
        float distancia=0f;
        //PickResult[] lista;
        //lista = juego.explorador.pickAllSorted();
        if(objMasCercano!=null)
            distancia = (float) objMasCercano.getClosestIntersection(posActual).getDistance();
        if (objMasCercano != null && objMasCercano.getObject().getUserData() != null
                && (((String) objMasCercano.getObject().getUserData()).contains("plataforma_pilar")
                || ((String) objMasCercano.getObject().getUserData()).contains("rampa0")
                || ((String) objMasCercano.getObject().getUserData()).contains("cajaFinal")
                || ((String) objMasCercano.getObject().getUserData()).contains("cuadrilatero"))) {
           
            if (distancia < 1f && ((String) objMasCercano.getObject().getUserData()).contains("rampa0")) {
                //System.out.println("colision con " + objMasCercano.getObject().getUserData());
                colision = true;
            }else if(distancia < 0.35f/* && ((String) objMasCercano.getObject().getUserData()).contains("plataforma_pilar")*/){
                //System.out.println("colision con " + objMasCercano.getObject().getUserData());
                colision = true;
            }
        }else if(objMasCercano != null 
                && objMasCercano.getObject().getUserData() == null
                && distancia<0.5f){
            colision = true;
            colisionCiervo=true;
        }
    }

    float controlarAlturaSuelo(Transform3D t3dPersonaje, PickTool localizador, float objAlSuelo) {
        //System.out.println(posicionActual);
        float subirBajarPersonaje = 0;
        PickResult objMasCercano;
        Point3d posActual = new Point3d(posiciones[0], posiciones[1], posiciones[2]);
        
        localizador.setShapeRay(posActual, new Vector3d(posActual.x, -20, posActual.z));
        //localizador.setShapeCylinderRay(posActual, new Vector3d(posActual.x,-20,posActual.z),0.05d);
        objMasCercano = localizador.pickClosest();
        if (objMasCercano != null) {
            if (objMasCercano.getObject().getUserData() != null
                    && (((String) objMasCercano.getObject().getUserData()).contains("suelo")
                    || (((String) objMasCercano.getObject().getUserData()).contains("puente")
                    || (((String) objMasCercano.getObject().getUserData()).contains("rampa1")
                    || (((String) objMasCercano.getObject().getUserData()).contains("cajaFinal")
                    || (((String) objMasCercano.getObject().getUserData()).contains("pilar4"))))))) {
                //System.out.println("abajo "+objMasCercano.getObject().getUserData());
                if(((String) objMasCercano.getObject().getUserData()).contains("suelo"))
                    suelo=true;
                else
                    suelo=false;
                if(((String) objMasCercano.getObject().getUserData()).contains("cajaFinal")){
                    juego.juegoFinal=1;//contadorFinal++;//System.out.println(contadorFinal);
                }else if(!((String) objMasCercano.getObject().getUserData()).contains("puente")) {
                    sobrePuente = false;
                    float distanciaSuelo = (float) objMasCercano.getClosestIntersection(posActual).getDistance();
                    subirBajarPersonaje = objAlSuelo - distanciaSuelo;
                }else
                    sobrePuente = true;
            }
        }
        return subirBajarPersonaje;
    }

    private void rotaBrazos() {
        if ((!derecha && !izquierda && !adelante && !atras)
                && (!(anguloB > Math.PI - 0.05 && anguloB < Math.PI + 0.05))) {
            anguloB = anguloB + (float) (Math.PI / 1700d) * -sentido;
        } else {
            anguloB = anguloB + (float) (Math.PI / 1700d) * sentido;
        }

        if (anguloB > 2 * Math.PI / 3 - 0.05 && anguloB < 2 * Math.PI / 3 + 0.05) {
            sentido = -sentido;
        } else if (anguloB > (5 * Math.PI) / 4 - 0.05 && anguloB < (5 * Math.PI) / 4 + 0.05) {
            sentido = -sentido;
        }

        Transform3D transformaBrazo = new Transform3D();
        rota.set(new AxisAngle4f(1f, 0f, 0f, (float) anguloB));
        preparaRotable.set(new Vector3f(0f, longBrazo / 2f, 0f));
        transformaBrazo.mul(rota);
        transformaBrazo.mul(preparaRotable);
        tgBrazoD.setTransform(transformaBrazo);

        transformaBrazo = new Transform3D();
        rota.set(new AxisAngle4f(1f, 0f, 0f, (float) -anguloB));
        transformaBrazo.mul(rota);
        transformaBrazo.mul(preparaRotable);
        tgBrazoI.setTransform(transformaBrazo);
    }

    private BranchGroup crearEsqueleto() {
        //Crear brazos | 1 = izquierda; -1 = derecha
        BranchGroup escenaBD = crearBrazo(Math.PI, Math.PI / 2, -1);
        BranchGroup escenaBI = crearBrazo(Math.PI, Math.PI / 2, 1);

        //Crear rueda
        BranchGroup escenaR = crearRueda();

        //Crear cuerpo
        BranchGroup escenaCuerpo = crearCuerpo();
        //Crear cabeza
        BranchGroup escenaCabeza = crearCabeza();

        tgEsqueleto = new TransformGroup();
        tgEsqueleto.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        BranchGroup objRoot = new BranchGroup();
        BranchGroup esqueleto = new BranchGroup();

        Transform3D moverASuelo = new Transform3D();
        moverASuelo.set(new Vector3f(0, (longTorso / 2) + altoHorquilla + radioRueda, 0));
        TransformGroup tgMoverASuelo = new TransformGroup();
        //tgMoverASuelo.setTransform(moverASuelo);
        BranchGroup bgEsqueleto = new BranchGroup();

        objRoot.addChild(tgMoverASuelo);
        tgMoverASuelo.addChild(bgEsqueleto);
        bgEsqueleto.addChild(tgEsqueleto);
        tgEsqueleto.addChild(esqueleto);
        esqueleto.addChild(escenaBI);
        esqueleto.addChild(escenaBD);
        esqueleto.addChild(escenaR);
        esqueleto.addChild(escenaCabeza);
        esqueleto.addChild(escenaCuerpo);


        return objRoot;
    }

    private BranchGroup crearCabeza() {
        BranchGroup objRoot = new BranchGroup();

        Sphere cabeza = new Sphere(radioCabeza, Primitive.GENERATE_NORMALS, 50);
        int i = 0;
        while (cabeza.getShape(i) != null) {
            cabeza.getShape(i).setUserData("figura_" + identificador + "_cabeza");
            PickTool.setCapabilities(cabeza.getShape(i), PickTool.INTERSECT_FULL);
            cabeza.getShape(i).setPickable(false);
            i++;
        }
        Sphere ojoI = new Sphere(0.02f, Primitive.GENERATE_NORMALS, 50);
        i = 0;
        while (ojoI.getShape(i) != null) {
            ojoI.getShape(i).setUserData("figura_" + identificador + "_ojoI");
            PickTool.setCapabilities(ojoI.getShape(i), PickTool.INTERSECT_FULL);
            ojoI.getShape(i).setPickable(false);
            i++;
        }
        Sphere ojoD = new Sphere(0.02f, Primitive.GENERATE_NORMALS, 50);
        i = 0;
        while (ojoD.getShape(i) != null) {
            ojoD.getShape(i).setUserData("figura_" + identificador + "_ojoD");
            PickTool.setCapabilities(ojoD.getShape(i), PickTool.INTERSECT_FULL);
            ojoD.getShape(i).setPickable(false);
            i++;
        }

        //Translado
        Transform3D desplazamiento = new Transform3D();
        desplazamiento.set(new Vector3f(0, 0.52f, 0));
        TransformGroup tgCabezaTrasladada = new TransformGroup(desplazamiento);

        Transform3D desplazamientoOjoD = new Transform3D();
        desplazamientoOjoD.set(new Vector3f(-0.1f, 0, 0.19f));
        TransformGroup tgOjoTrasladadoD = new TransformGroup(desplazamientoOjoD);

        Transform3D desplazamientoOjoI = new Transform3D();
        desplazamientoOjoI.set(new Vector3f(0.1f, 0, 0.19f));
        TransformGroup tgOjoTrasladadoI = new TransformGroup(desplazamientoOjoI);

        BranchGroup bgCara = new BranchGroup();
        //Estructura
        objRoot.addChild(tgCabezaTrasladada);
        tgCabezaTrasladada.addChild(bgCara);
        bgCara.addChild(cabeza);
        bgCara.addChild(tgOjoTrasladadoD);
        tgOjoTrasladadoD.addChild(ojoD);
        bgCara.addChild(tgOjoTrasladadoI);
        tgOjoTrasladadoI.addChild(ojoI);

        return objRoot;
    }

    private BranchGroup crearRueda() {
        BranchGroup objRoot = new BranchGroup();

        //Objeto
        Cylinder horquillaI = new Cylinder(0.02f, altoHorquilla);
        int i = 0;
        while (horquillaI.getShape(i) != null) {
            horquillaI.getShape(i).setUserData("figura_" + identificador + "_horquillaI");
            PickTool.setCapabilities(horquillaI.getShape(i), PickTool.INTERSECT_FULL);
            horquillaI.getShape(i).setPickable(false);
            i++;
        }

        Cylinder horquillaD = new Cylinder(0.02f, altoHorquilla);
        i = 0;
        while (horquillaD.getShape(i) != null) {
            horquillaD.getShape(i).setUserData("figura_" + identificador + "_horquillaD");
            PickTool.setCapabilities(horquillaD.getShape(i), PickTool.INTERSECT_FULL);
            horquillaD.getShape(i).setPickable(false);
            i++;
        }

        Cylinder soporte = new Cylinder(0.01f, altoHorquilla / 2);
        i = 0;
        while (soporte.getShape(i) != null) {
            soporte.getShape(i).setUserData("figura_" + identificador + "_soporte");
            PickTool.setCapabilities(soporte.getShape(i), PickTool.INTERSECT_FULL);
            soporte.getShape(i).setPickable(false);
            i++;
        }

        Sphere bolaI = new Sphere(0.02f, Primitive.GENERATE_NORMALS, 50);
        bolaI.getShape(0).setUserData("figura_" + identificador + "_bolaI");
        PickTool.setCapabilities(bolaI.getShape(0), PickTool.INTERSECT_FULL);
        bolaI.getShape(0).setPickable(false);

        Sphere bolaD = new Sphere(0.02f, Primitive.GENERATE_NORMALS, 50);
        bolaD.getShape(0).setUserData("figura_" + identificador + "_bolaD");
        PickTool.setCapabilities(bolaD.getShape(0), PickTool.INTERSECT_FULL);
        bolaD.getShape(0).setPickable(false);


        Cylinder rueda = new Cylinder(radioRueda, 0.06f);
        i = 0;
        while (rueda.getShape(i) != null) {
            rueda.getShape(i).setUserData("figura_" + identificador + "_rueda");
            PickTool.setCapabilities(rueda.getShape(i), PickTool.INTERSECT_FULL);
            rueda.getShape(i).setPickable(false);
            i++;
        }

        //Rotacion 
        Transform3D tSoporte = new Transform3D();
        Transform3D rota = new Transform3D();
        //Configuraciony creaci�n de TransformGroups
        tSoporte.set(new Vector3f(0f, -(altoHorquilla + longTorso / 2), 0f));
        rota.rotZ(-Math.PI / 2d);
        tSoporte.mul(rota);
        TransformGroup tgSoporte = new TransformGroup(tSoporte);

        Transform3D tRueda = new Transform3D();
        //Configuraciony creaci�n de TransformGroups
        tRueda.set(new Vector3f(0f, -(altoHorquilla + longTorso / 2), 0f));
        rota.rotZ(-Math.PI / 2d);
        tRueda.mul(rota);
        TransformGroup tgRueda = new TransformGroup(tRueda);
        //tgRueda.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);


        //Traslado	
        int sentido = 1;
        Transform3D desplazamientoHI = new Transform3D();
        desplazamientoHI.set(new Vector3f(0.08f * sentido, -(longTorso + altoHorquilla) / 2, 0));
        sentido = -1;
        Transform3D desplazamientoHD = new Transform3D();
        desplazamientoHD.set(new Vector3f(0.08f * sentido, -(longTorso + altoHorquilla) / 2, 0));
        TransformGroup tgDesplazamientoHI = new TransformGroup(desplazamientoHI);
        TransformGroup tgDesplazamientoHD = new TransformGroup(desplazamientoHD);

        Transform3D desplazamientoB = new Transform3D();
        desplazamientoB.set(new Vector3f(0, -(altoHorquilla / 2), 0));
        TransformGroup tgDesplazamientoBD = new TransformGroup(desplazamientoB);
        TransformGroup tgDesplazamientoBI = new TransformGroup(desplazamientoB);

        //Estructura
        objRoot.addChild(tgDesplazamientoHI);
        objRoot.addChild(tgDesplazamientoHD);
        tgDesplazamientoHI.addChild(horquillaI);
        tgDesplazamientoHD.addChild(horquillaD);
        tgDesplazamientoHI.addChild(tgDesplazamientoBI);
        tgDesplazamientoHD.addChild(tgDesplazamientoBD);
        tgDesplazamientoBI.addChild(bolaI);
        tgDesplazamientoBD.addChild(bolaD);
        objRoot.addChild(tgSoporte);
        tgSoporte.addChild(soporte);
        tgSoporte.addChild(rueda);

        return objRoot;
    }

    private BranchGroup crearBrazo(double beta, double alfa, int lado) {
        BranchGroup objRoot = new BranchGroup();

        //Objeto
        //Cilindro 1
        Cylinder brazo = new Cylinder(0.05f, longBrazo);

        //Cilindro 2
        Cylinder anteBrazo = new Cylinder(0.05f, longAnteBrazo);

        //Articulacion
        Sphere codo = new Sphere(0.08f, Primitive.GENERATE_NORMALS, 50);

        //Rotacion Brazo
        Transform3D transformaBrazo = new Transform3D();
        Transform3D rotaBeta = new Transform3D();
        Transform3D preparaRotable = new Transform3D();

        //Configuraciony creaci�n de TransformGroups
        rotaBeta.rotX(lado * -beta);

        //rotaBeta.set(new AxisAngle4f(0f, 1f, 0f, (float) beta));
        preparaRotable.set(new Vector3f(0f, longBrazo / 2f, 0f));
        transformaBrazo.mul(rotaBeta);
        transformaBrazo.mul(preparaRotable);
        if (lado == -1) {
            tgBrazoD = new TransformGroup(transformaBrazo);
            tgBrazoD.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        } else {
            tgBrazoI = new TransformGroup(transformaBrazo);
            tgBrazoI.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        }


        //Rotacion AnteBrazo
        Transform3D transformaAnteBrazo = new Transform3D();
        Transform3D rotaAlfa = new Transform3D();
        //Transform3D preparaRotable=new Transform3D(); 

        //Configuraciony creaci�n de TransformGroups
        rotaAlfa.rotX(-alfa);
        preparaRotable.set(new Vector3f(0f, longAnteBrazo / 2f, 0f));
        transformaAnteBrazo.mul(rotaAlfa);
        transformaAnteBrazo.mul(preparaRotable);
        TransformGroup tgAnteBrazoD = null;
        TransformGroup tgAnteBrazoI = null;
        if (lado == -1) {
            tgAnteBrazoD = new TransformGroup(transformaAnteBrazo);
            tgAnteBrazoD.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        } else {
            tgAnteBrazoI = new TransformGroup(transformaAnteBrazo);
            tgAnteBrazoI.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        }


        //Traslado
        Transform3D desplazamientoBrazo = new Transform3D();
        desplazamientoBrazo.set(new Vector3f(lado * 0.17f, 0.25f, 0));
        Transform3D rota = new Transform3D();
        rota.rotZ(lado * Math.PI / 9);
        desplazamientoBrazo.mul(rota);
        TransformGroup tgTrasladaBrazo = new TransformGroup(desplazamientoBrazo);

        Transform3D desplazamientoCodo = new Transform3D();
        desplazamientoCodo.set(new Vector3f(0, longBrazo / 2f, 0));
        TransformGroup tgTrasladoCodo = new TransformGroup(desplazamientoCodo);

        //Estructura		
        objRoot.addChild(tgTrasladaBrazo);
        tgTrasladoCodo.addChild(codo);

        if (lado == -1) {
            tgTrasladaBrazo.addChild(tgBrazoD);
            tgBrazoD.addChild(brazo);
            tgBrazoD.addChild(tgTrasladoCodo);
            tgTrasladoCodo.addChild(tgAnteBrazoD);
            tgAnteBrazoD.addChild(anteBrazo);
        } else {
            tgTrasladaBrazo.addChild(tgBrazoI);
            tgBrazoI.addChild(brazo);
            tgBrazoI.addChild(tgTrasladoCodo);
            tgTrasladoCodo.addChild(tgAnteBrazoI);
            tgAnteBrazoI.addChild(anteBrazo);
        }

        return objRoot;
    }

    private BranchGroup crearCuerpo() {
        BranchGroup objRoot = new BranchGroup();

        //Objeto
        torso = new Cylinder(0.15f, longTorso);
        torsoShape=torso.getShape(0);
        torso.getShape(0).setUserData("figura_" + identificador);
        PickTool.setCapabilities(torso.getShape(0), PickTool.INTERSECT_FULL);
        torso.getShape(0).setPickable(false);
        torso.getShape(1).setUserData("figura_" + identificador);
        PickTool.setCapabilities(torso.getShape(1), PickTool.INTERSECT_FULL);
        torso.getShape(1).setPickable(false);
        torso.getShape(2).setUserData("figura_" + identificador);
        PickTool.setCapabilities(torso.getShape(2), PickTool.INTERSECT_FULL);
        torso.getShape(2).setPickable(false);

        Sphere hombroD = new Sphere(0.08f);
        hombroD.getShape(0).setUserData("figura_" + identificador);
        PickTool.setCapabilities(hombroD.getShape(0), PickTool.INTERSECT_FULL);
        hombroD.getShape(0).setPickable(false);

        Sphere hombroI = new Sphere(0.08f);
        hombroI.getShape(0).setUserData("figura_" + identificador);
        PickTool.setCapabilities(hombroI.getShape(0), PickTool.INTERSECT_FULL);
        hombroI.getShape(0).setPickable(false);

        Sphere caderaD = new Sphere(0.08f);
        caderaD.getShape(0).setUserData("figura_" + identificador);
        PickTool.setCapabilities(caderaD.getShape(0), PickTool.INTERSECT_FULL);
        caderaD.getShape(0).setPickable(false);

        Sphere caderaI = new Sphere(0.08f);
        caderaI.getShape(0).setUserData("figura_" + identificador);
        PickTool.setCapabilities(caderaI.getShape(0), PickTool.INTERSECT_FULL);
        caderaI.getShape(0).setPickable(false);

        Cone cuello = new Cone(altoCuello, 0.25f);
        cuello.getShape(0).setUserData("figura_" + identificador);
        PickTool.setCapabilities(cuello.getShape(0), PickTool.INTERSECT_FULL);
        cuello.getShape(0).setPickable(false);
        cuello.getShape(1).setUserData("figura_" + identificador);
        PickTool.setCapabilities(cuello.getShape(1), PickTool.INTERSECT_FULL);
        cuello.getShape(1).setPickable(false);

        //Traslado
        Transform3D desplazamientoHombroD = new Transform3D();
        desplazamientoHombroD.set(new Vector3f(-0.15f, 0.27f, 0));
        TransformGroup tgHombroTrasladadoD = new TransformGroup(desplazamientoHombroD);

        Transform3D desplazamientoHombroI = new Transform3D();
        desplazamientoHombroI.set(new Vector3f(0.15f, 0.27f, 0));
        TransformGroup tgHombroTrasladadoI = new TransformGroup(desplazamientoHombroI);

        Transform3D desplazamientoCaderaD = new Transform3D();
        desplazamientoCaderaD.set(new Vector3f(-0.08f, -0.23f, 0));
        TransformGroup tgCaderaTrasladadoD = new TransformGroup(desplazamientoCaderaD);

        Transform3D desplazamientoCaderaI = new Transform3D();
        desplazamientoCaderaI.set(new Vector3f(0.08f, -0.23f, 0));
        TransformGroup tgCaderaTrasladadoI = new TransformGroup(desplazamientoCaderaI);

        Transform3D desplazamientoCuello = new Transform3D();
        desplazamientoCuello.set(new Vector3f(0, 0.37f, 0));
        TransformGroup tgCuelloTrasladado = new TransformGroup(desplazamientoCuello);

        //Estructura
        objRoot.addChild(torso);
        objRoot.addChild(tgCuelloTrasladado);
        tgCuelloTrasladado.addChild(cuello);
        objRoot.addChild(tgHombroTrasladadoD);
        tgHombroTrasladadoD.addChild(hombroD);
        objRoot.addChild(tgHombroTrasladadoI);
        tgHombroTrasladadoI.addChild(hombroI);
        objRoot.addChild(tgCaderaTrasladadoD);
        tgCaderaTrasladadoD.addChild(caderaD);
        objRoot.addChild(tgCaderaTrasladadoI);
        tgCaderaTrasladadoI.addChild(caderaI);

        return objRoot;
    }
}
