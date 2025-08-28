package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.text.NumberFormat;
import java.util.function.Consumer;


/**
 * La classe public et finale AircraftTableController gère la table de vue des aéronefs.
 *
 * @author Marwa Chiguer (325221)
 * @author Imane Oujja (344332)
 */
public final class AircraftTableController {
    private static final int OACI_WIDTH = 60;
    private static final int DESCRIPTION_WIDTH = 70;
    private static final int INDICATIF_WIDTH = 70;
    private static final int IMMATRICULATION_WIDTH = 90;
    private static final int MODEL_WIDTH = 230;
    private static final int Type_WIDTH = 50;
    private static final int MINIMUM_FRACTION_DIGITS1 = 0;
    private static final int MINIMUM_FRACTION_DIGITS2 = 4;
    public static final String OACI = "OACI";
    public static final String INDICATIF = "Indicatif";
    public static final String IMMATRICULATION = "Immatriculation";
    public static final String MODÈLE = "Modèle";
    public static final String TYPE = "Type";
    public static final String DESCRIPTION = "Description";
    public static final String LONGITUDE = "Longitude (°)";
    public static final String LATITUDE = "Latitude (°)";
    public static final String ALTITUDE_M = "Altitude (m)";
    public static final String VELOCITY = "Vitesse (km/h)";
    private final TableView<ObservableAircraftState> tableView;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;
    private final ObservableSet<ObservableAircraftState> aircraftStates;


    /**
     * Le constructeur public de la classe AircraftTableController
     *
     * @param aircraftStates   l'ensemble des états des aéronefs qui doivent apparaître sur la vue
     * @param selectedAircraft la propriété JavaFX contenant l'état de l'aéronef sélectionné,
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftStates,
                                   ObjectProperty<ObservableAircraftState> selectedAircraft) {
        this.tableView = new TableView<>();
        this.selectedAircraft = selectedAircraft;
        this.aircraftStates = aircraftStates;

        configureTableView();
        configureColumns();
        addEventHandlers();

    }


    /**
     * La méthode getTableView() retourne une vue du tableau
     *
     * @return une vue du tableau
     */
    public TableView<ObservableAircraftState> getTableView() {
        return tableView;
    }


