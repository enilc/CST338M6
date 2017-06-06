/* ---------------------------------------------------------------------------------------------------------------- 
Nautilus Group
Caleb Allen
Daisy Mayorga
David Harrison
Dustin Whittington
Michael Cline
CST 338
M6: GUI Card Java Program
30 May 2017
Phase 2
----------------------------------------------------------------------------------------------------------------- */
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;



public class Assig6
{
   static final int NUM_CARDS_PER_HAND = 7;
   static final int NUM_PLAYERS = 2;
   static int playedCardID = -1;
private static GameTimer timer;

   public static void main(String[] args)
   {
      // Constants from spec
      int numPacksPerDeck = 1;
      int numJokersPerPack = 0;
      int numUnusedCardsPerPack = 0;
      Card[] unusedCardsPerPack = null;

      // Create highCardGame handler
      CardGameFramework highCardGame = new CardGameFramework(numPacksPerDeck, numJokersPerPack, numUnusedCardsPerPack,
            unusedCardsPerPack, NUM_PLAYERS, NUM_CARDS_PER_HAND);
      // Create game table as in previous phases.
      CardTable myCardTable = new CardTable("CardTable", NUM_CARDS_PER_HAND, NUM_PLAYERS, timer);
      // Deal cards to players.

      GameTimer timer = new GameTimer();
      timer.startTimer();
      
      Controller controller = new Controller(myCardTable, highCardGame);
   }
}

class GameTimer extends JLabel
{
   private Timer time;
   private static final long serialVersionUID = 1L;
   private String text;
   
   public GameTimer()
   {
      text = "";
      time = null;
   }
   
   public void startTimer()
   {
      time = new Timer();
      Thread t = new Thread(time);
      t.start();
   }
   
   public void addText(String text) 
   {
      this.setText(text);
   }
   
   public void toggleTimer() 
   {
      time.toggleTimer();
   }
   
   private class Timer extends Thread
   {
      private long startTime;
      private long cachedTime;
      private boolean pauseTimer;
      
      public Timer()
      {
         pauseTimer = false;
         startTime = System.currentTimeMillis();
         cachedTime = 0;
      }
      
      @Override
      public void run() 
      {
         while(true){
            
            long time = timerPaused();
            
            long sec = time / 1000 ;
            long min = sec / 60;
            sec = sec % 60;

            text = String.format("%02d:%02d", min , sec );
            addText(text);
         }
      }
   
      
      public long timerPaused()
      {
    
         long time = System.currentTimeMillis();
         
         while(pauseTimer){
            doNothing(0);
            cachedTime = System.currentTimeMillis() - time;
         }
         
         return time - startTime - cachedTime;
      }
      
      private void toggleTimer()
      {
         pauseTimer = !pauseTimer;
      }
      
      private void doNothing(int millis)
      {
         try
         {
            Thread.sleep(millis);
         }
         catch(InterruptedException e)
         {
            System.out.println("Unexpected Interuption");
            System.exit(0);
         }
      }
      
    } 
}


class CardGameFramework
{
   private static final int MAX_PLAYERS = 50;

   private int numPlayers;
   private int numPacks; // # standard 52-card packs per deck
                         // ignoring jokers or unused cards
   private int numJokersPerPack; // if 2 per pack & 3 packs per deck, get 6
   private int numUnusedCardsPerPack; // # cards removed from each pack
   private int numCardsPerHand; // # cards to deal each player
   private Deck deck; // holds the initial full deck and gets
                      // smaller (usually) during play
   private Hand[] hand; // one Hand for each player
   private Card[] unusedCardsPerPack; // an array holding the cards not used
                                      // in the game. e.g. pinochle does not
                                      // use cards 2-8 of any suit
   // Variables to track winnings.
   private Card[] playerWinnings;
   private int playerWinningsCount;
   private Card[] computerWinnings;
   private int computerWinningsCount;

   // last cards played by human and computer
   private Card lastHumanCard;
   private Card lastComputerCard;

   public CardGameFramework(int numPacks, int numJokersPerPack, int numUnusedCardsPerPack, Card[] unusedCardsPerPack,
         int numPlayers, int numCardsPerHand)
   {
      playerWinnings = new Card[numPacks * 56];
      computerWinnings = new Card[numPacks * 56];
      playerWinningsCount = 0;
      computerWinningsCount = 0;
      int k;

      // filter bad values
      if (numPacks < 1 || numPacks > 6)
         numPacks = 1;
      if (numJokersPerPack < 0 || numJokersPerPack > 4)
         numJokersPerPack = 0;
      if (numUnusedCardsPerPack < 0 || numUnusedCardsPerPack > 50) // > 1 card
         numUnusedCardsPerPack = 0;
      if (numPlayers < 1 || numPlayers > MAX_PLAYERS)
         numPlayers = 4;
      // one of many ways to assure at least one full deal to all players
      if (numCardsPerHand < 1 || numCardsPerHand > numPacks * (52 - numUnusedCardsPerPack) / numPlayers)
         numCardsPerHand = numPacks * (52 - numUnusedCardsPerPack) / numPlayers;

      // allocate
      this.unusedCardsPerPack = new Card[numUnusedCardsPerPack];
      this.hand = new Hand[numPlayers];
      for (k = 0; k < numPlayers; k++)
         this.hand[k] = new Hand();
      deck = new Deck(numPacks);

      // assign to members
      this.numPacks = numPacks;
      this.numJokersPerPack = numJokersPerPack;
      this.numUnusedCardsPerPack = numUnusedCardsPerPack;
      this.numPlayers = numPlayers;
      this.numCardsPerHand = numCardsPerHand;
      for (k = 0; k < numUnusedCardsPerPack; k++)
         this.unusedCardsPerPack[k] = unusedCardsPerPack[k];

      // prepare deck and shuffle
      newGame();
   }

