package org.ois.core.utils.io.data.formats;

import org.ois.core.utils.io.data.DataNode;

// Implementation of https://en.wikipedia.org/wiki/Wavefront_.obj_file
public class ObjFormat implements DataFormat
{
    private static final ObjFormat HUMAN_READABLE = new ObjFormat(true);
    private static final ObjFormat COMPACT = new ObjFormat(false);

    public static final String VERTICES_ATTRIBUTE = "vertices";
    public static final String VERTEX_NORMALS_ATTRIBUTE = "normals";
    public static final String VERTEX_TEXTURES_ATTRIBUTE = "textures";
    public static final String FACES_ATTRIBUTE = "faces";
    public static final String COMMENTS_ATTRIBUTE = "comments";

    public static final String VERTEX_ATTRIBUTE = "vertex";
    public static final String VERTEX_NORMAL_ATTRIBUTE = "normal";
    public static final String VERTEX_TEXTURE_ATTRIBUTE = "texture";

    public static ObjFormat parser(boolean removeUnknown) {
        if (removeUnknown) {
            return COMPACT;
        }
        return HUMAN_READABLE;
    }

    private final boolean saveUnknown;

    public ObjFormat(boolean saveUnknown) {
        this.saveUnknown = saveUnknown;
    }

    @Override
    public DataNode deserialize(String data) {
        // Init the dataNodes that will hold the parsed information
        DataNode root = DataNode.Object();
        DataNode verticesNode = root.getProperty(VERTICES_ATTRIBUTE);
        DataNode facesNode = root.getProperty(FACES_ATTRIBUTE);
        // Go over line by line and parse
        String[] lines = data.split("\n");
        for (int row = 0; row < lines.length; row++) {
            String line = lines[row].trim();
            if (line.isEmpty() || line.startsWith("#")) {
                if (!saveUnknown) {
                    // Nothing to do
                    continue;
                }
                // set the row number as the attribute key to reconstruct later
                root.getProperty(COMMENTS_ATTRIBUTE).set(String.valueOf(row), lines[row]);
                continue;
            }
            // row delimiter is white space, the first token identify the row information type
            String[] tokens = line.split("\\\\s+");
            switch (tokens[0].toLowerCase()) {
                case "v":
                    // Add to list (vertex id = its index)
                    verticesNode.add(parseVertex(tokens, row));
                    continue;
                case "vn":
                    root.getProperty(VERTEX_NORMALS_ATTRIBUTE).add(parseNormal(tokens, row));
                    continue;
                case "vt":
                    root.getProperty(VERTEX_TEXTURES_ATTRIBUTE).add(parseTexture(tokens));
                    continue;
                case "f":
                    facesNode.add(parseFace(tokens, root, row));
                    continue;
            }
            if (!saveUnknown) {
                continue;
            }
            // Unknown value, save as comment, set the row number as the attribute key to reconstruct later
            root.getProperty(COMMENTS_ATTRIBUTE).set(String.valueOf(row), lines[row]);
        }
        return root;
    }

    // # List of geometric vertices, with (x, y, z, [w]) coordinates, w is optional and defaults to 1.0.
    private DataNode parseVertex(String[] tokens, int row) {
        // vertex information - 3 floats (position in space)
        if (tokens.length < 3) {
            throw new RuntimeException(String.format("vertex line (row = %d) expected at least 3 floats but found %d", row, tokens.length - 1));
        }
        DataNode vertexData = DataNode.Object();
        vertexData.set("x", tokens[1]);
        vertexData.set("y", tokens[2]);
        vertexData.set("z", tokens[3]);
        String w = "1.0";
        if (tokens.length > 4) {
            w = tokens[4];
        }
        vertexData.set("w", w);
        return vertexData;
    }

    // # List of texture coordinates, in (u, [v, w]) coordinates, these will vary between 0 and 1. v, w are optional and default to 0.
    private DataNode parseTexture(String[] tokens) {
        DataNode vertexData = DataNode.Object();
        String x = "0.0";
        if (tokens.length > 2) {
            x = tokens[1];
        }
        vertexData.set("x", x);
        String y = "0.0";
        if (tokens.length > 3) {
            y = tokens[2];
        }
        vertexData.set("y", y);
        return vertexData;
    }

    // # List of vertex normals in (x,y,z) form; normals might not be unit vectors.
    private DataNode parseNormal(String[] tokens, int row) {
        if (tokens.length != 4) {
            throw new RuntimeException(String.format("vertex normal line (row = %d) expected 3 floats but found %d", row, tokens.length - 1));
        }
        DataNode vertexData = DataNode.Object();
        vertexData.set("x", tokens[1]);
        vertexData.set("y", tokens[2]);
        vertexData.set("z", tokens[3]);
        return vertexData;
    }

