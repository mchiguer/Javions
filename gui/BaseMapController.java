package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.io.IOException;


/**
 * Cette classe publique et finale, gère l'affichage et l'interaction avec le fond de carte
 *
 * @author Marwa Chiguer (325221)
 * @author Imane Oujja (344332)
 */
public final class BaseMapController {

    public static final int PIXELS = 256;
    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private final Pane pane;
    private final Canvas canvas;
    private boolean redrawNeeded;
    private double lastMouseX, lastMouseY;
    private boolean isDragging;


    /**
     * Le constructeur public de BaseMapController
     *
     * @param tileManager le gestionnaire de tuiles à utiliser pour obtenir les tuiles de la carte
     * @param parameters  les paramètres de la portion visible de la carte
     */
    public BaseMapController(TileManager tileManager, MapParameters parameters) {
        this.mapParameters = parameters;
        this.tileManager = tileManager;
        canvas = new Canvas();
        pane = new Pane(canvas);
        redrawNeeded = true;
        isDragging = false;
        bindings();
        listeners();
        events();
    }


    /**
     * Cette méthode public qui permet d'accéder à un objet de type Pane
     *
     * @return un objet de type Pane
     */
    public Pane pane() {
        return pane;
    }


    /**
     * Cette méthode public déplace la portion visible de la carte afin qu'elle soit centrée en ce point
     *
     * @param point un point à la surface de la Terre
     */
    public void centerOn(GeoPos point) {
        double xActuel = WebMercator.x(mapParameters.getZoom(), point.longitude());
        double yActuel = WebMercator.y(mapParameters.getZoom(), point.latitude());
        double xCenter = (xActuel - canvas.getWidth()) / 2 - mapParameters.getMinX();
        double yCenter = (yActuel - canvas.getHeight() / 2) - mapParameters.getMinY();
        mapParameters.scroll(xCenter, yCenter);
    }


    // Cette méthode privée effectue des liaisons (bindings)
    private void bindings() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
    }


    //Cette méthode privée configure des listeners
    private void listeners() {
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        canvas.widthProperty().addListener((observable, oldValue, newValue) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> redrawOnNextPulse());
    }


    //Gestion des évènements
    private void events() {

        //changement du niveau de zoom
        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            mapParameters.scroll(-e.getX(), -e.getY());
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(e.getX(), e.getY());
            redrawOnNextPulse();
            e.consume();
        });

        //glissement de la carte
        canvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            isDragging = true;
        });

        //glissement de la carte
        canvas.setOnMouseDragged(e -> {
            if (isDragging) {
                double deltaX = e.getX() - lastMouseX;
                double deltaY = e.getY() - lastMouseY;
                mapParameters.scroll(deltaX, deltaY);
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                redrawOnNextPulse();
            }
        });

        //glissement de la carte
        canvas.setOnMouseReleased(e -> isDragging = false);
    }


    // Cette méthode privée effectue le redessin de l'objet canvas si un nouveau dessin est nécessaire
    private void redrawIfNeeded() {
        // Si aucun redessin n'est nécessaire, on quitte la méthode
        if (!redrawNeeded) return;
        // Obtient le contexte graphique du canvas
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        // Indique que le redessin a été effectué
        redrawNeeded = false;
        // Efface le contenu précédent du canvas
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Calcule les coordonnées de départ ett de fin pour chaque coordonnée
        int startX = (int) Math.floor(mapParameters.getMinX() / PIXELS);
        int startY = (int) Math.floor(mapParameters.getMinY() / PIXELS);
        double endX = (canvas.getWidth() + mapParameters.getMinX()) / PIXELS;
        double endY = (canvas.getHeight() + mapParameters.getMinY()) / PIXELS;

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                if (TileManager.TileId.isValid(mapParameters.getZoom(), x, y)) {
                    TileManager.TileId tileId = new TileManager.TileId(mapParameters.getZoom(), x, y);
                    try {
                        // Obtient l'image de tuile correspondante
                        Image tileImage = tileManager.imageForTileAt(tileId);
                        double drawX = x * PIXELS - mapParameters.getMinX();
                        double drawY = (y * PIXELS - mapParameters.getMinY());
                        graphicsContext.drawImage(tileImage, drawX, drawY);
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    // Marque le besoin de redessiner et demande le prochain pulse à la plateforme
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}

