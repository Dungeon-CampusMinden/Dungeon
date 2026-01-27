package modules.computer;

import modules.computer.content.EmailsTab;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ComputerStateLocal {

  public static ComputerStateLocal Instance = new ComputerStateLocal();

  private String tab;
  private String username = "";
  private String password = "";
  private EmailsTab.Email selectedEmail;
  private float emailListScrollY;
  private String browserUrl = "";

  private final Set<String> openFiles = new LinkedHashSet<>();

  public ComputerStateLocal() {
    this.tab = "login";
  }

  public String tab(){
    return tab;
  }
  public void tab(String tab){
    this.tab = tab;
  }

  public String username() {
    return username;
  }
  public void username(String username) {
    this.username = username;
  }

  public String password() {
    return password;
  }
  public void password(String password) {
    this.password = password;
  }

  public void selectedEmail(EmailsTab.Email email) {
    this.selectedEmail = email;
  }
  public EmailsTab.Email selectedEmail() {
    return selectedEmail;
  }

  public float emailListScrollY() {
    return emailListScrollY;
  }
  public void emailListScrollY(float emailListScrollY) {
    this.emailListScrollY = emailListScrollY;
  }

  public String browserUrl() {
    return browserUrl;
  }
  public void browserUrl(String browserUrl) {
    this.browserUrl = browserUrl;
  }

  public Set<String> openFiles() {
    return openFiles;
  }
}