   // constructor overload/default for game like bridge
   public CardGameFramework()
   {
      this(1, 0, 0, null, 4, 13);
   }

   public Card getLastHumanCard()
   {
      return lastHumanCard;
   }

   public void setLastHumanCard(Card lastHumanCard)
   {
      this.lastHumanCard = lastHumanCard;
   }

   public Card getLastComputerCard()
   {
      return lastComputerCard;
   }

   public void setLastComputerCard(Card lastComputerCard)
   {
      this.lastComputerCard = lastComputerCard;
   }

   public Hand getHand(int k)
   {
      // hands start from 0 like arrays

      // on error return automatic empty hand
      if (k < 0 || k >= numPlayers)
         return new Hand();

      return hand[k];
   }

   public Card getCardFromDeck()
   {
      return deck.dealCard();
   }

   public int getNumCardsRemainingInDeck()
   {
      return deck.getNumCards();
   }

   public void newGame()
   {
      int k, j;

      // clear the hands
      for (k = 0; k < numPlayers; k++)
         hand[k].resetHand();

      // restock the deck
      deck.init(numPacks);

      // remove unused cards
      for (k = 0; k < numUnusedCardsPerPack; k++)
         deck.removeCard(unusedCardsPerPack[k]);

      // add jokers
      for (k = 0; k < numPacks; k++)
         for (j = 0; j < numJokersPerPack; j++)
            deck.addCard(new Card('X', Card.Suit.values()[j]));

      // shuffle the cards
      deck.shuffle();
   }

   public boolean deal()
   {
      // returns false if not enough cards, but deals what it can
      int k, j;
      boolean enoughCards;

      // clear all hands
      for (j = 0; j < numPlayers; j++)
         hand[j].resetHand();

      enoughCards = true;
      for (k = 0; k < numCardsPerHand && enoughCards; k++)
      {
         for (j = 0; j < numPlayers; j++)
            if (deck.getNumCards() > 0)
               hand[j].takeCard(deck.dealCard());
            else
            {
               enoughCards = false;
               break;
            }
      }
      return enoughCards;
   }

   void sortHands()
   {
      int k;

      for (k = 0; k < numPlayers; k++)
         hand[k].sort();
   }

   Card playCard(int playerIndex, int cardIndex)
   {
      // returns bad card if either argument is bad
      if (playerIndex < 0 || playerIndex > numPlayers - 1 || cardIndex < 0 || cardIndex > numCardsPerHand - 1)
      {
         // Creates a card that does not work
         return new Card('M', Card.Suit.spades);
      }
      return hand[playerIndex].playCard(cardIndex);
   }

   public void playerWonRound(Card lastHumanCard, Card lastComputerCard)
   {
      playerWinnings[playerWinningsCount++] = lastHumanCard;
      playerWinnings[playerWinningsCount++] = lastComputerCard;
   }

   public void computerWonRound(Card lastHumanCard, Card lastComputerCard)
   {
      computerWinnings[computerWinningsCount++] = lastHumanCard;
      computerWinnings[computerWinningsCount++] = lastComputerCard;
   }

   public Card[] getPlayerWinnings()
   {
      return playerWinnings;
   }

   public Card[] getComputerWinnings()
   {
      return computerWinnings;
   }

   boolean takeCard(int playerIndex)
   {
      // returns false if either argument is bad
      if (playerIndex < 0 || playerIndex > numPlayers - 1)
         return false;
      // Are there enough Cards?
      if (deck.getNumCards() <= 0)
         return false;
      return hand[playerIndex].takeCard(deck.dealCard());
   }
}

class CardTable extends JFrame
{
   static final int MAX_CARDS_PER_HAND = 56;
   static final int MAX_PLAYERS = 2;

   private int numCardsPerHand;
   private int numPlayers;
   
   private GameTimer timer;
   
   private JPanel pnlComputerHand, pnlHumanHand, pnlPlayArea;
   private JPanel pnlControls;
   private JPanel pnlTimer;
   private JLabel[] computerLabels;
   private JLabel[] humanLabels;
   private JLabel[] playedCardLabels;
   private JLabel[] playLabelText;
   
