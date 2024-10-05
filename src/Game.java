import java.util.*;

public class Game {
    private ArrayList<Player> playerList = new ArrayList<>();;
    private int playerCount = 0;
    private boolean gameOver = false;
    private Deck currentDeck = new Deck();
    private Table currentTable = new Table();
    public static final String ANSI_CLEAR = "\033[H\033[2J";

    //constructor
    public Game(){
        currentDeck.shuffle();
    }

    public void createPlayerCount() {
        Scanner sc = new Scanner(System.in);
        while (playerCount > 5 || playerCount < 2) {
            try {
                System.out.print("Enter number of players (2-5): ");
                playerCount = sc.nextInt();
                System.out.println();
                if ( playerCount < 2) {
                    throw new InputMismatchException("Player count is too low!");
                } if ( playerCount > 5 ) {
                    throw new InputMismatchException("Player count is too high!");
                }
            } catch (InputMismatchException e) {
                System.out.println(e.getMessage() + " Choose a value between 2 to 5.");
                sc.nextLine();
            }
        }
    }

    public void draw(Player currentPlayer, int cardsToDraw){
        for (int i = 0; i < cardsToDraw; i++) {
            //reshuffle from played pile if deck runs out
            if (currentDeck.getCurrentDeck().isEmpty()) {
                //game ends if played pile is empty
                if (currentTable.getTableCards() == null){
                    displayWinner(checkWinnerEmptyDeck());
                    gameOver = true;
                    return;
                }
                currentDeck = new Deck(currentTable.getTableCards());
                currentDeck.shuffle();
                currentTable.reset();
            }
            currentPlayer.drawCard(currentDeck);
        }
    }
    //what if both have same cards
    public Player checkWinnerEmptyDeck(){
        Player winner = playerList.get(0);
        for (Player player : playerList){
            if (player.getHand().getCardsInHand().size() < winner.getHand().getCardsInHand().size()){
                winner = player;
            }
        }
        return winner;
    }

    public void displayWinner(Player winner){
        System.out.println(ASCIIArtGenerator.textToAscii(winner.getName()));
        System.out.println(ASCIIArtGenerator.textToAscii("wins!"));
    }

    public void firstTurn(Hand currentHand){
        boolean isActionCard = true;
        Card firstCard = null;

        int countNumAction = 0;
        for (Card card : currentHand.getCardsInHand()){
            if (card.getNumericValue() >= 10){
                countNumAction++;
            }
        }
        if (countNumAction == currentHand.getCardsInHand().size()){
            System.out.println("First Hand all action cards. Restart Game");
            gameOver = true;
            return;
        }

        setFirstPlayableCards(currentHand);
        Scanner sc = new Scanner(System.in);

        //check if is action card say nono if action
        while (isActionCard){
            try {
                System.out.print("Select the first card to play: ");
                int cardIdx = sc.nextInt();
                
                firstCard = currentHand.getCardsInHand().get(cardIdx - 1);

                //check action card (error exception)
// changed
                if (firstCard.getNumericValue() >= 10){
                    throw new IllegalArgumentException() ; 
// System.out.println("First card cannot be action card!");
// continue;
                } 

                isActionCard = false;
                currentHand.removeCard(firstCard);

// changed too 
            } catch (InputMismatchException e) {
                System.out.println("Input must be a number. Please enter a valid number!");
                sc.nextLine();  
            } catch ( IndexOutOfBoundsException  e ) {
                System.out.println("Index is out of bounds. Please enter a valid number!");
                sc.nextLine();
            } 
            catch ( IllegalArgumentException e ) {
                System.out.println( "First card cannot be action card. Please enter a valid number!");
                sc.nextLine();
            } 
        }

        //set most recent card into current table
        currentTable.setMostRecent(firstCard);

        //add card into table's card array
        currentTable.addCard(firstCard);
    }

    // only for first turn
    public void setFirstPlayableCards(Hand currentHand) {
        for (Card c : currentHand.getCardsInHand()) {
            if (c.getNumericValue() < 10) {
                currentHand.getPlayableCards().add(c);
            }
        }
        currentHand.displayHand();
    }

    public void setPlayableCards(Hand currentHand, ArrayList<Card> currentlyPicked) {
        if (currentlyPicked.size() == 0) {
            currentHand.setPlayableCards(currentlyPicked, currentTable.getMostRecent());
        } else {
            currentHand.setPlayableCards(currentlyPicked, currentlyPicked.get(currentlyPicked.size() - 1));
        }
        currentHand.displayHand();
    }

