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
PURPOSE
Over several phases, we will be using the classes we wrote from M3 (Card, Hand, and Deck) and adding to those classes
the a GUI framework.  To do this we will use some additional classes and create some of our own.
This is the third phase out of three.  This third and final phase The final phase will add the CardGameFramework
class so that your card tools can be combined with your GUI tools to create a GUI program that has real computational
power for a GUI card game, "High Card".
----------------------------------------------------------------------------------------------------------------- */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Dimension;
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

	private static Controller.Games gameToPlay = null;

	public static void playHighCard()
	{
		//Constants from spec
		int numPacksPerDeck = 1;

		//CardGameFramework no longer needs to add jokers
		//We will now remove them instead since they are part of the masterpack
		int numUnusedCardsPerPack = 4;
		Card[] unusedCardsPerPack = 
			{
					new Card('X', Card.Suit.clubs),
					new Card('X', Card.Suit.diamonds),
					new Card('X', Card.Suit.hearts), 
					new Card('X', Card.Suit.spades)
			};
		// Create highCardGame handler
		CardGameFramework highCardGame = new CardGameFramework(numPacksPerDeck, numUnusedCardsPerPack,
				unusedCardsPerPack, NUM_PLAYERS, NUM_CARDS_PER_HAND);
		// Create game table as in previous phases.
		CardTable myCardTable = new CardTable("CardTable", NUM_CARDS_PER_HAND, NUM_PLAYERS, Controller.Games.highCard);
		// Deal cards to players.
		Controller controller = new Controller(myCardTable, highCardGame, Controller.Games.highCard);
	}

	public static void playBuild()
	{
		//Constants from spec
		int numPacksPerDeck = 1;

		//CardGameFramework no longer needs to add jokers
		//We will now remove them instead since they are part of the masterpack
		int numUnusedCardsPerPack = 4;
		Card[] unusedCardsPerPack = 
			{
					new Card('X', Card.Suit.clubs),
					new Card('X', Card.Suit.diamonds),
					new Card('X', Card.Suit.hearts), 
					new Card('X', Card.Suit.spades)
			};

		// Create highCardGame handler
		CardGameFramework buildGame = new CardGameFramework(numPacksPerDeck, numUnusedCardsPerPack,
				unusedCardsPerPack, NUM_PLAYERS, NUM_CARDS_PER_HAND);
		CardTable myCardTable = new CardTable("CardTable", NUM_CARDS_PER_HAND, NUM_PLAYERS, Controller.Games.build);
		Controller controller = new Controller(myCardTable, buildGame, Controller.Games.build);
	}


	// Mutator for gameToPlay
	public static boolean setGameToPlay(Controller.Games game)
	{
		// Get list of available games.
		Controller.Games[] availableGames = Controller.Games.values();

		// Flag indicating whether the passed game is a valid choice.
		boolean isGameFound = false;

		// Check to see if 'game' is available to play.
		for(int i = 0; i < availableGames.length && !isGameFound; i++)
		{
			if(availableGames[i] == game)
			{
				isGameFound = true;
				gameToPlay = game;
			}
		}

		return isGameFound;
	}

	// 
	public static void main(String[] args)
	{
		CardTable gameChooser = new CardTable();
		while(gameToPlay == null)
		{
			// Wait for player to choose a game.
		}

		if(gameToPlay == Controller.Games.highCard)
		{
			gameChooser.dispose();
			playHighCard();
		}
		else if(gameToPlay == Controller.Games.build)
		{
			gameChooser.dispose();
			playBuild();
		}

	}
}

class CardGameFramework
{
	private static final int MAX_PLAYERS = 50;

	private int numPlayers;
	private int numPacks; // # standard 52-card packs per deck
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

