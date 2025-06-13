interface Config {
  API_URL: string;
  CHARACTER_MAX_MOVEMENT: number;
  VARIABLE_MAX_VALUE: number;
  REPEAT_MAX_VALUE: number;
  HIDE_GENERATED_CODE: boolean;
  HIDE_RESPONSE_INFO: boolean;
  ARRAY_MAX_VALUE: number;
  LIMITS: {
    [key: string]: number;
  };
}

export const config: Config = {
  API_URL: "http://localhost:8080/",
  CHARACTER_MAX_MOVEMENT: 999,
  VARIABLE_MAX_VALUE: 999,
  REPEAT_MAX_VALUE: 999,
  HIDE_GENERATED_CODE: false,
  HIDE_RESPONSE_INFO: true,
  ARRAY_MAX_VALUE: 999,
  LIMITS: {
    //"fireball_.*": 1, // max 1 fireball
  }
};