    public void selectCards(Hand currentHand, Player currentPlayer, ArrayList<Card> currentlyPicked, Scanner sc){
        boolean isSelecting = true;
        while (isSelecting) {
            // setPlayableCards(currentHand, currentlyPicked);

            if (currentHand.getPlayableCards().size() == 0){
                System.out.println("There are no more playable cards.");
                System.out.println();
                break;
            }

            System.out.print("Hi Player " + currentPlayer.getName() + ". Enter the card you want to play:");
            int idxPicked = -1; 

            while (idxPicked < 1 || idxPicked > currentHand.getCardsInHand().size()){
                try {
                    idxPicked = sc.nextInt();

// changed here 
                    Card selectedCard = currentHand.getCardsInHand().get(idxPicked - 1);

                    if (!(currentHand.getPlayableCards().contains( /*HERE*/ selectedCard) )) {
// idxPicked = -1;
                        throw new InputMismatchException();
                    }
                    
                    if (currentlyPicked.contains( /*HERE*/ selectedCard ) ) {
                        System.out.println("You just picked this dumbass"); // If they picked a card they already picked
// idxPicked = -1;
                        throw new InputMismatchException();
                    }

                    currentlyPicked.add( /*HERE*/ selectedCard);


                } catch (InputMismatchException | IndexOutOfBoundsException e) {
                    System.out.print("Please enter the index of a playable card: ");
                    sc.nextLine();
// i moved idxPicked = -1 here 
                    idxPicked = -1;
                }
            }

            currentHand.clearPlayableCards();

            System.out.println();
            System.out.println("Currently Picked Cards: " + currentlyPicked);
            System.out.println();

            //added to handle wrong inputs
            boolean correctInput = false;
            while (!correctInput){
                System.out.print("Continue adding cards? (Y/N) ");
                String answer = sc.next();
                if (answer.equalsIgnoreCase("N")) {
                    isSelecting = false;
                    correctInput = true;
                }
                else if (answer.equalsIgnoreCase("Y")){
                    currentHand.clearPlayableCards();
                    setPlayableCards(currentHand, currentlyPicked);
                    if (currentHand.getCardsInHand().size() - currentlyPicked.size() == 1){
                        isSelecting = false;
                        System.out.println("You cannot win by stacking your last card. Play it on its own to win");
                    }
                    correctInput = true;
                }
                else{
                    System.out.println("Invalid input, try again.\n");
                }
            }
        }
    }
    // private helper function
    private void initializePlayers() {
        Scanner sc = new Scanner(System.in);
        ArrayList<String> existingNames = new ArrayList<>();

        for (int i = 1; i <= playerCount; i++) {
            // use getPlayerName(i) helper function
            String name = getPlayerName(i, existingNames);


            Player player = new Player(name);
            player.drawInitialCards(currentDeck);
            playerList.add(player);
        }
    }

    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

// private helper function
    private String getPlayerName(int playerNumber, ArrayList<String> existingNames ) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter player " + playerNumber + " name to start: ");
        String name = sc.nextLine().trim();

        //make sure name only has letters
        boolean onlyLetters = true;
        for (int i = 0; i < name.length(); i++){
            if (!isAlpha(name.charAt(i))){
                onlyLetters = false;
                break;
            }
        }

