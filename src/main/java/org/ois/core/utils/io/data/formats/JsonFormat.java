package org.ois.core.utils.io.data.formats;

import org.ois.core.utils.io.data.DataNode;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonFormat implements DataFormat {

    private static final JsonFormat HUMAN_READABLE = new JsonFormat(new Options());
    private static final JsonFormat COMPACT = new JsonFormat(new Options("", ""));

    /**
     * Options for controlling the formatting of the output JSON string.
     * If all attributes equal to "", the output will have no whitespace at all.
     */
    public static class Options {
        public final String indentSymbol;
        public final String newLineSymbol;

        public Options() {
            this("\t", "\n");
        }

        public Options(String indentSymbol, String newLineSymbol) {
            this.indentSymbol = indentSymbol;
            this.newLineSymbol = newLineSymbol;
        }

        public boolean isCompact() {
            return indentSymbol.isEmpty() && newLineSymbol.isEmpty();
        }
    }

    private final Options options;

    public JsonFormat(Options options) {
        this.options = options;
    }

    // Static method for human-readable JSON format
    public static JsonFormat humanReadable() {
        return HUMAN_READABLE;
    }

    // Static method for compact (no-whitespace) JSON format
    public static JsonFormat compact() {
        return COMPACT;
    }

    @Override
    public String serialize(DataNode data) {
        return buildJson(data, 0);
    }

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


    private static class ParseState {
        String json;
        int currentIndex;
        int lineNumber;
        int columnNumber;

        public ParseState(String data) {
            this.json = data;
            this.currentIndex = 0;
            this.lineNumber = 1;
            this.columnNumber = 1;
        }

        public char current() {
            return json.charAt(currentIndex);
        }

        public boolean hasNextToken() {
            return currentIndex < json.length();
        }

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

        public void consumeWhiteSpace() {
            while (hasNextToken() && Character.isWhitespace(json.charAt(currentIndex))) {
                consume(1,false);
            }
        }

        public String remaining() {
            return json.substring(currentIndex);
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public int getColumnNumber() {
            return columnNumber;
        }
    }

    @Override
    public DataNode deserialize(String data) {
        return parseJsonValue(new ParseState(data));
    }

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

    private DataNode parseJsonString(ParseState state) {
        String str = parseString(state);
        return DataNode.Primitive(str);
    }

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

    private String parseJsonKey(ParseState state) {
        return parseString(state);
    }

    private DataNode parsePrimitiveValue(ParseState state) {
        if (state.current() == 't' || state.current() == 'f') {
            return parseJsonBoolean(state);
        } else if (state.current() == 'n') {
            return parseJsonNull(state);
        } else {
            return parseJsonNumber(state);
        }

//        StringBuilder value = new StringBuilder();
//
//        while (state.hasNextToken() && !Character.isWhitespace(state.current()) && state.current() != ',' && state.current() != '}' && state.current() != ']') {
//            value.append(state.current());
//            state.consume(1);
//        }
//
//        String valueStr = value.toString();
//        if (valueStr.equals("null")) {
//            return null;
//        } else if (valueStr.equals("true") || valueStr.equals("false")) {
//            return DataNode.Primitive(Boolean.parseBoolean(valueStr));
//        } else {
//            try {
//                // Check if it's a number (integer or float)
//                if (valueStr.contains(".")) {
//                    return DataNode.Primitive(Float.parseFloat(valueStr));
//                } else {
//                    return DataNode.Primitive(Integer.parseInt(valueStr));
//                }
//            } catch (NumberFormatException e) {
//                throw new IllegalArgumentException("Invalid number format at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
//            }
//        }
    }

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

    private DataNode parseJsonNull(ParseState state) {
        String nullStr = state.json.substring(state.currentIndex, state.currentIndex + 4);
        if (nullStr.equals("null")) {
            state.consume(4, true);
            return null; // JSON null corresponds to null in DataNode
        }
        throw new IllegalArgumentException("Invalid null value at line: " + state.getLineNumber() + ", column: " + state.getColumnNumber());
    }

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










//    // Look at first char
//    // If { -> advance current by 1, return parseJsonObject
//    // If [ -> advance current by 1, return parseJsonArray
//    // If " -> advance current by 1, return parseJsonString
//    // matchLongestNumber, if string not empty return new DataNode with its value, advance by matchLongestNumber size to consume
//    // if equals to false or true, return new DataNode with its value, advance by str size to consume
//    // if equals null return null
//    private DataNode parseJsonValue(ParseState state) {
//        skipWhitespace(state);
//        if (!state.hasNextToken()) {
//            throw new JsonParseException("Unexpected end of input at position " + state.getPosition());
//        }
//
//        char currentChar = state.current();
//        if (currentChar == '{') {
//            state.consume(1);
//            return parseJsonObject(state);
//        } else if (currentChar == '[') {
//            state.consume(1);
//            return parseJsonArray(state);
//        } else if (currentChar == '"') {
//            state.consume(1);
//            return parseJsonString(state);
//        } else if (currentChar == 't' || currentChar == 'f') {
//            return parseJsonBoolean(state);
//        } else if (currentChar == 'n') {
//            return parseJsonNull(state);
//        } else {
//            return parseJsonNumber(state);
////        }
//    }
//
//    // create node
//    // while next is not }
//    // * check that next is " for the property key, advance 1 current index to consume
//    // * get key with parseJsonKey
//    // * check that next is : , advance 1 current index to consume
//    // * get value with parseJsonValue
//    // * put in the node the value with the key
//    // * check if next is , if it is advance 1 current index to consume
//    // after while check next is } advance 1 current index to consume
//    // return node
//    private DataNode parseJsonObject(ParseState state) {
//        DataNode objectNode = DataNode.Object();
//        skipWhitespace(state);
//
//        while (state.hasNextToken() && state.current() != '}') {
//            if (state.current() != '"') {
//                throw new JsonParseException("Expected '\"' to start a key at position " + state.getPosition());
//            }
//            state.consume(1); // consume '\"'
//            String key = parseJsonKey(state);
//            skipWhitespace(state);
//            if (state.current() != ':') {
//                throw new JsonParseException("Expected ':' after key at position " + state.getPosition());
//            }
//            state.consume(1); // consume ':'
//            DataNode value = parseJsonValue(state);
//            objectNode.set(key, value);
//
//            skipWhitespace(state);
//            if (state.current() == ',') {
//                state.consume(1); // consume ','
//            } else if (state.current() != '}') {
//                throw new JsonParseException("Expected '}' or ',' at position " + state.getPosition());
//            }
//        }
//        if (state.hasNextToken()) {
//            state.consume(1); // consume '}'
//        } else {
//            throw new JsonParseException("Unexpected end of input, Expected '}' at position " + state.getPosition());
//        }
//        return objectNode;
//    }
//
//    // create node
//    // while next is not ]
//    // * get value with parseJsonValue
//    // * add value to node
//    // * check if next is , if it is advance 1 current index to consume
//    // after while check next is ] advance 1 current index to consume
//    // return node
//    private DataNode parseJsonArray(ParseState state) {
//        DataNode arrayNode = DataNode.Collection();
//        skipWhitespace(state);
//
//        while (state.hasNextToken() && state.current() != ']') {
//            DataNode value = parseJsonValue(state);
//            arrayNode.add(value);
//
//            skipWhitespace(state);
//            if (state.current() == ',') {
//                state.consume(1); // consume ','
//            } else if (state.current() != ']') {
//                throw new JsonParseException("Expected ']' or ',' at position " + state.getPosition());
//            }
//        }
//        if (state.hasNextToken()) {
//            state.consume(1); // consume ']'
//        } else {
//            throw new JsonParseException("Expected ']' at position " + state.getPosition());
//        }
//        return arrayNode;
//    }
//
//    // create node
//    // build string until the next " not escaped, dont forget to advance for the amount consumed
//    // advance 1 current index to consume the closing "
//    // return node
//    private DataNode parseJsonString(ParseState state) {
//        String stringValue = parseString(state);
//        return DataNode.Primitive(stringValue);
//    }
//
//    // build string until the next " not escaped, dont forget to advance for the amount consumed
//    // advance 1 current index to consume the closing "
//    // return string
//    private String parseString(ParseState state) {
//        StringBuilder sb = new StringBuilder();
//        while (state.hasNextToken()) {
//            char currentChar = state.current();
//            if (currentChar == '"') {
//                state.consume(1); // consume '"'
//                return sb.toString();
//            } else if (currentChar == '\\') { // Handle escape sequences
//                state.consume(1);
//                if (!state.hasNextToken()) {
//                    throw new JsonParseException("Unexpected end of input after escape character at position " + state.getPosition());
//                }
//                currentChar = state.current();
//                switch (currentChar) {
//                    case '"':
//                    case '\\':
//                    case '/':
//                        sb.append(currentChar);
//                        break;
//                    case 'b':
//                        sb.append('\b');
//                        break;
//                    case 'f':
//                        sb.append('\f');
//                        break;
//                    case 'n':
//                        sb.append('\n');
//                        break;
//                    case 'r':
//                        sb.append('\r');
//                        break;
//                    case 't':
//                        sb.append('\t');
//                        break;
//                    default:
//                        throw new JsonParseException("Invalid escape character at position " + state.getPosition());
//                }
//            } else {
//                sb.append(currentChar);
//            }
//            state.consume(1); // consume the current character
//        }
//        throw new JsonParseException("Unexpected end of input in string at position " + state.getPosition());
//    }
//
//    private String parseJsonKey(ParseState state) {
//        return parseString(state);
//    }
//
//    private DataNode parseJsonBoolean(ParseState state) {
//        String booleanStr = state.json.substring(state.getPosition(), state.getPosition() + 4);
//        if (booleanStr.equals("true")) {
//            state.consume(4);
//            return DataNode.Primitive(true);
//        } else if (booleanStr.equals("false")) {
//            state.consume(5);
//            return DataNode.Primitive(false);
//        }
//        throw new JsonParseException("Invalid boolean value at position " + state.getPosition());
//    }
//
//    private DataNode parseJsonNull(ParseState state) {
//        String nullStr = state.json.substring(state.getPosition(), state.getPosition() + 4);
//        if (nullStr.equals("null")) {
//            state.consume(4);
//            return null; // JSON null corresponds to null in DataNode
//        }
//        throw new JsonParseException("Invalid null value at position " + state.getPosition());
//    }
//
//    private DataNode parseJsonNumber(ParseState state) {
//        int startIndex = state.getPosition();
//        while (state.hasNextToken() && (Character.isDigit(state.current()) || state.current() == '-' || state.current() == '.')) {
//            state.consume(1);
//        }
//        String numberStr = state.json.substring(startIndex, state.getPosition());
//        if (numberStr.contains(".")) {
//            return DataNode.Primitive(Float.parseFloat(numberStr));
//        } else {
//            return DataNode.Primitive(Integer.parseInt(numberStr));
//        }
//    }
//
//    private void skipWhitespace(ParseState state) {
//        while (state.hasNextToken() && Character.isWhitespace(state.current())) {
//            state.consume(1);
//        }
//    }
//
//    // Custom exception class for JSON parsing errors
//    public static class JsonParseException extends RuntimeException {
//        public JsonParseException(String message) {
//            super(message);
//        }
//    }






























//    @Override
//    public DataNode deserialize(String data) {
//        Stack<DataNode> nodeStack = new Stack<>();
//        Stack<String> keyStack = new Stack<>();
//        DataNode rootNode = null; // This will track the actual root node (obj or array)
//        DataNode currentNode = null;
//        StringBuilder token = new StringBuilder();
//        boolean inString = false;
//        boolean isEscaped = false;
//
//        for (int i = 0; i < data.length(); i++) {
//            char c = data.charAt(i);
//
//            // Handle escape characters in strings
//            if (inString && isEscaped) {
//                token.append(c);
//                isEscaped = false;
//                continue;
//            }
//
//            if (c == '\\' && inString) {
//                isEscaped = true;
//                continue;
//            }
//
//            // Handle string values
//            if (c == '"') {
//                if (inString) {
//                    inString = false;
//                    String tokenStr = token.toString();
//                    token.setLength(0); // Clear token
//
//                    // End of a string, decide whether it's a key or value
//                    if (currentNode.getType() == DataNode.Type.Object && keyStack.isEmpty()) {
//                        keyStack.push(tokenStr); // Store key for object
//                    } else if (currentNode.getType() == DataNode.Type.Object) {
//                        currentNode.set(keyStack.pop(), DataNode.Primitive(tokenStr)); // Set key-value pair
//                    } else if (currentNode.getType() == DataNode.Type.Collection) {
//                        currentNode.add(DataNode.Primitive(tokenStr)); // Add to array
//                    }
//                } else {
//                    inString = true;
//                }
//                continue;
//            }
//
//            // Skip whitespaces outside of strings
//            if (Character.isWhitespace(c) && !inString) {
//                continue;
//            }
//
//            // Handle object opening
//            if (c == '{') {
//                DataNode objNode = DataNode.Object();
//                if (currentNode == null) {
//                    rootNode = objNode; // Set root to the first object
//                } else if (currentNode.getType() == DataNode.Type.Object) {
//                    // Only pop if there is a key to set
//                    if (!keyStack.isEmpty()) {
//                        currentNode.set(keyStack.pop(), objNode);
//                    }
//                } else if (currentNode.getType() == DataNode.Type.Collection) {
//                    currentNode.add(objNode);
//                }
//                nodeStack.push(currentNode);
//                currentNode = objNode;
//                continue;
//            }
//
//            // Handle array opening
//            if (c == '[') {
//                DataNode arrayNode = DataNode.Collection();
//                if (currentNode == null) {
//                    rootNode = arrayNode; // Set root to the first array
//                } else if (currentNode.getType() == DataNode.Type.Object) {
//                    // Only pop if there is a key to set
//                    if (!keyStack.isEmpty()) {
//                        currentNode.set(keyStack.pop(), arrayNode);
//                    }
//                } else if (currentNode.getType() == DataNode.Type.Collection) {
//                    currentNode.add(arrayNode);
//                }
//                nodeStack.push(currentNode);
//                currentNode = arrayNode;
//                continue;
//            }
//
//            // Handle object closing
//            if (c == '}') {
//                if (token.length() > 0 && !keyStack.isEmpty()) {
//                    currentNode.set(keyStack.pop(), DataNode.Primitive(token.toString()));
//                }
//                token.setLength(0); // Clear token
//                currentNode = nodeStack.pop();
//                continue;
//            }
//
//            // Handle array closing
//            if (c == ']') {
//                if (token.length() > 0) {
//                    currentNode.add(DataNode.Primitive(token.toString()));
//                }
//                token.setLength(0); // Clear token
//                currentNode = nodeStack.pop();
//                continue;
//            }
//
//            // Handle key-value separator
//            if (c == ':' && currentNode.getType() == DataNode.Type.Object) {
//                keyStack.push(token.toString()); // Store the key in stack
//                token.setLength(0); // Clear token
//                continue;
//            }
//
//            // Handle array/object separators
//            if (c == ',' && !inString) {
//                if (token.length() > 0) {
//                    if (currentNode.getType() == DataNode.Type.Collection) {
//                        currentNode.add(DataNode.Primitive(token.toString()));
//                    } else if (currentNode.getType() == DataNode.Type.Object && !keyStack.isEmpty()) {
//                        currentNode.set(keyStack.pop(), DataNode.Primitive(token.toString()));
//                    }
//                }
//                token.setLength(0); // Clear token
//                continue;
//            }
//
//            // Append character to token
//            token.append(c);
//        }
//
//        // Handle any remaining token as a primitive value
//        if (token.length() > 0 && currentNode.getType() == DataNode.Type.Primitive) {
//            currentNode.setValue(token.toString());
//        }
//
//        // Return the root node, which is either the first object or array parsed
//        return rootNode;
//    }

//    private static class ParseState {
//        String json;
//        int currentIndex;
//
//        public ParseState(String data) {
//            this.json = data;
//        }
//
//        public char current() {
//            return json.charAt(currentIndex);
//        }
//
//        public boolean currentStartsWith(String str) {
//            return json.substring(currentIndex).startsWith(str);
//        }
//
//        public void consume(int count) {
//            currentIndex += count;
//        }
//
//        public boolean hasNextToken() {
//            return currentIndex < json.length();
//        }
//    }
//
//    @Override
//    public DataNode deserialize(String data) {
//        return parseJsonValue(new ParseState(data));
//    }
//
//    // Look at first char
//    // If { -> advance current by 1, return parseJsonObject
//    // If [ -> advance current by 1, return parseJsonArray
//    // If " -> advance current by 1, return parseJsonString
//    // matchLongestNumber, if string not empty return new DataNode with its value, advance by matchLongestNumber size to consume
//    // if equals to false or true, return new DataNode with its value, advance by str size to consume
//    // if equals null return null
//    private DataNode parseJsonValue(ParseState state) {
//
//    }
//
//    // create node
//    // while next is not }
//    // * check that next is " for the property key, advance 1 current index to consume
//    // * get key with parseJsonKey
//    // * check that next is : , advance 1 current index to consume
//    // * get value with parseJsonValue
//    // * put in the node the value with the key
//    // * check if next is , if it is advance 1 current index to consume
//    // after while check next is } advance 1 current index to consume
//    // return node
//    private DataNode parseJsonObject(ParseState state) {
//
//    }
//
//    // create node
//    // while next is not ]
//    // * get value with parseJsonValue
//    // * add value to node
//    // * check if next is , if it is advance 1 current index to consume
//    // after while check next is ] advance 1 current index to consume
//    // return node
//    private DataNode parseJsonArray(ParseState state) {
//
//    }
//
//    // create node
//    // build string until the next " not escaped, dont forget to advance for the amount consumed
//    // advance 1 current index to consume the closing "
//    // return node
//    private DataNode parseJsonString(ParseState state) {
//
//    }
//
//    // build string until the next " not escaped, dont forget to advance for the amount consumed
//    // advance 1 current index to consume the closing "
//    // return string
//    private String parseJsonKey(ParseState state) {
//
//    }
//
//
//
//
//
//
//    private DataNode parseJsonValue(ParseState state) {
//        switch (state.current()) {
//            case '{':
//                break;
//            case '[':
//                break;
//            case '"':
//                break;
//            default:
//                // Match the longest number at the start of the string
//                String numberMatch = matchLongestNumber(state);
//                if (!numberMatch.isEmpty()) {
//                    return parseJsonNumber(numberMatch);
//                } else if (state.currentStartsWith("true") || state.currentStartsWith("false")) {
//                    return DataNode.Primitive(Boolean.parseBoolean(json));
//                } else if (state.currentStartsWith("null")) {
//                    return new DataNode(); // Treat null as unknown
//                }
//        }
//        throw new IllegalArgumentException("Unknown JSON value: " + json);
//    }
//
//    @Override
//    public DataNode deserialize(String data) {
//        data = data.trim();
//        if (data.startsWith("{")) {
//            return parseJsonObject(data);
//        } else if (data.startsWith("[")) {
//            return parseJsonArray(data);
//        } else {
//            throw new IllegalArgumentException("Invalid JSON data");
//        }
//    }
//
//    private DataNode parseJsonObject(String json) {
//        DataNode node = DataNode.Object();
//        json = json.substring(1).trim(); // Start from the first character after '{'
//
//        while (!json.isEmpty()) {
//            int colonIndex = json.indexOf(":");
//            if (colonIndex == -1) {
//                throw new IllegalArgumentException("Invalid JSON object: " + json);
//            }
//
//            // Use findClosingDelimiter to find the key
//            String key = extractString(json);
//            json = json.substring(colonIndex + 1).trim();
//
//            // Find the matching closing delimiter for the value
//            char firstChar = json.charAt(0);
//            int closingIndex = findClosingDelimiter(json, firstChar);
//            DataNode valueNode = parseJsonValue(json.substring(0, closingIndex + 1));
//
//            node.set(key, valueNode);
//            json = removeProcessedPart(json.substring(closingIndex + 1).trim());
//        }
//        return node;
//    }
//
//    private DataNode parseJsonArray(String json) {
//        DataNode node = DataNode.Collection(new ArrayList<>());
//        json = json.substring(1).trim(); // Start from the first character after '['
//
//        while (!json.isEmpty()) {
//            char firstChar = json.charAt(0);
//            int closingIndex = findClosingDelimiter(json, firstChar);
//            DataNode valueNode = parseJsonValue(json.substring(0, closingIndex + 1));
//            node.add(valueNode);
//
//            json = removeProcessedPart(json.substring(closingIndex + 1).trim());
//        }
//        return node;
//    }
//
//    private DataNode parseJsonValue(String json) {
//        json = json.trim();
//        if (json.startsWith("{")) {
//            return parseJsonObject(json);
//        } else if (json.startsWith("[")) {
//            return parseJsonArray(json);
//        } else if (json.startsWith("\"")) {
//            int closingIndex = findClosingDelimiter(json, '"');
//            return DataNode.Primitive(json.substring(1, closingIndex));
//        } else {
//            // Match the longest number at the start of the string
//            String numberMatch = matchLongestNumber(json);
//            if (!numberMatch.isEmpty()) {
//                return parseJsonNumber(numberMatch);
//            } else if (json.startsWith("true") || json.startsWith("false")) {
//                return DataNode.Primitive(Boolean.parseBoolean(json));
//            } else if (json.startsWith("null")) {
//                return new DataNode(); // Treat null as unknown
//            }
//        }
//        throw new IllegalArgumentException("Unknown JSON value: " + json);
//    }
//
//    private String matchLongestNumber(String json) {
//        // Regex to match numbers with optional sign and decimal point
//        Pattern numberPattern = Pattern.compile("^-?\\d+(\\.\\d+)?");
//        Matcher matcher = numberPattern.matcher(json);
//        if (matcher.find()) {
//            return matcher.group(); // Returns the longest matching number
//        }
//        return ""; // Return empty string if no match
//    }
//
//    private DataNode parseJsonNumber(String json) {
//        // Determine if it's an integer or float
//        if (json.contains(".")) {
//            return DataNode.Primitive(Float.parseFloat(json));
//        } else {
//            return DataNode.Primitive(Integer.parseInt(json));
//        }
//    }
//
//    private String removeProcessedPart(String json) {
//        json = json.trim();
//
//        // Check if the json is ending with closing delimiter without any comma
//        if (json.startsWith("}") || json.startsWith("]")) {
//            return json.substring(1).trim(); // Remove the closing bracket
//        }
//
//        int nextComma = json.indexOf(",");
//
//        if (nextComma == -1) {
//            return ""; // No more values to process
//        } else {
//            return json.substring(nextComma + 1).trim(); // Remove everything up to the next comma
//        }
//    }
//
//    private String extractString(String json) {
//        int endIndex = json.indexOf("\"", 1);
//        if (endIndex == -1) {
//            throw new IllegalArgumentException("Invalid string format: " + json);
//        }
//        return json.substring(1, endIndex);
//    }
//
//    private int findClosingDelimiter(String json, char openChar) {
//        char closeChar = openChar == '{' ? '}' : (openChar == '[' ? ']' : '"');
//        int count = 1;
//        boolean inString = false;
//
//        for (int i = 1; i < json.length(); i++) {
//            char c = json.charAt(i);
//
//            // Handle string encapsulation
//            if (c == '"' && json.charAt(i - 1) != '\\') {
//                inString = !inString; // Toggle inString mode
//                if (!inString && openChar == '"') {
//                    return i; // Found the closing quote
//                }
//                continue;
//            }
//
//            if (inString) continue;
//
//            // Handle nested brackets or quotes
//            if (c == openChar) count++;
//            if (c == closeChar) count--;
//
//            if (count == 0) {
//                return i; // Found the matching closing delimiter
//            }
//        }
//
//        throw new IllegalArgumentException("No matching closing delimiter found for: " + openChar);
//    }

}
