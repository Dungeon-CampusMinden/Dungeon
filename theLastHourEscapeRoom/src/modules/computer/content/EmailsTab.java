package modules.computer.content;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import core.sound.Sounds;
import core.utils.Cursors;
import core.utils.Scene2dElementFactory;
import java.util.Arrays;
import java.util.List;
import modules.computer.ComputerDialog;
import modules.computer.ComputerStateComponent;
import util.LastHourSounds;

public class EmailsTab extends ComputerTab {

  private static final String PARAGRAPH_SPLIT = "\\\\p";
  private static final String LINK_TOKEN = "\\a";

  private static final String OWN_MAIL = "dr.martin.brenner@company.xyz";
  private static final String OWN_NAME = "Dr. Martin Brenner";
  private static final String MAILBOX_NAME = "Lookout";
  private static final List<Email> inbox =
      List.of(
          new Email(
              "Dr. Smith",
              "dr.smith@gmail.com",
              "Highly confidential research",
              "Hiii,\nthis is a test.\\pAnother paragraph\\p\\aCheck out this website I made!;https://www.example.com",
              List.of("Hello.html", "Important.xlsx")),
          new Email(
              "Account Security",
              "security@paypa1.com",
              "Unusual login attempt detected",
              "We detected a suspicious sign-in attempt on your account.\\pIf this was not you, verify your identity immediately.\\p\\aVisit Paypa1;https://paypa1.com/secure",
              List.of()),
          new Email(
              "cloud support team",
              "notifications@cloudservice",
              "Wir haben Ihr Konto gesperrt! Ihre Fotos und Videos werden am",
              "\\aZur Löschung vorgesehen;https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm\\p\\aIhr Konto war inaktiv und hat das Speicherlimit überschritten. Gemäß unserer Aufbewahrungsrichtlinien sind Ihre Dateien zur Löschung vorgesehen;https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm\\p\\aDAUERTHAFTER DATENVERLUST\nWenn Sie Ihren Speicherplan nicht bis zum verlängern, werden Ihre Daten dauerhaft von unseren Servern gelöscht;https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm\\p\\p\\aMeine Dateien behalten;https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm\\p\\p\\aAbmelden;https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm",
              List.of("License_renewal.ics")),
          new Email(
              "IT Support",
              "support@company.internal",
              "Password expiration notice",
              "Hello,\\pYour password will expire in 3 days. Please update it as soon as possible.\\p\\aChange Password;https://intranet.company/reset",
              List.of("Help.html")),
          new Email(
              "HR Department",
              "hr@company.internal",
              "Updated employee handbook",
              "Dear employee,\\pWe have published an updated version of the employee handbook.\\pPlease review the attached document.",
              List.of("Employee_Handbook_2026.pdf")),
          new Email(
              "Microsoft Billing",
              "billing@microslop.com",
              "Your subscription will be suspended",
              "We were unable to process your last payment.\\pTo avoid service interruption, confirm your billing information now.\\p\\ahttps://microsoft-support.co/billing;https://microslop-support.co/billing",
              List.of("Receipt.html")),
          new Email(
              "Alex Johnson",
              "alex.johnson@partner.org",
              "Meeting notes & next steps",
              "Hey,\\pThanks for the meeting earlier today.\\pI've attached the notes and action items we discussed.",
              List.of("Meeting_Notes.docx")),
          new Email(
              "Microsoft Cloud-Speicher",
              "Microsoft_account_team@onmicrosoft.com",
              "HEUTE: Ihre Fotos und Videos werden gelöscht - Sofort handeln",
              "Microsoft\\pKontobenachrichtigung - Cloud-Speicher - SOFORTIGE AKTION\\pSehr geehrte Nutzerin / sehr geehrter Nutzer,\\pDies ist eine AUTOMATISCHE KRITISCHE WARNUNG des Sicherheitssystems von Microsoft.\\pIHRE FOTOS UND VIDEOS\nWERDEN DAUERHAFT GELÖSCHT\\pServicedetails:\\pProdukt: Microsoft Cloud-Speicher\\pAblaufdatum: HEUTE - LETZER TAG\\p\\aSPEICHER JETZT FREISCHALTEN\nKOSTENLOS;https://subkuzb.somelinkshortener.org/4MkOtH235ArQM4epqmbnrkgm18NVSIMYXZAXQPQZB1017602ARSS330N1",
              List.of()),
          new Email(
              "Online Store",
              "no-reply@shop.example",
              "Your order has shipped",
              "Good news!\\pYour order #48392 has been shipped and is on its way.\\p\\ahttps://shop.example/track/48392",
              List.of("Invoice_48392.pdf")),
          new Email(
              "Online Store",
              "no-reply@shop.example",
              "Your order has shipped 2",
              "Good news!\\pYour order #48392 has been shipped and is on its way.\\p\\ahttps://shop.example/track/48392",
              List.of("Invoice_48392.pdf")),
          new Email(
              "Online Store",
              "no-reply@shop.example",
              "Your order has shipped 3",
              "Good news!\\pYour order #48392 has been shipped and is on its way.\\p\\ahttps://shop.example/track/48392",
              List.of("Invoice_48392.pdf")));