        while (existingNames.contains(name.toUpperCase()) || name.isEmpty() || !onlyLetters || name.length() > 10) {
            System.out.println("The name is invalid or already taken. Names can only contain letters and must be less than 10 characters long");
            System.out.print("Please enter a valid player " + playerNumber + " name to start: : ");
            name = sc.nextLine();
            onlyLetters = true;
            for (int i = 0; i < name.length(); i++){
                if (!isAlpha(name.charAt(i))){
                    onlyLetters = false;
                    break;
                }
            }
        }
        existingNames.add(name.toUpperCase());
        return name;
    }
    /**
     * 
     * 
     */

    public void startGame(){
        createPlayerCount();
        Evaluator currentEvaluator = new Evaluator();

        Scanner sc = new Scanner(System.in);
        initializePlayers();

        //game loop
        int currentIdx = 0;
        int cardsToDraw = 0;
        while(!gameOver){
            //clear terminal
            System.out.print(ANSI_CLEAR);

            Player currentPlayer = playerList.get(currentIdx);
            // System.out.println("currentIdx : " + currentIdx);

            //draw cards from previous turn
            Hand currentHand = currentPlayer.getHand();
            draw(currentPlayer, cardsToDraw);
            cardsToDraw = 0;

            //urmoms name
            System.out.println();
            currentPlayer.getDisplayName();

            //checking if player can end
            if (currentPlayer.hasUno()){
                //player's last card is action card = cannot end
                if (currentHand.getCardsInHand().get(0).getNumericValue() >= 10){
                    currentHand.displayHand();
                    draw(currentPlayer, 1);
                    System.out.print("You can't end with an action card. Drawing card (press enter until continue)");
                    sc.nextLine();
                    sc.nextLine();
                    currentIdx = currentEvaluator.movePlayer(currentIdx, playerCount);
                    continue;
                }
            }

            ArrayList<Card> currentlyPicked = new ArrayList<>(); 

            //when table is empty
            //entire if for first turn only
            if (currentTable.getMostRecent() == null){
                firstTurn(currentHand);
                currentIdx = currentEvaluator.movePlayer(currentIdx, playerCount);
                continue;
            }

            System.out.println("Top card on table: " + currentTable.getMostRecent());
            System.out.println();

            boolean playable = false;
            while(!playable){

                int result = 0;
                setPlayableCards(currentHand, currentlyPicked);
                while (!(result == 1 || result == 2)){
                    try {
                        System.out.println("Actions:");
                        System.out.printf("1) Draw\n2) Play\nOption: ");
                        result = sc.nextInt();
                        if (!(result == 1 || result == 2)){
                            throw new InputMismatchException();
                        }
                        
                    } catch (InputMismatchException e) {
                        System.out.println("\nPlease enter 1 or 2\n");
                        sc.nextLine();
                    }
                }
                //if player draws
                if (result == 1){
                    draw(currentPlayer, 1);
                    currentIdx = currentEvaluator.movePlayer(currentIdx, playerCount);
                    break;
                }
                selectCards(currentHand, currentPlayer, currentlyPicked, sc);

               
                if (currentlyPicked.size() == 0){ // autodraw
                    draw(currentPlayer, 1);
                    sc.nextLine();
                    System.out.print("A card has been drawn for you (press enter)");
                    sc.nextLine();
                    currentIdx = currentEvaluator.movePlayer(currentIdx, playerCount);
                    break;
                }
                playable = true;

                currentTable.setMostRecent(currentlyPicked.get(currentlyPicked.size() - 1));
                for (Card c : currentlyPicked) {
                    currentTable.getTableCards().add(c);
                    currentHand.getCardsInHand().remove(c);
                }

                System.out.println("currentpicked : " + currentlyPicked);

                //check card effect
                currentIdx = currentEvaluator.effect(currentlyPicked, currentTable, playerCount, currentIdx, sc);

                sc.nextLine();
                System.out.print("Ending turn (press enter)");
                sc.nextLine();
            }

            //update cards to draw
            cardsToDraw = currentEvaluator.getCardsToDraw();

            //reset draw
            currentEvaluator.resetDraw();

            

            // when player win
            if (currentHand.getCardsInHand().size() == 0){
                displayWinner(currentPlayer);
                gameOver = true;
            }
        }
    }
}


// import java.util.*;

// public class Game {
//     private ArrayList<Player> playerList = new ArrayList<>();
//     private int playerCount = 0;
//     private boolean gameOver = false;
//     private Deck currentDeck = new Deck();
//     private Table currentTable = new Table();
//     public static final String ANSI_CLEAR = "\033[H\033[2J";

   
//     public Game(){
//         currentDeck.shuffle();
//     }

//     public void createPlayerCount() {
//         Scanner sc = new Scanner(System.in);
//         while (playerCount > 5 || playerCount < 2) {
//             try {
//                 System.out.print("Number of Players: ");
//                 playerCount = sc.nextInt();
//                 System.out.println();
//                 if ( playerCount < 2) {
//                     throw new InputMismatchException("Player count is too low!");
//                 } if ( playerCount > 5 ) {
//                     throw new InputMismatchException("Player count is too high!");
//                 }
//             } catch (InputMismatchException e) {
//                 System.out.println(e.getMessage() + " Choose a value between 2 to 5.");
//                 sc.nextLine();
//             }
//         }
//     }

//     public void draw(Player currentPlayer, int cardsToDraw){
//         for (int i = 0; i < cardsToDraw; i++) {
//             if (currentDeck.getCurrentDeck().isEmpty()) {
//                 currentDeck = new Deck(currentTable.getTableCards());
//                 currentDeck.shuffle();
//                 currentTable.reset();
//             }
//             currentPlayer.drawCard(currentDeck);
//         }
//     }

//     public void firstTurn(Hand currentHand){
//         boolean isActionCard = true;
//         Card firstCard = null;
//         setFirstPlayableCards(currentHand);
//         Scanner sc = new Scanner(System.in);

//         //check if is action card , say nono if action
//         while (isActionCard){
//             try {
//                 System.out.print("Select the first card to play: ");
//                 int cardIdx = sc.nextInt();
                
