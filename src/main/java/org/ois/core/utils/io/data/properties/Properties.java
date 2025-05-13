package org.ois.core.utils.io.data.properties;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.math.Transform;

public class Properties {

    public interface DataPropertyHandler<D> {
        D load(DataNode attributeValue);
        DataNode convert(D data);
    }

    public static <C> Property<C> create(String key, DataPropertyHandler<C> handler) {
        return new Property<>(key) {
            @Override
            public C loadProperty(DataNode attributeValue) {
                return handler.load(attributeValue);
            }

            @Override
            public DataNode appendProperty(DataNode root) {
                root.set(key, convertToDataNode());
                return root;
            }

            @Override
            public DataNode convertToDataNode() {
                return handler.convert(managedData);
            }
        };
    }

    public static Property<Color> color(String key) {
        return create(key, new ColorDataPropertyHandler());
    }

    private static class ColorDataPropertyHandler implements DataPropertyHandler<Color> {
        @Override
        public Color load(DataNode attributeValue) {
            if (!attributeValue.getType().equals(DataNode.Type.Object)) {
                switch (attributeValue.getString().toLowerCase()) {
                    case "green":
                        return Color.GREEN;
                    case "red":
                        return Color.RED;
                    case "blue":
                        return Color.BLUE;
                    case "black":
                        return Color.BLACK;
                    case "yellow":
                        return Color.YELLOW;
                    case "orange":
                        return Color.ORANGE;
                    case "white":
                        return Color.WHITE;
                    case "purple":
                        return Color.PURPLE;
                    case "brown":
                        return Color.BROWN;
                    case "gray":
                        return Color.GRAY;
                }
            }
            Color color = new Color();
            if (attributeValue.contains("r")) {
                color.r = attributeValue.get("r").getFloat();
            }
            if (attributeValue.contains("g")) {
                color.g = attributeValue.get("g").getFloat();
            }
            if (attributeValue.contains("b")) {
                color.b = attributeValue.get("b").getFloat();
            }
            if (attributeValue.contains("a")) {
                color.a = attributeValue.get("a").getFloat();
            }
            return color;
        }

        @Override
        public DataNode convert(Color data) {
            DataNode root = DataNode.Object();
            String stringValue = null;
            if (Color.GREEN.equals(data)) {
                stringValue = "green";
            } else if (Color.RED.equals(data)) {
                stringValue = "red";
            } else if (Color.BLUE.equals(data)) {
                stringValue = "blue";
            } else if (Color.BLACK.equals(data)) {
                stringValue = "black";
            } else if (Color.YELLOW.equals(data)) {
                stringValue = "yellow";
            } else if (Color.ORANGE.equals(data)) {
                stringValue = "orange";
            } else if (Color.WHITE.equals(data)) {
                stringValue = "white";
            } else if (Color.PURPLE.equals(data)) {
                stringValue = "purple";
            } else if (Color.BROWN.equals(data)) {
                stringValue = "brown";
            } else if (Color.GRAY.equals(data)) {
                stringValue = "gray";
            }
            if (stringValue != null) {
                // string value representation
                root.setValue(stringValue);
                return root;
            }
            // Set actual values
            root.set("r", data.r);
            root.set("g", data.g);
            root.set("b", data.b);
            root.set("a", data.a);
            return root;
        }
    }

    public static Property<Transform> transform(String key) {
        return create(key, transform("position", "scale", "rotation"));
    }

    public static DataPropertyHandler<Transform> transform(String positionProperty, String scaleProperty, String rotationProperty) {
        DataPropertyHandler<Vector3> positionHandler = vector3();
        DataPropertyHandler<Vector3> scaleHandler = vector3();
        DataPropertyHandler<Vector3> rotationHandler = vector3();
        return new DataPropertyHandler<>() {
            @Override
            public Transform load(DataNode attributeValue) {
                Transform transform = new Transform();
                if (attributeValue.contains(positionProperty)) {
                    transform.position = positionHandler.load(attributeValue.get(positionProperty));
                }
                if (attributeValue.contains(scaleProperty)) {
                    transform.scale = scaleHandler.load(attributeValue.get(scaleProperty));
                }
                if (attributeValue.contains(rotationProperty)) {
                    transform.rotation = rotationHandler.load(attributeValue.get(rotationProperty));
                }
                return transform;
            }

            @Override
            public DataNode convert(Transform data) {
                DataNode root = DataNode.Object();
                root.set(positionProperty, positionHandler.convert(data.position));
                root.set(scaleProperty, scaleHandler.convert(data.scale));
                root.set(rotationProperty, rotationHandler.convert(data.rotation));
                return root;
            }
        };
    }

    public static Property<Vector3> vector3(String key) {
        return create(key, vector3());
    }

    public static DataPropertyHandler<Vector3> vector3() {
        return vector3("x", "y", "z");
    }

    public static DataPropertyHandler<Vector3> vector3(String xProperty, String yProperty, String zProperty) {
        return new DataPropertyHandler<>() {
            @Override
            public Vector3 load(DataNode attributeValue) {
                Vector3 vector = new Vector3();
                if (attributeValue.contains(xProperty)) {
                    vector.x = attributeValue.get(xProperty).getFloat();
                }
                if (attributeValue.contains(yProperty)) {
                    vector.y = attributeValue.get(yProperty).getFloat();
                }
                if (attributeValue.contains(zProperty)) {
                    vector.z = attributeValue.get(zProperty).getFloat();
                }
                return vector;
            }

            @Override
            public DataNode convert(Vector3 data) {
                DataNode root = DataNode.Object();
                root.set(xProperty, data.x);
                root.set(yProperty, data.y);
                root.set(zProperty, data.z);
                return root;
            }
        };
    }

    public static Property<Vector2> vector2(String key) {
        return create(key, vector2());
    }

    public static DataPropertyHandler<Vector2> vector2() {
        return vector2("x", "y");
    }

    public static DataPropertyHandler<Vector2> vector2(String xProperty, String yProperty) {
        return new DataPropertyHandler<>() {
            @Override
            public Vector2 load(DataNode attributeValue) {
                Vector2 vector = new Vector2();
                if (attributeValue.contains(xProperty)) {
                    vector.x = attributeValue.get(xProperty).getFloat();
                }
                if (attributeValue.contains(yProperty)) {
                    vector.y = attributeValue.get(yProperty).getFloat();
                }
                return vector;
            }

            @Override
            public DataNode convert(Vector2 data) {
                DataNode root = DataNode.Object();
                root.set(xProperty, data.x);
                root.set(yProperty, data.y);
                return root;
            }
        };
    }
}