    /**
     * Cette méthode public appelle sa méthode accept lorsqu'un clic double est effectué sur la table
     * et qu'un aéronef est actuellement sélectionné
     * @param onDoubleClickConsumer
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> onDoubleClickConsumer) {
        onDoubleClickConsumer.accept(selectedAircraft.get());
    }


    private void configureTableView() {
        // Définit la politique de redimensionnement des colonnes
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        // Affiche le bouton du menu du tableView
        tableView.setTableMenuButtonVisible(true);
        // Ajoute une classe CSS spécifique au tableView
        tableView.getStyleClass().add("table.css");
    }


    private void configureColumns() {
        // Crée les colonnes du tableView avec leurs titres
        TableColumn<ObservableAircraftState, String> oaciColumn = new TableColumn<>(OACI);
        TableColumn<ObservableAircraftState, String> indicatifColumn = new TableColumn<>(INDICATIF);
        TableColumn<ObservableAircraftState, String> immatriculationColumn = new TableColumn<>(IMMATRICULATION);
        TableColumn<ObservableAircraftState, String> modeleColumn = new TableColumn<>(MODÈLE);
        TableColumn<ObservableAircraftState, String> typeColumn = new TableColumn<>(TYPE);
        TableColumn<ObservableAircraftState, String> descriptionColumn = new TableColumn<>(DESCRIPTION);
        TableColumn<ObservableAircraftState, String> longitudeColumn = new TableColumn<>(LONGITUDE);
        TableColumn<ObservableAircraftState, String> latitudeColumn = new TableColumn<>(LATITUDE);
        TableColumn<ObservableAircraftState, String> altitudeColumn = new TableColumn<>(ALTITUDE_M);
        TableColumn<ObservableAircraftState, String> velocityColumn = new TableColumn<>(VELOCITY);


        tableView.getColumns().setAll(oaciColumn, indicatifColumn, immatriculationColumn, modeleColumn,
                typeColumn, descriptionColumn, longitudeColumn, latitudeColumn, altitudeColumn, velocityColumn);


        // Définit la largeur préférée des colonnes
        oaciColumn.setPrefWidth(OACI_WIDTH);
        indicatifColumn.setPrefWidth(INDICATIF_WIDTH);
        immatriculationColumn.setPrefWidth(IMMATRICULATION_WIDTH);
        modeleColumn.setPrefWidth(MODEL_WIDTH);
        typeColumn.setPrefWidth(Type_WIDTH);
        descriptionColumn.setPrefWidth(DESCRIPTION_WIDTH);


        // Configure les valeurs des cellules pour chaque colonne
        oaciColumn.setCellValueFactory(f ->
                new ReadOnlyObjectWrapper<>(f.getValue().getIcaoAddress().string()));
        indicatifColumn.setCellValueFactory(f ->
                f.getValue().callSignProperty().map(CallSign::string));
        immatriculationColumn.setCellValueFactory(f ->
                new ReadOnlyObjectWrapper<>((f.getValue().getData())).map(a ->
                a.registration().string()));
        modeleColumn.setCellValueFactory(f ->
                new ReadOnlyObjectWrapper<>((f.getValue().getData())).map(AircraftData::model));
        typeColumn.setCellValueFactory(f ->
                new ReadOnlyObjectWrapper<>((f.getValue().getData())).map(a -> a.typeDesignator().string()));
        descriptionColumn.setCellValueFactory(f ->
                new ReadOnlyObjectWrapper<>((f.getValue().getData())).map(a -> a.description().string()));


        // Configuration des formats numériques
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumFractionDigits(MINIMUM_FRACTION_DIGITS1);
        numberFormat.setMaximumFractionDigits(MINIMUM_FRACTION_DIGITS1);
        NumberFormat numberFormat2 = NumberFormat.getInstance();
        numberFormat2.setMinimumFractionDigits(MINIMUM_FRACTION_DIGITS2);
        numberFormat2.setMaximumFractionDigits(MINIMUM_FRACTION_DIGITS2);


        // Configure les valeurs des cellules pour les colonnes numériques
        longitudeColumn.setCellValueFactory(f -> f.getValue().positionProperty().map(p ->
                numberFormat2.format(p.longitude())));
        latitudeColumn.setCellValueFactory(f -> f.getValue().positionProperty().map(p ->
                numberFormat2.format(p.latitude())));
        altitudeColumn.setCellValueFactory(f -> f.getValue().altitudeProperty().map(p ->
                p != null ? numberFormat.format(p.doubleValue()) : ""));
        velocityColumn.setCellValueFactory(f -> f.getValue().velocityProperty().map(p ->
                p != null ? numberFormat.format(p.doubleValue()) : ""));


        //Définit les comparateurs
        latitudeColumn.setComparator((s1, s2) -> compareNumericValues(s1, s2, numberFormat2));
        longitudeColumn.setComparator((s1, s2) -> compareNumericValues(s1, s2, numberFormat2));
        altitudeColumn.setComparator((s1, s2) -> compareNumericValues(s1, s2, numberFormat));
        velocityColumn.setComparator((s1, s2) -> compareNumericValues(s1, s2, numberFormat));
    }



    private void addEventHandlers() {
        //listeners
        selectedAircraft.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tableView.getSelectionModel().select(newValue);
                tableView.scrollTo(newValue);
            }
        });
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasRemoved()) {
                tableView.getItems().remove(change.getElementRemoved());
            } else if (change.wasAdded()) {
                tableView.getItems().add(change.getElementAdded());
                // Trie les éléments dans le tableView
                tableView.sort();
            }
        });

        // Met à jour la valeur sélectionnée dans selectedAircraft
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedAircraft.set(newValue);
        });
    }


    //Cette méthode privée compare deux valeurs numériques représentées sous forme de chaînes de caractères
    private int compareNumericValues(String s1, String s2, NumberFormat numberFormat) {
        // Comparaison lexicographique par défaut si l'une des chaînes est vide
        if (s1.isEmpty() || s2.isEmpty()) {
            return s1.compareTo(s2);
        } else {
            try {
                // Convertir les chaines en nombre en utilisant le format numberFormat
                Number n1 = numberFormat.parse(s1);
                Number n2 = numberFormat.parse(s2);
                return Double.compare(n1.doubleValue(), n2.doubleValue());
            } catch (Exception e) {
                return 0;
            }
        }
    }

}