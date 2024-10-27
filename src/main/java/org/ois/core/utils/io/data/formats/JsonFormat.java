package org.ois.core.utils.io.data.formats;

import org.ois.core.utils.io.data.DataNode;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of the DataFormat interface for handling JSON data.
 * Provides methods to serialize and deserialize JSON strings to and from DataNode structures.
 */
public class JsonFormat implements DataFormat {

    private static final JsonFormat HUMAN_READABLE = new JsonFormat(new Options());
    private static final JsonFormat COMPACT = new JsonFormat(new Options("", ""));

    /**
     * Options for controlling the formatting of the output JSON string.
     * If all attributes are empty strings, the output will have no whitespace at all.
     */
    public static class Options {
        public final String indentSymbol;
        public final String newLineSymbol;

        /**
         * Default constructor which initializes options with a tab for indentation
         * and a newline character for line breaks.
         */
        public Options() {
            this("\t", "\n");
        }

        /**
         * Constructor that allows custom symbols for indentation and new lines.
         *
         * @param indentSymbol the string used for indentation
         * @param newLineSymbol the string used for new lines
         */
        public Options(String indentSymbol, String newLineSymbol) {
            this.indentSymbol = indentSymbol;
            this.newLineSymbol = newLineSymbol;
        }

        /**
         * Checks if the options are set for compact (no whitespace) output.
         *
         * @return true if both indent and new line symbols are empty, false otherwise
         */
        public boolean isCompact() {
            return indentSymbol.isEmpty() && newLineSymbol.isEmpty();
        }
    }

    private final Options options;

    /**
     * Constructs a JsonFormat instance with the specified options.
     *
     * @param options the formatting options to use
     */
    public JsonFormat(Options options) {
        this.options = options;
    }

    /**
     * Static method for obtaining a human-readable JSON format instance.
     *
     * @return a JsonFormat instance configured for human-readable output
     */
    public static JsonFormat humanReadable() {
        return HUMAN_READABLE;
    }

    /**
     * Static method for obtaining a compact (no-whitespace) JSON format instance.
     *
     * @return a JsonFormat instance configured for compact output
     */
    public static JsonFormat compact() {
        return COMPACT;
    }

    @Override
    public String serialize(DataNode data) {
        return buildJson(data, 0);
    }

    /**
     * Builds a JSON string from a DataNode at a specified indentation level.
     *
     * @param dataNode the DataNode to convert
     * @param indentLevel the current level of indentation
     * @return a JSON string representation of the DataNode
     */
    private String buildJson(DataNode dataNode, int indentLevel) {
        switch (dataNode.getType()) {
            case Object:
                return buildJsonObject(dataNode, indentLevel);
            case Collection:
                return buildJsonArray(dataNode, indentLevel);
            case Primitive:
                return buildJsonPrimitive(dataNode);
            default:
                return "null";
        }
    }

    /**
     * Builds a JSON string representation of a DataNode of type Object.
     *
     * @param dataNode the DataNode to convert
     * @param indentLevel the current level of indentation
     * @return a JSON string representation of the DataNode
     */
    private String buildJsonObject(DataNode dataNode, int indentLevel) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        String indent = options.isCompact() ? "" : options.indentSymbol.repeat(indentLevel + 1);
        boolean firstEntry = true;

        for (Map.Entry<String, DataNode> entry : dataNode.properties()) {
            if (!firstEntry) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append(options.newLineSymbol)
                    .append(indent)
                    .append("\"").append(escapeJson(entry.getKey())).append("\":")
                    .append(options.isCompact() ? "" : " ")
                    .append(buildJson(entry.getValue(), indentLevel + 1));
            firstEntry = false;
        }

