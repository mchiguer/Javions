package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static javafx.collections.FXCollections.*;


/**
 * La classe publique et finale AircraftStateManager a pour but de garder à jour les états d'un ensemble d'aéronefs
 * en fonction des messages reçus d'eux
 *
 * @author Marwa Chiguer (325221)
 * @author Imane Oujja (344332)
 */
public final class AircraftStateManager {
    public static final double TIME = 6 * Math.pow(10, 10);
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> association = new HashMap<>();
    private final ObservableSet<ObservableAircraftState> set;
    private final AircraftDatabase dataBase;
    private final ObservableSet<ObservableAircraftState> states;
    private long currentTime;


    /**
     * Ce constructeur public de la classe AircraftStateManager
     *
     * @param data les données de l'aéronef
     */
    public AircraftStateManager(AircraftDatabase data) {
        this.dataBase = data;
        set = observableSet();
        states = unmodifiableObservableSet(set);
    }


    /**
     * Cette méthode public retourne des états observables des aéronefs
     *
     * @return l'ensemble observable, mais non modifiable, des états observables des aéronefs dont la position est connue
     */
    public ObservableSet<ObservableAircraftState> states() {
        return states;
    }


    /**
     * Cette méthode public utilise le message recu l'utilisant pour mettre à jour l'état de l'aéronef qui l'a envoyé
     *
     * @param message un message
     */
    public void updateWithMessage(Message message) throws IOException {
        currentTime = message.timeStampNs();
        IcaoAddress key = message.icaoAddress();
        if (association.containsKey(key)) {
            association.get(key).update(message);
            if (association.get(key).stateSetter().getPosition() != null) {
                set.add(association.get(key).stateSetter());
            }
        } else {
            association.put(key, new AircraftStateAccumulator<>(new ObservableAircraftState(key, dataBase.get(key))));
        }
    }


    /**
     * Cette méthode public supprime de l'ensemble des états observables tous ceux correspondant à des aéronefs dont
     * aucun message n'a été reçu dans la minute précédant la réception du dernier message passé à updateWithMessage
     */
    public void purge() {
        Iterator<AircraftStateAccumulator<ObservableAircraftState>> iterator = (association.values()).iterator();
        while (iterator.hasNext()) {
            AircraftStateAccumulator<ObservableAircraftState> state = iterator.next();
            if (currentTime - (state.stateSetter()).getLastMessageTimeStampNs() > TIME) {
                iterator.remove();
                set.remove(state.stateSetter());
            }
        }
    }
}


