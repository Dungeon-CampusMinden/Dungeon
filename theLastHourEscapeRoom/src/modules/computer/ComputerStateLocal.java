package modules.computer;

public class ComputerStateLocal {

  public static ComputerStateLocal Instance = new ComputerStateLocal();

  private String tab;
  private String username;
  private String password;

  public ComputerStateLocal() {
    this.tab = "login";
    this.username = "";
    this.password = "";
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

}
