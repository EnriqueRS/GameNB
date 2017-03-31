/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package juegonb.figuras;

import com.sun.j3d.utils.image.TextureLoader;
import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;
import juegonb.Juego;

/**
 *
 * @author Enrique Rios Santos
 */
public class Suelo extends BranchGroup {

    private final Appearance app = new Appearance();
    private final TransformGroup tg = new TransformGroup();
    private TriangleStripArray tsa = null;
    private Juego juego;

   
    public Suelo(Juego juego) {
        this.juego=juego;
        this.setCapability(BranchGroup.ALLOW_DETACH);

        tsa = new TriangleStripArray(4,
                TriangleStripArray.COORDINATES
                | TriangleStripArray.TEXTURE_COORDINATE_2, new int[]{4});
        tsa.setCoordinate(0, new Point3d(15d, 0d, -8d));
        tsa.setCoordinate(1, new Point3d(-15d, 0d, -8d));
        tsa.setCoordinate(2, new Point3d(15d, 0d, 30d));
        tsa.setCoordinate(3, new Point3d(-15d, 0d, 30d));
        tsa.setTextureCoordinate(0, 0, new TexCoord2f(0.0f, 0.0f));
        tsa.setTextureCoordinate(0, 1, new TexCoord2f(1.0f, 0.0f));
        tsa.setTextureCoordinate(0, 2, new TexCoord2f(0.0f, 1.0f));
        tsa.setTextureCoordinate(0, 3, new TexCoord2f(1.0f, 1.0f));

        ColoringAttributes coloringAttributes = new ColoringAttributes();
        coloringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        app.setColoringAttributes(coloringAttributes);
        app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        Shape3D s3d =new Shape3D(tsa, app);
        s3d.setUserData("suelo");
        tg.addChild(s3d);
        setTexture();
        this.addChild(tg);
    }

    public void setScale(double escale) {
        Transform3D t3d = new Transform3D();
        tg.getTransform(t3d);
        t3d.setScale(escale);
        tg.setTransform(t3d);

        /* Change texture coordinates so that the image
         * will appear with the same size when the scale
         * changes.
         */
        tsa.setTextureCoordinate(0, 0, new TexCoord2f(
                0.0f,
                0.0f));
        tsa.setTextureCoordinate(0, 1, new TexCoord2f(
                (float) (1.0f * escale),
                0.0f));
        tsa.setTextureCoordinate(0, 2, new TexCoord2f(
                0.0f,
                (float) (1.0f * escale)));
        tsa.setTextureCoordinate(0, 3, new TexCoord2f(
                (float) (1.0f * escale),
                (float) (1.0f * escale)));
    }

    public void setTexture() {
        TextureLoader tl =new TextureLoader(System.getProperty("user.dir") + "/img/suelo.jpg", juego);
        Texture texture = tl.getTexture();
        texture.setBoundaryModeS(Texture.WRAP);
        texture.setBoundaryModeT(Texture.WRAP);
        app.setTexture(texture);
    }
}
