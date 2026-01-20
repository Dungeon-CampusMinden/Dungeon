package modules.computer.content;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import core.utils.components.draw.TextureGenerator;
import modules.computer.ComputerDialog;
import modules.computer.ComputerState;
import modules.computer.ComputerStateComponent;
import modules.computer.ComputerStateLocal;
import util.Scene2dElementFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmailsTab extends ComputerTab {

  private static final String PARAGRAPH_SPLIT = "\\\\p";
  private static final String LINK_TOKEN = "\\a";

  private static final String OWN_MAIL = "dr.martin.brenner@company.xyz";
  private static final String OWN_NAME = "Dr. Martin Brenner";
  private static final String MAILBOX_NAME = "Lookout";
  private static final List<Email> inbox = List.of(
    new Email(
      "Dr. Smith",
      "dr.smith@gmail.com",
      "Highly confidential research",
      "Hiii,\nthis is a test.\\pAnother paragraph\\p\\ahttps://google.com",
      List.of("test.html", "Important.xlsx")
    ),
    new Email(
      "Account Security",
      "security@paypa1.com",
      "Unusual login attempt detected",
      "We detected a suspicious sign-in attempt on your account.\\pIf this was not you, verify your identity immediately.\\p\\ahttps://paypa1.com/secure",
      List.of()
    ),
    new Email(
      "IT Support",
      "support@company.internal",
      "Password expiration notice",
      "Hello,\\pYour password will expire in 3 days. Please update it as soon as possible.\\p\\ahttps://intranet.company/reset",
      List.of()
    ),
    new Email(
      "HR Department",
      "hr@company.internal",
      "Updated employee handbook",
      "Dear employee,\\pWe have published an updated version of the employee handbook.\\pPlease review the attached document.",
      List.of("Employee_Handbook_2026.pdf")
    ),
    new Email(
      "Microsoft Billing",
      "billing@microslop.com",
      "Your subscription will be suspended",
      "We were unable to process your last payment.\\pTo avoid service interruption, confirm your billing information now.\\p\\ahttps://microsoft-support.co/billing",
      List.of("Receipt.html")
    ),
    new Email(
      "Alex Johnson",
      "alex.johnson@partner.org",
      "Meeting notes & next steps",
      "Hey,\\pThanks for the meeting earlier today.\\pI've attached the notes and action items we discussed.",
      List.of("Meeting_Notes.docx")
    ),
    new Email(
      "Online Store",
      "no-reply@shop.example",
      "Your order has shipped",
      "Good news!\\pYour order #48392 has been shipped and is on its way.\\p\\ahttps://shop.example/track/48392",
      List.of("Invoice_48392.pdf")
    )
  );

  private Table emailDetailsContainer = null;
  private Email selectedEmail = null;

  public EmailsTab(ComputerStateComponent sharedState){
    super(sharedState, "emails", "E-Mails (5)", false);
  }

  protected void createActors(){
    this.clearChildren();
    this.add(createEmailListContainer()).top();
    this.add(Scene2dElementFactory.createVerticalDivider()).width(4).pad(0, 10, 0, 10).fillY();
    emailDetailsContainer = createEmailDetailsContainer();
    this.add(emailDetailsContainer).grow();
  }

  private Table createEmailListContainer(){
    Table container = new Table();

    Label inboxLabel = Scene2dElementFactory.createLabel(MAILBOX_NAME, 64, Color.BLACK);
    container.add(inboxLabel).left().padBottom(10).row();

    Label userLabel = Scene2dElementFactory.createLabel(OWN_NAME + " <" + OWN_MAIL + ">", 20, Color.DARK_GRAY);
    container.add(userLabel).left().padBottom(20).row();

    Table subArea = new Table();
    container.add(subArea).grow().padLeft(30).row();

    Label emailsLabel = Scene2dElementFactory.createLabel("E-Mails: ("+inbox.size()+")", 24, Color.BLACK);
    subArea.add(emailsLabel).expandX().left().padBottom(10).row();

    Table emailList = new Table(skin);
    emailList.setBackground("generic-area");
    emailList.top();
    for(Email email : inbox){
      Table emailEntry = createEmailHeader(email);
      emailList.add(emailEntry).height(50).left().padBottom(5).row();
    }
    subArea.add(emailList).left().growY();

    return container;
  }

  private Table createEmailHeader(Email email){
    boolean isSelected = email.equals(selectedEmail);

    Table container = new Table(skin);
    container.setBackground(isSelected ? "blue_square_flat" : "button_rectangle_border_blue");
    container.pad(10);

    Label subjectLabel = Scene2dElementFactory.createLabel(email.subject(), 18, isSelected ? Color.WHITE : Color.BLACK);
    container.add(subjectLabel).width(330).padLeft(10);

    container.add(Scene2dElementFactory.createVerticalDivider()).width(4).pad(0, 10, 0, 10).expandY();

    Label senderLabel = Scene2dElementFactory.createLabel(email.sender() + " <" + email.senderMail() + ">", 16, isSelected ? Color.WHITE : Color.DARK_GRAY);
    container.add(senderLabel).width(400);

    container.setTouchable(Touchable.enabled);
    container.addListener(new ClickListener(Input.Buttons.LEFT){
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        showEmail(email);
        return super.touchDown(event, x, y, pointer, button);
      }
    });

    return container;
  }

  private Table createEmailDetailsContainer(){
    Table container = new Table();

    if(selectedEmail == null){
      // Placeholder
      Label placeholder = Scene2dElementFactory.createLabel("Select an email to view its details.", 24, Color.GRAY);
      placeholder.setAlignment(Align.center);
      container.add(placeholder).minSize(400, 500).center();
      return container;
    }

    container.top().left();

    Label senderLabel = Scene2dElementFactory.createLabel("From: " + selectedEmail.sender(), 22, Color.BLACK);
    container.add(senderLabel).left().row();
    Label senderMailLabel = Scene2dElementFactory.createLabel("Email: " + selectedEmail.senderMail(), 18, Color.DARK_GRAY);
    container.add(senderMailLabel).left().padBottom(20).row();

    Label subjectLabel = Scene2dElementFactory.createLabel("Subject: " + selectedEmail.subject(), 22, Color.BLACK);
    container.add(subjectLabel).left().padBottom(10).row();
    Label contentHeaderLabel = Scene2dElementFactory.createLabel("Content:", 18, Color.BLACK);
    container.add(contentHeaderLabel).left().padBottom(10).row();

    Table contentTable = new Table(skin);
    contentTable.setBackground("generic-area");
    contentTable.top().left();
    contentTable.pad(15);
    for(String line : selectedEmail.parsedContentLines()){
      if(selectedEmail.isLink(line)){
        String url = line.substring(LINK_TOKEN.length());
        Label linkLabel = Scene2dElementFactory.createLabel(url, 18, Color.BLUE);
        contentTable.add(linkLabel).left().padBottom(15).row();
      } else {
        Label contentLineLabel = Scene2dElementFactory.createLabel(line, 18, Color.DARK_GRAY);
        contentTable.add(contentLineLabel).left().padBottom(15).row();
      }
    }
    container.add(contentTable).grow().row();

    if(!selectedEmail.attachments().isEmpty()){
      Label attachmentsHeaderLabel = Scene2dElementFactory.createLabel("Attachments:", 20, Color.BLACK);
      container.add(attachmentsHeaderLabel).left().padTop(20).padBottom(10).row();

      Table attachmentsTable = new Table();
      for(String attachment : selectedEmail.attachments()){
        TextButton tb = Scene2dElementFactory.createButton(attachment, "clean-blue-outline", 18);
        tb.padLeft(tb.getPadLeft() + 10);
        tb.padRight(tb.getPadRight() + 10);
        tb.addListener(new ChangeListener() {
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

  private void showEmail(Email email){
    System.out.println(email);
    selectedEmail = email;
    createActors();
  }

  private void clickedAttachment(String attachmentName){
    ComputerDialog.getInstance().ifPresent(c -> {
      c.addTab(new TestMask(sharedState(), attachmentName, "FILE: "+attachmentName, true, Color.BLACK));
    });
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {

  }

  private record Email(String sender, String senderMail, String subject, String content, List<String> attachments){

    // Split the content into its paragraphs, split by a \p.
    public List<String> parsedContentLines(){
      return Arrays.asList(content.split(PARAGRAPH_SPLIT));
    }

    public boolean isLink(String line){
      return line.startsWith(LINK_TOKEN);
    }
  }
}
