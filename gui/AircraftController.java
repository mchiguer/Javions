package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.List;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static javafx.beans.binding.Bindings.createDoubleBinding;
import static javafx.scene.paint.CycleMethod.NO_CYCLE;

/**
 * La classe AircraftController publique et finale, gère la vue des aéronefs.
 * * @author Marwa Chiguer (325221)
 * * @author Imane Oujja (344332)
 */
public final class AircraftController {
    public static final double COLOR_CONSTANT = 12000;
    public static final int START_X = 0;
    public static final int Y = 0;
    public static final int END_X = 1;

    private final MapParameters parameters;
    private final ObservableSet<ObservableAircraftState> aircraftStates;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftState;
    private final Pane aircraftPane;

    /**
     * Le constructeur public prend en arguments :
     *
     * @param parameters            les paramètres de la portion de la carte visible à l'écran.
     * @param aircraftStates        l'ensemble (observable mais non modifiable) des états des aéronefs qui
     *                              doivent apparaître sur la vue.
     * @param selectedAircraftState une propriété JavaFX contenant l'état de l'aéronef sélectionné.
     */
    public AircraftController(MapParameters parameters, ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> selectedAircraftState) {
        this.parameters = parameters;
        this.aircraftStates = aircraftStates;
        this.selectedAircraftState = selectedAircraftState;
        aircraftPane = new Pane();
        aircraftPane.setPickOnBounds(false);
        aircraftPane.getStylesheets().add("aircraft.css");
        observeAircraft();
    }

    /**
     * méthode retournant le panneau JavaFX sur lequel les aéronefs sont affichés.
     *
     * @return aircraftPane
     */
    public Pane pane() {
        return aircraftPane;
    }

