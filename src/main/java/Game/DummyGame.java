package Game;

/**
 * Created by IceEye on 2017-03-02.
 */

import engine.*;
import engine.graph.Mesh;
import engine.graph.OBJLoader;
import engine.graph.lights.DirectionalLight;
import engine.graph.lights.PointLight;
import engine.graph.lights.SpotLight;
import engine.items.GameItem;
import engine.items.NoiseTerrain;
import engine.items.SkyBox;
import org.joml.Vector3f;
import engine.graph.Texture;
import engine.graph.Camera;
import org.joml.*;
import engine.graph.*;
import engine.items.Terrain;
import engine.Window;
import java.util.List;

import java.lang.Math;

import engine.graph.ImprovedNoise;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private SceneLight sceneLight;

    private Hud hud;

    private static final float CAMERA_POS_STEP = 0.05f;

    private Scene scene;

    GameItem[] gameItems;

    Vector3f ambiantLight;

    int boost = 15;
    int moveSpeed = 1;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(10.0f, 10.0f, 10.0f);

        gameItems = new GameItem[1];

    }

    @Override
    public void init(Window window) throws Exception {

        OBJMaker objMaker = new OBJMaker();
        objMaker.createFile("OutputObject.obj"); // Name of the created object file

        renderer.init(window);
        scene = new Scene();

        //Set up terrain
        float terrainScale = 100;
        int terrainSize = 1;
        float minY = -0.08f;
        float maxY = 0.08f;
        int textInc = 40;

        //Terrain terrain = new Terrain(terrainSize, terrainScale, minY, maxY, "/textures/rivermap.png", "/textures/terrain.png", textInc); //Specifying which Heightmap and texture file to load and binding it to a Terrain type
        NoiseTerrain terrain = new NoiseTerrain(terrainSize, terrainScale, minY, maxY, "/textures/terrain.png", textInc);
        scene.setGameItems(terrain.getGameItems());

        List<Float> tPositions = terrain.getNoiseMapMesh().getPositions();
        List<Integer> iPositions = terrain.getNoiseMapMesh().getIndices();

        objMaker.setVerticies(tPositions, iPositions);
        objMaker.setIndices(iPositions);

        //objMaker.closeFile();

        GameItem vase = new GameItem();
        Mesh vaseMesh = OBJLoader.loadMesh("/models/greek_vase.obj");
        Texture tex = new Texture("/textures/VASE_TEX.png");
        vaseMesh.setMaterial(new Material(tex));
        vase.setScale(0.01f);
        vase.setPosition(3,3,3);
        vase.setMesh(vaseMesh);

        gameItems[0] = vase;

        scene.setGameItems(gameItems);

        float skyBoxScale = 400.0f;
        float extension = 2.0f;

        float startx = extension * (-skyBoxScale+0.5f);
        float startz = extension * (skyBoxScale - 0.5f);
        float starty = - 1.0f;
        float inc = 0.5f * 2;

        setupLights();

        //Skybox setup
        SkyBox skyBox = new SkyBox("/models/skybox.obj", "/textures/SpaceBox.png");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);


        hud = new Hud(("X: " + camera.getPosition().x + " Y: " + camera.getPosition().y + " Z: " + camera.getPosition().x));
        gameItems[0].setPosition(-1,-1,-1);


    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);

        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            moveSpeed = boost;

        } else {
            moveSpeed = 1;
        }

        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -moveSpeed;

        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = moveSpeed;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -moveSpeed;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = moveSpeed;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -moveSpeed;

        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = moveSpeed;
        }

    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        Vector3f curPos = camera.getPosition();

        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

        // Update camera based on mouse
        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);

            // Update HUD compass
            hud.rotateCompass(camera.getRotation().y);

        }

        PointLight[] pointLightList = sceneLight.getPointLightList();

        pointLightList[0].setPosition(camera.getPosition());

        hud.setStatusText(("X: " + Math.floor(curPos.x) + " Y: " + Math.floor(curPos.y) + " Z: " + Math.floor(curPos.z)));

    }

    @Override
    public void render(Window window) {
        hud.updateSize(window);
        renderer.render(window, camera, scene, hud);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
        hud.cleanup();
    }

    public void setupLights(){
        sceneLight = new SceneLight();

        // Ambient Light
        ambiantLight = (new Vector3f(0.2f, 0.2f, 0.2f));
        sceneLight.setAmbientLight(ambiantLight);
        scene.setSceneLight(sceneLight);


        //Directional Light
        Vector3f lightPosition = new Vector3f(-1, 1, -1);
        float lightIntensity = 3.0f;
        sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1.1f, 1, 1), lightPosition, lightIntensity));

        // Point Light
        lightPosition = new Vector3f(0, 0, 1);
        lightIntensity = 0.0f;
        PointLight pointLight = new PointLight(new Vector3f(1.1f, 1, 1), lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);
        sceneLight.setPointLightList(new PointLight[]{pointLight});

        // Spot Light
        lightPosition = new Vector3f(0, 0.0f, 10f);
        lightIntensity = 4.0f;
        pointLight = new PointLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
        att = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
        pointLight.setAttenuation(att);
        Vector3f coneDir = camera.getRotation();
        float cutoff = (float) Math.cos(Math.toRadians(180));
        SpotLight spotLight = new SpotLight(pointLight, coneDir, cutoff);
        sceneLight.setSpotLightList(new SpotLight[]{spotLight});

    }

}