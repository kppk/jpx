package kppk.jpx.module;

import java.io.IOException;
import java.io.Reader;

/**
 * Scans module-info.java reader and tokenize it.
 */
final class Scanner {

    static final Token TOKEN_CURLY_OPEN = new Token(TokenType.CURLY_OPEN, "{");
    static final Token TOKEN_CURLY_CLOSE = new Token(TokenType.CURLY_CLOSE, "}");
    static final Token TOKEN_SEMICOLON = new Token(TokenType.SEMICOLON, ";");
    static final Token TOKEN_COMMA = new Token(TokenType.COMMA, ",");
    static final Token TOKEN_EOF = new Token(TokenType.EOF, "");

    private final Reader reader;
    private int prevCh;

    Scanner(Reader reader) throws IOException {
        this.reader = reader;
        this.prevCh = reader.read();
    }


    Token nextToken() throws IOException {
        int ch = prevCh;

        /*
         * space character ' ' (0x20),
         * tab character (hex 0x09),
         * form feed character (hex 0x0c),
         * line separators characters newline (hex 0x0a)
         * carriage return (hex 0x0d)
         */

        // strip whitespace
        while (ch != -1 && (ch == 0x20 ||
                ch == 0x09 || ch == 0x0c ||
                ch == 0x0a || ch == 0x0d)) {

            ch = reader.read();
        }

        switch (ch) {
            case -1:
                return TOKEN_EOF;
            case '{':
                prevCh = reader.read();
                return TOKEN_CURLY_OPEN;
            case '}':
                prevCh = reader.read();
                return TOKEN_CURLY_CLOSE;
            case ';':
                prevCh = reader.read();
                return TOKEN_SEMICOLON;
            case ',':
                prevCh = reader.read();
                return TOKEN_COMMA;
            default:
                return new Token(TokenType.WORD, readWord(ch));
        }

    }

    private String readWord(int c) throws IOException {
        StringBuilder val = new StringBuilder();
        val.append((char) c);
        do {
            int ch = reader.read();

            switch (ch) {
                case -1:
                case 0x20:
                case 0x09:
                case 0x0c:
                case 0x0a:
                case 0x0d:
                case ',':
                case ';':
                    prevCh = ch;
                    return val.toString();
                default:
                    val.append((char) ch);
            }

        } while (true);
    }


    public static final class Token {
        final TokenType type;
        final String value;

        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum TokenType {
        CURLY_OPEN, CURLY_CLOSE, WORD, SEMICOLON, COMMA, EOF
    }


}