//                 firstCard = currentHand.getCardsInHand().get(cardIdx - 1);

//                 if (firstCard.getNumericValue() >= 10){
//                     throw new IllegalArgumentException() ; 

//                 } 

//                 isActionCard = false;
//                 currentHand.removeCard(firstCard);


//             } catch (InputMismatchException e) {
//                 System.out.println("Input must be a number. Please enter a valid number!");
//                 sc.nextLine();  
//             } catch ( IndexOutOfBoundsException  e ) {
//                 System.out.println("Index is out of bounds. Please enter a valid number!");
//                 sc.nextLine();
//             } 
//             catch ( IllegalArgumentException e ) {
//                 System.out.println( "First card cannot be action card. Please enter a valid number!");
//                 sc.nextLine();
//             } 
//         }

//         //set most recent card into current table
//         currentTable.setMostRecent(firstCard);

//         //add card into table's card array
//         currentTable.addCard(firstCard);
//     }

//     // only for first turn (because the first card played cannot be an action card)
//     public void setFirstPlayableCards(Hand currentHand) {
//         for (Card c : currentHand.getCardsInHand()) {
//             if (c.getNumericValue() < 10) {
//                 currentHand.getPlayableCards().add(c);
//             }
//         }
//         currentHand.displayHand();
//     }



//     public void setPlayableCards(Hand currentHand, ArrayList<Card> currentlyPicked) {
//         if (currentlyPicked.size() == 0) {

//             currentHand.setPlayableCards(currentlyPicked, currentTable.getMostRecent());
//         } else {

//             currentHand.setPlayableCards(currentlyPicked, currentlyPicked.get(currentlyPicked.size() - 1));
//         }
//         currentHand.displayHand();
//     }

//     public void selectCards(Hand currentHand, Player currentPlayer, ArrayList<Card> currentlyPicked, Scanner sc){
//         boolean isSelecting = true;
//         while (isSelecting) {
//             // setPlayableCards(currentHand, currentlyPicked);

//             if (currentHand.getPlayableCards().size() == 0){
//                 System.out.println("There are no more playable cards.");
//                 break;
//             }

//             System.out.print("Hi Player " + currentPlayer.getName() + ":) Enter the card you want to play: ");
//             int idxPicked = -1; 

//             while (idxPicked < 1 || idxPicked > currentHand.getCardsInHand().size()){
//                 try {
//                     idxPicked = sc.nextInt();


//                     Card selectedCard = currentHand.getCardsInHand().get(idxPicked - 1);

//                     if (!(currentHand.getPlayableCards().contains( /*HERE*/ selectedCard) )) {

//                         throw new InputMismatchException();
//                     }
                    
//                     if (currentlyPicked.contains( /*HERE*/ selectedCard ) ) {
//                         System.out.println("You already picked this dumbass."); // If they picked a card they already picked

//                         throw new InputMismatchException();
//                     }

//                     currentlyPicked.add( /*HERE*/ selectedCard);


//                 } catch (InputMismatchException | IndexOutOfBoundsException e) {
//                     System.out.println("Please enter the index of a playable card");
//                     sc.nextLine();

//                     idxPicked = -1;
//                 }
//             }

//             currentHand.clearPlayableCards();

//             System.out.println();
//             System.out.print("Currently Picked Cards: " + currentlyPicked);

//             //added to handle wrong inputs
//             boolean correctInput = false;
//             while (!correctInput){
//                 System.out.print("Continue adding cards? (Y/N)");

//                 String answer = sc.next();

//                 if (answer.equalsIgnoreCase("N") ) {
//                     isSelecting = false;
//                     correctInput = true;
//                 }
//                 else if (answer.equalsIgnoreCase("Y") ){
//                     currentHand.clearPlayableCards();
//                     setPlayableCards(currentHand, currentlyPicked);
//                     if (currentHand.getCardsInHand().size() - currentlyPicked.size() == 1){
//                         isSelecting = false;
//                         System.out.println("Cannot stack with your last card");
//                     }
//                     correctInput = true;
//                 }
//                 else {
//                     System.out.println("Invalid input, try again.\n");
//                 }
//             }
//         }
//     }

// // private helper function
//     private void initializePlayers(Scanner sc) {
//         ArrayList<String> existingNames = new ArrayList<>();

//         for (int i = 1; i <= playerCount; i++) {
//             // use getPlayerName(i) helper function
//             String name = getPlayerName(i, sc, existingNames);

//             Player player = new Player(name);
//             player.drawInitialCards(currentDeck);
//             playerList.add(player);
//         }
//     }


