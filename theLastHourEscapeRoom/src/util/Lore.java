package util;

import core.utils.Tuple;
import java.util.List;
import modules.computer.content.BlogTab;
import modules.computer.content.EmailsTab;

public class Lore {

  public static final String CompanyName = "CyberTech Solutions";
  public static final String CompanyDrawable = "company_logo";
  public static final String ScientistName = "Prof. Dr. Martin Brenner";
  public static final String ScientistNameShort = "Dr. Brenner";
  public static final String ScientistEmail = "dr.brenner@cybertech-solutions.com";
  public static final String ScientistPortraitDrawable = "scientist_portrait";

  public static final String LoginEmail = ScientistEmail;
  public static final String LoginPassword = "a12b34xy";

  public static final List<Tuple<String, Integer>> IntroTexts =
      List.of(Tuple.of("Story here", 32), Tuple.of("The Last Hour", 120));
  public static final List<String> PostIntroDialogTexts =
      List.of("Goal explanation here", "Possible multi stage.");

  public static final List<Tuple<String, Integer>> OutroTexts =
      List.of(Tuple.of("Outro story here", 32), Tuple.of("Congratulations!\nYou escaped! :D", 256));

  public static final List<BlogTab.BlogEntry> BlogEntries =
      List.of(
          new BlogTab.BlogEntry("Kleine Panne", "Blog entry 1", List.of()),
          new BlogTab.BlogEntry(
              "Vielen Dank - aber mit Vorsicht",
              "Blog entry 2",
              List.of(
                  new BlogTab.BlogComment("TechAnalyst", "Blog comment 1", 180),
                  new BlogTab.BlogComment("CyberLab", "Blog comment 2", 300))));

  public static final List<EmailsTab.Email> EmailList =
      List.of(
          new EmailsTab.Email(
              "Dr. Smith",
              "dr.smith@gmail.com",
              "Highly confidential research",
              "Hiii,\nthis is a test.\\pAnother paragraph\\p\\aCheck out this website I made!;https://www.example.com",
              List.of("Hello.html", "Important.xlsx")),
          new EmailsTab.Email(
              "Account Security",
              "security@paypa1.com",
              "Unusual login attempt detected",
              "We detected a suspicious sign-in attempt on your account.\\pIf this was not you, verify your identity immediately.\\p\\aVisit Paypa1;https://paypa1.com/secure",
              List.of()),
          new EmailsTab.Email(
              "cloud support team",
              "notifications@cloudservice",
              "Wir haben Ihr Konto gesperrt! Ihre Fotos und Videos werden am",
              "\\aZur Löschung vorgesehen;https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm\\p\\aIhr Konto war inaktiv und hat das Speicherlimit überschritten. Gemäß unserer Aufbewahrungsrichtlinien sind Ihre Dateien zur Löschung vorgesehen;https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm\\p\\aDAUERTHAFTER DATENVERLUST\nWenn Sie Ihren Speicherplan nicht bis zum verlängern, werden Ihre Daten dauerhaft von unseren Servern gelöscht;https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm\\p\\p\\aMeine Dateien behalten;https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm\\p\\p\\aAbmelden;https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm",
              List.of("License_renewal.ics")),
          new EmailsTab.Email(
              "IT Support",
              "support@company.internal",
              "Password expiration notice",
              "Hello,\\pYour password will expire in 3 days. Please update it as soon as possible.\\p\\aChange Password;https://intranet.company/reset",
              List.of("Help.html")));
}
