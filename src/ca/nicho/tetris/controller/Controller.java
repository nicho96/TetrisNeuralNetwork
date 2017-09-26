package ca.nicho.tetris.controller;

import ca.nicho.tetris.Board;

public abstract class Controller {

	public static final int INPUT_AMOUNT = 3;
	
	public boolean rightPressed;
	public boolean leftPressed;
	public boolean rotatePressed;
	
	public Board board;
	
	public Controller(Board board){
		this.board = board;
	}
	
	public abstract void update();
	
	public abstract void finished();
	
}
