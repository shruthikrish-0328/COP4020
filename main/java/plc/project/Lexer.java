package plc.project;

import java.util.ArrayList;
import java.util.List;
//WEDNESDAY SUBMISSION

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid or missing.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation a lot easier.
 */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        //throw new UnsupportedOperationException(); //TODO
        List<Token> output = new ArrayList<Token>();
        while(chars.index < chars.input.length()){
            if(peek("[\b|\n|\r|\t| ]")){
                match("[\b|\n|\r|\t| ]");
                //chars.advance();
                chars.skip();
            } else{
                output.add(lexToken());
            }
        }

        /*
        for(int i = 0; i < output.size(); i++){
            System.out.println("Type: " + output.get(i).getType() + " Literal: " + output.get(i).getLiteral() + " Index: " + output.get(i).getIndex());
        }
        */

        return output;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {
        //throw new UnsupportedOperationException(); //TODO
        //System.out.println("lexToken Char Input : " + chars.input);

        if (peek("[A-Za-z_]") ){ //if first character is A-Z or a-z or _ its an identifier
            return lexIdentifier();
        } else if (peek("[0-9]") || peek("[+-]","[0-9]")) {
            return lexNumber();
        } else if (peek("[']")){
            return lexCharacter();
        } else if (peek("\"")){
            return lexString();
        } else {
            return lexOperator();
        }

    }

    public Token lexIdentifier() {
        //throw new UnsupportedOperationException(); //TODO
        match("[A-Za-z_]");

        while (chars.index < chars.input.length()) {
            if (!match("[A-Za-z0-9_-]")) { //cant be identifier if it doesn't fit the pattern
                //System.out.println(" Peek : False");
                break;
                //throw new ParseException("Invalid Identifier",chars.index);
                //What return type should we use if the string is not a Identifier?
            }
        }

        //System.out.println("Ready to Emit");
        return chars.emit(Token.Type.IDENTIFIER); //is an identifier if it goes through the string matching the pattern
    }

    public Token lexNumber() {
        //throw new UnsupportedOperationException(); //TODO
        boolean decFound = false;

        match("[0-9+-]");

        while(match("[0-9]"));

        if(peek("\\.","[0-9]")){
            match("\\.","[0-9]");
            decFound = true;
        }

        while(match("[0-9]"));

        if(decFound){
            return chars.emit(Token.Type.DECIMAL);
        }
        else{
            return chars.emit(Token.Type.INTEGER);
        }

        /*
        while(chars.index < chars.input.length()){

            if(peek("\\.")){ ///123.456
                if(peek("\\.","[0-9]")){
                    decFound = true;
                } else{
                    //throw new ParseException("Invalid Identifier",chars.index);
                    break;
                }
            }
            if(!match("[0-9\\.]")){ //1.
                //throw new ParseException("Invalid Identifier",chars.index);
                break;
            }
        }
        */
    }

    public Token lexCharacter() {
        //throw new UnsupportedOperationException(); //TODO

        match("[']"); // '\n'
        if (peek("[^'\n\r\\\\]")){
            match(".");
        } else if (peek("[\\\\]")){
            //match("[\\\\]");
            //System.out.println("Entered");
                    if(peek("[\\\\]","[b|n|r|t|'|\"|\\\\]")){
                        match("[\\\\]","[b|n|r|t|'|\"|\\\\]");
                    }
                    else {
                        lexEscape();
                        //throw new ParseException("Invalid Character",chars.index);
                    }
        } else {
            lexEscape();
            //throw new ParseException("Invalid Character",chars.index);
        }

        if (!match("[']")){
            lexEscape();
            //throw new ParseException("Invalid Character",chars.index);
        }

        return new Token(Token.Type.CHARACTER, chars.input, 0);
    }

    public Token lexString() {
        //throw new UnsupportedOperationException(); //TODO
        match("[\"]");

        while (chars.index < chars.input.length() && peek("[^\"]")) {
            if(peek("[^\n\r\\\\]")){
                match(".");
            }
            else if(peek("[\\\\]")){
                if(peek("[\\\\]","[b|r|n|t|\"|'|\\\\]")){
                    match("[\\\\]","[b|r|n|t|\"|'|\\\\]");
                }
                else{
                    lexEscape();
                    //throw new ParseException("Invalid String",chars.index);
                }
            }
            else{
                lexEscape();
                //throw new ParseException("Invalid String",chars.index);
            }
        }

        if(!match("\"")){
            lexEscape();
            //throw new ParseException("Invalid String",chars.index);
        }

        return chars.emit(Token.Type.STRING);
    }

    public void lexEscape() {
        //throw new UnsupportedOperationException(); //TODO
        throw new ParseException("Invalid String/Character",chars.index);
    }

    public Token lexOperator() {
        //throw new UnsupportedOperationException(); //TODO

        if(peek("[<>!=]","[=]")){
            match("[<>!=]","[=]");
        } else{
            chars.advance();
            //match(".");
        }

        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        //System.out.println("Patterns Length: " + patterns.length);
        for (int i = 0; i < patterns.length; i++ ) {
            //System.out.println("i  : " + i );
            //System.out.println("Pattern: " + patterns[i]);
            if(chars.has(i)){
                //System.out.println("Char.get : " + chars.get(i));
            }
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
               // System.out.println("Returning False");
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture)
        boolean peek = peek(patterns);
        //System.out.println(" Chars Index" + chars.index);
        if (peek) {
            for ( int i = 0; i < patterns.length; i++) {
                //System.out.println("Advancing Char");
                chars.advance();
            }
        }
        //System.out.println(" Chars Index" + chars.index);
        return peek;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }

}