   private JButton toggleTimerBtn;

 
   public CardTable(String title, int numCardsPerHand, int numPlayers, GameTimer timer)
   {
      /*
       * We will use three Public JPanels, one for each hand (player-bottom and
       * computer-top) and a middle "playing" JPanel. The client (below) will
       * generate the human's cards at random and will be visible in the bottom
       * JPanel, while the computer's cards will be chosen (again, by the
       * client) to be all back-of-card images in the top JPanel. The middle
       * JPanel will display cards that are "played" by the computer and human
       * during the conflict. Let's assume that each player plays one card per
       * round, so for a 2-person game (computer + human) there will be exactly
       * two cards played in the central region per round of battle. My client
       * chose a joker for the two central cards, just so we would have
       * something to see in the playing region.
       */
      super(title);
      this.numPlayers = numPlayers;
      this.numCardsPerHand = numCardsPerHand;
      this.timer = timer;
      
      // computer's hand
      pnlComputerHand = new JPanel();
      pnlComputerHand.setBorder(BorderFactory.createTitledBorder("Computer Hand"));

      // player's hand
      pnlHumanHand = new JPanel();
      pnlHumanHand.setBorder(BorderFactory.createTitledBorder("Your Hand"));

      // play area
      pnlPlayArea = new JPanel();
      pnlPlayArea.setLayout(new GridLayout(2, 2));
      pnlPlayArea.setBorder(BorderFactory.createTitledBorder("Play Area"));

      // Controls
      pnlTimer = new JPanel();
      //pnlTimer.add(this.timer);
      pnlControls = new JPanel();
      toggleTimerBtn = new JButton("Stop Timer");
      pnlControls.add(pnlTimer);
      pnlControls.add(toggleTimerBtn);
      pnlControls.setBorder(BorderFactory.createTitledBorder("Controls")); 

      add(pnlComputerHand, BorderLayout.NORTH);
      add(pnlHumanHand, BorderLayout.SOUTH);
      add(pnlPlayArea, BorderLayout.CENTER);
      add(pnlControls, BorderLayout.EAST);

      setSize(1200, 600);
      setLocationRelativeTo(null);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setVisible(true);
   }

   public GameTimer getTimer()
   {
      return timer;
   }
   public int getNumCardsPerHand()
   {
      /*
       * Accessor to get the number of cards per hand
       */
      return this.numCardsPerHand;
   }

   public int getNumPlayers()
   {
      /*
       * Accessor to get the number of players
       */
      return this.numPlayers;
   }

   public void displayRoundResults(String results)
   {
      JOptionPane.showMessageDialog(pnlPlayArea, results);
   }

   public void displayEndGameResults(Card[] computerWinnings, Card[] playerWinnings)
   {
      remove(pnlPlayArea);
      pnlComputerHand.removeAll();
      pnlComputerHand.setBorder(BorderFactory.createTitledBorder("Computer Winnings"));
      pnlHumanHand.removeAll();
      pnlHumanHand.setBorder(BorderFactory.createTitledBorder("Your Winnings"));
      computerLabels = new JLabel[computerWinnings.length];
      for (int i = 0; i < computerLabels.length; i++)
      {
         if (computerWinnings[i] == null)
         {

         } else
         {
            computerLabels[i] = new JLabel(GUICard.getIcon(computerWinnings[i]));
            pnlComputerHand.add(computerLabels[i]);
         }
         humanLabels = new JLabel[playerWinnings.length];
      }

      for (int i = 0; i < humanLabels.length; i++)
      {
         if (playerWinnings[i] == null)
         {

         } else
         {
            humanLabels[i] = new JLabel(GUICard.getIcon(playerWinnings[i]));
            pnlHumanHand.add(humanLabels[i]);
         }
      }
      repaint();
      revalidate();
   }

   public void updatePlayAreaPanel(int computerHandID, int playerHandID, Card lastComputerCard, Card lastHumanCard)
   {
      // play area
      pnlPlayArea.removeAll();
      playLabelText = new JLabel[numPlayers];
      int playerCount = 0;
      for (JLabel l : playLabelText)
      {
         if (playerCount == computerHandID)
         {
            l = new JLabel("Computer ", JLabel.CENTER);
         } else
         {
            l = new JLabel("Player " + playerCount, JLabel.CENTER);
         }
         pnlPlayArea.add(l, JLabel.CENTER);
         playerCount++;
      }
      playedCardLabels = new JLabel[numPlayers];
      for (int i = 0; i < numPlayers; i++)
      {
         if (i == computerHandID)
         {
            try
            {
               playedCardLabels[i] = new JLabel(GUICard.getIcon(lastComputerCard));
            } catch (Exception exception)
            {
               playedCardLabels[i] = new JLabel();
            }
         } else
         {
            try
            {
               playedCardLabels[i] = new JLabel(GUICard.getIcon(lastHumanCard));
            } catch (Exception exception)
            {
               playedCardLabels[i] = new JLabel();
            }
         }
         pnlPlayArea.add(playedCardLabels[i], JLabel.CENTER);
      }
      repaint();
      revalidate();
   }

   public void updatePlayerHandPanel(Hand playerHand)
   {
      // player's hand
      pnlHumanHand.removeAll();
      humanLabels = new JLabel[playerHand.getNumCards()];
      for (int i = 0; i < humanLabels.length; i++)
      {
         humanLabels[i] = new JLabel(GUICard.getIcon(playerHand.inspectCard(i)));
         JButton b = new JButton(new Integer(i).toString());
         b.addActionListener(new cardPlayListener());
         b.add(humanLabels[i]);
         pnlHumanHand.add(b);
      }

      repaint();
      revalidate();
   }

   public void updateComputerHandPanel(Hand computerHand)
   {
      // Computer's hand
      pnlComputerHand.removeAll();
      computerLabels = new JLabel[computerHand.getNumCards()];
      for (int i = 0; i < computerLabels.length; i++)
      {
         computerLabels[i] = new JLabel(GUICard.getBackCardIcon());
         pnlComputerHand.add(computerLabels[i]);
      }
      repaint();
      revalidate();
   }
   
