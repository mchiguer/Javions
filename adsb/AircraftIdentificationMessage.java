package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;


import java.util.Objects;

import static ch.epfl.javions.Bits.extractUInt;

/**
 * Enregistrement Messages d'identification implémentant l'interface Message
 *
 * @param timeStampNs l'horodatage du message, en nanosecondes
 * @param icaoAddress l'adresse OACI de l'expéditeur du message
 * @param category    la catégorie d'aéronef de l'expéditeur
 * @param callSign    l'indicatif de l'expéditeur
 * @author Marwa Chiguer (325221)
 * @author Imane Oujja (344332)
 */


public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category,
                                            CallSign callSign) implements Message {

    private static final String[] tabLettre = new String[]{"", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    private static final String[] tabChiffre = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private static final int START_CA = 48;
    private static final int SIZE_CA = 3;
    private static final int BOUND_START = 1;
    private static final int BOUND26 = 26;
    private static final int BOUND32 = 32;
    private static final int BOUND57 = 57;
    private static final int BITS_NUMBER = 6;
    private static final int MSG_LENGTH = 48;


    /**
     * Retourne le message d'identification correspondant au message brut donné,
     * ou null si au moins un des caractères de l'indicatif qu'il contient est invalide
     *
     * @param rawMessage le message brut donné
     * @return le message d'identification correspondant au message brut donné
     */

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        StringBuilder string = new StringBuilder();

        int CA = extractUInt(rawMessage.payload(), START_CA, SIZE_CA);
        int TC = (14 - rawMessage.typeCode(rawMessage.payload())) << 4;
        for (int j = 0; j < MSG_LENGTH; j = j + BITS_NUMBER) {
            int msg = extractUInt(rawMessage.payload(), j, BITS_NUMBER);
            if ((msg < BOUND_START || (msg > BOUND26 && msg < START_CA && msg != BOUND32) || msg > BOUND57)) {
                return null;
            } else if (msg <= BOUND26) {
                string.append(tabLettre[msg]);
            } else if (msg == BOUND32) {
                string.append("");
            } else {
                string.append(tabChiffre[msg]);
            }
        }

        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), TC | CA, new CallSign(string.reverse().toString()));
    }



    /**
     * Le constructeur de la classe AircraftIdentificationMessage
     *
     * @throws NullPointerException     si l'adresse OACI est nulle
     * @throws IllegalArgumentException si l'horodotage est inférieur strictement à 0
     */
    public AircraftIdentificationMessage {
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >= 0);
    }



    /**
     * Une redéfinition de la méthode icaoAddress
     *
     * @return l'adresse ICAO
     */
    @Override
    public IcaoAddress icaoAddress() {
        return icaoAddress;
    }

    /**
     * Une redéfinition de la méthode timeStampNs
     *
     * @return l'horodatage
     */
    @Override
    public long timeStampNs() {
        return timeStampNs;
    }

}
