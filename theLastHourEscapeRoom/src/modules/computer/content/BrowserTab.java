package modules.computer.content;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import contrib.hud.dialogs.DialogCallbackResolver;
import core.utils.Scene2dElementFactory;
import java.util.*;
import java.util.List;
import modules.computer.ComputerDialog;
import modules.computer.ComputerFactory;
import modules.computer.ComputerStateComponent;
import util.Lore;

/** A tab representing a web browser within the computer dialog. */
public class BrowserTab extends ComputerTab {

  private static BrowserTab Instance;

  private static final String PLACEHOLDER = "Enter URL...";
  private String url = "";
  private TextField urlField;
  private Table contentTable;
  private Table historyTable;
  private Map<String, Actor> websites = null;

  /**
   * Creates a new BrowserTab with the given shared state.
   *
   * @param sharedState the shared computer state component
   */
  public BrowserTab(ComputerStateComponent sharedState) {
    super(sharedState, "browser", "Browser", false);
    Instance = this;
    navigate(url);
  }

  /**
   * Gets the map of available websites and their corresponding content actors. Lazily initialized.
   *
   * @return a map of URLs to content actors
   */
  public static Optional<BrowserTab> getInstance() {
    return Optional.ofNullable(Instance);
  }

  protected void createActors() {
    this.url = localState().browserUrl();
    this.clearChildren();

    Table main = new Table();

    Table browseArea = new Table();
    browseArea.add(createAdressBar()).growX().row();
    browseArea
        .add(Scene2dElementFactory.createHorizontalDivider())
        .height(4)
        .pad(10, 0, 10, 0)
        .fillX()
        .row();
    contentTable = new Table();
    browseArea.add(contentTable).grow();

    historyTable = new Table();
    refreshHistorySidebar();

    main.add(browseArea).grow();
    main.add(Scene2dElementFactory.createVerticalDivider()).width(4).fillY().pad(0, 10, 0, 10);
    main.add(historyTable).width(300).fillY();
    this.add(main).grow();
  }

  private void refreshHistorySidebar() {
    Label header = Scene2dElementFactory.createLabel("History", 24, Color.GRAY);

    Table scrollContent = new Table();
    scrollContent.top().left();
    localState()
        .browserHistory()
        .forEach(
            entry -> {
              Label link = EmailsTab.createLinkLabel(entry, entry);
              link.setAlignment(Align.left);
              link.setWrap(true);
              scrollContent.add(link).growX().row();
              scrollContent
                  .add(Scene2dElementFactory.createHorizontalDivider())
                  .height(4)
                  .pad(5, 0, 5, 0)
                  .fillX()
                  .row();
            });
    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(scrollContent, false, true);

    Table table = new Table();
    table.add(header).padBottom(10).row();
    table.add(scrollPane).grow();

    historyTable.clearChildren();
    historyTable.add(table).grow();
  }

