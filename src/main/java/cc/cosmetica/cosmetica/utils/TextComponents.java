/*
 * Copyright 2022 EyezahMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.cosmetica.cosmetica.utils;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Useful abstraction for handling components cross-version.
 */
public class TextComponents {
	public static MutableComponent translatable(String translationKey) {
		return Component.translatable(translationKey);
	}

	public static Component dummy() {
		return Component.literal("lorem ipsum dolor sit amet");
	}

	public static MutableComponent literal(String text) {
		return Component.literal(text);
	}

	public static MutableComponent formattedTranslatable(String translationKey, Object... args) {
		return Component.translatable(translationKey, args);
	}

	public static Component chatEncode(String remoteText) throws IllegalArgumentException, IndexOutOfBoundsException {
		StringBuilder literalTxt = new StringBuilder();
		List<Object> tokens = new ArrayList<>(4);

		// tokeniser
		for (int i = 0; i < remoteText.length(); i++) {
			char c = remoteText.charAt(i);

			if (c == '\\') {
				literalTxt.append(remoteText.charAt(++i));
			}
			else if (c == '[') {
				if (!literalTxt.isEmpty()) {
					tokens.add(literalTxt.toString());
					literalTxt = new StringBuilder();
				}

				tokens.add(Token.LINK_START);
			}
			else if (c == ']') {
				if (remoteText.charAt(++i) != '(') {
					throw new IllegalArgumentException("Unexpected character " + remoteText.charAt(i) + " after list plaintext delimiter ']'!");
				}

				if (!literalTxt.isEmpty()) {
					tokens.add(literalTxt.toString());
					literalTxt = new StringBuilder();
				}

				tokens.add(Token.LINK_MID);
			}
			else if (c == ')') {
				if (!literalTxt.isEmpty()) {
					tokens.add(literalTxt.toString());
					literalTxt = new StringBuilder();
				}

				tokens.add(Token.LINK_END);
			}
			else {
				literalTxt.append(c);
			}
		}

		if (!literalTxt.isEmpty()) tokens.add(literalTxt.toString());

		// parser
		MutableComponent result = Component.literal("");

		for (int i = 0; i < tokens.size(); i++) {
			Object currentToken = tokens.get(i);

			if (currentToken instanceof String text) {
				result.append(text);
			}
			else if (currentToken == Token.LINK_START) {
				i = parseURL(result, tokens, i);
			}
			else {
				throw new IllegalArgumentException("Unexpected token " + currentToken + " in string " + remoteText + "!");
			}
		}

		return result;
	}

	private static int parseURL(MutableComponent result, List<Object> tokens, int i) throws IllegalArgumentException {
		MutableComponent link = Component.literal("");

		// In reality, there should really be only one plaintext token between LINK_START, LINK_MID, LINK_END
		// however it has been designed like this in case other token types get added in the future, in which case there could be multiple types in a link, to make it easy to adapt the code

		// parse link
		try {
			Object token;
			while ((token = tokens.get(++i)) != Token.LINK_MID) { // ++i ensures it starts at the next token
				if (token instanceof String text) {
					link.append(text);
				}
				else {
					throw new IllegalArgumentException("Unexpected non-plaintext token " + token + " parsing link plaintext.");
				}
			}
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Could not find LINK_MID token after indicated link start.", e);
		}

		// will be pointed at a LINK_MID by now.
		// url can't have weird formats so assume it has format PLAINTEXT LINK_END

		try {
			if (!(tokens.get(++i) instanceof String))
				throw new IllegalArgumentException("URL is not plaintext: " + tokens.get(i));

			String url = (String) tokens.get(i);

			if (tokens.get(++i) != Token.LINK_END)
				throw new IllegalArgumentException("No LINK_END token after indicated link mid.");

			link.setStyle(
					link.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
			);
			result.append(link);
			return i;
		}
		catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Unexpected End-Of-Text after indicated link mid.", e);
		}
	}

	public static String stripColour(String lore) {
		final char sectionSign = '\u00A7';

		if (lore.charAt(0) == sectionSign) {
			return lore.substring(2);
		}
		else {
			return lore;
		}
	}

	private enum Token {
		LINK_START,
		LINK_MID,
		LINK_END
	}
}
