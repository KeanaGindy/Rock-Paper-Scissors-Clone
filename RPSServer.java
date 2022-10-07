package RPS;
import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**	RPSServer.java all the server connections to a multi-thread RPS game. It contains the server initialization and game logic.
*
*	@author	Keana Gindlesperger
*	@version	1v Sep 21, 2022.
*/
public class RPSServer extends Application 
    implements RPSConstants {
  private int sessionNo = 1; // Number a session
  
  @Override // Override the start method in the Application class
  /** Creates the GUI for the server
   * @param primaryStage
   */
  public void start(Stage primaryStage) {
    TextArea taLog = new TextArea();

    // Create a scene and place it in the stage
    Scene scene = new Scene(new ScrollPane(taLog), 450, 200);
    primaryStage.setTitle("RockPaperScissorsServer"); // Set the stage title
    primaryStage.setScene(scene); // Place the scene in the stage
    primaryStage.show(); // Display the stage

    new Thread( () -> {
      try {
        // Create a server socket
        ServerSocket serverSocket = new ServerSocket(8000);
        Platform.runLater(() -> taLog.appendText(new Date() +
          ": Server started at socket 8000\n"));
  
        // Ready to create a session for every two players
        while (true) {
          Platform.runLater(() -> taLog.appendText(new Date() +
            ": Wait for players to join session " + sessionNo + '\n'));
  
          // Connect to player 1
          Socket player1 = serverSocket.accept();
  
          Platform.runLater(() -> {
            taLog.appendText(new Date() + ": Player 1 joined session " 
              + sessionNo + '\n');
            taLog.appendText("Player 1's IP address" +
              player1.getInetAddress().getHostAddress() + '\n');
          });
  
          // Notify that the player is Player 1
          new DataOutputStream(
            player1.getOutputStream()).writeInt(PLAYER1);
  
          // Connect to player 2
          Socket player2 = serverSocket.accept();
  
          Platform.runLater(() -> {
            taLog.appendText(new Date() +
              ": Player 2 joined session " + sessionNo + '\n');
            taLog.appendText("Player 2's IP address" +
              player2.getInetAddress().getHostAddress() + '\n');
          });
  
          // Notify that the player is Player 2
          new DataOutputStream(
            player2.getOutputStream()).writeInt(PLAYER2);
  
          // Display this session and increment session number
          Platform.runLater(() -> 
            taLog.appendText(new Date() + 
              ": Start a thread for session " + sessionNo++ + '\n'));
  
          // Launch a new thread for this session of two players
          new Thread(new HandleASession(player1, player2)).start();
        }
      }
      catch(IOException ex) {
        ex.printStackTrace();
      }
    }).start();
  }

  // Define the thread class for handling a new session for two players
  class HandleASession implements Runnable, RPSConstants {
    private Socket player1;
    private Socket player2;
  
    private DataInputStream fromPlayer1;
    private DataOutputStream toPlayer1;
    private DataInputStream fromPlayer2;
    private DataOutputStream toPlayer2;

	 int countTotal = 0;
	 int player1N = 0;
	 int player2N = 0;
  
    // Continue to play
    private boolean continueToPlay = true;
  
    /** Construct a thread */
    public HandleASession(Socket player1, Socket player2) {
      this.player1 = player1;
      this.player2 = player2;
      
    }
  
    /** Implement the run() method for the thread */
    public void run() {
      try {
        // Create data input and output streams
        DataInputStream fromPlayer1 = new DataInputStream(
          player1.getInputStream());
        DataOutputStream toPlayer1 = new DataOutputStream(
          player1.getOutputStream());
        DataInputStream fromPlayer2 = new DataInputStream(
          player2.getInputStream());
        DataOutputStream toPlayer2 = new DataOutputStream(
          player2.getOutputStream());
  
        // Write anything to notify player 1 to start
        // This is just to let player 1 know to start
        toPlayer1.writeInt(1);
  
        // Continuously serve the players and determine and report
        // the game status to the players
        while (true) {
          // Receive a move from player 1
          int userSelection = fromPlayer1.readInt();
          int userSelectionPlayer2 = fromPlayer2.readInt();
          
          // Check if Player 1 wins
          if (countTotal >= 5 || userSelection >= 3) {
        	 if(userSelectionPlayer2 > userSelection) {
          		toPlayer1.writeInt(PLAYER2_WON);
                  toPlayer2.writeInt(PLAYER2_WON);
          	}else {
          		toPlayer1.writeInt(PLAYER1_WON);
                  toPlayer2.writeInt(PLAYER1_WON);
          	}
              
            sendMove(toPlayer2, userSelection);
            break; // Break the loop
          }
          else {
            // Notify player 2 to take the turn
            sendMove(toPlayer2, userSelection);
            try {
            	isWon(userSelection, userSelectionPlayer2);
            }catch(IOException e) {
      		  System.out.println("IOException caught");
          }
          }
  
          // Check if Player 2 wins
          if (countTotal >= 5 || userSelectionPlayer2 >= 3) {
        	if(userSelectionPlayer2 > userSelection) {
        		toPlayer1.writeInt(PLAYER2_WON);
                toPlayer2.writeInt(PLAYER2_WON);
        	}else {
        		toPlayer1.writeInt(PLAYER1_WON);
                toPlayer2.writeInt(PLAYER1_WON);
        	}
            
            sendMove(toPlayer1, userSelectionPlayer2);
            break;
          }
          else {

            sendMove(toPlayer1, userSelectionPlayer2);
            try {
            	isWon(userSelection, userSelectionPlayer2);
            }catch(IOException e) {
      		  System.out.println("IOException caught");
      	  }
  
          }
        }
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  
	/** Send the move to other player */
    private void sendMove(DataOutputStream out, int userSelection)
        throws IOException {
      out.writeInt(userSelection); // Send row index
    }
  
   
    /** Determine if the player with the specified token wins 
     * @throws IOException */
    private void isWon(int playerOne, int playerTwo) throws IOException {
      
      if(playerOne == playerTwo) {
    	  toPlayer1.writeInt(DRAW);
          toPlayer2.writeInt(DRAW);
          countTotal++;
          System.out.println("COUNT" +countTotal);
      }else if(playerOne == 1) {
    	  if(playerTwo == 2) {
    		  toPlayer1.writeInt(PLAYER2_WON);
              toPlayer2.writeInt(PLAYER2_WON);
              countTotal++;
              player2N++;
              System.out.println("2 WON" + player2N);
    	  }
    	  if(playerTwo == 3) {
    		  toPlayer1.writeInt(PLAYER1_WON);
              toPlayer2.writeInt(PLAYER1_WON);
              countTotal++;    
              player1N++;
    	  }
      }else if(playerOne == 2) {
    	  if(playerTwo == 1) {
    		  toPlayer1.writeInt(PLAYER1_WON);
              toPlayer2.writeInt(PLAYER1_WON);
              countTotal++;
              player1N++;
    	  }
    	  if(playerTwo == 3) {
    		  toPlayer1.writeInt(PLAYER2_WON);
              toPlayer2.writeInt(PLAYER2_WON);
              countTotal++;
              player2N++;
    	  }
      }else if(playerOne == 3) {
    	  if(playerTwo == 1) {
    		  toPlayer1.writeInt(PLAYER2_WON);
              toPlayer2.writeInt(PLAYER2_WON);
              countTotal++;
              player2N++;
    	  }
    	  if(playerTwo == 2) {
    		  toPlayer1.writeInt(PLAYER1_WON);
              toPlayer2.writeInt(PLAYER1_WON);
              countTotal++;
              player1N++;
    	  }
      }
  
    }
  }
  
  /**
   * The main method is only needed for the IDE with limited
   * JavaFX support. Not needed for running from the command line.
   */
  public static void main(String[] args) {
    launch(args);
  }
}