  private Table createAdressBar() {
    urlField = Scene2dElementFactory.createTextField(url);
    urlField.setMessageText(PLACEHOLDER);
    urlField.setAlignment(Align.left);
    urlField.addListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (urlField.getText().equals(PLACEHOLDER)) {
              urlField.setText("");
            }
            return super.touchDown(event, x, y, pointer, button);
          }

          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode == Input.Keys.ENTER) {
              url = urlField.getText().trim();
              navigate(url);
              return true;
            }
            return super.keyDown(event, keycode);
          }
        });

    Button goButton = Scene2dElementFactory.createButton("Go", "clean-green");
    goButton.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            url = urlField.getText().trim();
            navigate(url);
            return super.touchDown(event, x, y, pointer, button);
          }
        });

    Table table = new Table();
    table.add(urlField).growX().height(50);
    table.add(goButton).width(100).height(50).padLeft(10);
    return table;
  }

  /**
   * Navigates the browser state to a given URL.
   *
   * @param url the URL to navigate to
   */
  public void navigate(String url) {
    if (Lore.VirusWebsites.contains(url)) {
      localState().browserHistory().add(url);
      var newState =
          ComputerStateComponent.getState()
              .orElseThrow()
              .withVirusType(
                  Lore.CodePageIndexToVirusType.get(
                      (int) (Math.random() * Lore.CodePageIndexToVirusType.size())))
              .withInfection(true);
      DialogCallbackResolver.createButtonCallback(
              context().dialogId(), ComputerFactory.UPDATE_STATE_KEY)
          .accept(newState);
      return;
    }

    this.url = url;
    localState().browserUrl(url);
    localState().browserHistory().add(url);
    urlField.setText(url);
    contentTable.clear();

    Actor target;
    if (getWebsites().containsKey(url)) {
      target = getWebsites().get(url);
    } else {
      target = create404Page();
    }

    contentTable.add(target).grow();
    refreshHistorySidebar();
  }

  private Actor create404Page() {
    Table table = new Table();
    Label label =
        Scene2dElementFactory.createLabel("404 - Page Not Found", 48, new Color(0.8f, 0, 0, 1));
    table.add(label);
    return table;
  }

  private Actor createNoContentPage() {
    Table table = new Table();
    Label label =
        Scene2dElementFactory.createLabel("Enter a URL to visit a website!", 48, Color.GRAY);
    table.add(label);
    return table;
  }

  private Actor createSecurityCodePage(String pageUrl, int index) {
    Table table = new Table();
    table.top().left();
    table.pad(20);

    // Header banner
    Image header = new Image(skin, "sg4-header");
    table.add(header).width(600).height(200).center().colspan(1).row();

    table
        .add(Scene2dElementFactory.createHorizontalDivider())
        .height(4)
        .pad(10, 0, 10, 0)
        .fillX()
        .row();

    Label breadcrumb =
        Scene2dElementFactory.createLabel(
            "Home  >  Support  >  SG-4 Recovery  >  Sequence 1 of 2", 16, Color.GRAY);
    breadcrumb.setAlignment(Align.left);
    table.add(breadcrumb).left().padBottom(15).row();

    Label title =
        Scene2dElementFactory.createLabel(
            "SG-4 Access Recovery - Code Fragment #3", 32, Color.BLACK);
    title.setAlignment(Align.left);
    table.add(title).left().padBottom(5).row();

    Label subtitle =
        Scene2dElementFactory.createLabel(
            "SecuGate Systems  |  Authorized Recovery Portal  |  Ref: SG4-2205",
            16,
            Color.DARK_GRAY);
    subtitle.setAlignment(Align.left);
    table.add(subtitle).left().padBottom(15).row();

    table
        .add(Scene2dElementFactory.createHorizontalDivider())
        .height(2)
        .pad(0, 0, 15, 0)
        .fillX()
        .row();

    Label aboutHeader = Scene2dElementFactory.createLabel("About the SG-4", 24, Color.BLACK);
    aboutHeader.setAlignment(Align.left);
    table.add(aboutHeader).left().padBottom(8).row();

    Label aboutText =
        Scene2dElementFactory.createLabel(
            "The SecuGate SG-4 is a high-security electronic door locking system designed for "
                + "research facilities and corporate environments. Featuring 256-bit encrypted "
                + "access tokens, tamper-resistant hardware, and multi-factor authentication "
                + "support, the SG-4 provides industry-leading protection for restricted areas. "
                + "In the event of a lockout, authorized personnel may use this recovery portal "
                + "to reconstruct their access credentials.",
            18,
            Color.DARK_GRAY);
    aboutText.setWrap(true);
    aboutText.setAlignment(Align.left);
    table.add(aboutText).growX().left().padBottom(20).row();

    // Important content
    Table noticeBox = new Table(skin);
    noticeBox.setBackground("blue_square_border");
    noticeBox.pad(12);
    Label noticeIcon =
        Scene2dElementFactory.createLabel("> Recovery Notice", 18, new Color(0.1f, 0.3f, 0.7f, 1));
    noticeIcon.setAlignment(Align.left);
    noticeBox.add(noticeIcon).left().row();
    Label noticeText =
        Scene2dElementFactory.createLabel(
            "The following data block contains an encoded credential fragment for your SG-4 "
                + "unit. Use your decryption manual to determine the 4-character password from "
                + "the data shown below. Do not share this information with unauthorized personnel.",
            16,
            Color.DARK_GRAY);
    noticeText.setWrap(true);
    noticeText.setAlignment(Align.left);
    noticeBox.add(noticeText).growX().left().padTop(6);
    table.add(noticeBox).growX().padBottom(20).row();

    // Binary data display
    Label dataHeader = Scene2dElementFactory.createLabel("Encoded Data Block", 22, Color.BLACK);
    dataHeader.setAlignment(Align.left);
    table.add(dataHeader).left().padBottom(8).row();

    String asciiCode = Lore.AsciiCodes.get(index);
    String binaryData = toBinary(asciiCode);

    // Stack binary codes vertically in 8-bit rows
    Table binaryBox = new Table(skin);
    binaryBox.setBackground("blue_square_border");
    binaryBox.pad(16);
    for (int i = 0; i < binaryData.length(); i += 8) {
      String octet = binaryData.substring(i, Math.min(i + 8, binaryData.length()));
      Label octetLabel = Scene2dElementFactory.createLabel(octet, 28, new Color(0, 0.5f, 0, 1));
      octetLabel.setAlignment(Align.center);
      binaryBox.add(octetLabel).center().row();
    }
    table.add(binaryBox).growX().padBottom(20).row();

    table
        .add(Scene2dElementFactory.createHorizontalDivider())
        .height(2)
        .pad(0, 0, 15, 0)
        .fillX()
        .row();

    // Verification section
    Label verifyHeader = Scene2dElementFactory.createLabel("Verification", 22, Color.BLACK);
    verifyHeader.setAlignment(Align.left);
    table.add(verifyHeader).left().padBottom(8).row();

    Label verifyDesc =
        Scene2dElementFactory.createLabel(
            "Enter the decoded 4-character password to verify your identity and begin the download.",
            18,
            Color.DARK_GRAY);
    verifyDesc.setWrap(true);
    verifyDesc.setAlignment(Align.left);
    table.add(verifyDesc).growX().left().padBottom(10).row();

    // Password input
    Table inputRow = new Table();
    Label passwordLabel = Scene2dElementFactory.createLabel("Password:", 20, Color.BLACK);
    inputRow.add(passwordLabel).padRight(10);

    TextField passwordField = Scene2dElementFactory.createTextField("");
    passwordField.setMessageText("Decoded password...");
    inputRow.add(passwordField).width(300).height(45).padRight(10);

    Label feedback = Scene2dElementFactory.createLabel("", 18, Color.RED);

    Button submitButton = Scene2dElementFactory.createButton("Verify", "clean-green", 20);
    submitButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            String input = passwordField.getText().trim();
            if (input.equals(asciiCode)) {
              navigate(pageUrl + "/download");
            } else {
              feedback.setText("Incorrect password. Please check your decryption manual.");
              feedback.setColor(Color.RED);
            }
          }
        });
    inputRow.add(submitButton).height(45);

    table.add(inputRow).left().padBottom(8).row();
    table.add(feedback).left().padBottom(20).row();

    table
        .add(Scene2dElementFactory.createHorizontalDivider())
        .height(2)
        .pad(0, 0, 15, 0)
        .fillX()
        .row();

    // And back to filler stuff
    Label productHeader = Scene2dElementFactory.createLabel("SG-4 Specifications", 20, Color.BLACK);
    productHeader.setAlignment(Align.left);
    table.add(productHeader).left().padBottom(6).row();

    Label productInfo =
        Scene2dElementFactory.createLabel(
            "Model: SG-4 Pro  |  Firmware: v3.8.1  |  Encryption: AES-256  |  "
                + "Certification: ISO 27001  |  Warranty: 5 years",
            14,
            Color.GRAY);
    productInfo.setWrap(true);
    productInfo.setAlignment(Align.left);
    table.add(productInfo).growX().left().padBottom(15).row();

    Label footer =
        Scene2dElementFactory.createLabel(
            "2026 SecuGate Systems GmbH  |  All rights reserved  |  support@secugate-systems.com",
            14,
            Color.GRAY);
    footer.setAlignment(Align.center);
    table.add(footer).center().padTop(10).row();

    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(table, false, true);
    return scrollPane;
  }

  private Actor createDownloadPage(int index) {
    Table table = new Table();
    table.top().padTop(40);

    Label successLabel =
        Scene2dElementFactory.createLabel("Verification successful.", 32, new Color(0, 0.6f, 0, 1));
    successLabel.setAlignment(Align.center);
    table.add(successLabel).center().padBottom(20).row();

    Label downloadInfo =
        Scene2dElementFactory.createLabel(
            "Your access credential fragment is ready for download.", 20, Color.DARK_GRAY);
    downloadInfo.setAlignment(Align.center);
    table.add(downloadInfo).center().padBottom(20).row();

    TextButton downloadButton =
        Scene2dElementFactory.createButton("Open PDF", "clean-blue-outline", 18);
    downloadButton.padLeft(downloadButton.getPadLeft() + 10);
    downloadButton.padRight(downloadButton.getPadRight() + 10);
    downloadButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (index == 0) {
              ComputerDialog.getInstance()
                  .ifPresent(
                      c -> c.addTab(new FileTab(sharedState(), Lore.AccessCodeDownloadFileName)));
            } else {
              String virusType = Lore.CodePageIndexToVirusType.get(index - 1);
              var newState =
                  ComputerStateComponent.getState()
                      .orElseThrow()
                      .withVirusType(virusType)
                      .withInfection(true);
              DialogCallbackResolver.createButtonCallback(
                      context().dialogId(), ComputerFactory.UPDATE_STATE_KEY)
                  .accept(newState);
            }
          }
        });
    table.add(downloadButton).center();

    return table;
  }

  private Map<String, Actor> getWebsites() {
    if (websites == null) registerWebsites();
    return websites;
  }

  private void registerWebsites() {
    websites = new HashMap<>();
    websites.put("", createNoContentPage());
    websites.put(
        "https://www.example.com",
        Scene2dElementFactory.createLabel("Helloooo world :D", 96, Color.BLACK));

    for (int i = 0; i < Lore.EmailCodeUrls.size(); i++) {
      String url = Lore.EmailCodeUrls.get(i);
      websites.put(url, createSecurityCodePage(url, i));
      websites.put(url + "/download", createDownloadPage(i));
    }
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {}

  /**
   * Converts a string input into its binary representation, where each character is represented by
   * its 8-bit ASCII code.
   *
   * @param input the string to convert to binary
   * @return a string representing the binary encoding of the input string
   */
  private String toBinary(String input) {
    List<Integer> asciiCodes = new ArrayList<>();
    for (char c : input.toCharArray()) {
      asciiCodes.add((int) c);
    }

    StringBuilder binaryBuilder = new StringBuilder();
    for (int code : asciiCodes) {
      String binaryString = String.format("%8s", Integer.toBinaryString(code)).replace(' ', '0');
      binaryBuilder.append(binaryString);
    }
    return binaryBuilder.toString();
  }
}