    // # Polygonal face element, defined using lists of vertex, texture and normal indices.
    // each index starts at 1 and increases corresponding to the order in which the referenced element was defined
    // Polygons such as quadrilaterals can be defined by using more than three indices.
    private DataNode parseFace(String[] tokens, DataNode root, int row) {
        if (tokens.length < 3) {
            throw new RuntimeException(String.format("face line (row = %d) expected at least 3 vertices but found %d", row, tokens.length - 1));
        }
        DataNode face = DataNode.Object();
        for (int i = 1; i < tokens.length; i++) {
            face.add(parseFaceVertexData(tokens[i], root, row));
        }
        return face;
    }

    // Formats for each vertex data:
    // vertex_index
    // vertex_index/texture_index
    // vertex_index/texture_index/normal_index
    // vertex_index//normal_index
    // * vertex_index If an index is positive then it refers to the offset in that vertex list, starting at 1. If an index is negative then it relatively refers to the end of the vertex list, -1 referring to the last element.
    private DataNode parseFaceVertexData(String token, DataNode root, int row) {
        DataNode faceVertexDataNode = DataNode.Object();
        String[] tokens = token.split("/");
        faceVertexDataNode.set(VERTEX_ATTRIBUTE, parseIndexAndValidate(tokens[0], root.get(VERTICES_ATTRIBUTE).contentCount(), row));
        if (tokens.length == 1) {
            // Format: vertex_index
            return faceVertexDataNode;
        }
        if (!tokens[1].isBlank()) {
            // texture is optional
            faceVertexDataNode.set(VERTEX_TEXTURE_ATTRIBUTE, parseIndexAndValidate(tokens[1], root.get(VERTEX_TEXTURES_ATTRIBUTE).contentCount(), row));
        }
        if (tokens.length == 2) {
            // normal is optional
            return faceVertexDataNode;
        }
        faceVertexDataNode.set(VERTEX_NORMAL_ATTRIBUTE, parseIndexAndValidate(tokens[2], root.get(VERTEX_NORMALS_ATTRIBUTE).contentCount(), row));
        return faceVertexDataNode;
    }

    private int parseIndexAndValidate(String index, int listSize, int row) {
        int indexValue = parseIndex(index);
        if (indexValue >= listSize) {
            throw new RuntimeException(String.format("index out off bounds (row = %d) list size is %d but found index %d", row, listSize, indexValue));
        }
        return indexValue;
    }

    private int parseIndex(String index) {
        // indexes in obj file starts at 1, we should adjust when parsing to start at 0
        return Integer.parseInt(index) - 1;
    }

    // -- Serialization ---------

    private enum SerializeState {
        Vertex, VertexTextures, VertexNormals, Faces, Done
    }

    @Override
    public String serialize(DataNode data) {
        // Validate required attributes
        if (!data.contains(VERTICES_ATTRIBUTE)) {
            throw new RuntimeException(String.format("obj data node must contain '%s' attribute", VERTICES_ATTRIBUTE));
        }
        if (!data.contains(FACES_ATTRIBUTE)) {
            throw new RuntimeException(String.format("obj data node must contain '%s' attribute", FACES_ATTRIBUTE));
        }

        boolean hasNormals = data.contains(VERTEX_NORMALS_ATTRIBUTE);
        boolean hasTextures = data.contains(VERTEX_TEXTURES_ATTRIBUTE);

        StringBuilder output = new StringBuilder();
        int row = 0;
        SerializeState serializeState = SerializeState.Vertex;
        while (serializeState != SerializeState.Done) {
            switch (serializeState) {
                case Vertex:
                    row = outputVertices(data, output, row);
                    serializeState = getNextState(serializeState, hasNormals, hasTextures);
                    break;
                case VertexTextures:
                    row = outputTextures(data, output, row);
                    serializeState = getNextState(serializeState, hasNormals, hasTextures);
                    break;
                case VertexNormals:
                    row = outputNormals(data, output, row);
                    serializeState = getNextState(serializeState, hasNormals, hasTextures);
                    break;
                case Faces:
                    row = outputFaces(data, output, row, hasNormals, hasTextures);
                    serializeState = getNextState(serializeState, hasNormals, hasTextures);
                    break;
            }
        }
        return output.toString();
    }

    private SerializeState getNextState(SerializeState current, boolean hasNormals, boolean hasTextures) {
        switch (current) {
            case Vertex:
                return hasTextures ? SerializeState.VertexTextures : (hasNormals ? SerializeState.VertexNormals : SerializeState.Faces);
            case VertexTextures:
                return hasNormals ? SerializeState.VertexNormals : SerializeState.Faces;
            case VertexNormals:
                return SerializeState.Faces;
            default:
                // After Faces comes Done (also default for unknown)
                return SerializeState.Done;
        }
    }

