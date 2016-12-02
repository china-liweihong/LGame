/**
 * Copyright 2008 - 2015 The Loon Game Engine Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.5
 */
package loon.opengl;

import loon.LRelease;
import loon.LSystem;
import loon.canvas.LColor;
import loon.font.LFont;
import loon.utils.ObjectMap;
import loon.utils.ObjectMap.Entries;
import loon.utils.ObjectMap.Entry;
import loon.utils.TArray;

public final class LSTRDictionary {

	public static void setAsyn(boolean asyn) {
		LSTRDictionary.tmp_asyn = asyn;
	}

	public static boolean isAsyn() {
		return LSTRDictionary.tmp_asyn;
	}

	public static boolean asyn() {
		return LSTRDictionary.tmp_asyn;
	}

	private static boolean tmp_asyn = true;

	private final static ObjectMap<String, LFont> cacheList = new ObjectMap<String, LFont>(
			20);

	private final static ObjectMap<String, Dict> fontList = new ObjectMap<String, Dict>(
			20);

	private final static ObjectMap<LFont, Dict> englishFontList = new ObjectMap<LFont, Dict>(
			20);

	// 每次渲染图像到纹理时，同时追加一些常用非中文标记上去，以避免LSTRFont反复重构纹理
	public final static String added = "0123456789abcdefgABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz:.,!?@#$%^&*(){}[]<>\"'\\/+-";

	private final static char[] checkMessage = added.toCharArray();

	public final static char split = '$';

	private static StringBuffer lazyKey;

	static class Dict implements LRelease {

		TArray<Character> dicts;

		LSTRFont font;

		static Dict newDict() {
			return new Dict();
		}

		Dict() {
			dicts = new TArray<Character>(512);
		}

