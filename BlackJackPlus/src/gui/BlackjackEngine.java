package gui;
import java.util.ArrayList;

import backend.Computer;
import backend.Deck;
import backend.House;
import backend.Player;

public class BlackjackEngine{ 
	private String gameMode; 
	private int numOfParticipants; 
	private int gameSeed; 
	
	private Deck cardDeck;
	private Player humanUser; 
	private House dealer; 
	private ArrayList<Computer> aiList; 
	private ArrayList<Player> localPvPList; 
	
	public BlackjackEngine(String mode, int participants, int seed) {
		this.gameMode = mode; 
		this.numOfParticipants = participants; 
		this.gameSeed = seed; 
		
		initializeGameState();
	}
	
	private void initializeGameState() {
		cardDeck = new Deck(); 
		cardDeck.shuffle(gameSeed); 
		
		humanUser = new Player(); 
		dealer= new House(); 
		
		//initial deal card
		humanUser.addCard(cardDeck.dealCard());
		humanUser.addCard(cardDeck.dealCard());
		dealer.addCard(cardDeck.dealCard());
		
		if (gameMode.equals("COMPUTER")) {
			aiList = new ArrayList<>(); 
			java.util.Random aiRand = new java.util.Random(gameSeed); 
			for (int i = 0; i< numOfParticipants - 1; i++) {
				Computer ai = new Computer(aiRand, cardDeck, i+ 2); 
				ai.addCard(cardDeck.dealCard());
				ai.addCard(cardDeck.dealCard());
				aiList.add(ai); 
			}
		} else {
			localPvPList = new ArrayList<>();
			for (int i= 0; i< numOfParticipants - 1; i++) {
				Player p = new Player(); 
				p.addCard(cardDeck.dealCard());
				p.addCard(cardDeck.dealCard());
				localPvPList.add(p); 
			}
		}
	}
	
	public String getGameMode() {return gameMode; }
	public Deck getCardDeck() {return cardDeck; }
	public Player getHumanUser() {return humanUser; }
	public House getDealer() {return dealer; }
	public ArrayList<Computer> getAiList() {return aiList; }
	public ArrayList<Player> getLocalPvPList() {return localPvPList; }

}