    //Observation des aéronefs
    private void observeAircraft() {
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                // Ajouter un nouvel aéronef à la vue des aéronefs
                ObservableAircraftState aircraftState = change.getElementAdded();
                Group aircraftGroup = new Group();
                aircraftGroup.getChildren().addAll(createIconLabelGroup(aircraftState), trajectory(aircraftState));
                aircraftGroup.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
                aircraftPane.getChildren().add(aircraftGroup);

            } else if (change.wasRemoved()) {
                // Supprimer un aéronef de la vue des aéronefs
                ObservableAircraftState aircraftState = change.getElementRemoved();
                String aircraftIcaoAddress = aircraftState.getIcaoAddress().string();
                aircraftPane.getChildren().removeIf(node ->
                        node.getId().equals(aircraftIcaoAddress));
            }
        });
    }


    // Méthode créant le SVGPath de l'icone
    private SVGPath createIcon(ObservableAircraftState aircraftState) {
        AircraftIcon aircraftIcon;

        if (aircraftState.getData() != null) {
            AircraftTypeDesignator type = aircraftState.getData().typeDesignator();
            AircraftDescription description = aircraftState.getData().description();
            int category = aircraftState.getCategory();
            WakeTurbulenceCategory wakeTurbulenceCategory = aircraftState.getData().wakeTurbulenceCategory();
            aircraftIcon = AircraftIcon.iconFor(type, description, category, wakeTurbulenceCategory);
        } else {
            aircraftIcon = AircraftIcon.iconFor(new AircraftTypeDesignator(""), new AircraftDescription(""),
                    aircraftState.getCategory(), WakeTurbulenceCategory.UNKNOWN);
        }
        SVGPath icon = new SVGPath();
        icon.setContent(aircraftIcon.svgPath());
        icon.getStyleClass().add("aircraft");

        // bindings
        icon.contentProperty().bind(new SimpleStringProperty(aircraftIcon.svgPath()));
        icon.rotateProperty().bind(Bindings.createDoubleBinding(() -> (aircraftIcon.canRotate()) ?
                        Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE) : 0d,
                aircraftState.trackOrHeadingProperty()));
        icon.fillProperty().bind(aircraftState.altitudeProperty().map((b) ->
                ColorRamp.PLASMA.at(Math.cbrt(aircraftState.getAltitude() / COLOR_CONSTANT))));

        icon.setOnMouseClicked(e -> selectedAircraftState.set(aircraftState));

        return icon;
    }


    //Méthode créant le groupe de l'étiquette
    private Group createLabel(ObservableAircraftState aircraftState) {
        Text label = new Text();
        Rectangle rectangle = new Rectangle();
        Group labelGroup = new Group(rectangle, label);
        labelGroup.getStyleClass().add("label");

        //bindings
        label.textProperty().bind(Bindings.createStringBinding(() -> {
                    String firstPart = "", secondPart = "";
                    if (aircraftState.getData() != null) {
                        firstPart = aircraftState.getData().registration() != null ?
                                aircraftState.getData().registration().string() :
                                aircraftState.getCallSign() != null ? aircraftState.getCallSign().string() :
                                        aircraftState.getIcaoAddress().string();
                        secondPart = (aircraftState.velocityProperty() != null ?
                                (int) (aircraftState.getVelocity()) : "?") + "  Km/h " +
                                (aircraftState.altitudeProperty() != null ?
                                        (int) (aircraftState.getAltitude()) : "?") + "  m";
                    }
                    return firstPart + "\n" + secondPart;
                }, aircraftState.altitudeProperty(),
                aircraftState.velocityProperty(), aircraftState.callSignProperty()));


        //bindings
        rectangle.widthProperty().bind(createDoubleBinding(() -> label.getLayoutBounds().getWidth() + 4,
                label.layoutBoundsProperty()));
        rectangle.heightProperty().bind(createDoubleBinding(() -> label.getLayoutBounds().getHeight() + 4,
                label.layoutBoundsProperty()));
        labelGroup.visibleProperty().bind(createBooleanBinding(() ->
                        parameters.getZoom() >= 11 || selectedAircraftState.get() == aircraftState,
                parameters.zoomProperty(), selectedAircraftState));

        return labelGroup;
    }


    //Méthode créant le groupe de l'icone et l'étiquette
    private Group createIconLabelGroup(ObservableAircraftState aircraftState) {
        SVGPath icon = createIcon(aircraftState);
        Group label = createLabel(aircraftState);
        Group group = new Group(icon, label);

        //bindings
        DoubleBinding positionX = createDoubleBinding(() ->
                        WebMercator.x(parameters.getZoom(), aircraftState.getPosition().longitude()) - parameters.getMinX(),
                parameters.zoomProperty(), parameters.minX(), aircraftState.positionProperty());
        DoubleBinding positionY = createDoubleBinding(() ->
                        WebMercator.y(parameters.getZoom(), aircraftState.getPosition().latitude()) - parameters.getMinY(),
                parameters.zoomProperty(), parameters.minY(), aircraftState.positionProperty());
        group.layoutXProperty().bind(positionX);
        group.layoutYProperty().bind(positionY);

        aircraftState.positionProperty().addListener((observable, oldValue, newValue) ->
                aircraftState.setPosition(newValue));

        return group;
    }


    //Méthode créant le groupe de la trajectoire
    private Group trajectory(ObservableAircraftState aircraftState) {
        Group trajectoryGroup = new Group();
        trajectoryGroup.getStyleClass().add("trajectory");

        //listeners
        InvalidationListener listener = change -> drawTrajectory(aircraftState, trajectoryGroup);
        InvalidationListener trajectoryList= change-> drawTrajectory(aircraftState, trajectoryGroup);

        trajectoryGroup.visibleProperty().addListener((observable, oldV, newV) -> {
            if (newV) {
                aircraftState.getUnmodifiableTrajectory().addListener(trajectoryList);
                parameters.zoomProperty().addListener(listener);
                drawTrajectory(aircraftState, trajectoryGroup);
            } else {
                aircraftState.getUnmodifiableTrajectory().removeListener(trajectoryList);
                parameters.zoomProperty().removeListener(listener);
                trajectoryGroup.getChildren().clear();
            }
        });

        //binding
        trajectoryGroup.visibleProperty().bind(selectedAircraftState.isEqualTo(aircraftState));

        return trajectoryGroup;
    }

    //Méthode dessinant la trajectoire
    public void drawTrajectory(ObservableAircraftState aircraftState, Group group) {
        group.getChildren().clear();
        List<ObservableAircraftState.AirbornePos> airbonePos = aircraftState.getUnmodifiableTrajectory();
        List<ObservableAircraftState.AirbornePos> set = airbonePos.subList(1, airbonePos.size());
        ObservableAircraftState.AirbornePos start = airbonePos.get(0);
        for (ObservableAircraftState.AirbornePos next : set) {
            Line line = Lines(start, next);
            group.getChildren().addAll(line);
            start = next;
        }
    }


    //Méthode créant les lignes de trajectoire
    private Line Lines(ObservableAircraftState.AirbornePos start, ObservableAircraftState.AirbornePos end) {
        Line line = new Line(WebMercator.x(parameters.getZoom(), start.position().longitude()),
                WebMercator.y(parameters.getZoom(), start.position().latitude()),
                WebMercator.x(parameters.getZoom(), end.position().longitude()),
                WebMercator.y(parameters.getZoom(), end.position().latitude()));
        line.getStyleClass().add("trajectory Line");

        //bindings
        line.layoutXProperty().bind(parameters.minX().negate());
        line.layoutYProperty().bind(parameters.minY().negate());

        Color c1 = ColorRamp.PLASMA.at(Math.cbrt(end.altitude()) / COLOR_CONSTANT);
        Color c2 = ColorRamp.PLASMA.at(Math.cbrt(start.altitude()) / COLOR_CONSTANT);


        if (start.altitude() == end.altitude()) {
            line.setStroke(c1);
        } else {
            LinearGradient lineGradient = new LinearGradient(START_X, Y, END_X, Y, true,
                    NO_CYCLE, new Stop(0, c2), new Stop(1, c1));
            line.setStroke(lineGradient);
        }
        return line;
    }


}