   public void toggleTimerListener(ActionListener l)
   {
      toggleTimerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
      toggleTimerBtn.addActionListener(l);
   }
   public boolean setTimerButtonText(String text)
   {
      if(text == null)
         return false;
      
      toggleTimerBtn.setText(text);
      return true;
   }
}

class Controller
{
   private CardTable view;
   private CardGameFramework model;
   boolean isComputerFirst = false;
   boolean isComputerFinished = true;
   // The array index of card played by human
   static int playedCardID = -1;
   // Array Index of hands
   private final int humanHandID = 1;
   private final int computerHandID = 0;

   public Controller(CardTable view, CardGameFramework model)
   {
      this.view = view;
      this.model = model;
      // timer
      this.view.toggleTimerListener(new ToggleTimerListener());
      // deal the cards to all the hands
      model.deal();
      // update hand panels to show cards in hands
      view.updateComputerHandPanel(model.getHand(computerHandID));
      view.updatePlayerHandPanel(model.getHand(humanHandID));

      // Loop of game
      play();
   }

   private void play()
   {
      // Timer to allow for game to timeout if ignored.
      long activityTimer = (new Date().getTime()) / 1000;

      // While the program has not timed out. (5 minutes)
      // and there are more cards to play.
      while (((((new Date().getTime()) / 1000) - activityTimer) < 300) && model.getHand(humanHandID).getNumCards() > 0)
      {
         if (isComputerFirst && !isComputerFinished)
         {
            // run the computer's AI. Play as if trying to beat a card of
            // average value.
            int computerIndex = computerTurn(model.getHand(computerHandID), new Card('8', Card.Suit.spades));
            // set computer's card choice.
            model.setLastComputerCard(model.playCard(computerHandID, computerIndex));
            isComputerFinished = true;

            // change play area to show computer's card only
            view.updatePlayAreaPanel(computerHandID, humanHandID, model.getLastComputerCard(), null);
         }
         if (playedCardID >= 0)
         {
            // We play the card clicked by the player onto the board.
            // model.lastHumanCard = model.playCard(model.humanHandID,
            // playedCardID);
            model.setLastHumanCard(model.playCard(humanHandID, playedCardID));

            // Choose computer card if the computer is going second.
            if (!isComputerFirst)
            {
               // run the computer's AI.
               int computerIndex = computerTurn(model.getHand(computerHandID), model.getLastHumanCard());
               // set computer's card choice.
               // model.lastComputerCard = model.playCard(model.computerHandID,
               // computerIndex);
               model.setLastComputerCard(model.playCard(computerHandID, computerIndex));
            }

            view.updatePlayAreaPanel(computerHandID, humanHandID, model.getLastComputerCard(),
                  model.getLastHumanCard());

            // Figure out who won and store winnings
            if (compareCards(model.getLastHumanCard(), model.getLastComputerCard()) > 0)
            {
               model.playerWonRound(model.getLastHumanCard(), model.getLastComputerCard());
               view.displayRoundResults(
                     "You Win!\n" + model.getLastHumanCard() + " is greater than " + model.getLastComputerCard());

               // make player go first next round
               isComputerFirst = false;
               isComputerFinished = false;
            } else if (compareCards(model.getLastHumanCard(), model.getLastComputerCard()) < 0)
            {
               model.computerWonRound(model.getLastHumanCard(), model.getLastComputerCard());
               view.displayRoundResults(
                     "You Lose!\n" + model.getLastHumanCard() + " is less than " + model.getLastComputerCard());

               // make computer go first next round
               isComputerFirst = true;
               isComputerFinished = false;
            }
            // update both hand panels and play area panel for next round
            view.updateComputerHandPanel(model.getHand(computerHandID));
            view.updatePlayerHandPanel(model.getHand(humanHandID));
            view.updatePlayAreaPanel(computerHandID, humanHandID, null, null);
            // Loop Housekeeping, reset timer and clicked ID.
            playedCardID = -1;
            activityTimer = (new Date().getTime()) / 1000;
         }
      }
      view.displayEndGameResults(model.getComputerWinnings(), model.getPlayerWinnings());
   }

   private static int computerTurn(Hand computerHand, Card opponentCard)
   {
      /*
       * Game AI for Computer if the computer is going second.
       * 
       * Returns the index of the card in hand that the computer wishes to play.
       */

      int bestOptionIndex = -1;
      int bestOptionDifference = 1000;

      int lowestCardIndex = -1;
      int lowestCardDifference = 1000;

      // compare each card in hand to the player's.
      for (int i = 0; i < computerHand.getNumCards(); i++)
      {
         int difference = compareCards(computerHand.inspectCard(i), opponentCard);

         // Find the card that is closest to, but still beats, the opponent.
         if (difference > 0 && difference < bestOptionDifference)
         {
            bestOptionIndex = i;
            bestOptionDifference = difference;
         }

         // While we're here, find the lowest card in hand.
         if (difference <= 0 && difference < lowestCardDifference)
         {
            lowestCardIndex = i;
            lowestCardDifference = difference;
         }
      }

      // If we have a card that beats the opponent's, play it. No hoarding.
      if (bestOptionIndex >= 0)
      {
         return bestOptionIndex;
      }
      // If we don't have a card that can win, burn the lowest card.
      else
      {
         return lowestCardIndex;
      }
   }

