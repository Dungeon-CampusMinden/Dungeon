interface Config {
  API_URL: string;
  CHARACTER_MAX_MOVEMENT: number;
  VARIABLE_MAX_VALUE: number;
  REPEAT_MAX_VALUE: number;
  HIDE_GENERATED_CODE: boolean;
  HIDE_RESPONSE_INFO: boolean;
}

export const config: Config = {
  API_URL: "http://localhost:8080/",
  CHARACTER_MAX_MOVEMENT: 20,
  VARIABLE_MAX_VALUE: 20,
  REPEAT_MAX_VALUE: 10,
  HIDE_GENERATED_CODE: false,
  HIDE_RESPONSE_INFO: true,
};
