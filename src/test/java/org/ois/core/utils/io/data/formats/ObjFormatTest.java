package org.ois.core.utils.io.data.formats;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import static org.testng.Assert.assertEquals;

import org.ois.core.utils.io.data.DataNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ObjFormatTest {

    Path testFilesDirPath = Paths.get(".").toAbsolutePath().normalize().resolve(Paths.get("src","test","resources", "obj"));
    DataNode cube;
    DataNode triangulatedCube;

    @BeforeTest
    public void prepareTestNode() {
        cube = DataNode.Object();
        triangulatedCube = DataNode.Object();

        // Vertices

        DataNode vertices = DataNode.Object();
        vertices.add(
                createVertexData(-0.5f,-0.5f,-0.5f),
                createVertexData(0.5f,-0.5f,-0.5f),
                createVertexData(0.5f,0.5f,-0.5f),
                createVertexData(-0.5f,0.5f,-0.5f),
                createVertexData(-0.5f,-0.5f,0.5f),
                createVertexData(0.5f,-0.5f,0.5f),
                createVertexData(0.5f,0.5f,0.5f),
                createVertexData(-0.5f,0.5f,0.5f)
        );

        cube.set(ObjFormat.VERTICES_ATTRIBUTE, vertices);
        triangulatedCube.set(ObjFormat.VERTICES_ATTRIBUTE, vertices);

        // Textures

        DataNode textures = DataNode.Object();
        textures.add(
                createTextureData(0.0f, 0.0f),
                createTextureData(1.0f, 0.0f),
                createTextureData(1.0f, 1.0f),
                createTextureData(0.0f, 1.0f)
        );

        triangulatedCube.set(ObjFormat.VERTEX_TEXTURES_ATTRIBUTE, textures);

        // Normals

        DataNode normals = DataNode.Object();
        normals.add(
                createVertexData(0.0f, 0.0f, -1.0f),
                createVertexData(0.0f, 0.0f, 1.0f),
                createVertexData(-1.0f, 0.0f, 0.0f),
                createVertexData(1.0f, 0.0f, 0.0f),
                createVertexData(0.0f, 1.0f, 0.0f),
                createVertexData(0.0f, -1.0f, 0.0f)
        );

        triangulatedCube.set(ObjFormat.VERTEX_NORMALS_ATTRIBUTE, normals);

        // Faces

        DataNode cubeFaces = cube.getProperty(ObjFormat.FACES_ATTRIBUTE);
        cubeFaces.add(
                createFace(0, 1, 2, 3),
                createFace(4, 5, 6, 7),

                createFace(0, 4, 7, 3),
                createFace(1, 5, 6, 2),

                createFace(3, 2, 6, 7),
                createFace(0, 1, 5, 4)
        );

        DataNode triangulatedCubeFaces = triangulatedCube.getProperty(ObjFormat.FACES_ATTRIBUTE);
        triangulatedCubeFaces.add(
                DataNode.Collection(createFaceVertexData(0, 0, 0), createFaceVertexData(1, 1, 0), createFaceVertexData(2, 2, 0)),
                DataNode.Collection(createFaceVertexData(0, 0, 0), createFaceVertexData(2, 2, 0), createFaceVertexData(3, 3, 0)),

                DataNode.Collection(createFaceVertexData(4, 0, 1), createFaceVertexData(7, 3, 1), createFaceVertexData(6, 2, 1)),
                DataNode.Collection(createFaceVertexData(4, 0, 1), createFaceVertexData(6, 2, 1), createFaceVertexData(5, 1, 1)),

                DataNode.Collection(createFaceVertexData(0, 0, 2), createFaceVertexData(3, 3, 2), createFaceVertexData(7, 2, 2)),
                DataNode.Collection(createFaceVertexData(0, 0, 2), createFaceVertexData(7, 2, 2), createFaceVertexData(4, 1, 2)),

                DataNode.Collection(createFaceVertexData(1, 0, 3), createFaceVertexData(5, 1, 3), createFaceVertexData(6, 2, 3)),
                DataNode.Collection(createFaceVertexData(1, 0, 3), createFaceVertexData(6, 2, 3), createFaceVertexData(2, 3, 3)),

                DataNode.Collection(createFaceVertexData(3, 0, 4), createFaceVertexData(2, 1, 4), createFaceVertexData(6, 2, 4)),
                DataNode.Collection(createFaceVertexData(3, 0, 4), createFaceVertexData(6, 2, 4), createFaceVertexData(7, 3, 4)),

                DataNode.Collection(createFaceVertexData(0, 0, 5), createFaceVertexData(4, 3, 5), createFaceVertexData(5, 2, 5)),
                DataNode.Collection(createFaceVertexData(0, 0, 5), createFaceVertexData(5, 2, 5), createFaceVertexData(1, 1, 5))
        );
    }

    private DataNode createVertexData(float x, float y, float z) {
        DataNode node = DataNode.Object();
        node.set("x", x);
        node.set("y", y);
        node.set("z", z);
        return node;
    }

    private DataNode createTextureData(float x, float y) {
        DataNode node = DataNode.Object();
        node.set("x", x);
        node.set("y", y);
        return node;
    }

    private DataNode createFace(int ...vertices) {
        DataNode face = DataNode.Collection();
        for (int vertex :  vertices) {
            face.add(DataNode.Object().set(ObjFormat.VERTEX_ATTRIBUTE, vertex));
        }
        return face;
    }

    private DataNode createFaceVertexData(int vertex, int texture, int normal) {
        DataNode faceVertexData = DataNode.Object();
        faceVertexData.set(ObjFormat.VERTEX_ATTRIBUTE, vertex);
        faceVertexData.set(ObjFormat.VERTEX_TEXTURE_ATTRIBUTE, texture);
        faceVertexData.set(ObjFormat.VERTEX_NORMAL_ATTRIBUTE, normal);
        return faceVertexData;
    }

    @Test
    public void testSerialize() throws IOException {
        // Human readable
        String actual = ObjFormat.parser(true).serialize(cube);
        System.out.println(actual);
        assertEquals(actual, readTestObjFile(Files.readString(testFilesDirPath.resolve("testCube.obj"))));

        // Compact
        actual = ObjFormat.parser(true).serialize(triangulatedCube);
        System.out.println(actual);
        assertEquals(actual, readTestObjFile(Files.readString(testFilesDirPath.resolve("testCubeTriangulated.obj"))));
    }

    private String readTestObjFile(String content) {
        // Clean comments and extra spaces
        return content.replaceAll("\r","").replaceAll("\\s*#.*", "").replaceAll("\n\n", "\n").stripLeading().replaceAll(" +", " ").replaceAll(" $", "");
    }

    @Test
    public void testDeserialize() throws IOException {
        ObjFormat parser = ObjFormat.parser(true);
        // Parse object with 1-based indexing Faces
        assertEquals(parser.deserialize(Files.readString(testFilesDirPath.resolve("testCube.obj"))), cube);
        // Parse object with triangles Faces (v/vt/vn)
        assertEquals(parser.deserialize(Files.readString(testFilesDirPath.resolve("testCubeTriangulated.obj"))), triangulatedCube);
    }
}
