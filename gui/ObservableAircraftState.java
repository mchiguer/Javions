package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 * La classe ObservableAircraftState publique et finale, représente l'état d'un aéronef.
 *
 * @author Marwa Chiguer (325221)
 * @author Imane Oujja (344332)
 */

public final class ObservableAircraftState implements AircraftStateSetter {
    private final IcaoAddress icaoAddress;
    private final AircraftData data;
    private final LongProperty lastMessageTimeStampNs;
    private final IntegerProperty category;
    private final ObjectProperty<CallSign> callSign;
    private final ObjectProperty<GeoPos> position;
    private final ObservableList<AirbornePos> trajectory;
    private final DoubleProperty altitude;
    private final DoubleProperty velocity;
    private final DoubleProperty trackOrHeading;
    private final ObservableList<AirbornePos> unmodifiableTrajectory;



    /**
     * Le constructeur public de la classe ObservableAircraftState
     *
     * @param icaoAddress l'adresse OACI de l'aéronef
     * @param data        les caractéristiques fixes de cet aéronef, provenant de la base de données mictronics.
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData data) {
        this.icaoAddress = icaoAddress;
        this.data = data;
        lastMessageTimeStampNs = new SimpleLongProperty();
        category = new SimpleIntegerProperty();
        callSign = new SimpleObjectProperty<>();
        position = new SimpleObjectProperty<>();
        trajectory = FXCollections.observableArrayList();
        altitude = new SimpleDoubleProperty();
        velocity = new SimpleDoubleProperty();
        trackOrHeading = new SimpleDoubleProperty();
        unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory);
    }


    /**
     * Un enregistrement public, AirbornePos, imbriqué dans ObservableAircraftState.
     *
     * @param position la position de l'aéronef
     * @param altitude l'altitude de l'aéronef
     */
    public record AirbornePos(GeoPos position, double altitude) {
    }


    /**
     * Un getter public de la catégorie de l'aéronef
     *
     * @return la catégorie de l'aéronef
     */
    public int getCategory() {
        return category.get();
    }


    /**
     * Un getter public de la data de l'aéronef
     *
     * @return les données de l'aéronef encapsulées dans la classe AircraftData
     */
    public AircraftData getData() {
        return data;
    }


    /**
     * Un getter public de l'horodatage du dernier message reçu de l'aéronef
     *
     * @return l'horodatage du dernier message reçu de l'aéronef, en nanosecondes
     */
    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.get();
    }


    /**
     * Un getter public de la position de l'aéronef
     *
     * @return la position de l'aéronef à la surface de la Terre (longitude et latitude, en radians)
     */
    public GeoPos getPosition() {
        return position.get();
    }


    /**
     * Un getter public de l'altitude de l'aéronef
     *
     * @return l'altitude de l'aéronef, en mètres
     */
    public double getAltitude() {
        return altitude.get();
    }


    /**
     * Un getter public du cap de l'aéronef
     *
     * @return la route ou le cap de l'aéronef, en radians
     */
    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }


    /**
     * Un getter public de la vitesse de l'aéronef
     *
     * @return la vitesse de l'aéronef, en mètres par seconde
     */
    public double getVelocity() {
        return velocity.get();
    }


    /**
     * Un getter public de l'indicatif de l'aéronef
     *
     * @return l'indicatif de l'aéronef
     */
    public CallSign getCallSign() {
        return callSign.get();
    }


    /**
     * Un getter public de l'adresse OACI de l'aéronef
     *
     * @return l'adresse OACI de l'aéronef
     */
    public IcaoAddress getIcaoAddress() {
        return icaoAddress;
    }


    /**
     * Cette méthode retourne une liste observable non modifiable (ObservableList) de type AirbornePos
     *
     * @return la trajectoire de l'aéronef
     */
    public ObservableList<AirbornePos> getUnmodifiableTrajectory() {
        return unmodifiableTrajectory;
    }


    /**
     * Un setter public de l'horodotage du message
     *
     * @param timeStampNs l'horodotage du message
     */
    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        lastMessageTimeStampNs.set(timeStampNs);
    }


    /**
     * Un setter public de la catégorie d'un aéronef
     *
     * @param category la catégorie d'un aéronef.
     */
    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }


    /**
     * Un setter public de l'indicatif de l'aéronef
     *
     * @param callSign l'indicatif de l'aéronef.
     */
    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set((callSign));
    }


    /**
     * Un setter public de la position de l'aéronef
     *
     * @param position la position de l'aéronef.
     */
    @Override
    public void setPosition(GeoPos position) {
        trajectory.add(new AirbornePos(position, getAltitude()));
        this.position.set(position);
    }


    /**
     * Un setter public de l'altitude de l'aéronef
     *
     * @param altitude l'altitude de l'aéronef.
     */
    @Override
    public void setAltitude(double altitude) {
        if (getPosition() != null) {
            AirbornePos last= trajectory.get(trajectory.size() - 1);
            if (getUnmodifiableTrajectory().isEmpty()) {
                trajectory.set(trajectory.size() - 1, new AirbornePos(getPosition(), altitude));
            } else if (!(last.equals(new AirbornePos(getPosition(), altitude))))
                trajectory.add(new AirbornePos(getPosition(), altitude));
            }
        this.altitude.setValue(altitude);
        }


    /**
     * Un setter public de la vitesse de l'aéronef
     *
     * @param velocity la vitesse de l'aéronef.
     */
    @Override
    public void setVelocity(double velocity) {
        this.velocity.set((velocity));
    }


    /**
     * Un setter public de la direction de l'aéronef
     *
     * @param trackOrHeading la direction de l'aéronef.
     */
    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }


    /**
     * Cette méthode public permet d'observer les changements de la valeur de l'objet CallSign sans la possibilité de
     * la modifier
     *
     * @return une propriété en lecture seule (ReadOnlyObjectProperty), associée à callSign
     */
    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSign;
    }


    /**
     * Cette méthode public permet d'observer les changements de la valeur de l'objet GeoPos sans la possibilité de la
     * modifier
     *
     * @return une propriété en lecture seule (ReadOnlyObjectProperty), associée à position
     */
    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return position;
    }


    /**
     * Cette méthode public est une propriété en lecture seule, qui permet d'observer les changements de la valeur
     * altitude de sans pouvoir la modifier
     *
     * @return la propriété de l'attribut altitude qui représente l'altitude
     */
    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }


    /**
     * Cette méthode public est une propriété en lecture seule, qui permet d'observer les changements de la valeur
     * velocity de sans pouvoir la modifier
     *
     * @return la propriété de l'attribut velocity qui représente la vitesse
     */
    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }


    /**
     * Cette méthode public est une propriété en lecture seule, qui permet d'observer les changements de la valeur
     * trackOrHeading de sans pouvoir la modifier
     *
     * @return la propriété de l'attribut trackOrHeading qui représente la direction de l'aéronef
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }


}

