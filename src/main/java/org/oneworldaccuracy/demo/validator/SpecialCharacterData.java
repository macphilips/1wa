package org.oneworldaccuracy.demo.validator;

import org.passay.CharacterData;

public class SpecialCharacterData implements CharacterData {
     private static final char[] special = new char[]{'!', '"', '#', '$', '%', '&', '\'', '(', ')', '*',
         '+', '-', '.', '/', ':', '<', '=', '>', '?', '@', '[', '\\', ']',
         '^', '_', '`', '{', '|', '}', '~'};

     @Override
     public String getErrorCode() {
         return "INSUFFICIENT_SPECIAL";
     }

     @Override
     public String getCharacters() {
         return new String(special);
     }
 }