   private static int compareCards(Card cardOne, Card cardTwo)
   {
      /*
       * Compares two cards.
       * 
       * Returns a positive number if cardOne > cardTwo Returns a negative
       * number if cardOne < cardTwo Returns zero if cardOne is equal to cardTwo
       */
      // If the cards are the same.
      if (cardOne.equals(cardTwo))
      {
         return 0;
      }
      // If values are the same, only suits matter.
      else if (cardOne.getValue() == cardTwo.getValue())
      {
         int indexOne = -1;
         int indexTwo = -1;
         for (int i = 0; i < Card.suitRanks.length; i++)
         {
            if (cardOne.getSuit() == Card.suitRanks[i])
            {
               indexOne = i;
            }
            if (cardTwo.getSuit() == Card.suitRanks[i])
            {
               indexTwo = i;
            }
         }
         return indexOne - indexTwo;
      }
      // compare card value primarily
      else
      {
         int indexOne = -1;
         int indexTwo = -1;
         for (int i = 0; i < Card.valuRanks.length; i++)
         {
            if (cardOne.getValue() == Card.valuRanks[i])
            {
               indexOne = i;
            }
            if (cardTwo.getValue() == Card.valuRanks[i])
            {
               indexTwo = i;
            }
         }
         return indexOne - indexTwo;
      }
   }

   static void playCardFromUI(String cardID)
   {
      /*
       * This function is here for cardPlayListener to use to pass the index of
       * a card that the player wishes to play from their hand.
       */
      int id = Integer.parseInt(cardID);
      if (id >= 0 && id < 50)
      {
         playedCardID = Integer.parseInt(cardID);
      }
   }
}



// ActionListener to allow the player to play the card they click on.
class cardPlayListener implements ActionListener
{
   public void actionPerformed(ActionEvent e)
   {
      String cardID = e.getActionCommand();
      Controller.playCardFromUI(cardID);
   }
}
class ToggleTimerListener implements ActionListener
{
   private boolean playing;
   private GameTimer timer;
private CardTable view;
   
   public ToggleTimerListener()
   {
      playing = true;
      timer = view.getTimer();
   }
   
   @Override
   public void actionPerformed(ActionEvent e)
   {
      if(playing)
      {
         playing = false;
         view.setTimerButtonText("Start Timer");
      }
      else{
         playing = true;
         view.setTimerButtonText("Stop Timer");
      }
      timer.toggleTimer();
   }
   
}

class Card
{
   /*
    * CARD UPDATES REQUIRED Values need to be updated to include Joker public
    * static char[] valuRanks //used to rank the cards from low to high static
    * void arraySort(Card[], int arraySize) //use a bubble sort routine to sort
    * an array of cards
    */
   // Enumerator for the card's suit
   public enum Suit
   {
      clubs, diamonds, hearts, spades
   };

   // For card ranking purposes
   public static char[] valuRanks =
   { '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A', 'X' };
   public static Suit[] suitRanks =
   { Suit.clubs, Suit.diamonds, Suit.hearts, Suit.spades };

   // Private instance variables
   private char value;
   private Suit suit;
   private boolean errorFlag;

   // Default constructor for card overloaded to return A of spades
   public Card()
   {
      this.value = 'A';
      this.suit = Suit.spades;
   }

   // Parameterized constructor for card that accepts value and suit
   public Card(char value, Suit suit)
   {
      set(value, suit);
   }

   // Parameterized constructor for card that accepts value, suit,
   // and errorFlag
   public Card(char value, Suit suit, boolean errorFlag)
   {
      set(value, suit);
      this.errorFlag = errorFlag;
   }

   // If error flag is false, returns value and suit of card in a single
   // string, otherwise returns invalid
   public String toString()
   {
      if (errorFlag == false)
      {
         return this.value + " of " + this.suit;
      } else
      {
         return "[ invalid ]";
      }
   }

   // Mutator to set value and suit for a card
   public boolean set(char value, Suit suit)
   {
      if (isValid(value, suit))
      {
         this.value = value;
         this.suit = suit;
         this.errorFlag = false;
         return true;
      } else
      {
         this.errorFlag = true;
         return false;
      }
   }

   // Accessor for card suit
   public Suit getSuit()
   {
      return this.suit;
   }

   public char getValue()
   {
      /*
       * Accessor for Value
       */
      return this.value;
   }

   public boolean getErrorFlag()
   {
      /*
       * Accessor for errorFlag
       */
      return this.errorFlag;
   }

   private boolean isValid(char value, Suit suit)
   {
      /*
       * a private helper method that returns true or false, depending on the
       * legality of the parameters. Note that, although it may be impossible
       * for suit to be illegal (due to its enum-ness), we pass it, anyway, in
       * anticipation of possible changes to the type from enum to, say, char or
       * int, someday. We only need to test value, at this time.
       */
      char[] cardType =
      { 'A', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'X' };
      for (int i = 0; i < cardType.length; i++)
      {
         if (value == cardType[i])
         {
            return true;
         }
      }
      return false;
   }

   // method that returns true if all members are equal, false otherwise
   public boolean equals(Card card)
   {
      if (card == this)
      {
         return true;
      } else
      {
         return (card.getValue() == this.getValue()) && (card.getSuit().equals(this.getSuit()))
               && (card.getErrorFlag() == this.getErrorFlag());
      }
   }

   // Find the card index
   private static int cardIndex(Card card)
   {
      for (int i = 0; i < valuRanks.length; i++)
      {
         if (card.getValue() == valuRanks[i])
            return i;
      }
      return -1;
   }

