package org.test;

import loon.LTransition;
import loon.Screen;
import loon.canvas.LColor;
import loon.component.LButton;
import loon.component.LPaper;
import loon.event.ActionKey;
import loon.event.GameTouch;
import loon.font.LFont;
import loon.opengl.GLEx;
import loon.utils.Scale;
import loon.utils.timer.LTimerContext;

public class TitleScreen extends Screen {

	LButton start, end;

	LPaper title;
	
	public TitleScreen() {

	}

	public void onLoad() {

		//设置默认字体大小为20号字
		LFont.setDefaultFont(LFont.getFont(20));
		
		add(MultiScreenTest.getBackButton(this,1));
		setBackground("assets/avg/back1.png");

		start = new LButton("assets/avg/title_start.png", 191, 57) {

			ActionKey action = new ActionKey(
					ActionKey.DETECT_INITIAL_PRESS_ONLY);

			public void doClick() {
				if (!action.isPressed()) {
					action.press();
					replaceScreen(new MyAVGScreen(), MoveMethod.FROM_LEFT);
				}
			}
		};

		start.setLocation(2, 5);

		start.setEnabled(false);

		add(start);

		LButton btn2 = new LButton("assets/avg/title_load.png", 160, 56);

		btn2.setLocation(2, start.getY() + start.getHeight() + 20);

		btn2.setEnabled(false);

		add(btn2);

		LButton btn3 = new LButton("assets/avg/title_option.png", 215, 57);

		btn3.setLocation(2, btn2.getY() + btn2.getHeight() + 20);

		btn3.setEnabled(false);

		add(btn3);

		end = new LButton("assets/avg/title_end.png", 142, 57) {
			public void doClick() {

			}
		};

		end.setLocation(2, btn3.getY() + btn3.getHeight() + 20);

		end.setEnabled(false);

		add(end);

		title = new LPaper("assets/avg/title.png", -200, 0);

		add(title);
	}

	public void alter(LTimerContext c) {
		if (isOnLoadComplete()) {
			if (title.getScreenX() + title.getWidth() + 25 <= getWidth()) {
				title.move_right(3);
			} else {
				start.setEnabled(true);
				end.setEnabled(true);
			}
		}
	}

	public void draw(GLEx g) {

	}

	public void touchDown(GameTouch e) {

	}

	public void touchMove(GameTouch e) {

	}

	public void touchUp(GameTouch e) {

	}

	@Override
	public void touchDrag(GameTouch e) {

	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void resume() {

	}

	@Override
	public void pause() {

	}

	@Override
	public void close() {

	}

}
