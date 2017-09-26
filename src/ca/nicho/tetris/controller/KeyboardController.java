package ca.nicho.tetris.controller;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;

import ca.nicho.tetris.Board;


public class KeyboardController extends Controller {
	
	public KeyboardController(Board board){
		super(board);		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);
		
	}
	
	public void update(){
		for(Key k : keys){
			k.execute();
		}
	}
	
	public void triggerKeys(){
		for(Key k : keys){
			k.execute();
		}
	}
	
	private Key r = new Key(){
		public void execute(){
			if(isPressed){
				rotatePressed = true;
				isPressed = false;
			}
		}
	};
	
	private Key d = new Key(){
		public void execute(){
			if(isPressed){
				rightPressed = true;
				isPressed = false;
			}
		}
	};
	
	private Key a = new Key(){
		public void execute(){
			if(isPressed){
				leftPressed = true;
				isPressed = false;
			}
		}
	};
	
	Key[] keys = {r, d, a};
	
	private KeyEventDispatcher keyDispatcher = new KeyEventDispatcher() {
		public boolean dispatchKeyEvent(KeyEvent e) {
			if(e.getID() == KeyEvent.KEY_PRESSED){
				keyPressed(e);
			}else if(e.getID() == KeyEvent.KEY_RELEASED){
				keyReleased(e);
			}
			return false;
	    }
		
		public void keyPressed(KeyEvent e) {
			if(e.getKeyChar() == 'r'){
				r.pressed();
			}else if(e.getKeyChar() == 'd'){
				d.pressed();
			}else if(e.getKeyChar() == 'a'){
				a.pressed();
			}
		}

		public void keyReleased(KeyEvent e) {
			if(e.getKeyChar() == 'r'){
				r.released();
			}else if(e.getKeyChar() == 'd'){
				d.released();
			}else if(e.getKeyChar() == 'a'){
				a.released();
			}
		}
	};
	
	abstract class Key {
		
		public boolean isPressed = false;
		
		public void pressed(){
			isPressed = true;
		}
		
		public void released(){
			isPressed = false;
		}
		
		abstract void execute();
		
	}
	
	public void finished(){
		//
	}
	
}