   // Use the bubble sort for sorting the array
   public static void arraySort(Card[] cardArray, int arraySize)
   {
      Card temp;

      for (int i = 0; i < arraySize; i++)
      {
         for (int j = 1; j < arraySize - i; j++)
         {
            if (cardIndex(cardArray[j - 1]) > cardIndex(cardArray[j]))
            {
               temp = cardArray[j - 1];
               cardArray[j - 1] = cardArray[j];
               cardArray[j] = temp;
            }
         }
      }
   }
}

class Deck
{
   /*
    * DECK UPDATES REQUIRED Jokers need to be added to MasterPack boolean
    * addCard(Card card) //make sure that there are not too many instances of
    * the card in the deck if you add it. Return false if there will be too
    * many. It should put the card on the top of the deck. boolean
    * removeCard(Card card) // you are looking to remove a specific card from
    * the deck. Put the current top card into its place. Be sure the card you
    * need is actually still in the deck, if not return false. void sort()
    * //cards.arraySort() getNumcards() //returns topCard
    */

   // Sets maximum number of cards to be played which is 6 decks (6 * 56 = 336)
   public final static int MAX_CARDS = 336;

   // This is a private static Card array containing exactly 56 card
   // references, which point to all the standard cards
   // Avoids repeatedly declaring the same 56 cards as game play continues
   private static Card[] masterPack;

   /*
    * Changes to true as soon as masterPack is created for the first time.
    * Future deck objects will not re-create master pack once set to true.
    */
   private static boolean masterPackCreated = false;

   // private data members
   private Card[] cards;
   private int topCard;
   private int numPacks = 1;

   public Deck(int numPacks)
   {
      // Over loaded deck method so a user can request the number of decks to
      // use.
      init(numPacks);
   }

   public Deck()
   {
      // Calls init to build a deck
      numPacks = 1;
      init(numPacks);
   }

   public void init(int numPacks)
   {
      // Ensures the deck is not more than the maximum size.
      if (numPacks > 6)
      {
         numPacks = 6;
      }
      // Initializes the pointer and cards array based on the requested number
      // of packs.
      topCard = numPacks * 56;
      cards = new Card[topCard];
      // Calls allocateMasterPack to make sure the master pack has been
      // created.
      allocateMasterPack();
      // Uses arraycopy to copy the number of requested packs from masterPack
      // into cards.
      for (int count = 0; numPacks > count; count++)
      {
         System.arraycopy(masterPack, 0, cards, 56 * count, 56);
      }
   }

   public void shuffle()
   {
      int randomNumber;
      Card copy;
      Random generator = new Random();
      // Applies the Fisher-Yates shuffle algorithm to the cards array.
      for (int deckCount = topCard - 1; deckCount > 0; deckCount--)
      {
         randomNumber = generator.nextInt(deckCount + 1);
         copy = cards[randomNumber];
         cards[randomNumber] = cards[deckCount];
         cards[deckCount] = copy;
      }
   }

   public Card dealCard()
   {
      if (topCard < 0 || topCard > numPacks * 56)
      {
         return null;
      } else
      {
         // Removes the top card from the deck before reducing top card.
         Card card = cards[topCard - 1];
         cards[topCard - 1] = null;
         topCard--;
         // The top card is returned
         return card;
      }
   }

   // Accessor to return array index of top card
   // This value also tells us how many total cards are currently in the array
   public int getTopCard()
   {
      return this.topCard;
   }

   // Method to test that the index of the card is legal
   public Card inspectCard(int k)
   {
      if (k >= 0 && k <= topCard)
      {
         return new Card(cards[k].getValue(), cards[k].getSuit(), cards[k].getErrorFlag());
      } else
      {
         return new Card('Q', Card.Suit.hearts, true);
      }
   }

