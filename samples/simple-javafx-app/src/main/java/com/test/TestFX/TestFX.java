package com.test.TestFX;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TestFX extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		 String javaVersion = System.getProperty("java.version");
	        String javafxVersion = System.getProperty("javafx.version");
	        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ". Text 1. prop2=" + System.getProperty("prop2"));
	        Scene scene = new Scene(l, 640, 480);
	        stage.setScene(scene);
	        stage.show();
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
