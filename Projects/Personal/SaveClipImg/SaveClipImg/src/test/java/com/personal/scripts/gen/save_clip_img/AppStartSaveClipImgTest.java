package com.personal.scripts.gen.save_clip_img;

import org.junit.jupiter.api.Test;

class AppStartSaveClipImgTest {

	@Test
	void testMain() {

		final String[] args;
		final int input = Integer.parseInt("11");
		if (input == 1) {
			args = new String[] { "D:\\tmp\\SaveClipImg" };

		} else if (input == 11) {
			args = new String[] { "D:\\tmp\\SaveClipImg", "img1" };

		} else if (input == 21) {
			args = new String[] { "D:\\tmp\\SaveClipImg", "image1.png" };

		} else if (input == 101) {
			args = new String[] { "" };
		} else if (input == 102) {
			args = new String[] { "-help" };

		} else {
			throw new RuntimeException();
		}

		AppStartSaveClipImg.main(args);
	}
}