   private static void allocateMasterPack()
   {
      // Check if master pack has already been created by previous deck
      // objects
      if (!masterPackCreated)
      {
         // initialize card array
         masterPack = new Card[56];
         // Create all 56 Card objects for master pack
         masterPack[0] = new Card('A', Card.Suit.clubs);
         masterPack[1] = new Card('A', Card.Suit.diamonds);
         masterPack[2] = new Card('A', Card.Suit.hearts);
         masterPack[3] = new Card('A', Card.Suit.spades);
         masterPack[4] = new Card('2', Card.Suit.clubs);
         masterPack[5] = new Card('2', Card.Suit.diamonds);
         masterPack[6] = new Card('2', Card.Suit.hearts);
         masterPack[7] = new Card('2', Card.Suit.spades);
         masterPack[8] = new Card('3', Card.Suit.clubs);
         masterPack[9] = new Card('3', Card.Suit.diamonds);
         masterPack[10] = new Card('3', Card.Suit.hearts);
         masterPack[11] = new Card('3', Card.Suit.spades);
         masterPack[12] = new Card('4', Card.Suit.clubs);
         masterPack[13] = new Card('4', Card.Suit.diamonds);
         masterPack[14] = new Card('4', Card.Suit.hearts);
         masterPack[15] = new Card('4', Card.Suit.spades);
         masterPack[16] = new Card('5', Card.Suit.clubs);
         masterPack[17] = new Card('5', Card.Suit.diamonds);
         masterPack[18] = new Card('5', Card.Suit.hearts);
         masterPack[19] = new Card('5', Card.Suit.spades);
         masterPack[20] = new Card('6', Card.Suit.clubs);
         masterPack[21] = new Card('6', Card.Suit.diamonds);
         masterPack[22] = new Card('6', Card.Suit.hearts);
         masterPack[23] = new Card('6', Card.Suit.spades);
         masterPack[24] = new Card('7', Card.Suit.clubs);
         masterPack[25] = new Card('7', Card.Suit.diamonds);
         masterPack[26] = new Card('7', Card.Suit.hearts);
         masterPack[27] = new Card('7', Card.Suit.spades);
         masterPack[28] = new Card('8', Card.Suit.clubs);
         masterPack[29] = new Card('8', Card.Suit.diamonds);
         masterPack[30] = new Card('8', Card.Suit.hearts);
         masterPack[31] = new Card('8', Card.Suit.spades);
         masterPack[32] = new Card('9', Card.Suit.clubs);
         masterPack[33] = new Card('9', Card.Suit.diamonds);
         masterPack[34] = new Card('9', Card.Suit.hearts);
         masterPack[35] = new Card('9', Card.Suit.spades);
         masterPack[36] = new Card('T', Card.Suit.clubs);
         masterPack[37] = new Card('T', Card.Suit.diamonds);
         masterPack[38] = new Card('T', Card.Suit.hearts);
         masterPack[39] = new Card('T', Card.Suit.spades);
         masterPack[40] = new Card('J', Card.Suit.clubs);
         masterPack[41] = new Card('J', Card.Suit.diamonds);
         masterPack[42] = new Card('J', Card.Suit.hearts);
         masterPack[43] = new Card('J', Card.Suit.spades);
         masterPack[44] = new Card('Q', Card.Suit.clubs);
         masterPack[45] = new Card('Q', Card.Suit.diamonds);
         masterPack[46] = new Card('Q', Card.Suit.hearts);
         masterPack[47] = new Card('Q', Card.Suit.spades);
         masterPack[48] = new Card('K', Card.Suit.clubs);
         masterPack[49] = new Card('K', Card.Suit.diamonds);
         masterPack[50] = new Card('K', Card.Suit.hearts);
         masterPack[51] = new Card('K', Card.Suit.spades);
         masterPack[52] = new Card('X', Card.Suit.clubs);
         masterPack[53] = new Card('X', Card.Suit.diamonds);
         masterPack[54] = new Card('X', Card.Suit.hearts);
         masterPack[55] = new Card('X', Card.Suit.spades);
         /*
          * Set masterPackCreated to true now that master pack has been created
          * once
          */
         masterPackCreated = true;
      }
   }

   // boolean addCard(Card card)
   public boolean addCard(Card card)
   {
      int numberOfInstances = 0;
      for (int i = 0; i < topCard; i++)
      {
         if (cards[i].equals(card))
            numberOfInstances++;
      }

      if (numberOfInstances >= numPacks)
      {
         return false;
      }
      cards[topCard].set(card.getValue(), card.getSuit());
      topCard++;
      return true;
   }

   // boolean removeCard(Card card)
   public boolean removeCard(Card card)
   {
      for (int i = 0; i < topCard; i++)
      {
         if (cards[i].equals(card))
         {
            cards[i].set(cards[topCard - 1].getValue(), cards[topCard - 1].getSuit());
            cards[topCard - 1] = null;
            topCard--;
            return true;
         }
      }
      return false;
   }

   public void sort()
   {
      Card.arraySort(cards, topCard);
   }

   public int getNumCards()
   {
      return topCard;
   }
}

class GUICard
{
   private static Icon[][] iconCards = new ImageIcon[14][4]; // 14 = A thru K +
                                                             // joker
   private static Icon iconBack;
   static boolean iconsLoaded = false;

   // loads all of the card images into a 2D array one time only
   static void loadCardIcons()
   {
      if (!iconsLoaded)
      {
         for (int j = 0; j <= 3; j++)
         {
            for (int k = 0; k <= 13; k++)
            {
               iconCards[k][j] = new ImageIcon("C:\\Users\\Daisy\\Desktop\\BUILD2\\src" + turnIntIntoCardValue(k) + turnIntIntoCardSuit(j) + ".gif");
            }
         }
         iconBack = new ImageIcon("C:\\Users\\Daisy\\Desktop\\CSU 5\\src\\images\\BK.gif"); // loads the back of card
                                                    // image
         iconsLoaded = true; // this keeps array from loading more than once
      }
   }

   // accessor for front card image (56 possibilities)
   static public Icon getIcon(Card card)
   {
      loadCardIcons(); // only instantiates one time
      return iconCards[valueAsInt(card)][suitAsInt(card)];
   }

   // accessor for back of card image
   static public Icon getBackCardIcon()
   {
      loadCardIcons(); // only instantiates one time
      return iconBack;
   }

   // turns value into an int
   private static int valueAsInt(Card card)
   {
      char cardsValue = card.getValue();
      switch (cardsValue)
      {
      case 'A':
         return 0;
      case '2':
         return 1;
      case '3':
         return 2;
      case '4':
         return 3;
      case '5':
         return 4;
      case '6':
         return 5;
      case '7':
         return 6;
      case '8':
         return 7;
      case '9':
         return 8;
      case 'T':
         return 9;
      case 'J':
         return 10;
      case 'Q':
         return 11;
      case 'K':
         return 12;
      case 'X':
         return 13;
      }
      return 0;
   }