  private static final String LINK_NONE = "Full Link: <none>";
  private static final String LINK_SOME = "Full Link: %s";

  private Table emailDetailsContainer = null;
  private Label emailLinkFull = null;
  private Email selectedEmail = null;

  public EmailsTab(ComputerStateComponent sharedState) {
    super(sharedState, "emails", "E-Mails", false);
  }

  protected void createActors() {
    this.title("E-Mails (" + inbox.size() + ")");
    this.selectedEmail = localState().selectedEmail();
    this.clearChildren();
    this.add(createEmailListContainer()).growY().top();
    this.add(Scene2dElementFactory.createVerticalDivider()).width(4).pad(0, 10, 0, 10).fillY();
    emailDetailsContainer = createEmailDetailsContainer();
    this.add(emailDetailsContainer).grow();
  }

  private Table createEmailListContainer() {
    System.out.println("Rebuilding email list container");
    Table container = new Table();

    Label inboxLabel = Scene2dElementFactory.createLabel(MAILBOX_NAME, 64, Color.BLACK);
    container.add(inboxLabel).left().padBottom(10).row();

    Label userLabel =
        Scene2dElementFactory.createLabel(OWN_NAME + " <" + OWN_MAIL + ">", 20, Color.DARK_GRAY);
    container.add(userLabel).left().padBottom(20).row();

    Table subArea = new Table();
    container.add(subArea).grow().padLeft(15).row();

    Label emailsLabel =
        Scene2dElementFactory.createLabel("E-Mails: (" + inbox.size() + ")", 24, Color.BLACK);
    subArea.add(emailsLabel).expandX().left().padBottom(10).row();

    Table emailList = new Table();
    emailList.top().left();

    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(emailList, false, true);
    // Action to constantly save scroll position, since we want to restore it when reopening the
    // computer UI / this tab
    scrollPane.addAction(
        Actions.forever(
            Actions.run(() -> localState().emailListScrollY(scrollPane.getVisualScrollY()))));
    for (Email email : inbox) {
      Table emailEntry = createEmailHeader(email);
      emailList.add(emailEntry).left().pad(5, 0, 5, 16 + 5).growX().row();
    }
    subArea.add(scrollPane).left().growY();
    Scene2dElementFactory.scrollPaneScrollTo(scrollPane, 0, localState().emailListScrollY());

    return container;
  }

