package RPS;
import java.io.*;
import java.net.*;
import java.util.Date;

import javax.swing.GroupLayout.Group;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Button;
/**	RPSClinet.java contains the GUI for a multi-thread RPS game. It is responsible for the switching between players.
*
*	@author	Keana Gindlesperger
*	@version	1v Sep 21, 2022.
*/
public class RPSClient extends Application 
    implements RPSConstants {

  // Create and initialize a title label
  private Label lblTitle = new Label();

  // Create and initialize a status label
  private Label lblStatus = new Label();


  // Input and output streams from/to server
  private DataInputStream fromServer;
  private DataOutputStream toServer;

  // Continue to play?
  private boolean continueToPlay = true;

  // Wait for the player to mark a cell
  private boolean waiting = true;

  // Host name or ip
  private String host = "localhost";
  
  private Button newGameButton;  
  private Button quitButton;
  private Button rock;
  private Button scissors;
  private Button paper;
  private int userSelection;
  
  private int move;
  @Override // Override the start method in the Application class
  /** Start of the GUI 
   *  @param primaryStage
   *  @throws FileNotFoundException
   */
  public void start(Stage primaryStage) throws FileNotFoundException {
	  
	 newGameButton = new Button("New Game");
     quitButton = new Button("Quit");
     rock = new Button("Rock");
     paper = new Button("Paper");
     scissors = new Button("Scissors");
     try {
    	 newGameButton.setOnAction( e -> doNewGame());
    	 quitButton.setOnAction(e-> doNewGame());
     }catch(NullPointerException e){
    	 System.out.println("Invalid Entry");
     }
     rock.setOnAction( e-> {
         userSelection = 1;
         System.out.println(userSelection);
     });
     paper.setOnAction( e -> {
         userSelection = 2;
         System.out.println(userSelection);
     });
     scissors.setOnAction( e -> {
         userSelection = 3;
         System.out.println(userSelection);
     });
    // Pane to hold cell
	//button and message relocations
    newGameButton.relocate(200,20);
    rock.relocate(70,200);
    paper.relocate(200,200);
    scissors.relocate(340,200);
    quitButton.relocate(220,450);
    lblTitle.relocate(10,400);
    lblStatus.relocate(10,450);
   
    lblTitle.setTextFill( Color.rgb(100,255,100) ); 
    lblTitle.setFont(Font.font(null, FontWeight.BOLD, 14) );
    
    lblStatus.setTextFill( Color.rgb(100,255,100) ); 
    lblStatus.setFont(Font.font(null, FontWeight.BOLD, 14) );
    
    newGameButton.setManaged(false);
    newGameButton.resize(150,30);
    quitButton.setManaged(false);
    quitButton.resize(150, 30);
    rock.setManaged(false);
    rock.resize(100,100);
    scissors.setManaged(false);
    paper.resize(100,100);
    paper.setManaged(false);    
    scissors.resize(100,100);
    // Create a scene and place it in the stage
    Pane root = new Pane();
    
    root.setPrefWidth(500);
    root.setPrefHeight(500);
    root.getChildren().addAll(quitButton, newGameButton, rock, paper, scissors, lblTitle, lblStatus);
    root.setStyle("-fx-background-color: BLACK; ");
    Scene scene = new Scene(root);
    primaryStage.setTitle("RPSClient"); // Set the stage title
    primaryStage.setScene(scene); // Place the scene in the stage
    primaryStage.show(); // Display the stage   

    
    // Connect to the server
    connectToServer();
  }
  //Restarts game, does not take any arguments
  private void doNewGame() {
	  userSelection = 0;
	  connectToServer();
	  
  }
  //connects to server, responsible for output seen on GUI
private void connectToServer() {
    try {
      // Create a socket to connect to the server
      Socket socket = new Socket(host, 8000);

      // Create an input stream to receive data from the server
      fromServer = new DataInputStream(socket.getInputStream());

      // Create an output stream to send data to the server
      toServer = new DataOutputStream(socket.getOutputStream());
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

    // Control the game on a separate thread
    new Thread(() -> {
      try {
        // Get notification from the server
        int player = fromServer.readInt();
        System.out.println("PLAYER: " + player);
        // Am I player 1 or 2?
        if (player == PLAYER1) {
          Platform.runLater(() -> {
            lblTitle.setText("Player 2, Rock beats Scissors, Scissors beats Paper, Paper beats Rock");
            lblStatus.setText("Player 1");
          });
  
          // Receive startup notification from the server
          fromServer.readInt(); // Whatever read is ignored
          
          // The other player has joined
          Platform.runLater(() -> 
            lblStatus.setText("Player 2 has joined. Waiti ng for players to choose."));
  
        }
        else if (player == PLAYER2) {
          Platform.runLater(() -> {
            lblTitle.setText("Player 2, Rock beats Scissors, Scissors beats Paper, Paper beats Rock");
            lblStatus.setText("Waiting for player 1 to move");
          });
        }
  
        // Continue to play
        while (continueToPlay) {      
          if (player == PLAYER1) {
        	  try{
        		  receiveInfoFromServer(); // Receive info from the server
        		  sendMove(); // Send the move to the server
        	  }catch(Exception e) {
        		  System.out.println("Exception caught");
        	  }
          }
          else if (player == PLAYER2) {
            try {
        	  receiveInfoFromServer(); // Receive info from the server
        	  sendMove(); // Send player 2's move to the server
            }catch(Exception e) {
      		  System.out.println("Exception caught");
      	  }
          }
          if (player == PLAYER1_WON) {
        	  try {
        		  receiveInfoFromServer(); // Receive info from the server
        		  sendMove(); // Send the move to the server
        	  }catch(Exception e) {
        		  System.out.println("Exception caught");
        	  }
              Platform.runLater(() -> {
                  lblTitle.setText("Player 2, Rock beats Scissors, Scissors beats Paper, Paper beats Rock");
                  lblStatus.setText("Player 1 WON");
                  });
          } else if (player == PLAYER2_WON ) {
        	  try {
        		  receiveInfoFromServer(); // Receive info from the server
        		  sendMove(); // Send player 2's move to the server
        	  }catch(Exception e) {
        		  System.out.println("Exception caught");
        	  }
              Platform.runLater(() -> {
                  lblTitle.setText("Player 2, Rock beats Scissors, Scissors beats Paper, Paper beats Rock");
                  lblStatus.setText("Player 2 WON");
              });
            } else if (player == DRAW) {
            	try {
            		receiveInfoFromServer(); // Receive info from the server
            		sendMove(); // Send player 2's move to the server
            	}catch(Exception e) {
          		  System.out.println("Exception caught");
          	  }
                Platform.runLater(() -> {
                    lblTitle.setText("Player 2, Rock beats Scissors, Scissors beats Paper, Paper beats Rock");
                    lblStatus.setText("Draw!");
                });
            }
        }
      }
      
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }).start();
  }


  /** Send this player's move to the server */
  private void sendMove() throws IOException {
    toServer.writeInt(userSelection); // Send the selected button
  }

  /** Receive info from the server */
  private void receiveInfoFromServer() throws IOException {
	  userSelection = fromServer.readInt();
  }
    

  /**
   * The main method is only needed for the IDE with limited
   * JavaFX support. Not needed for running from the command line.
   */
  public static void main(String[] args) {
	  try {
		  launch(args);
	  }catch(Exception e) {
		  System.out.println("Exception caught");
	  }
  }
}