   // turns suit into an int
   private static int suitAsInt(Card card)
   {
      Card.Suit cardsSuit = card.getSuit();
      switch (cardsSuit)
      {
      case clubs:
         return 0;
      case diamonds:
         return 1;
      case hearts:
         return 2;
      case spades:
         return 3;
      }
      return 3;
   }

   // turns 0 - 13 into "A", "2", "3", ... "Q", "K", "X"
   static String turnIntIntoCardValue(int k)
   {
      switch (k)
      {
      case 0:
         return "A";
      case 1:
         return "2";
      case 2:
         return "3";
      case 3:
         return "4";
      case 4:
         return "5";
      case 5:
         return "6";
      case 6:
         return "7";
      case 7:
         return "8";
      case 8:
         return "9";
      case 9:
         return "T";
      case 10:
         return "J";
      case 11:
         return "Q";
      case 12:
         return "K";
      case 13:
         return "X";
      }
      return "A";
   }

   // turns 0 - 3 into "C", "D", "H", "S"
   static String turnIntIntoCardSuit(int j)
   {
      switch (j)
      {
      case 0:
         return "C";
      case 1:
         return "D";
      case 2:
         return "H";
      case 3:
         return "S";
      }
      return "S";
   }
}

class Hand
{
   /*
    * HAND UPDATES REQUIRED void sort() //sorts the hand by calling the
    * arraySort method for the Card class "myCards.arraySort()"
    */

   // Safeguard to prevent a runaway program from creating a monster array
   public final static int MAX_CARDS = 50;

   // private data members
   private Card[] myCards;
   private int numCards;

   public Hand()
   {
      /*
       * Default Constructor;
       */
      this.numCards = 0;
      this.myCards = new Card[MAX_CARDS];
   }

   public void resetHand()
   {
      /*
       * remove all cards from the hand (in the simplest way).
       */

      // Remove all reference to the old hand.
      this.myCards = new Card[MAX_CARDS];

      // Reset the card counter.
      this.numCards = 0;
   }

   public boolean takeCard(Card card)
   {
      /*
       * adds a card to the next available position in the myCards array. This
       * is an object copy, not a reference copy, since the source of the Card
       * might destroy or change its data after our Hand gets it -- we want our
       * local data to be exactly as it was when we received it.
       */

      // Make sure we're not above our hand size limit.
      if (numCards < MAX_CARDS)
      {
         // Create a copy of the taken card and advance the card counter.
         myCards[numCards++] = new Card(card.getValue(), card.getSuit());
         return true;
      } else
      {
         return false;
      }
   }

   public Card playCard()
   {
      /*
       * returns and removes the card in the top occupied position of the array.
       */
      if (numCards > 0)
      {
         // Make a copy of the card in the myCards array.
         Card playedCard = new Card(myCards[numCards - 1].getValue(), myCards[numCards - 1].getSuit());

         // Decrement card counter. Remove the topmost card from the array.
         myCards[--numCards] = null;
         return playedCard;
      } else
      {
         // Returns an invalid card to be consistent with inspectCard()
         return new Card('Q', Card.Suit.hearts, true);
      }
   }

   public Card playCard(int cardIndex)
   {
      /*
       * returns and removes the card in the top occupied position of the array.
       */
      if (numCards > 0 && cardIndex < numCards)
      {

         // Get the card we are playing.
         Card playedCard = new Card(myCards[cardIndex].getValue(), myCards[cardIndex].getSuit());

         // Temporary container for cards in hand.
         Card[] tempCards = new Card[numCards - 1];

         // Add cards up until index to temporary array
         for (int i = 0; i < cardIndex; i++)
         {
            tempCards[i] = myCards[i];
         }

         // Add cards after index to temporary array
         for (int i = cardIndex + 1; i < numCards; i++)
         {
            tempCards[i - 1] = myCards[i];
         }

         // Reset the player's hand
         resetHand();

         // Copy temporary references to the hand
         for (int i = 0; i < tempCards.length; i++)
         {
            myCards[i] = tempCards[i];
         }

         numCards = tempCards.length;

         return playedCard;
      } else
      {
         // Returns an invalid card to be consistent with inspectCard()
         return new Card('Q', Card.Suit.hearts, true);
      }
   }

   public String toString()
   {
      /*
       * a stringizer that the client can use prior to displaying the entire
       * hand
       */
      String output = "( ";
      for (int i = 0; i < numCards; i++)
      {
         // print the card.
         output += this.myCards[i].toString();

         // add a comma to every card except the last.
         if (i + 1 < numCards)
         {
            output += ", ";
         }
      }
      return output + " )";
   }

   public int getNumCards()
   {
      /*
       * Accessor for numCards
       */
      return this.numCards;
   }

   public Card inspectCard(int k)
   {
      /*
       * Accessor for an individual card. Returns a card with errorFlag = true
       * if k is bad.
       * 
       * Valid k: 0 <= k < numCards
       */

      // Returns card if k is valid
      if (0 <= k && k < numCards)
      {
         return new Card(myCards[k].getValue(), myCards[k].getSuit(), myCards[k].getErrorFlag());
      } else
      {
         // Returns invalid card if k is bad
         return new Card('Q', Card.Suit.hearts, true);
      }
   }

   public void sort()
   {
      Card.arraySort(myCards, numCards);
   }
}
