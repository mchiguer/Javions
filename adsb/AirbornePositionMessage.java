package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

import static ch.epfl.javions.Bits.extractUInt;
import static ch.epfl.javions.Units.Length.FOOT;
import static ch.epfl.javions.Units.Length.METER;

/**
 * Enregistrement Messages de positionnement implémentant l'interface Message
 * @author Marwa Chiguer (325221)
 * @author Imane Oujja (344332)
 */
public record AirbornePositionMessage (long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x, double y) implements Message {

    private static final int START =11;
    private static final int BIT_SIZE= 1 ;
    private static final int SIZE_L = 17;
    private static final double POWER = Math.pow(2, -SIZE_L);
    private static final int START0 = 0;
    private static final int SIZE_F = 1;
    private static final int START_ALT = 36;
    private static final int SIZE_ALT = 12 ;
    private static final int START_F = 34 ;
    private static final int INDEX =4;
    private static final int TWENTY_FIVE_FOOT =25;
    private static final int THOUSAND_FOOT =1000;
    private static final int START_A = 5;
    private static final int SIZE_A = 7 ;
    private static final int D = 4;
    private static final int A = 10;
    private static final int B = 5;
    private static final int C = 11;
    private static final int HUND_FOOT = 100;
    private static final int FIVE_HUND_FOOT = 500;
    private static final int ALT_BASE = -1300;
    private static final int ALT_DEM_NUM = 3;
    private static final int SIZE_GROUP2=9 ;



    /**
     *
     * @param timeStampNs l'horodatage du message, en nanosecondes.
     * @param icaoAddress l'adresse OACI de l'expéditeur du message.
     * @param altitude l'altitude à laquelle se trouvait l'aéronef au moment de l'envoi du message, en mètres.
     * @param parity la parité du message (0 s'il est pair, 1 s'il est impair).
     * @param x la longitude locale et normalisée donc comprise entre 0 et 1 à laquelle se
     *          trouvait l'aéronef au moment de l'envoi du message,
     * @param y la latitude locale et normalisée à laquelle se trouvait l'aéronef au moment
     *          de l'envoi du message
     * @throws NullPointerException si l'adresse OACI est nulle
     * @throws IllegalArgumentException si l'horodotage est strictement inférieure à 0, ou parity est différent de 0 ou 1
     *  ou x ou y ne sont pas compris entre 0 (inclus) et 1 (exclu)
     */
    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs>=0 && (parity==0 || parity==1)&& (x>=0 && x<1)&&(y>=0 && y<1));
    }


    /**
     * Méthode retournant le message de positionnement en vol correspondant au message brut donné,
     * ou null si l'altitude qu'il contient est invalide
     * @param rawMessage le message brut.
     * @return le message de positionnement en vol correspondant au message brut donné
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {
        double altitude = 0;
        int parite = extractUInt(rawMessage.payload(), START_F, SIZE_F);
        double x = extractUInt(rawMessage.payload(), START0, SIZE_L) * POWER;
        double y = extractUInt(rawMessage.payload(), SIZE_L, SIZE_L) * POWER;
        int ALT = extractUInt(rawMessage.payload(), START_ALT, SIZE_ALT);


        if (Bits.testBit(ALT, INDEX)) {
            int alt = (extractUInt(ALT,START_A , SIZE_A)) << INDEX | extractUInt(ALT, START0, INDEX);
            altitude = - THOUSAND_FOOT + alt * TWENTY_FIVE_FOOT;
        } else if (!(Bits.testBit(ALT, INDEX))) {
            int ALTDemele = ALTdem(ALT);
            int groupe1 = decodeGray(extractUInt(ALTDemele, START0, ALT_DEM_NUM));
            int groupe2 = decodeGray(extractUInt(ALTDemele, ALT_DEM_NUM , SIZE_GROUP2));
            if (groupe1 == 0 || groupe1 == 5 || groupe1 == 6) {
                altitude = Double.NaN;
            } else {
                if (groupe1 == 7) {
                    groupe1 = 5;
                }
                if (groupe2 % 2 != 0) {
                    groupe1 = 6 - groupe1;
                }

                altitude = ALT_BASE + HUND_FOOT * groupe1 + FIVE_HUND_FOOT * groupe2;
            }

        }
        if (Double.isNaN(altitude)) {
            return null;
        } else {
            altitude = Units.convert(altitude, FOOT, METER);
            return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), altitude, parite, x, y);
        }
    }


    //Cette méthode a pour but le démêlage, consiste à permuter l'ordre des bits afin de faciliter leur interprétation ultérieure
    private static int ALTdem(int ALT){
        int j=1;
        int Alt = (Bits.extractUInt(ALT,D,j)<<START) |
                (Bits.extractUInt(ALT,D-2,BIT_SIZE)<<START-j) |
                (Bits.extractUInt(ALT,D-4,BIT_SIZE)<<START-j-1) |
                (Bits.extractUInt(ALT,A,BIT_SIZE)<<START-j-2) |
                (Bits.extractUInt(ALT,A-2,BIT_SIZE)<<START-j-3) |
                (Bits.extractUInt(ALT,A-4,BIT_SIZE)<<START-j-4) |
                (Bits.extractUInt(ALT,B,BIT_SIZE)<<START-j-5) |
                (Bits.extractUInt(ALT,B-2,BIT_SIZE)<<START-j-6) |
                (Bits.extractUInt(ALT,B-4,BIT_SIZE)<<START-j-7) |
                (Bits.extractUInt(ALT,C,BIT_SIZE)<<START-j-8) |
                (Bits.extractUInt(ALT,C-2,BIT_SIZE)<<START-j-9) |
                Bits.extractUInt(ALT,C-4,BIT_SIZE);
        return Alt;
    }

    //Cette méthode prend comme un argment un nombre représentés par le code Gray et les convertit à la representation binaire
    private static int decodeGray(int numberGray){
        int numberBinaire =numberGray;
        for(int k=8 ; k>=1; k=k/2){
            numberBinaire= numberBinaire ^ (numberBinaire>>k);
        }
        return numberBinaire;
    }
}