    private int outputVertices(DataNode data, StringBuilder output, int currentRow) {
        int row = outputCommentsIfExists(data, output, currentRow);
        for (DataNode vertexNode : data.get(VERTICES_ATTRIBUTE)) {
            // Appends ordered vertex information
            float x = vertexNode.get("x").getFloat();
            float y = vertexNode.get("y").getFloat();
            float z = vertexNode.get("z").getFloat();
            if (!vertexNode.contains("w") || vertexNode.get("w").getFloat() == 1.0f) {
                output.append(String.format("v %s %s %s\n", x, y, z));
            } else {
                output.append(String.format("v %s %s %s %s\n", x, y, z, vertexNode.get("w").getFloat()));
            }
            row = outputCommentsIfExists(data, output, row + 1);
        }
        return row;
    }

    private int outputNormals(DataNode data, StringBuilder output, int currentRow) {
        int row = outputCommentsIfExists(data, output, currentRow);
        for (DataNode vertexNormalNode : data.get(VERTEX_NORMALS_ATTRIBUTE)) {
            // Appends ordered normal information
            float x = vertexNormalNode.get("x").getFloat();
            float y = vertexNormalNode.get("y").getFloat();
            float z = vertexNormalNode.get("z").getFloat();
            output.append(String.format("vn %s %s %s\n", x, y, z));
            row = outputCommentsIfExists(data, output, row + 1);
        }
        return row;
    }

    private int outputTextures(DataNode data, StringBuilder output, int currentRow) {
        int row = outputCommentsIfExists(data, output, currentRow);
        for (DataNode vertexTextureNode : data.get(VERTEX_TEXTURES_ATTRIBUTE)) {
            // Appends ordered texture information
            float x = vertexTextureNode.get("x").getFloat();
            float y = vertexTextureNode.get("y").getFloat();
            output.append(String.format("vt %s %s\n", x, y));
            row = outputCommentsIfExists(data, output, row + 1);
        }
        return row;
    }

    private int outputFaces(DataNode data, StringBuilder output, int currentRow, boolean hasNormals, boolean hasTextures) {
        int row = outputCommentsIfExists(data, output, currentRow);
        for (DataNode faceNode : data.getProperty(FACES_ATTRIBUTE)) {
            // Appends face information
            output.append("f").append(getFace(faceNode, hasNormals, hasTextures)).append("\n");
            row = outputCommentsIfExists(data, output, row + 1);
        }
        return row;
    }

    private String getFace(DataNode faceNode, boolean hasNormals, boolean hasTextures) {
        StringBuilder output = new StringBuilder();
        for (DataNode faceVertexNode : faceNode) {
            output.append(" ").append(getFaceVertexData(faceVertexNode, hasNormals, hasTextures));
        }
        return output.toString();
    }

    private String getFaceVertexData(DataNode faceVertexNode, boolean hasNormals, boolean hasTextures) {
        if (!hasNormals && !hasTextures) {
            return String.format("%d", faceVertexNode.get(VERTEX_ATTRIBUTE).getInt() + 1);
        }
        if (!hasNormals) {
            return String.format("%d/%d", faceVertexNode.get(VERTEX_ATTRIBUTE).getInt() + 1, faceVertexNode.get(VERTEX_TEXTURE_ATTRIBUTE).getInt() + 1);
        }
        if (!hasTextures) {
            return String.format("%d//%d", faceVertexNode.get(VERTEX_ATTRIBUTE).getInt() + 1, faceVertexNode.get(VERTEX_NORMAL_ATTRIBUTE).getInt() + 1);
        }
        return String.format("%d/%d/%d", faceVertexNode.get(VERTEX_ATTRIBUTE).getInt() + 1, faceVertexNode.get(VERTEX_TEXTURE_ATTRIBUTE).getInt() + 1, faceVertexNode.get(VERTEX_NORMAL_ATTRIBUTE).getInt() + 1);
    }

    private int outputCommentsIfExists(DataNode data, StringBuilder output, int currentRow) {
        int next = currentRow;
        String comment = getComment(data, next);
        while (comment != null) {
            output.append(String.format("%s\n", comment));
            next++;
            // check if next is also a comment
            comment = getComment(data, next);
        }
        return next;
    }

    private String getComment(DataNode root, int row) {
        String rowValue = String.valueOf(row);
        if (!root.contains(COMMENTS_ATTRIBUTE) || !root.get(COMMENTS_ATTRIBUTE).contains(rowValue)) {
            // Nothing to do
            return null;
        }
        // read the comment at the index
        return root.get(COMMENTS_ATTRIBUTE, rowValue).getString();
    }

}