        if (!options.isCompact() && dataNode.getPropertyCount() > 0) {
            jsonBuilder.append(options.newLineSymbol)
                    .append(options.indentSymbol.repeat(indentLevel));
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    /**
     * Builds a JSON string representation of a DataNode of type Collection (Array).
     *
     * @param dataNode the DataNode to convert
     * @param indentLevel the current level of indentation
     * @return a JSON string representation of the DataNode
     */
    private String buildJsonArray(DataNode dataNode, int indentLevel) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        String indent = options.isCompact() ? "" : options.indentSymbol.repeat(indentLevel + 1);
        boolean firstElement = true;

        for (DataNode item : dataNode) {
            if (!firstElement) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append(options.newLineSymbol)
                    .append(indent)
                    .append(buildJson(item, indentLevel + 1));
            firstElement = false;
        }

        if (!options.isCompact() && dataNode.contentCount() > 0) {
            jsonBuilder.append(options.newLineSymbol)
                    .append(options.indentSymbol.repeat(indentLevel));
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    /**
     * Builds a JSON string representation of a DataNode of type Primitive.
     *
     * @param dataNode the DataNode to convert
     * @return a JSON string representation of the DataNode
     */
    private String buildJsonPrimitive(DataNode dataNode) {
        String value = dataNode.getString();
        try {
            int intValue = Integer.parseInt(value);
            return String.valueOf(intValue);
        } catch (NumberFormatException e1) {
            try {
                float floatValue = Float.parseFloat(value);
                return String.valueOf(floatValue);
            } catch (NumberFormatException e2) {
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    return value.toLowerCase();
                }
                return "\"" + escapeJson(value) + "\"";  // Properly escape strings
            }
        }
    }

    /**
     * Escapes special characters in a JSON string to ensure valid JSON format.
     *
     * @param value the string to escape
     * @return the escaped string
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder escaped = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(c);
            }
        }
        return escaped.toString();
    }

    /**
     * A helper class to maintain the state of the JSON parsing process.
     */
    private static class ParseState {
        String json;
        int currentIndex;
        int lineNumber;
        int columnNumber;

        /**
         * Constructs a ParseState for a given JSON string.
         *
         * @param data the JSON string to parse
         */
        public ParseState(String data) {
            this.json = data;
            this.currentIndex = 0;
            this.lineNumber = 1;
            this.columnNumber = 1;
        }

        /**
         * Returns the current character in the JSON string being parsed.
         *
         * @return the current character
         */
        public char current() {
            return json.charAt(currentIndex);
        }

        /**
         * Checks if there are more tokens to parse.
         *
         * @return true if there are more tokens, false otherwise
         */
        public boolean hasNextToken() {
            return currentIndex < json.length();
        }

        /**
         * Consumes a specified number of characters and optionally removes whitespace.
         *
         * @param count the number of characters to consume
         * @param removeWhiteSpace whether to remove whitespace after consuming
         */
        public void consume(int count, boolean removeWhiteSpace) {
            for (int i = 0; i < count; i++) {
                if (json.charAt(currentIndex) == '\n') {
                    lineNumber++;
                    columnNumber = 1;
                } else {
                    columnNumber++;
                }
                currentIndex++;
            }
            if (!removeWhiteSpace) {
                return;
            }
            consumeWhiteSpace();
        }

        /**
         * Consumes all whitespace characters in the JSON string.
         */
        public void consumeWhiteSpace() {
            while (hasNextToken() && Character.isWhitespace(json.charAt(currentIndex))) {
                consume(1,false);
            }
        }

        /**
         * Returns the remaining part of the JSON string from the current index.
         *
         * @return the remaining substring
         */
        public String remaining() {
            return json.substring(currentIndex);
        }

        /**
         * Returns the current line number in the JSON string being parsed.
         *
         * @return the line number
         */
        public int getLineNumber() {
            return lineNumber;
        }

        /**
         * Returns the current column number in the JSON string being parsed.
         *
         * @return the column number
         */
        public int getColumnNumber() {
            return columnNumber;
        }
    }

    @Override
    public DataNode deserialize(String data) {
        return parseJsonValue(new ParseState(data));
    }

    /**
     * Parses the next JSON value from the provided parsing state.
     * This method identifies the type of JSON value (object, array, string, or primitive)
     * based on the current character and calls the appropriate parsing method.
     *
     * @param state the current parsing state containing the JSON data
     * @return a DataNode representing the parsed JSON value
     * @throws IllegalArgumentException if the end of JSON data is reached unexpectedly
     */
    private DataNode parseJsonValue(ParseState state) {
        if (!state.hasNextToken()) {
            throw new IllegalArgumentException("Unexpected end of JSON data at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
        }

        char currentChar = state.current();
        switch (currentChar) {
            case '{':
                state.consume(1, true);
                return parseJsonObject(state);
            case '[':
                state.consume(1, true);
                return parseJsonArray(state);
            case '"':
                state.consume(1, false);
                return parseJsonString(state);
            default:
                return parsePrimitiveValue(state);
        }
    }

    /**
     * Parses a JSON object from the provided parsing state.
     * This method expects the object to be enclosed in curly braces and parses key-value pairs.
     *
     * @param state the current parsing state containing the JSON data
     * @return a DataNode representing the parsed JSON object
     * @throws IllegalArgumentException if the expected tokens (keys, colons, etc.) are not found
     */
    private DataNode parseJsonObject(ParseState state) {
        DataNode node = DataNode.Object();

        while (state.hasNextToken() && state.current() != '}') {
            if (state.current() != '"') {
                throw new IllegalArgumentException("Expected '\"' at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
            }
            state.consume(1, true);
            String key = parseJsonKey(state);
            state.consumeWhiteSpace();
            // Check for colon
            if (state.current() != ':') {
                throw new IllegalArgumentException("Expected ':' after key at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
            }
            state.consume(1, true); // Consume ':'
            DataNode value = parseJsonValue(state);
            node.set(key, value);
            if (state.current() == ',') {
                state.consume(1, true); // Consume ','
            }
        }

        if (!state.hasNextToken()) {
            throw new IllegalArgumentException("Expected '}' at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
        }
        state.consume(1, true); // Consume '}'
        return node;
    }

    /**
     * Parses a JSON array from the provided parsing state.
     * This method expects the array to be enclosed in square brackets and parses its elements.
     *
     * @param state the current parsing state containing the JSON data
     * @return a DataNode representing the parsed JSON array
     * @throws IllegalArgumentException if the expected closing bracket ']' is not found
     */
    private DataNode parseJsonArray(ParseState state) {
        DataNode node = DataNode.Collection();

        while (state.hasNextToken() && state.current() != ']') {
            DataNode value = parseJsonValue(state);
            node.add(value);
            if (state.current() == ',') {
                state.consume(1, true); // Consume ','
            }
        }

        if (!state.hasNextToken()) {
            throw new IllegalArgumentException("Expected ']' at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
        }
        state.consume(1, true); // Consume ']'
        return node;
    }

    /**
     * Parses a JSON string from the provided parsing state.
     * This method handles escape sequences and returns the corresponding DataNode.
     *
     * @param state the current parsing state containing the JSON data
     * @return a DataNode representing the parsed JSON string
     */
    private DataNode parseJsonString(ParseState state) {
        String str = parseString(state);
        return DataNode.Primitive(str);
    }

    /**
     * Parses a string from the provided parsing state, handling escape sequences.
     *
     * @param state the current parsing state containing the JSON data
     * @return the parsed string
     * @throws IllegalArgumentException if the string is unterminated or if an invalid escape sequence is encountered
     */
    private String parseString(ParseState state) {
        StringBuilder result = new StringBuilder();

        while (state.hasNextToken()) {
            char currentChar = state.current();
            if (currentChar == '"') {
                state.consume(1, true); // Consume closing '"'
                return result.toString();
            } else if (currentChar == '\\') {
                // Handle escape sequences
                state.consume(1, false); // Consume '\'
                if (!state.hasNextToken()) {
                    throw new IllegalArgumentException("Unexpected end of string escape at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
                }
                char escapedChar = state.current();
                switch (escapedChar) {
                    case '"':
                    case '\\':
                    case '/':
                        result.append(escapedChar);
                        break;
                    case 'b':
                        result.append('\b');
                        break;
                    case 'f':
                        result.append('\f');
                        break;
                    case 'n':
                        result.append('\n');
                        break;
                    case 'r':
                        result.append('\r');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid escape sequence at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
                }
            } else {
                result.append(currentChar);
            }
            state.consume(1,false);
        }

        throw new IllegalArgumentException("Unterminated string at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
    }

    /**
     * Parses a JSON key from the provided parsing state.
     * This method is essentially a wrapper around parseString to enforce string parsing.
     *
     * @param state the current parsing state containing the JSON data
     * @return the parsed key as a string
     */
    private String parseJsonKey(ParseState state) {
        return parseString(state);
    }

    /**
     * Parses a primitive value (boolean, null, or number) from the provided parsing state.
     * Determines the type of primitive based on the current character and calls the appropriate parsing method.
     *
     * @param state the current parsing state containing the JSON data
     * @return a DataNode representing the parsed primitive value
     */
    private DataNode parsePrimitiveValue(ParseState state) {
        if (state.current() == 't' || state.current() == 'f') {
            return parseJsonBoolean(state);
        } else if (state.current() == 'n') {
            return parseJsonNull(state);
        } else {
            return parseJsonNumber(state);
        }
    }

    /**
     * Parses a boolean value from the provided parsing state.
     * Expects the value to be either "true" or "false" and returns the corresponding DataNode.
     *
     * @param state the current parsing state containing the JSON data
     * @return a DataNode representing the parsed boolean value
     * @throws IllegalArgumentException if the value is not a valid boolean
     */
    private DataNode parseJsonBoolean(ParseState state) {
        String booleanStr = state.json.substring(state.currentIndex, state.currentIndex + 4);
        if (booleanStr.equals("true")) {
            state.consume(4, true);
            return DataNode.Primitive(true);
        }
        booleanStr = state.json.substring(state.currentIndex, state.currentIndex + 5);
        if (booleanStr.equals("false")) {
            state.consume(5, true);
            return DataNode.Primitive(false);
        }
        throw new IllegalArgumentException("Invalid boolean value at at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
    }

    /**
     * Parses a null value from the provided parsing state.
     * Expects the value to be "null" and returns null as a DataNode.
     *
     * @param state the current parsing state containing the JSON data
     * @return null, representing the JSON null value
     * @throws IllegalArgumentException if the value is not a valid null
     */
    private DataNode parseJsonNull(ParseState state) {
        String nullStr = state.json.substring(state.currentIndex, state.currentIndex + 4);
        if (nullStr.equals("null")) {
            state.consume(4, true);
            return null; // JSON null corresponds to null in DataNode
        }
        throw new IllegalArgumentException("Invalid null value at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
    }

    /**
     * Parses a numeric value from the provided parsing state.
     * This method can handle both integers and floating-point numbers.
     *
     * @param state the current parsing state containing the JSON data
     * @return a DataNode representing the parsed numeric value
     */
    private DataNode parseJsonNumber(ParseState state) {
        int startIndex = state.currentIndex;
        while (state.hasNextToken() && (Character.isDigit(state.current()) || state.current() == '-' || state.current() == '.')) {
            state.consume(1,false);
        }
        String numberStr = state.json.substring(startIndex, state.currentIndex);
        state.consumeWhiteSpace();
        if (numberStr.contains(".")) {
            return DataNode.Primitive(Float.parseFloat(numberStr));
        } else {
            return DataNode.Primitive(Integer.parseInt(numberStr));
        }
    }

}