//     private String getPlayerName(int playerNumber, Scanner sc, ArrayList<String> existingNames ) {
//         System.out.print("Please enter player " + playerNumber + " name to start: ");
//         String name = sc.nextLine().trim();

//         while (existingNames.contains(name.toUpperCase()) || name.isEmpty()) {
//             System.out.println("The name is invalid or already taken DUMBASS");
//             System.out.print("Please enter a valid player " + playerNumber + " name to start: : ");
//             name = sc.nextLine();
//         }
//         existingNames.add(name.toUpperCase());
//         return name;
//     }

//     public void startGame(){
//         createPlayerCount();
//         Scanner sc = new Scanner(System.in);
//         initializePlayers(sc);

//         Evaluator currentEvaluator = new Evaluator();
        
//         //game loop
//         int currentIdx = 0;
//         int cardsToDraw = 0;
//         while(!gameOver){
//             //clear terminal
//             System.out.print(ANSI_CLEAR);

//             Player currentPlayer = playerList.get(currentIdx);
//             // System.out.println("currentIdx : " + currentIdx);

//             //draw cards from previous turn
//             Hand currentHand = currentPlayer.getHand();
//             draw(currentPlayer, cardsToDraw);
//             cardsToDraw = 0;

//             //urmoms name
//             System.out.println();
//             System.out.println(currentPlayer.getName() + ":");

//             //checking if player can end
//             if (currentPlayer.hasUno()){
//                 //player's last card is action card = cannot end
//                 if (currentHand.getCardsInHand().get(0).getNumericValue() >= 10){
//                     draw(currentPlayer, 1);
//                     System.out.print("You can't end with an action card. Drawing card (press enter until continue)");
//                     sc.nextLine();
//                     sc.nextLine();
//                     currentIdx = currentEvaluator.movePlayer(currentIdx, playerCount);
//                     continue;
//                 }
//             }

//             ArrayList<Card> currentlyPicked = new ArrayList<>(); 

//             // when table is empty
//             // entire if for first turn only
//             if (currentTable.getMostRecent() == null){
//                 firstTurn(currentHand);
//                 currentIdx = currentEvaluator.movePlayer(currentIdx, playerCount);
//                 continue;
//             }

//             System.out.println ("Top card on table: " + currentTable.getMostRecent());

//             boolean playable = false;
//             while(!playable){


//                 int result = 0;
//                 setPlayableCards(currentHand, currentlyPicked);
//                 while (!(result == 1 || result == 2)){
//                     try {
//                         System.out.println("Actions:");
//                         System.out.printf("1) Draw\n2) Play\nOption: ");
//                         result = sc.nextInt();
//                         if (!(result == 1 || result == 2)){
//                             throw new InputMismatchException();
//                         }
                        
//                     } catch (InputMismatchException e) {
//                         System.out.print("\nPlease enter 1 or 2\n");
//                         sc.nextLine();
//                     }
//                 }
//                 //if player draws
//                 if (result == 1){
//                     draw(currentPlayer, 1);
//                     currentIdx = currentEvaluator.movePlayer(currentIdx, playerCount);
//                     break;
//                 }
//                 selectCards(currentHand, currentPlayer, currentlyPicked, sc);

               
//                 if (currentlyPicked.size() == 0){ // autodraw
//                     draw(currentPlayer, 1);
//                     sc.nextLine();
//                     System.out.print("A card has been drawn one for u dumbass (press enter dumbass)");
//                     sc.nextLine();
//                     currentIdx = currentEvaluator.movePlayer(currentIdx, playerCount);
//                     break;
//                 }
//                 playable = true;

//                 currentTable.setMostRecent(currentlyPicked.get(currentlyPicked.size() - 1));
//                 for (Card c : currentlyPicked) {
//                     currentTable.getTableCards().add(c);
//                     currentHand.getCardsInHand().remove(c);
//                 }

//                 // System.out.println("currentpicked : " + currentlyPicked);

//                 //check card effect
//                 currentIdx = currentEvaluator.effect(currentlyPicked, currentTable, playerCount, currentIdx, sc);

//                 sc.nextLine();
//                 System.out.print("Ending turn (press enter)");
//                 sc.nextLine();
//             }

//             //update cards to draw
//             cardsToDraw = currentEvaluator.getCardsToDraw();

//             //reset draw
//             currentEvaluator.resetDraw();

            

//             // when player win
//             if (currentHand.getCardsInHand().size() == 0){
//                 System.out.println("Player " + currentPlayer.getName() + " Wins!");
//                 gameOver = true;
//             }
//         }
//     }
// }