  private Table createEmailHeader(Email email) {
    boolean isSelected = email.equals(selectedEmail);

    Table container = new Table(skin);
    container.setBackground(isSelected ? "blue_square_flat" : "blue_square_border");
    container.pad(10, 20, 10, 10);

    Label subjectLabel =
        Scene2dElementFactory.createLabel(
            email.subject(), 18, isSelected ? Color.WHITE : Color.BLACK);
    subjectLabel.setWrap(true);
    container.add(subjectLabel).width(330);

    container
        .add(Scene2dElementFactory.createVerticalDivider())
        .width(4)
        .pad(0, 10, 0, 10)
        .height(50);

    Label senderLabel =
        Scene2dElementFactory.createLabel(
            email.sender() + " <" + email.senderMail() + ">",
            16,
            isSelected ? Color.WHITE : Color.DARK_GRAY);
    senderLabel.setWrap(true);
    container.add(senderLabel).width(400);

    container.setTouchable(Touchable.enabled);
    container.setUserObject(Cursors.INTERACT);
    container.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            showEmail(email);
            return super.touchDown(event, x, y, pointer, button);
          }
        });

    return container;
  }

  private Table createEmailDetailsContainer() {
    Table container = new Table();

    if (selectedEmail == null) {
      // Placeholder
      Label placeholder =
          Scene2dElementFactory.createLabel("Select an email to view its details.", 24, Color.GRAY);
      placeholder.setAlignment(Align.center);
      container.add(placeholder).minSize(400, 500).center();
      return container;
    }

    container.top().left();

    Label senderLabel =
        Scene2dElementFactory.createLabel("From: " + selectedEmail.sender(), 22, Color.BLACK);
    container.add(senderLabel).left().row();
    Label senderMailLabel =
        Scene2dElementFactory.createLabel(
            "Email: " + selectedEmail.senderMail(), 18, Color.DARK_GRAY);
    container.add(senderMailLabel).left().padBottom(20).row();

    Label subjectLabel =
        Scene2dElementFactory.createLabel("Subject: " + selectedEmail.subject(), 22, Color.BLACK);
    container.add(subjectLabel).left().padBottom(10).row();

    VerticalGroup contentTable = new VerticalGroup();
    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(contentTable, false, true);
    contentTable.top().left();
    contentTable.columnTop().columnLeft();
    contentTable.fill();
    contentTable.expand();
    contentTable.pad(5);
    contentTable.space(10);
    contentTable.wrap(false);
    for (String line : selectedEmail.parsedContentLines()) {
      Label lineLabel;
      if (Link.isLink(line)) {
        Link link = Link.parse(line);
        lineLabel = createLinkLabel(link.text, link.url);
        lineLabel.addListener(
            new InputListener() {
              @Override
              public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                BrowserTab.getInstance()
                  .ifPresent(bt -> ComputerDialog.getInstance().orElseThrow().clickedTab(bt.key()));
                return super.touchDown(event, x, y, pointer, button);
              }

              @Override
              public boolean mouseMoved(InputEvent event, float x, float y) {
                if (emailLinkFull != null) {
                  emailLinkFull.setText(String.format(LINK_SOME, link.url));
                }
                return super.mouseMoved(event, x, y);
              }

              @Override
              public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (emailLinkFull != null) {
                  emailLinkFull.setText(LINK_NONE);
                }
                super.exit(event, x, y, pointer, toActor);
              }
            });
      } else {
        lineLabel = Scene2dElementFactory.createLabel(line, 18, Color.DARK_GRAY);
      }
      lineLabel.setWrap(true);
      contentTable.addActor(lineLabel);
    }
    container.add(scrollPane).grow().row();

    // Link Label for the hovered link always at the bottom
    emailLinkFull = Scene2dElementFactory.createLabel(LINK_NONE, 20, Color.BLACK);
    emailLinkFull.setAlignment(Align.left);
    emailLinkFull.setWrap(true);
    container.add(emailLinkFull).left().padTop(10).growX().row();

    if (!selectedEmail.attachments().isEmpty()) {
      Label attachmentsHeaderLabel =
          Scene2dElementFactory.createLabel("Attachments:", 20, Color.BLACK);
      container.add(attachmentsHeaderLabel).left().padTop(20).padBottom(10).row();

      Table attachmentsTable = new Table();
      for (String attachment : selectedEmail.attachments()) {
        TextButton tb = Scene2dElementFactory.createButton(attachment, "clean-blue-outline", 18);
        tb.padLeft(tb.getPadLeft() + 10);
        tb.padRight(tb.getPadRight() + 10);
        tb.addListener(
            new ChangeListener() {
              @Override
              public void changed(ChangeEvent event, Actor actor) {
                clickedAttachment(attachment);
              }
            });
        attachmentsTable.add(tb).left().padRight(15);
      }
      container.add(attachmentsTable).left().row();
    }

    return container;
  }

  private void showEmail(Email email) {
    System.out.println(email);
    selectedEmail = email;
    localState().selectedEmail(email);
    createActors();
    Sounds.playLocal(LastHourSounds.COMPUTER_TAB_CLICKED, 0.65f, 0.3f);
  }

  private void clickedAttachment(String attachmentName) {
    ComputerDialog.getInstance()
        .ifPresent(
            c -> {
              c.addTab(new FileTab(sharedState(), attachmentName));
            });
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {}

  public record Email(
      String sender, String senderMail, String subject, String content, List<String> attachments) {

    // Split the content into its paragraphs, split by a \p.
    public List<String> parsedContentLines() {
      return Arrays.asList(content.split(PARAGRAPH_SPLIT));
    }
  }

  private record Link(String text, String url) {
    static boolean isLink(String line) {
      return line.startsWith(LINK_TOKEN);
    }

    static Link parse(String line) {
      String[] parts = line.substring(LINK_TOKEN.length()).split(";", 2);
      if (parts.length == 2) {
        return new Link(parts[0], parts[1]);
      } else {
        return new Link(parts[0], parts[0]);
      }
    }
  }

  public static Label createLinkLabel(String text, String url){
    Label label = Scene2dElementFactory.createLabel(text, 18, Color.BLUE);
    label.setUserObject(Cursors.EXTERNAL);
    label.addListener(
      new InputListener() {
        @Override
        public boolean touchDown(
          InputEvent event, float x, float y, int pointer, int button) {
          BrowserTab.getInstance()
            .ifPresent(
              bt -> {
                bt.navigate(url);
              });
          return super.touchDown(event, x, y, pointer, button);
        }
      });
    return label;
  }
}