		public boolean include(String mes) {
			final char[] chars = mes.toCharArray();
			int size = chars.length;
			for (int i = 0; i < size; i++) {
				char flag = chars[i];
				if (!dicts.contains(flag)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public void close() {
			if (font != null) {
				font.close();
				font = null;
			}
			if (dicts != null) {
				dicts.clear();
				dicts = null;
			}
		}

	}

	public synchronized static void clearEnglishLazy() {
		synchronized (englishFontList) {
			for (Dict d : englishFontList.values()) {
				if (d != null) {
					d.close();
					d = null;
				}
			}
			englishFontList.clear();
		}
	}

	public synchronized static void clearStringLazy() {
		synchronized (cacheList) {
			if (cacheList != null) {
				cacheList.clear();
			}
		}
		synchronized (fontList) {
			for (Dict d : fontList.values()) {
				if (d != null) {
					d.close();
					d = null;
				}
			}
			fontList.clear();
		}
	}

	private final static int size = LSystem.DEFAULT_MAX_CACHE_SIZE * 2;

	public final static boolean checkMessage(String key, String message) {
		final char[] chars = message.toCharArray();
		final char[] list = key.toCharArray();
		int size = chars.length;
		int limit = list.length;
		int idx = 0;
		for (int j = 0; j < limit; j++) {
			char name = list[j];
			for (int i = 0; i < size; i++) {
				char flag = chars[i];
				if (flag == name) {
					idx++;
				}
				if (idx >= limit) {
					return true;
				}
			}
			if (idx >= limit) {
				return true;
			}
		}
		return false;
	}

	private static boolean checkEnglishString(String mes) {
		int len = mes.length();
		int count = 0;
		for (int n = 0; n < len; n++) {
			for (int i = 0, j = checkMessage.length; i < j; i++) {
				if (count >= len) {
					return true;
				}
				if (checkMessage[i] == mes.charAt(n)) {
					count++;
				}
			}
		}

		return count == len;
	}

	public synchronized final static Dict bind(final LFont font,
			final String mes) {
		if (checkEnglishString(mes)) {
			if (englishFontList.size > size) {
				clearEnglishLazy();
			}
			Dict pDict = englishFontList.get(font);
			if (pDict == null) {
				pDict = Dict.newDict();
				pDict.font = new LSTRFont(font, added, tmp_asyn);
				englishFontList.put(font, pDict);
			}
			return pDict;
		}
		final String message = mes + added;
		if (cacheList.size > size) {
			clearStringLazy();
		}
		synchronized (fontList) {
			LFont cFont = cacheList.get(message);
			if (cFont == null) {
				for (Entries<String, LFont> it = cacheList.iterator(); it
						.hasNext();) {
					Entry<String, LFont> obj = it.next();
					String key = obj.key;
					if (checkMessage(key, message)) {
						cFont = obj.value;
						break;
					}
				}
			}
			String fontFlag = font.getFontName() + "_" + font.getStyle() + "_"
					+ font.getSize();
			Dict pDict = fontList.get(fontFlag);
			// 判定当前font与字体和已存在的文字图片纹理，是否和缓存的font适配
			if ((cFont == null || pDict == null || (pDict != null && !pDict
					.include(mes)))) {
				if (pDict == null) {
					pDict = Dict.newDict();
					fontList.put(fontFlag, pDict);
				}
				synchronized (pDict) {
					cacheList.put(message, font);
					TArray<Character> charas = pDict.dicts;
					int oldSize = charas.size;
					char[] chars = message.toCharArray();
					int size = chars.length;
					for (int i = 0; i < size; i++) {
						char flag = chars[i];
						if (!charas.contains(flag)) {
							charas.add(flag);
						}
					}
					int newSize = charas.size;
					// 如果旧有大小，不等于新的纹理字符大小，重新扩展LSTRFont纹理字符
					if (oldSize != newSize) {
						if (pDict.font != null) {
							pDict.font.close();
							pDict.font = null;
						}
						StringBuffer sbr = new StringBuffer(newSize);
						for (int i = 0; i < newSize; i++) {
							sbr.append(charas.get(i));
						}
						// 个别浏览器纹理同步会卡出国，只能异步……
						pDict.font = new LSTRFont(font, sbr.toString(),
								tmp_asyn);
					}
				}
			}

			return pDict;
		}
	}

	public synchronized final static void drawString(LFont font,
			String message, float x, float y, float angle, LColor c) {
		Dict pDict = bind(font, message);
		if (pDict.font != null) {
			synchronized (pDict.font) {
				pDict.font.drawString(message, x, y, angle, c);
			}
		}
	}

	public synchronized final static void drawString(LFont font,
			String message, float x, float y, float sx, float sy, float ax,
			float ay, float angle, LColor c) {
		Dict pDict = bind(font, message);
		if (pDict.font != null) {
			synchronized (pDict.font) {
				pDict.font.drawString(message, x, y, sx, sy, ax, ay, angle, c);
			}
		}
	}

	public synchronized final static void drawString(GLEx gl, LFont font,
			String message, float x, float y, float angle, LColor c) {
		Dict pDict = bind(font, message);
		if (pDict.font != null) {
			synchronized (pDict.font) {
				pDict.font.drawString(gl, message, x, y, angle, c);
			}
		}
	}

	public synchronized final static void drawString(GLEx gl, LFont font,
			String message, float x, float y, float sx, float sy, float angle,
			LColor c) {
		Dict pDict = bind(font, message);
		if (pDict.font != null) {
			synchronized (pDict.font) {
				pDict.font.drawString(gl, message, x, y, sx, sy, angle, c);
			}
		}
	}

	public synchronized final static void drawString(GLEx gl, LFont font,
			String message, float x, float y, float sx, float sy, float ax,
			float ay, float angle, LColor c) {
		Dict pDict = bind(font, message);
		if (pDict.font != null) {
			synchronized (pDict.font) {
				pDict.font.drawString(gl, x, y, sx, sy, ax, ay, angle, message,
						c);
			}
		}
	}

	/**
	 * 生成特定字符串的缓存用ID
	 * 
	 * @param font
	 * @param text
	 * @return
	 */
	public synchronized static String makeStringLazyKey(final LFont font,
			final String text) {
		int hashCode = 0;
		hashCode = LSystem.unite(hashCode, font.getSize());
		hashCode = LSystem.unite(hashCode, font.getStyle());
		hashCode = LSystem.unite(hashCode, font.getAscent());
		hashCode = LSystem.unite(hashCode, font.getLeading());
		hashCode = LSystem.unite(hashCode, font.getDescent());

		if (lazyKey == null) {
			lazyKey = new StringBuffer();
			lazyKey.append(font.getFontName().toLowerCase());
			lazyKey.append(hashCode);
			lazyKey.append(split);
			lazyKey.append(text);
		} else {
			lazyKey.delete(0, lazyKey.length());
			lazyKey.append(font.getFontName().toLowerCase());
			lazyKey.append(hashCode);
			lazyKey.append(split);
			lazyKey.append(text);
		}
		return lazyKey.toString();
	}

	public synchronized final static void dispose() {
		cacheList.clear();
		clearStringLazy();
		//单纯英文占用空间小,没必要时无需删,避免反复生成
		//clearEnglishLazy();
	}

}