	public CardGameFramework(int numPacks, int numUnusedCardsPerPack, Card[] unusedCardsPerPack,
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
		this(1, 0, null, 4, 13);
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
		int k;

		// clear the hands
		for (k = 0; k < numPlayers; k++)
			hand[k].resetHand();

		// restock the deck
		deck.init(numPacks);

		// remove unused cards
		for (k = 0; k < numUnusedCardsPerPack; k++)
			deck.removeCard(unusedCardsPerPack[k]);

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
		if (playerIndex < 0 || playerIndex > numPlayers - 1 || cardIndex < 0 || cardIndex > hand[playerIndex].getNumCards() - 1)
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
	private static final long serialVersionUID = 1L;
	static final int MAX_CARDS_PER_HAND = 56;
	static final int MAX_PLAYERS = 2;

	private int numCardsPerHand;
	private int numPlayers;

	private JPanel pnlComputerHand, pnlHumanHand, pnlPlayArea;
	private JLabel[] computerLabels;
	private JLabel[] humanLabels;
	private JLabel[] playedCardLabels;
	private JLabel[] playLabelText;

	private Controller.Games currentGame;

	public CardTable(String title, int numCardsPerHand, int numPlayers)
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

		add(pnlComputerHand, BorderLayout.NORTH);
		add(pnlHumanHand, BorderLayout.SOUTH);
		add(pnlPlayArea, BorderLayout.CENTER);

		setSize(1200, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public CardTable(String title, int numCardsPerHand, int numPlayers,
			Controller.Games game)
	{
		this(title, numCardsPerHand, numPlayers);
		currentGame = game;
	}

	public CardTable()
	{
		super("Game Chooser");

		// Get a list of games that the user can play.
		Controller.Games[] allGames = Controller.Games.values();

		// JPanel to display game choices.
		JPanel gameChoices = new JPanel();
		gameChoices.setBorder(BorderFactory.createTitledBorder("Available Games"));

		for (int i = 0; i < allGames.length; i++)
		{
			// Make font of readable/clickable size.
			Font f = new Font("SansSerif",Font.PLAIN,30);

			// Make button for player to choose the game.
			JButton b = new JButton(allGames[i].getFriendlyName());
			b.setFont(f);

			// Add an action Listener that passes the game type.
			b.addActionListener(new gameChoiceListener(allGames[i]));


			gameChoices.add(b);
		}

		add(gameChoices);

		setSize(600, 300);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
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

	public void displayEndGameResults(int computerTurns, int playerTurns)
	{
		remove(pnlPlayArea);
		pnlComputerHand.removeAll();
		pnlComputerHand.setBorder(BorderFactory.createTitledBorder("Computer turns passed: " + computerTurns));
		pnlHumanHand.removeAll();
		pnlHumanHand.setBorder(BorderFactory.createTitledBorder("Your turns passed: " + playerTurns));
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

	public void updatePlayAreaPanel(Card pileOneCard, Card pileTwoCard)
	{
		// play area
		pnlPlayArea.removeAll();

		//Layout buttons.
		// Guidance: https://docs.oracle.com/javase/tutorial/uiswing/layout/gridbag.html
		pnlPlayArea.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		//make buttons for players to use to play cards to a specific pile
		JButton[] playedCardButtons = new JButton[numPlayers];
		for (int i = 0; i < numPlayers; i++)
		{
			if (i == 0)
			{
				try
				{
					playedCardButtons[i] = new JButton(GUICard.getIcon(pileOneCard));
				} catch (Exception exception)
				{
					playedCardButtons[i] = new JButton();
				}
			} else
			{
				try
				{
					playedCardButtons[i] = new JButton(GUICard.getIcon(pileTwoCard));
				} catch (Exception exception)
				{
					playedCardButtons[i] = new JButton();
				}
			}
			// Buttons will correspond to a pile ID that is used by controller
			playedCardButtons[i].setActionCommand(Integer.toString(i));
			playedCardButtons[i].addActionListener(new pilePlayListener());

			// Format the two buttons to fit nicely
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = i;
			c.gridy = 0;


			pnlPlayArea.add(playedCardButtons[i], c);
			//pnlPlayArea.add(playedCardButtons[i]);

		}
		// Create a button for the player to indicate they cannot play a card.
		JButton cannotPlayButton = new JButton("Cannot Play!");
		cannotPlayButton.addActionListener(new cannotPlayListener());

		// Present button with large, readable font.
		Font f = new Font("SansSerif",Font.PLAIN,30);
		cannotPlayButton.setFont(f);

		// Format this button to span the other two.
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 1;

		pnlPlayArea.add(cannotPlayButton, c);


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
			b.addActionListener(new cardPlayListener(currentGame,b));
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
}

class Controller
{
	private CardTable view;
	private CardGameFramework model;

	//These variables (isComputerFirst - lastComputerCard) are only used by HighCard
	boolean isComputerFirst = false;
	boolean isComputerFinished = true;
	// last cards played by human and computer
	private Card lastHumanCard;
	private Card lastComputerCard;

	// The array index of card played by human
	static int playedCardID = -1;

	//These variables (computerTurn - isUnableToPlay) are only used by Build
	boolean computerTurn = false;
	private boolean emptyDeck = false;
	int playerTurnsSkipped = 0;
	int computerTurnsSkipped = 0;
	Card leftPlayCard;
	Card rightPlayCard;

	// Index of pile human plays card onto
	static int playedPileID = -1;

	// Flag for Build Game's "Cannot Play" button
	static boolean isUnableToPlay = false;

	// Array Index of hands
	private final int humanHandID = 1;
	private final int computerHandID = 0;



	private Games currentGame;

	public enum Games
	{
		highCard("High Card"), build("Build");

		// Needed a friendly name for UI printing.
		// Guidance: https://stackoverflow.com/questions/13291076/java-enum-why-use-tostring-instead-of-name
		private String friendlyName;

		private Games(String friendly)
		{
			friendlyName = friendly;
		}

		public String getFriendlyName()
		{
			return friendlyName;
		}

	};

	public Controller(CardTable view, CardGameFramework model, Games game)
	{
		this.view = view;
		this.model = model;
		// deal the cards to all the hands
		model.deal();
		// update hand panels to show cards in hands
		view.updateComputerHandPanel(model.getHand(computerHandID));
		view.updatePlayerHandPanel(model.getHand(humanHandID));
		// Loop of game
		if(game == Games.highCard)
		{
			currentGame = game;
			playHighCard();
		}
		else if(game == Games.build)
		{
			currentGame = game;
			playBuild();
		}

	}

	private void playBuild()
	{
		//Initiates the play area
		leftPlayCard = model.getCardFromDeck();
		rightPlayCard = model.getCardFromDeck();
		view.updatePlayAreaPanel(leftPlayCard, rightPlayCard);

		// Timer to allow for game to timeout if ignored.
		long activityTimer = (new Date().getTime()) / 1000;

		// While the program has not timed out. (5 minutes)
		// and there are more cards to play.
		while (((((new Date().getTime()) / 1000) - activityTimer) < 300) && model.getHand(humanHandID).getNumCards() > 0)
		{
			//if the deck is empty we end the game
			if (emptyDeck == true)
			{
				view.displayEndGameResults(computerTurnsSkipped, playerTurnsSkipped);
			}

			//initiates the computers turn
			if (this.computerTurn == true)
			{
				buildComputerTurn();
			}

			//Updates once the player has selected a card from their hand.
			if (playedCardID >= 0)
			{

				//Triggered once the player has also tried to play a card from their hand
				//onto one of the piles.
				if (playedPileID == 0 && checkValues(humanHandID, playedCardID, leftPlayCard))
				{
					leftPlayCard = model.playCard(humanHandID, playedCardID);
					view.updatePlayAreaPanel(leftPlayCard, rightPlayCard);
					buildHumanDraw();
				}
				if (playedPileID == 1 && checkValues(humanHandID, playedCardID, rightPlayCard))
				{
					rightPlayCard = model.playCard(humanHandID, playedCardID);
					view.updatePlayAreaPanel(leftPlayCard, rightPlayCard);
					buildHumanDraw();
				}
			}
			//Triggered if the player presses the "Cannot Play!" button and ends the current turn
			if (isUnableToPlay == true)
			{
				playerTurnsSkipped++;
				buildHumanDraw();
				isUnableToPlay = false;
			}
		}
	}

	//used to check if the card being played is compatible with the pile it is played on
	private boolean checkValues(int playerID, int cardID, Card card)
	{
		//The starting case will be the value of the pile a card was played on.
		//Each case checks if the played card is the value above or below the pile card.
		//If the played card is not valid, false is returned.
		switch (card.getValue()) {
		case 'A':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == 'K' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == '2')
			{
				return true;
			}
			return false;
		case '2':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == 'A' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == '3')
			{
				return true;
			}
			return false;
		case '3':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == '2' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == '4')
			{
				return true;
			}
			return false;
		case '4':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == '3' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == '5')
			{
				return true;
			}
			return false;
		case '5':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == '4' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == '6')
			{
				return true;
			}
			return false;
		case '6':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == '5' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == '7')
			{
				return true;
			}
			return false;
		case '7':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == '6' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == '8')
			{
				return true;
			}
			return false;
		case '8':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == '7' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == '9')
			{
				return true;
			}
			return false;
		case '9':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == '8' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == 'T')
			{
				return true;
			}
			return false;
		case 'T':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == '9' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == 'J')
			{
				return true;
			}
			return false;
		case 'J':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == 'T' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == 'Q')
			{
				return true;
			}
			return false;
		case 'Q':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == 'J' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == 'K')
			{
				return true;
			}
			return false;
		case 'K':
			if(model.getHand(playerID).inspectCard(cardID).getValue() == 'Q' 
			|| model.getHand(playerID).inspectCard(cardID).getValue() == 'A')
			{
				return true;
			}
			return false;
		default:
			return false;
		}
	}

	private void buildHumanDraw()
	{
		//The player draws a card, the panel is updated, and the listener variables are reset.
		model.getHand(humanHandID).takeCard(model.getCardFromDeck());
		view.updatePlayerHandPanel(model.getHand(humanHandID));
		playedPileID = -1;
		playedCardID = -1;

		//After drawing, it is the computers turn
		computerTurn = true;

		//Make sure the deck is not empty
		if(model.getNumCardsRemainingInDeck() == 0)
		{
			this.emptyDeck = true;
		}
	}

	private void buildComputerTurn()
	{
		boolean cardFound = false;
		Random generator = new Random();
		int randomNumber = 0;
		//The AI will check each card in it's hand and play the first playable card it comes across.
		for (int cardCount = 0; cardCount < model.getHand(computerHandID).getNumCards(); cardCount++)
		{
			//Rolls a random number between 0 and 1
			randomNumber = generator.nextInt(1);

			//If randomNumber is 0, the AI favors the leftPlayCard
			if (randomNumber == 0 && checkValues(computerHandID, cardCount, leftPlayCard))
			{
				leftPlayCard = model.playCard(computerHandID, cardCount);
				view.updatePlayAreaPanel(leftPlayCard, rightPlayCard);
				buildComputerDraw();
				cardFound = true;
				break;
			}
			if (randomNumber == 0 && checkValues(computerHandID, cardCount, rightPlayCard))
			{
				rightPlayCard = model.playCard(computerHandID, cardCount);
				view.updatePlayAreaPanel(leftPlayCard, rightPlayCard);
				buildComputerDraw();
				cardFound = true;
				break;
			}

			//If randomNumber is 1, the AI favors the rightPlayCard
			if (randomNumber == 1 && checkValues(computerHandID, cardCount, rightPlayCard))
			{
				rightPlayCard = model.playCard(computerHandID, cardCount);
				view.updatePlayAreaPanel(leftPlayCard, rightPlayCard);
				buildComputerDraw();
				cardFound = true;
				break;
			}
			if (randomNumber == 1 && checkValues(computerHandID, cardCount, leftPlayCard))
			{
				leftPlayCard = model.playCard(computerHandID, cardCount);
				view.updatePlayAreaPanel(leftPlayCard, rightPlayCard);
				buildComputerDraw();
				cardFound = true;
				break;
			}
		}
		if (!cardFound)
		{
			//If we make it here the AI did not find a matching card it could play.
			buildComputerDraw();
			//increments the number of times the AI has to skip it's turn.
			computerTurnsSkipped++;
		}
	}

	private void buildComputerDraw()
	{
		//The computer draws a card and the hand panel is updated.
		model.getHand(computerHandID).takeCard(model.getCardFromDeck());
		view.updateComputerHandPanel(model.getHand(computerHandID));

		//Play is passed back to the player
		computerTurn = false; 

		//Verify the deck is not empty yet
		if(model.getNumCardsRemainingInDeck() == 0)
		{
			this.emptyDeck = true;
		}
	}

	private void playHighCard()
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
				int computerIndex = highCardComputerTurn(model.getHand(computerHandID), new Card('8', Card.Suit.spades));
				// set computer's card choice.
				lastComputerCard = model.playCard(computerHandID, computerIndex);
				isComputerFinished = true;

				// change play area to show computer's card only
				view.updatePlayAreaPanel(computerHandID, humanHandID, lastComputerCard, null);
			}
			if (playedCardID >= 0)
			{
				// We play the card clicked by the player onto the board.
				// model.lastHumanCard = model.playCard(model.humanHandID,
				// playedCardID);
				lastHumanCard = model.playCard(humanHandID, playedCardID);

				// Choose computer card if the computer is going second.
				if (!isComputerFirst)
				{
					// run the computer's AI.
					int computerIndex = highCardComputerTurn(model.getHand(computerHandID), lastHumanCard);
					// set computer's card choice.
					// model.lastComputerCard = model.playCard(model.computerHandID,
					// computerIndex);
					lastComputerCard = model.playCard(computerHandID, computerIndex);
				}

				view.updatePlayAreaPanel(computerHandID, humanHandID, lastComputerCard, lastHumanCard);

				// Figure out who won and store winnings
				if (compareCards(lastHumanCard, lastComputerCard) > 0)
				{
					model.playerWonRound(lastHumanCard, lastComputerCard);
					view.displayRoundResults("You Win!\n" + lastHumanCard + " is greater than " + lastComputerCard);

					// make player go first next round
					isComputerFirst = false;
					isComputerFinished = false;
				} else if (compareCards(lastHumanCard, lastComputerCard) < 0)
				{
					model.computerWonRound(lastHumanCard, lastComputerCard);
					view.displayRoundResults("You Lose!\n" + lastHumanCard + " is less than " + lastComputerCard);

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

	private static int highCardComputerTurn(Hand computerHand, Card opponentCard)
	{
		/*
		 * High Card Game AI for Computer if the computer is going second.
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

	static void playPileFromUI(String cardID)
	{
		/*
		 * This function is here for cardPlayListener to use to pass the index of
		 * a card that the player wishes to play from their hand.
		 */
		int id = Integer.parseInt(cardID);
		if (id >= 0 && id <= 1)
		{
			playedPileID = Integer.parseInt(cardID);
		}
	}

	static void cannotPlayFromUI()
	{
		isUnableToPlay = true;
		System.out.println(isUnableToPlay);
	}
}

// ActionListener to allow the player to play the card they click on.
class cardPlayListener implements ActionListener
{
	private Controller.Games gameType;
	private JButton clickedButton;

	public void actionPerformed(ActionEvent e)
	{
		System.out.println(e.getActionCommand());
		if(gameType == Controller.Games.highCard)
		{
			String cardID = e.getActionCommand();
			Controller.playCardFromUI(cardID);
		}
		else if(gameType == Controller.Games.build)
		{
			// Tell the controller which card is being played

			String cardID = e.getActionCommand();
			Controller.playCardFromUI(cardID);

			// Print a border to indicate which card is being played.
			clickedButton.setBorder(BorderFactory.createLineBorder(Color.red, 5));
		}
	}

	//Guidance: https://stackoverflow.com/questions/11037622/pass-variables-to-actionlistener-in-java
	public cardPlayListener(Controller.Games game)
	{
		this.gameType = game;
	}
	public cardPlayListener(Controller.Games game, JButton button)
	{
		this.gameType = game;
		this.clickedButton = button;
	}
}

class pilePlayListener implements ActionListener
{

	public void actionPerformed(ActionEvent e)
	{
		System.out.println(e.getActionCommand());
		String cardID = e.getActionCommand();
		Controller.playPileFromUI(cardID);
	}
}

class cannotPlayListener implements ActionListener
{

	public void actionPerformed(ActionEvent e)
	{
		Controller.cannotPlayFromUI();
	}
}

class gameChoiceListener implements ActionListener
{
	private Controller.Games gameToPlay;

	public void actionPerformed(ActionEvent e)
	{
		Assig6.setGameToPlay(gameToPlay);
	}

	//Guidance: https://stackoverflow.com/questions/11037622/pass-variables-to-actionlistener-in-java
	public gameChoiceListener(Controller.Games game)
	{
		this.gameToPlay = game;
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
					iconCards[k][j] = new ImageIcon("images/" + turnIntIntoCardValue(k) + turnIntIntoCardSuit(j) + ".gif");
				}
			}
			iconBack = new ImageIcon("images/BK.gif"); // loads the back of card
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