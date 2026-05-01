package modules.computer;

import java.util.LinkedHashSet;
import java.util.Set;
import modules.computer.content.EmailsTab;

/** Local state for the computer UI. */
public class ComputerStateLocal {

  private static ComputerStateLocal Instance;

  private String tab;
  private String username = "";
  private String password = "";
  private EmailsTab.Email selectedEmail;
  private float emailListScrollY;
  private String browserUrl = "";

  private final Set<String> openFiles = new LinkedHashSet<>();
  private final Set<String> browserHistory = new LinkedHashSet<>();

  private ComputerStateLocal() {
    this.tab = "login";
  }

  /**
   * Get the singleton instance of the local computer state.
   *
   * @return The singleton instance of the local computer state
   */
  public static ComputerStateLocal getInstance() {
    if (Instance == null) {
      Instance = new ComputerStateLocal();
    }
    return Instance;
  }

  /**
   * Get the currently active tab in the computer UI.
   *
   * @return The key of the currently active tab (e.g. "login", "emails", "browser", etc.)
   */
  public String tab() {
    return tab;
  }

  /**
   * Set the currently active tab in the computer UI.
   *
   * @param tab The key of the tab to set as active (e.g. "login", "emails", "browser", etc.)
   */
  public void tab(String tab) {
    this.tab = tab;
  }

  /**
   * Get the currently entered username in the login tab.
   *
   * @return The currently entered username in the login tab
   */
  public String username() {
    return username;
  }

  /**
   * Set the currently entered username in the login tab.
   *
   * @param username The username to set as currently entered in the login tab
   */
  public void username(String username) {
    this.username = username;
  }

  /**
   * Get the currently entered password in the login tab.
   *
   * @return The currently entered password in the login tab
   */
  public String password() {
    return password;
  }

  /**
   * Set the currently entered password in the login tab.
   *
   * @param password The password to set as currently entered in the login tab
   */
  public void password(String password) {
    this.password = password;
  }

  /**
   * Set the currently selected email in the emails tab.
   *
   * @param email The email to set as currently selected in the emails tab, or null to indicate no
   */
  public void selectedEmail(EmailsTab.Email email) {
    this.selectedEmail = email;
  }

  /**
   * Get the currently selected email in the emails tab.
   *
   * @return The currently selected email in the emails tab, or null if no email is selected
   */
  public EmailsTab.Email selectedEmail() {
    return selectedEmail;
  }

  /**
   * Get the current scroll Y position of the email list in the emails tab.
   *
   * @return The current scroll Y position of the email list in the emails tab
   */
  public float emailListScrollY() {
    return emailListScrollY;
  }

  /**
   * Set the current scroll Y position of the email list in the emails tab.
   *
   * @param emailListScrollY The scroll Y position to set for the email list in the emails tab
   */
  public void emailListScrollY(float emailListScrollY) {
    this.emailListScrollY = emailListScrollY;
  }

  /**
   * Get the currently entered URL in the browser tab.
   *
   * @return The currently entered URL in the browser tab
   */
  public String browserUrl() {
    return browserUrl;
  }

  /**
   * Set the currently entered URL in the browser tab.
   *
   * @param browserUrl The URL to set as currently entered in the browser tab
   */
  public void browserUrl(String browserUrl) {
    this.browserUrl = browserUrl;
  }

  /**
   * Get the set of currently open files in the file explorer tab.
   *
   * @return The set of currently open files in the file explorer tab
   */
  public Set<String> openFiles() {
    return openFiles;
  }

  /**
   * Get the set of URLs in the browser history.
   *
   * @return The set of URLs in the browser history
   */
  public Set<String> browserHistory() {
    return browserHistory;
  }
}
