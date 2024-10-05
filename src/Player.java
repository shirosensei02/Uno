import java.util.*;

public class Player {

    private Hand hand;
    private String name;  
    private final static int STARTING_HAND_COUNT = 7;

    public Player(String name ){
        this.name = name;
        this.hand = new Hand();
    }

    // for if deck has less cards than draws
    public void drawCard(Deck deck){
        hand.getCardsInHand().add(deck.dealCard());
    }

    public void drawInitialCards(Deck deck){

        for (int i = 0; i < STARTING_HAND_COUNT; i++) {
            hand.getCardsInHand().add(deck.dealCard());
        }
    }

    public void getDisplayName() {
        // ASCIIArtGenerator generator = new ASCIIArtGenerator();
        System.out.println(ASCIIArtGenerator.textToAscii(name));
    }

    public String getName() {
        return name;
    }

    // method to get the player's hand
    public Hand getHand() {
        return hand;
    }

    public boolean hasUno() {
        return hand.getCardsInHand().size() == 1;
    }


}


