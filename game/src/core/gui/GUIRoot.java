package core.gui;

import static core.gui.util.Logging.log;

import core.Assets;
import core.gui.events.GUIElementListUpdateEvent;
import core.gui.events.GUIResizeEvent;
import core.gui.layouts.BorderLayout;
import core.gui.layouts.FillLayout;
import core.gui.layouts.hints.BorderLayoutHint;
import core.gui.math.Vector2f;
import core.gui.math.Vector4f;
import core.gui.util.Font;
import core.utils.logging.CustomLogLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GUIRoot {

    private static GUIRoot instance;

    public static GUIRoot getInstance() {
        return instance;
    }

    public static Optional<GUIRoot> getInstanceOptional() {
        return Optional.ofNullable(instance);
    }

    public static void init(IGUIBackend backend) {
        instance = new GUIRoot(backend);
    }

    private final IGUIBackend backend;
    private final GUIContainer rootContainer;
    private final List<GUIElement> elementList = new ArrayList<>();
    private boolean updateNextFrame = true;
    private boolean firstFrame = true;

    private GUIRoot(IGUIBackend backend) {
        this.backend = backend;
        this.rootContainer =
                new GUIContainer(new BorderLayout(BorderLayout.BorderLayoutMode.HORIZONTAL)) {
                    @Override
                    public void event(GUIEvent event) {
                        if (event instanceof GUIElementListUpdateEvent) {
                            GUIRoot.this.update();
                        }
                    }
                };
        this.rootContainer.position = Vector2f.zero();
        GUIRootListener.init(this);
    }

    private void setup() {
        try {
            Font[] fonts = Font.loadFont(Assets.Fonts.OPENSANS_REGULAR, 24);

            String longText =
                    "By in no ecstatic wondered disposal my speaking. Direct wholly valley or uneasy it at really. Sir wish like said dull and need make. Sportsman one bed departure rapturous situation disposing his. Off say yet ample ten ought hence. Depending in newspaper an september do existence strangers. Total great saw water had mirth happy new. Projecting pianoforte no of partiality is on. Nay besides joy society him totally six.\n"
                            + "\n"
                            + "Old education him departure any arranging one prevailed. Their end whole might began her. Behaved the comfort another fifteen eat. Partiality had his themselves ask pianoforte increasing discovered. So mr delay at since place whole above miles. He to observe conduct at detract because. Way ham unwilling not breakfast furniture explained perpetual. Or mr surrounded conviction so astonished literature. Songs to an blush woman be sorry young. We certain as removal attempt.\n"
                            + "\n"
                            + "Ignorant saw her her drawings marriage laughter. Case oh an that or away sigh do here upon. Acuteness you exquisite ourselves now end forfeited. Enquire ye without it garrets up himself. Interest our nor received followed was. Cultivated an up solicitude mr unpleasant.\n"
                            + "\n"
                            + "Travelling alteration impression six all uncommonly. Chamber hearing inhabit joy highest private ask him our believe. Up nature valley do warmly. Entered of cordial do on no hearted. Yet agreed whence and unable limits. Use off him gay abilities concluded immediate allowance.\n"
                            + "\n"
                            + "Much did had call new drew that kept. Limits expect wonder law she. Now has you views woman noisy match money rooms. To up remark it eldest length oh passed. Off because yet mistake feeling has men. Consulted disposing to moonlight ye extremity. Engage piqued in on coming.\n"
                            + "\n"
                            + "Shewing met parties gravity husband sex pleased. On to no kind do next feel held walk. Last own loud and knew give gay four. Sentiments motionless or principles preference excellence am. Literature surrounded insensible at indulgence or to admiration remarkably. Matter future lovers desire marked boy use. Chamber reached do he nothing be.\n"
                            + "\n"
                            + "Why end might ask civil again spoil. She dinner she our horses depend. Remember at children by reserved to vicinity. In affronting unreserved delightful simplicity ye. Law own advantage furniture continual sweetness bed agreeable perpetual. Oh song well four only head busy it. Afford son she had lively living. Tastes lovers myself too formal season our valley boy. Lived it their their walls might to by young.\n"
                            + "\n"
                            + "Talent she for lively eat led sister. Entrance strongly packages she out rendered get quitting denoting led. Dwelling confined improved it he no doubtful raptures. Several carried through an of up attempt gravity. Situation to be at offending elsewhere distrusts if. Particular use for considered projection cultivated. Worth of do doubt shall it their. Extensive existence up me contained he pronounce do. Excellence inquietude assistance precaution any impression man sufficient.\n"
                            + "\n"
                            + "Far curiosity incommode now led smallness allowance. Favour bed assure son things yet. She consisted consulted elsewhere happiness disposing household any old the. Widow downs you new shade drift hopes small. So otherwise commanded sweetness we improving. Instantly by daughters resembled unwilling principle so middleton. Fail most room even gone her end like. Comparison dissimilar unpleasant six compliment two unpleasing any add. Ashamed my company thought wishing colonel it prevent he in. Pretended residence are something far engrossed old off.\n"
                            + "\n"
                            + "Situation admitting promotion at or to perceived be. Mr acuteness we as estimable enjoyment up. An held late as felt know. Learn do allow solid to grave. Middleton suspicion age her attention. Chiefly several bed its wishing. Is so moments on chamber pressed to. Doubtful yet way properly answered humanity its desirous. Minuter believe service arrived civilly add all. Acuteness allowance an at eagerness favourite in extensive exquisite ye.";

            GUIText guiText1 = new GUIText(longText, fonts[0]);
            GUIText guiText2 = new GUIText(longText, fonts[0]);
            GUIText guiText3 = new GUIText(longText, fonts[0]);
            guiText1.backgroundColor(new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
            guiText2.backgroundColor(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
            guiText3.backgroundColor(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
            guiText1.scrollY(true);
            guiText2.scrollY(true);
            guiText3.scrollY(true);

            GUIImage debugImage = new GUIImage(backend.loadImage(Assets.Images.DEBUG_IMAGE));
            debugImage.scaleMode(GUIImage.ScaleMode.COVER);

            GUIContainer centerContainer =
                    new GUIContainer(new FillLayout(FillLayout.FillDirection.ROW));

            this.rootContainer.add(guiText3, BorderLayoutHint.SOUTH);
            this.rootContainer.add(guiText1, BorderLayoutHint.EAST);
            this.rootContainer.add(guiText2, BorderLayoutHint.WEST);
            this.rootContainer.add(debugImage, BorderLayoutHint.NORTH);
            // this.rootContainer.add(centerContainer, BorderLayoutHint.CENTER);

            this.update();
        } catch (IOException ex) {
            log(CustomLogLevel.ERROR, "Failed to load font", ex);
        }
    }

    public void render(float delta) {
        if (firstFrame) {
            firstFrame = false;
            this.setup();
            this.updateNextFrame = true;
        }
        this.backend.render(this.elementList, this.updateNextFrame);
        this.updateNextFrame = false;
    }

    public void event(GUIEvent event) {
        // Handle events
        if (event instanceof GUIResizeEvent resizeEvent) {
            this.backend.resize(resizeEvent.width(), resizeEvent.height());
            this.rootContainer.size = new Vector2f(resizeEvent.width(), resizeEvent.height());
            this.update();
            return;
        }
        if (event instanceof GUIElementListUpdateEvent) {
            this.update();
        }
        this.rootContainer.fireEvent(event, GUIEvent.TraverseMode.DOWN);
    }

    /** Invalidates the current layout and redraws & updates it. */
    public void update() {
        this.elementList.clear();
        // Breitendurchlauf durch elemente
        List<GUIContainer> queue = new ArrayList<>();
        queue.add(this.rootContainer);
        while (!queue.isEmpty()) {
            GUIContainer container = queue.remove(0);
            container.layout.layout(container, container.elements);
            container.elements.forEach(
                    element -> {
                        if (element instanceof GUIContainer container1) {
                            queue.add(container1);
                        } else {
                            this.elementList.add(element);
                        }
                        element.update();
                    });
        }
        this.updateNextFrame = true;
    }

    /**
     * Get the currently used GUIBackend.
     *
     * @return IGUIBackend
     */
    public IGUIBackend backend() {
        return this.backend;
    }
}
